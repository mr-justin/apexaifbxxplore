package org.team.xxplore.core.service.q2semantic.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.ObjectProperty;
import org.team.xxplore.core.service.q2semantic.SummaryGraphEdge;
import org.team.xxplore.core.service.q2semantic.SummaryGraphElement;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Build split summary graph from nt file
 * 
 * @author kaifengxu
 * 
 */
public class SplitSummaryGraphIndexServiceForBTFromNT extends
		SummaryGraphIndexServiceForBTFromNT {

	/* for scanning */
	public LuceneMap indiv2con;
	public TreeMap<String, Integer> conceptCount;
	public TreeMap<String, Integer> predPool;
	public TreeMap<String, SummaryGraphElement> elemPool;
	public TreeMap<String, Set<String>> cache;
	public Integer hits = 0;
	public int indivSize, propSize = 0;

	public Parameters para;

	public SplitSummaryGraphIndexServiceForBTFromNT() {
		para = Parameters.getParameters();
	}

	/**
	 * check condition to get subject type
	 * 
	 * @param pred
	 * @param obj
	 * @return
	 */
	public String getSubjectType(String pred, String obj) {
		if ((pred.equals(RDF.type.getURI()) && (obj.equals(RDFS.Class.getURI()) || obj
				.equals(OWL.Class.getURI())))
				|| pred.equals(RDFS.subClassOf.getURI())) {
			return para.CONCEPT;
		} else if ((pred.equals(RDFS.subPropertyOf.getURI()) && (obj
				.equals(OWL.ObjectProperty) || obj.equals(OWL.DatatypeProperty)))
				|| pred.equals(RDFS.domain.getURI())
				|| pred.equals(RDFS.range.getURI())) {
			return para.PROPERTY;
		} else if (pred.equals(RDF.type.getURI())
				|| getPredicateType(pred, obj).equals(para.OBJECTPROP)
				|| getPredicateType(pred, obj).equals(para.DATATYPEPROP)) {
			return para.INDIVIDUAL;
		}
		return "";
	}

	/**
	 * check condition to get object type
	 * 
	 * @param pred
	 * @param obj
	 * @return
	 */
	public String getObjectType(String pred, String obj) {
		if (pred.equals(RDF.type.getURI()) && !para.rdfsEdgeSet.contains(obj)) {
			return para.CONCEPT;
		} else if (getPredicateType(pred, obj).equals(para.OBJECTPROP)) {
			return para.INDIVIDUAL;
		} else if (getPredicateType(pred, obj).equals(para.DATATYPEPROP)) {
			return para.LITERAL;
		}
		return "";
	}

	/**
	 * check condition to get predicate type
	 * 
	 * @param pred
	 * @param obj
	 * @return
	 */
	// for example isEdgeTypeOf(Property.IS_INSTANCE_OF)
	public String getPredicateType(String pred, String obj) {
		if (para.rdfsEdgeSet.contains(pred)) {
			return para.RDFSPROP;
		} else if (obj.startsWith("http")) {
			return para.OBJECTPROP;
		} else if (obj.startsWith("\"")) {
			return para.DATATYPEPROP;
		}
		return "";
	}

	/**
	 * main entry for split summary graph building
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void buildGraphs(String path, boolean scoring) throws Exception {
		conceptCount = new TreeMap<String, Integer>();
		elemPool = new TreeMap<String, SummaryGraphElement>();
		indiv2con = new LuceneMap();

		firstScan(path, scoring);
		System.gc();

		secondScan(path, scoring);
		System.gc();

		thirdScan(path, false, false, scoring);
		System.gc();

	}

	/**
	 * first scan from nt file which will get (1)instance and predicate number
	 * (2)id2predicate pairs (3)indiv2concepts pool
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void firstScan(String path, boolean scoring) throws Exception {
		System.out.println("=======firstScan==========");
		TreeSet<String> conSet = new TreeSet<String>(), relSet = new TreeSet<String>(), attrSet = new TreeSet<String>();
		indiv2con.openWriter(path, true);
		// get pre-defined instance number
		TreeSet<String> indivSet = null;
		predPool = new TreeMap<String, Integer>();
		if (scoring) {
			indivSize = para.instNumMap.get(para.datasource) == null ? -1
					: para.instNumMap.get(para.datasource);
			indivSet = new TreeSet<String>();
		}
		PrintWriter pw;
		new File(para.objPropPool).mkdir();

		LineNumberReader br = new LineNumberReader(new FileReader(para.source));
		String line;
		int predID = 0;
		while ((line = br.readLine()) != null) {

			if (br.getLineNumber() % 10000 == 0) {
				System.out.println("1st scan\t" + br.getLineNumber());
			}

			String[] part = getSubjPredObj(line);
			if (part == null)
				continue;
			String subj = part[0];
			String pred = part[1];
			String obj = part[2];

			if (scoring && indivSize == -1
					&& getSubjectType(pred, obj).equals(para.INDIVIDUAL)) {
				indivSet.add(subj);
			}
			if (scoring && indivSize == -1
					&& getObjectType(pred, obj).equals(para.INDIVIDUAL)) {
				indivSet.add(obj);
			}
			if (scoring && !getPredicateType(pred, obj).equals(para.RDFSPROP)) {
				propSize++;
			}
			if (getSubjectType(pred, obj).equals(para.INDIVIDUAL)
					&& getObjectType(pred, obj).equals(para.CONCEPT)) {
				conSet.add(obj);
				indiv2con.put(subj, obj);
				if (scoring) {
					Integer i = conceptCount.get(obj);
					if (i == null)
						i = Integer.valueOf(0);
					conceptCount.put(obj, i + 1);
				}
			}
			if (getSubjectType(pred, obj).equals(para.INDIVIDUAL)
					&& getObjectType(pred, obj).equals(para.INDIVIDUAL)
					&& getPredicateType(pred, obj).equals(para.OBJECTPROP)) {
				relSet.add(pred);
				Integer id = predPool.get(pred);
				if (id == null) {
					id = predID;// transfer the string of predicate to a sequential number
					predPool.put(pred, predID++);
				}

				File output = new File(para.objPropPool + File.separator + id);
				if (!output.exists()){
					output.createNewFile();
				}
				pw = new PrintWriter(new FileWriter(output, true));
				pw.println(subj + "\t" + obj);// write relation's subj and obj into file							
				pw.close();

			}
			if (getSubjectType(pred, obj).equals(para.INDIVIDUAL) && getObjectType(pred, obj).equals(para.LITERAL)
					&& getPredicateType(pred, obj).equals(para.DATATYPEPROP)) {
				attrSet.add(pred);
			}
			if (pred.equals(RDFS.subClassOf)) {
				File output = new File(para.objPropPool + File.separator + para.SUBCLASS);
				pw = new PrintWriter(new FileWriter(output, true));
				pw.println(subj + "\t" + obj);// write relation's subj and obj into file
				pw.close();
			}
		}

		if (scoring && indivSize == -1) {
			indivSize = indivSet.size();
			indivSet.clear();
			indivSet = null;
		}
		br.close();

		indiv2con.closeWriter();
		pw = new PrintWriter(new FileWriter(para.resultFile));
		if (scoring) {
			pw.println(indivSize);// write total num of indiv
			pw.println(propSize);// wirte total num of prop
		}
		for (String key : predPool.keySet()){
			pw.println(key + "\t" + predPool.get(key));// write id and its  relation uri into  file
		}
		pw.close();

		if (scoring) {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(para.conceptCountObj));// save the obj of conceptCountPool
			out.writeObject(conceptCount);
			out.close();
		}
		PrintWriter conPw = new PrintWriter(new FileWriter( para.conceptFile)), relPw = new PrintWriter(new FileWriter(
				para.relationFile)), attrPw = new PrintWriter(
				new FileWriter(para.attributeFile));
		for (String con : conSet)
			conPw.println(con);
		for (String rel : relSet)
			relPw.println(rel);
		for (String attr : attrSet)
			attrPw.println(attr);
		conPw.close();
		relPw.close();
		attrPw.close();
		System.out.println("indivSize: " + indivSize + "\tpropSize: "
				+ propSize);
	}

	/**
	 * second scan from relation file written in first scan which will get graph
	 * edge file where each file means a relation and each line means the subj
	 * concept and obj concept of this relation
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void secondScan(String path, boolean scoring) throws Exception {
		System.out.println("=======secondScan==========");

		indiv2con.openSearcher(path);
		LineNumberReader root = new LineNumberReader(new FileReader(para.resultFile)), br;
		new File(para.graphEdgePool).mkdir();
		PrintWriter pw;
		if (scoring) {
			indivSize = Integer.parseInt(root.readLine());// read total num of
															// instance
			propSize = Integer.parseInt(root.readLine());// read total num of
															// property
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(para.conceptCountObj));
			conceptCount = (TreeMap<String, Integer>) in.readObject(); // read
																		// indiv2conceptsPool
			in.close();
		}
		cache = new TreeMap<String, Set<String>>();
		String predid;
		hits = 0;
		while ((predid = root.readLine()) != null) {
			String pred = predid.substring(0, predid.indexOf('\t'));
			String id = predid.substring(predid.indexOf('\t') + 1);
			System.out.println(root.getLineNumber() + " " + pred + "\tcache:"
					+ cache.size() + "\thits:" + hits);

			File file = new File(para.objPropPool + File.separator + id);
			if (file.length() > para.MAX_OBJPROP_FILESIZE)
				continue;// skip the relation file whose length is larger than
							// 1G
			br = new LineNumberReader(new FileReader(file));

			TreeMap<String, Integer> triples = new TreeMap<String, Integer>();

			String line;
			while ((line = br.readLine()) != null) {
				String subj = line.substring(0, line.indexOf('\t'));
				String obj = line.substring(line.indexOf('\t') + 1);
				Set<String> subjParent = null, objParent = null;
				subjParent = getParent(subj);
				if (subjParent == null)
					continue;

				objParent = getParent(obj);
				if (objParent == null)
					continue;

				for (String str : subjParent) {
					for (String otr : objParent) {
						Integer i = triples.get(str + '\t' + otr);
						if (i == null)
							i = 0;
						triples.put(str + '\t' + otr, i + 1);
					}
				}
				subjParent = null;
				objParent = null;
			}
			br.close();
			pw = new PrintWriter(new FileWriter(para.graphEdgePool
					+ File.separator + id));// write edge info into file
			// System.out.println("search finished! size:"+triples.size());
			int order = 0;
			for (String so : triples.keySet()) {
				String str = so.substring(0, so.indexOf('\t'));
				String otr = so.substring(so.indexOf('\t') + 1);
				String ptr = pred + "(" + order + ")";// split the edge through
														// different order, for
														// example name(1) and
														// name(2) and ...
				double sscore = 0, oscore = 0, pscore = 0;
				if (scoring) {
					if (str.equals(NamedConcept.TOP.getUri()))
						sscore = para.TOP_ELEMENT_SCORE;
					else {
						Integer j = conceptCount.get(str);
						if (j == null)
							sscore = Double.MIN_VALUE;
						else
							sscore = j.doubleValue() / indivSize;
					}
					if (otr.equals(NamedConcept.TOP.getUri()))
						oscore = para.TOP_ELEMENT_SCORE;
					else {
						Integer j = conceptCount.get(otr);
						if (j == null)
							oscore = Double.MIN_VALUE;
						else
							oscore = j.doubleValue() / indivSize;
					}
					Integer i = triples.get(so);
					if (i == null)
						pscore = Double.MIN_VALUE;
					else
						pscore = i.doubleValue() / propSize;
				}
				pw.println(str + "\t" + sscore + "\t" + ptr + "\t" + pscore
						+ "\t" + otr + "\t" + oscore);
				order++;
			}
			pw.close();
			triples.clear();
			triples = null;

		}
		root.close();
		indiv2con.closeSearcher();
	}

	/**
	 * third scan from edge file written in second scan which will get split
	 * graph obj
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void thirdScan(String path, boolean fileLengthRestriction,
			boolean scoreRestriction, boolean scoring) throws Exception {
		System.out.println("=======thirdScan==========");
		LineNumberReader root = new LineNumberReader(new FileReader(para.resultFile)), br;
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(
				SummaryGraphEdge.class);
		if (scoring) {
			root.readLine();
			root.readLine();
		}// ignore indivnum and propnum

		String line;
		while ((line = root.readLine()) != null) {
			// String pred = line.substring(0, line.indexOf('\t'));
			//System.out.println(root.getLineNumber()+" "+pred+" "+elemPool.size
			// ());
			String id = line.substring(line.indexOf('\t') + 1);
			File file = new File(para.graphEdgePool + File.separator
					+ id);
			if (!file.exists())
				continue;
			if (fileLengthRestriction
					&& (file.length() > para.MAX_GRAPHEDGE_FILESIZE || file
							.length() < para.MIN_GRAPHEDGE_FILESIZE))
				continue;// file length restriction
			br = new LineNumberReader(new FileReader(file));
			String line2;
			while ((line2 = br.readLine()) != null) {
				String[] part = line2.split("\t");

				if (scoreRestriction
						&& Double.parseDouble(part[3]) < para.MIN_OBJPROP_SCORE)
					continue;// score restriction
				SummaryGraphElement p = getElem(part[2],
						SummaryGraphElement.RELATION);
				if (scoring)
					p.setEF(Double.parseDouble(part[3]));
				SummaryGraphElement s = getElem(part[0],
						SummaryGraphElement.CONCEPT);
				if (scoring)
					s.setEF(Double.parseDouble(part[1]));
				SummaryGraphElement o = getElem(part[4],
						SummaryGraphElement.CONCEPT);
				if (scoring)
					o.setEF(Double.parseDouble(part[5]));

				if (!summaryGraph.containsVertex(s))
					summaryGraph.addVertex(s);
				if (!summaryGraph.containsVertex(o))
					summaryGraph.addVertex(o);
				if (!summaryGraph.containsVertex(p))
					summaryGraph.addVertex(p);

				SummaryGraphEdge edge1 = new SummaryGraphEdge(s, p,
						SummaryGraphEdge.DOMAIN_EDGE);
				SummaryGraphEdge edge2 = new SummaryGraphEdge(p, o,
						SummaryGraphEdge.RANGE_EDGE);
				if (!summaryGraph.containsEdge(edge1))
					summaryGraph.addEdge(s, p, edge1);
				if (!summaryGraph.containsEdge(edge2))
					summaryGraph.addEdge(p, o, edge2);
			}
			br.close();
		}
		root.close();

		br = new LineNumberReader(new FileReader( para.objPropPool
				+ File.separator + para.SUBCLASS));
		while ((line = br.readLine()) != null) {
			String[] part = line.split("\t");
			SummaryGraphElement s = getElemFromUri(part[0], scoring);
			SummaryGraphElement o = getElemFromUri(part[4], scoring);
			if (!summaryGraph.containsVertex(s))
				summaryGraph.addVertex(s);
			if (!summaryGraph.containsVertex(o))
				summaryGraph.addVertex(o);
			if (!summaryGraph.containsVertex(SummaryGraphElement.SUBCLASS)) {
				if (scoring) {
					SummaryGraphElement.SUBCLASS
							.setEF(para.SUBCLASS_ELEMENT_SCORE);
				}
				summaryGraph.addVertex(SummaryGraphElement.SUBCLASS);
			}
			SummaryGraphEdge edge1 = new SummaryGraphEdge(s,
					SummaryGraphElement.SUBCLASS,
					SummaryGraphEdge.SUBCLASS_EDGE);
			if (!summaryGraph.containsEdge(edge1))
				summaryGraph.addEdge(s, SummaryGraphElement.SUBCLASS, edge1);
			SummaryGraphEdge edge2 = new SummaryGraphEdge(
					SummaryGraphElement.SUBCLASS, o,
					SummaryGraphEdge.SUPERCLASS_EDGE);
			if (!summaryGraph.containsEdge(edge2))
				summaryGraph.addEdge(SummaryGraphElement.SUBCLASS, o, edge2);
		}
		br.close();

		System.out.println("write splitted summary graph");
		writeSummaryGraph(summaryGraph, para.summaryObj);
		writeSummaryGraphAsRDF(summaryGraph, para.summaryRDF);
	}

	public String[] getSubjPredObj(String line) {
		String[] part = Util4NT.processTripleLine(line);
		if (part == null || part[0].startsWith("_:node")
				|| part[0].length() < 2 || part[1].length() < 2)
			return null;

		if (!part[0].startsWith("<") || !part[1].startsWith("<"))
			return null;

		part[0] = part[0].substring(1, part[0].length() - 1);// remove <>
		part[1] = part[1].substring(1, part[1].length() - 1);// remove <>

		if (!part[2].startsWith("<")) {
			part[2] = "\"" + part[2] + "\"";// if obj is not instance, we give
											// it value "+obj+"
			// System.out.println(part[2]);
		} else if (part[2].length() >= 2)
			part[2] = part[2].substring(1, part[2].length() - 1);
		else
			return null;

		if (!part[0].startsWith("http") || !part[1].startsWith("http"))
			return null;
		return part;
	}

	public Set<String> getParent(String uri) {
		Set<String> parent = cache.get(uri);// get cache
		try {
			if (parent == null || parent.size() == 0)
				parent = indiv2con.search(uri);// do search
			else
				hits++;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (parent == null || parent.size() == 0) {
			parent = new TreeSet<String>();
			parent.add(NamedConcept.TOP.getUri());// using top when no res
		}
		if (!cache.containsKey(uri))
			cache.put(uri, parent);// update cache

		while (cache.size() >= para.MAX_CACHE_SIZE)
			cache.clear();// if cache is full, clear it
		return parent;
	}

	/**
	 * get GraphElement from uri which maybe new an element or return an
	 * existing one
	 * 
	 * @param uri
	 * @return
	 */
	public SummaryGraphElement getElemFromUri(String uri, boolean scoring) {
		if (uri.equals(NamedConcept.TOP.getUri())) {
			SummaryGraphElement elem = getElem(NamedConcept.TOP.getUri(),
					SummaryGraphElement.CONCEPT);
			if (scoring)
				elem.setEF(para.TOP_ELEMENT_SCORE);
			return elem;
		} else {
			SummaryGraphElement elem = getElem(uri, SummaryGraphElement.CONCEPT);

			if (scoring) {
				double EF;
				Integer i = conceptCount.get(uri);
				if (i == null)
					EF = Double.MIN_VALUE;
				else
					EF = i.doubleValue() / indivSize;
				elem.setEF(EF);
			}
			return elem;
		}
	}

	/**
	 * get element from uri and type, new an element or return an existing one
	 * 
	 * @param uri
	 * @param type
	 */
	public SummaryGraphElement getElem(String uri, int type) {
		SummaryGraphElement res = elemPool.get(type + uri);
		if (res == null) {
			if (type == SummaryGraphElement.CONCEPT)
				res = new SummaryGraphElement(new NamedConcept(uri),
						SummaryGraphElement.CONCEPT);
			else if (type == SummaryGraphElement.RELATION)
				res = new SummaryGraphElement(new ObjectProperty(uri),
						SummaryGraphElement.RELATION);
		}
		elemPool.put(type + uri, res);
		return res;
	}
}
