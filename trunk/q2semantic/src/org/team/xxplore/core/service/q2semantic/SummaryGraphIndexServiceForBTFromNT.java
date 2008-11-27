package org.team.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.graph.Pseudograph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.api.IProperty;
import org.team.xxplore.core.service.impl.DataProperty;
import org.team.xxplore.core.service.impl.Datatype;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.ObjectProperty;
import org.team.xxplore.core.service.impl.Property;


/**
 * Build summary and schema graph from nt file
 * @author kaifengxu
 *
 */
public class SummaryGraphIndexServiceForBTFromNT {

	/* CONSTANT */
	public static String CONCEPT="concept", 
						INDIVIDUAL="individual", 
						LITERAL="literal",
						PROPERTY="property", 
						DATATYPEPROP="datatypeprop", 
						OBJPROP="objprop", 
						RDFSPROP="rdfsprop";//predicate type
		
	public static String DATATYPE_URI = "http://www.w3.org/2001/XMLSchema#";
	public static String LABEL= DATATYPE_URI+"label";
	public static String INDIV2CON = "indiv2con";
	public static String RELPOOL = "-splitRelPool";
	public static double TOP_ELEMENT_SCORE = 2.0, SUBCLASS_ELEMENT_SCORE = 0;
	
	/* for scanning */
	public LuceneMap indiv2con;
	public TreeMap<String, Integer> propCount;
	public TreeMap<String, Integer> conceptCount;
	public TreeMap<String, Set<String>> cache;
	public TreeMap<String, SummaryGraphElement> elemPool;
	public int MAX_CACHE_SIZE = 10000000; //cache of indiv2concepts results
	public int indivSize, propSize = 0, hits = 0;
	
	/**
	 * check condition to get subject type
	 * @param pred
	 * @param obj
	 * @return
	 */
	public static String getSubjectType(String pred, String obj)
	{
		if( (pred.equals(BuildQ2SemanticService.rdfsEdge[0]) && obj.equals(BuildQ2SemanticService.rdfsEdge[8])) || pred.equals(BuildQ2SemanticService.rdfsEdge[1]))
			return CONCEPT;
		else if( (pred.equals(BuildQ2SemanticService.rdfsEdge[4]) && obj.equals(BuildQ2SemanticService.rdfsEdge[7]))
				|| pred.equals(BuildQ2SemanticService.rdfsEdge[2])
				|| pred.equals(BuildQ2SemanticService.rdfsEdge[3]))
			return PROPERTY;
		else if(pred.equals(BuildQ2SemanticService.rdfsEdge[0]) || getPredicateType(pred, obj).equals(OBJPROP) || getPredicateType(pred, obj).equals(DATATYPEPROP))
			return INDIVIDUAL;
		return "";
	}
	
	/**
	 * check condition to get object type
	 * @param pred
	 * @param obj
	 * @return
	 */
	public static String getObjectType(String pred, String obj)
	{
		if(pred.equals(BuildQ2SemanticService.rdfsEdge[0]) && !BuildQ2SemanticService.rdfsEdgeSet.contains(obj))
			return CONCEPT;
		else if(getPredicateType(pred, obj).equals(OBJPROP))
			return INDIVIDUAL;
		else if(getPredicateType(pred, obj).equals(DATATYPEPROP))
			return LITERAL;
		return "";
	}

	/**
	 * check condition to get predicate type
	 * @param pred
	 * @param obj
	 * @return
	 */
	// for example isEdgeTypeOf(Property.IS_INSTANCE_OF)
	public static String getPredicateType(String pred, String obj)
	{
		if(BuildQ2SemanticService.rdfsEdgeSet.contains(pred))
			return RDFSPROP;
		else if(obj.startsWith("http"))
			return OBJPROP;
		else if(obj.startsWith("\""))
			return DATATYPEPROP;
		return "";
	}

	/**
	 * main entry for graph building
	 * @param path
	 * @throws Exception
	 */
	public void buildGraphs(String path, boolean scoring) throws Exception
	{	
		propCount = new TreeMap<String, Integer>();
		conceptCount = new TreeMap<String, Integer>();
		elemPool = new TreeMap<String, SummaryGraphElement>();
		indiv2con = new LuceneMap();
		
		//preparation
		firstScan(path, scoring);
		System.gc();
		//summary graph
		secondScan(path, scoring);
		System.gc();
		//schema graph
		thirdScan(path, scoring);
		System.gc();

	}
	
	/**
	 * First scan for nt file to get (1)index of indiv2concept (2)concept and its freq (3)property and its freq (4)total number of instance and proerpty
	 * @param path
	 * @throws Exception
	 */
	public void firstScan(String path, boolean scoring) throws Exception
	{
		System.out.println("=======firstScan==========");
		//open index to prepare for writing
		indiv2con.openWriter(path, true);
		//get the instance number if we predefined which will save a lot of memory to maintain a indivSet
		TreeSet<String> indivSet = null;
		if(scoring)
		{
			indivSize = BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource)==null?
					-1:BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource);
			if(indivSize == -1)
				indivSet = new TreeSet<String>();
		}
		
		LineNumberReader br = new LineNumberReader(new FileReader(BuildQ2SemanticService.source));
		String line;
		
		while((line = br.readLine())!=null)
		{
			if(br.getLineNumber()%10000==0)
				System.out.println("1st scan\t"+br.getLineNumber());

			String[] part = getSubjPredObj(line);
			if(part==null) continue;
			String subj = part[0];
			String pred = part[1];
			String obj = part[2];
			
			if(scoring && indivSize ==-1 && getSubjectType(pred, obj).equals(INDIVIDUAL))
				indivSet.add(subj);//calculate instance number
			if(scoring && indivSize ==-1 && getObjectType(pred, obj).equals(INDIVIDUAL))
				indivSet.add(obj);//calculate instance number
			
			if(scoring && !getPredicateType(pred, obj).equals(RDFSPROP))
			{
				propSize++;
				Integer n = propCount.get(pred);
				if(n==null) n = Integer.valueOf(0);
				propCount.put(pred, n+1);//calculate property frequency
			}
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(CONCEPT))
			{
				indiv2con.put(subj, obj);
				if(scoring)
				{
					Integer i = conceptCount.get(obj);
					if(i==null) i = Integer.valueOf(0);
					conceptCount.put(obj, i+1);//calculate concept frequency
				}
			}
		}
		//if the instance number is not predefined, then compute it
		if(scoring && indivSize == -1)
		{
			indivSize = indivSet.size();
			indivSet.clear();
			indivSet = null;
		}
		br.close();
		//close the index
		indiv2con.closeWriter();
		System.out.println("indivSize: "+indivSize+"\tpropSize: "+propSize);
	}
	
	/**
	 * Building summary graph from nt file which will use the results of the first scan
	 * @param path
	 * @throws Exception
	 */
	public void secondScan(String path, boolean scoring) throws Exception
	{
		System.out.println("=======secondScan==========");
		indiv2con.openSearcher(path);//open indiv2con index
		cache = new TreeMap<String, Set<String>>();//cache of indiv-concepts results
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);
		
		LineNumberReader br = new LineNumberReader(new FileReader(BuildQ2SemanticService.source));
		String line;
		hits = 0;//record hits of cache
		while((line = br.readLine())!=null)
		{
			if(br.getLineNumber()%10000==0)
				System.out.println("2nd scan\t"+br.getLineNumber()+"\tcache:"+cache.size()+"\thits:"+hits);
			
			String[] part = getSubjPredObj(line);
			if(part == null) continue;
			String subj = part[0];
			String pred = part[1];
			String obj = part[2];
//			add concept-relation-concepjt into graph
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(OBJPROP))
			{
				Set<String> subjParent = null, objParent = null;
				subjParent = getParent(subj, hits);
				if(subjParent == null) continue;

				objParent = getParent(obj, hits);
				if(objParent == null) continue;
				
				for(String str: subjParent)
				{
					SummaryGraphElement s = getElemFromUri(str, scoring);
					for(String otr: objParent)
					{
						SummaryGraphElement o = getElemFromUri(otr, scoring);
						SummaryGraphElement p = getElem(pred, SummaryGraphElement.RELATION);
						if(scoring)
						{
							Integer i = propCount.get(pred);
							if(i==null) p.setCost(Double.MAX_VALUE);
							else p.setCost(i.doubleValue()/propSize);
						}
						if(!summaryGraph.containsVertex(s))
							summaryGraph.addVertex(s);
						if(!summaryGraph.containsVertex(o))
							summaryGraph.addVertex(o);
						if(!summaryGraph.containsVertex(p))
							summaryGraph.addVertex(p);

						SummaryGraphEdge edge1 = new SummaryGraphEdge(s, p, SummaryGraphEdge.DOMAIN_EDGE);
						SummaryGraphEdge edge2 = new SummaryGraphEdge(p, o, SummaryGraphEdge.RANGE_EDGE);
						if(!summaryGraph.containsEdge(edge1))
							summaryGraph.addEdge(s, p, edge1);
						if(!summaryGraph.containsEdge(edge2))
							summaryGraph.addEdge(p, o, edge2);
					}		
				}
				subjParent = null;
				objParent = null;
			}
//			add subclass relation between concepts
			else if(pred.equals(BuildQ2SemanticService.rdfsEdge[1]) && getSubjectType(pred, obj).equals(CONCEPT) && getObjectType(pred, obj).equals(CONCEPT))
			{
				SummaryGraphElement elem1 = getElemFromUri(subj, scoring);
				if(!summaryGraph.containsVertex(elem1))
					summaryGraph.addVertex(elem1);
				SummaryGraphElement elem2 = getElemFromUri(obj, scoring);
				if(!summaryGraph.containsVertex(elem2))
					summaryGraph.addVertex(elem2);
				if(!summaryGraph.containsVertex(SummaryGraphElement.SUBCLASS))
					summaryGraph.addVertex(SummaryGraphElement.SUBCLASS);
				SummaryGraphEdge edge1 = new SummaryGraphEdge(elem1, SummaryGraphElement.SUBCLASS, SummaryGraphEdge.SUBCLASS_EDGE);
				if(!summaryGraph.containsEdge(edge1))
					summaryGraph.addEdge(elem1, SummaryGraphElement.SUBCLASS, edge1);
				SummaryGraphEdge edge2 = new SummaryGraphEdge(SummaryGraphElement.SUBCLASS, elem2, SummaryGraphEdge.SUPERCLASS_EDGE);
				if(!summaryGraph.containsEdge(edge2))
					summaryGraph.addEdge(SummaryGraphElement.SUBCLASS, elem2,  edge2);
			}
		}
		br.close();
		System.out.println("write summary graph");
//		writer summary graph
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.summaryObj);
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.summaryRDF);

		indiv2con.closeSearcher();
	}
	
	/**
	 * Build schema graph from nt file
	 * @param path
	 * @throws Exception
	 */
	public void thirdScan(String path, boolean scoring) throws Exception
	{
		System.out.println("=======thirdScan==========");
		indiv2con.openSearcher(path);
		if(cache == null)//init the cache if it is not initialized
			cache = new TreeMap<String, Set<String>>();
//		read the graph from obj file
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = readGraphIndexFromFile(BuildQ2SemanticService.summaryObj);
		TreeMap<String, TreeSet<String>> con2attr = new TreeMap<String, TreeSet<String>>();
		TreeMap<String, String> attr2datatype = new TreeMap<String, String>();
		hits = 0;
		LineNumberReader br = new LineNumberReader(new FileReader(BuildQ2SemanticService.source));
		String line;
		while((line = br.readLine())!=null)
		{
			if(br.getLineNumber()%10000==0)
				System.out.println("3rd scan\t"+br.getLineNumber()+"\tcache:"+cache.size()+"\thits:"+hits);
			
			String[] part = getSubjPredObj(line);
			if(part == null) continue;
			String subj = part[0];
			String pred = part[1];
			String obj = part[2];
			
//			System.out.println(part[2]);
//			store attribute between concept and datatype
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(DATATYPEPROP) && getObjectType(pred, obj).equals(LITERAL))
			{
				Set<String> cons = null;
				cons = getParent(subj, hits);
				if(cons == null) continue;
				
				for(String con: cons)
				{
					TreeSet<String> set = con2attr.get(con);
					if(set==null) set = new TreeSet<String>();
					set.add(pred);
					con2attr.put(con, set);
				}
				cons.clear();
				cons = null;
				
				if(line.lastIndexOf('\"')!=-1)
				{
					String snippet = line.substring(line.lastIndexOf('\"'));
					String datatype = snippet.substring(snippet.indexOf('<')+1, snippet.lastIndexOf('>'));
					if(datatype.startsWith(DATATYPE_URI))
						attr2datatype.put(pred, datatype);
				}
			}
		}
		br.close();
		
		for(String con: con2attr.keySet())
		{
			for(String attr: con2attr.get(con))
			{
				SummaryGraphElement elem1 = getElemFromUri(con, scoring);
				SummaryGraphElement prop = getElem(attr, SummaryGraphElement.ATTRIBUTE);
				if(scoring)
				{
					Integer i = propCount.get(attr);
					if(i==null) prop.setCost(Double.MAX_VALUE);
					else prop.setCost(i.doubleValue()/propSize);
				}
				SummaryGraphElement elem2;
				if(attr2datatype.containsKey(attr))
					elem2 = getElem(attr2datatype.get(attr), SummaryGraphElement.DATATYPE);
				else elem2 = getElem(LABEL, SummaryGraphElement.DATATYPE);
				if(!summaryGraph.containsVertex(elem1))
					summaryGraph.addVertex(elem1);
				if(!summaryGraph.containsVertex(prop))
					summaryGraph.addVertex(prop);
				if(!summaryGraph.containsVertex(elem2))
					summaryGraph.addVertex(elem2);
				SummaryGraphEdge edge1 = new SummaryGraphEdge(elem1, prop, SummaryGraphEdge.DOMAIN_EDGE);
				if(!summaryGraph.containsEdge(edge1))
					summaryGraph.addEdge(elem1, prop, edge1);
				SummaryGraphEdge edge2 = new SummaryGraphEdge(prop, elem2, SummaryGraphEdge.RANGE_EDGE);
				if(!summaryGraph.containsEdge(edge2))
					summaryGraph.addEdge(prop, elem2, edge2);
			}
		}
		
		con2attr.clear();
		con2attr = null;
		attr2datatype.clear();
		attr2datatype = null;
		
		System.out.println("write schema graph");
//		write schema graph
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.schemaObj);
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.schemaRDF);
		
//		print
		System.out.println("=========print schema===========");
//		outputGraphInfo(summaryGraph);
		indiv2con.closeSearcher();
	}
	
	public String[] getSubjPredObj(String line)
	{
		String[] part = Util4NT.processTripleLine(line);
		if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2)
			return null;

		if(!part[0].startsWith("<") || !part[1].startsWith("<"))
			return null;

		part[0] = part[0].substring(1, part[0].length()-1);//remove <>
		part[1] = part[1].substring(1, part[1].length()-1);//remove <>

		if(!part[2].startsWith("<"))
		{
			part[2] = "\""+part[2]+"\"";//if obj is not instance, we give it value "+obj+"
//			System.out.println(part[2]);
		}else if(part[2].length()>=2) part[2] = part[2].substring(1, part[2].length()-1);
		else return null;
		
		if(!part[0].startsWith("http") || !part[1].startsWith("http"))
			return null;
		return part;
	}
	
	public Set<String> getParent(String uri, int hits)
	{
		Set<String> parent = cache.get(uri);//get cache
		try 
		{
			if(parent == null || parent.size()==0)
				parent = indiv2con.search(uri);//do search
			else hits++;
		} catch (Exception e) { e.printStackTrace(); return null; }
		
		if(parent == null || parent.size()==0)
		{
			parent = new TreeSet<String>();
			parent.add(NamedConcept.TOP.getUri());//using top when no res
		}
		if(!cache.containsKey(uri))
			cache.put(uri, parent);//update cache
		
		while(cache.size()>=MAX_CACHE_SIZE)
			cache.clear();//if cache is full, clear it
		return parent;
	}
	/**
	 * get GraphElement from uri which maybe new an element or return an existing one
	 * @param uri
	 * @return
	 */
	public SummaryGraphElement getElemFromUri(String uri, boolean scoring)
	{
		if(uri.equals(NamedConcept.TOP.getUri()))
		{
			SummaryGraphElement elem = getElem(NamedConcept.TOP.getUri(), SummaryGraphElement.CONCEPT);
			if(scoring)
				elem.setCost(TOP_ELEMENT_SCORE);
			return elem;
		}
		else
		{
			SummaryGraphElement elem = getElem(uri, SummaryGraphElement.CONCEPT);
			
			if(scoring)
			{
				double cost;
				Integer i = conceptCount.get(uri);
				if(i==null) cost = Double.MAX_VALUE;
				else cost = i.doubleValue()/indivSize;
				elem.setCost(cost);
			}
			return elem;
		}
	}
	
	/**
	 * low-level implementation of element generation from uri and type which will be called by getElementFromUri
	 * @param uri
	 * @param type
	 * @return
	 */
	public SummaryGraphElement getElem(String uri, int type)
	{
		SummaryGraphElement res = elemPool.get(type+uri);
		if(res == null)
		{
			if(type == SummaryGraphElement.CONCEPT)
				res = new SummaryGraphElement(new NamedConcept(uri), SummaryGraphElement.CONCEPT);
			else if(type == SummaryGraphElement.RELATION)
				res = new SummaryGraphElement(new ObjectProperty(uri), SummaryGraphElement.RELATION);
			else if(type == SummaryGraphElement.ATTRIBUTE)
				res = new SummaryGraphElement(new DataProperty(uri), SummaryGraphElement.ATTRIBUTE);
			else if(type == SummaryGraphElement.DATATYPE)
				res = new SummaryGraphElement(new Datatype(uri), SummaryGraphElement.DATATYPE);
		}
		elemPool.put(type+uri, res);
		return res;
	}

	/**
	 * Write the graph into obj file
	 * @param graph
	 * @param filepath
	 */
	public void writeSummaryGraph(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, String filepath){
		File graphIndex = new File(filepath);
		if(!graphIndex.exists()){
			graphIndex.getParentFile().mkdirs();
			try {
				graphIndex.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(graphIndex));
			out.writeObject(graph);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}

	/**
	 * Write the graph into xml file
	 * @param graph
	 * @param rdffile
	 */
	public void writeSummaryGraphAsRDF(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, String rdffile){
		new File(rdffile).getParentFile().mkdirs();
		BufferedWriter bw = null;
		RDFXMLWriter writer = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rdffile),"UTF-8"));
			writer = new RDFXMLWriter(bw);
			writer.startRDF();

			Set<SummaryGraphElement> nodes = graph.vertexSet();
			if (nodes != null){
				for (SummaryGraphElement node : nodes){
					if(node.getResource() instanceof NamedConcept){

						URI cvertex = new URIImpl(((NamedConcept)node.getResource()).getUri());
						writer.handleStatement(new StatementImpl(cvertex, RDF.TYPE, OWL.CLASS));
					}
					else if (node.equals(SummaryGraphElement.SUBCLASS)){
						Set<SummaryGraphEdge> edges = graph.edgesOf(node);
						URI sourceURI = null; 
						URI targetURI = null;
						for (SummaryGraphEdge edge : edges){
							if (edge.getEdgeLabel().equals(SummaryGraphEdge.SUBCLASS_EDGE)){
								if(sourceURI instanceof NamedConcept)
									sourceURI = new URIImpl(((NamedConcept)edge.getSource().getResource()).getUri());
								if(targetURI instanceof NamedConcept)	
									targetURI = new URIImpl(((NamedConcept)edge.getTarget().getResource()).getUri());
							}
						}
						if(sourceURI!=null && targetURI!=null)
							writer.handleStatement(new StatementImpl(sourceURI, RDFS.SUBCLASSOF, targetURI));
					}
				}
			}

			Set<SummaryGraphEdge> edges = graph.edgeSet();
			TreeMap<String, URI> predicateMap = new TreeMap<String, URI>();
			if (edges != null){
				for (SummaryGraphEdge edge : edges){
					
					String type = edge.getEdgeLabel();
					URI predicate = null;
					URI domain = null;
					URI range = null;
					URI datatype = null;
					if(type.equals(SummaryGraphEdge.DOMAIN_EDGE)){
						if(edge.getTarget().getResource() instanceof IProperty)
						{
							predicate = predicateMap.get(((IProperty)edge.getTarget().getResource()).getUri());
							if(predicate == null)
							{
								predicate = new URIImpl(((IProperty)edge.getTarget().getResource()).getUri());
								predicateMap.put(((IProperty)edge.getTarget().getResource()).getUri(), predicate);
								writer.handleStatement(new StatementImpl(predicate,	RDF.TYPE, OWL.OBJECTPROPERTY));	
							}
						}
						if(edge.getSource().getResource() instanceof INamedConcept)
							domain = new URIImpl(((INamedConcept)edge.getSource().getResource()).getUri());
					}
					else if(type.equals(SummaryGraphEdge.RANGE_EDGE)){
						if(edge.getSource().getResource() instanceof IProperty)
						{
							predicate = predicateMap.get(((IProperty)edge.getSource().getResource()).getUri());
							if(predicate == null)
							{
								predicate = new URIImpl(((IProperty)edge.getSource().getResource()).getUri());
								predicateMap.put(((IProperty)edge.getSource().getResource()).getUri(), predicate);
								writer.handleStatement(new StatementImpl(predicate,	RDF.TYPE, OWL.OBJECTPROPERTY));	
							}
						}
						if(edge.getTarget().getResource() instanceof INamedConcept)
							range = new URIImpl(((INamedConcept)edge.getTarget().getResource()).getUri());
						if(edge.getTarget().getResource() instanceof Datatype)
							datatype = new URIImpl(((Datatype)edge.getTarget().getResource()).getUri());
					}
				
					if(predicate != null && domain != null)
						writer.handleStatement(new StatementImpl(predicate, RDFS.DOMAIN, domain));
					if(predicate != null && range != null)
						writer.handleStatement(new StatementImpl(predicate, RDFS.RANGE, range));
					if(predicate != null && datatype != null)
						writer.handleStatement(new StatementImpl(predicate, RDFS.DATATYPE, datatype));
				}
			}

			writer.endRDF();
			if(bw != null) bw.close();

		} 

		catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Read graph from obj file
	 * @param filepath
	 * @return
	 */
	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> readGraphIndexFromFile(String filepath){
		// retrieve graphIndex
		ObjectInputStream in;
		Pseudograph<SummaryGraphElement,SummaryGraphEdge> newResourceGraph = null;
		try {
			System.out.println(filepath);
			in = new ObjectInputStream(new FileInputStream(filepath));
			newResourceGraph = (Pseudograph<SummaryGraphElement,SummaryGraphEdge>)in.readObject(); 
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newResourceGraph;
	}

	/**
	 * just for graph print
	 * @param graph
	 */
	public void outputGraphInfo(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph) {
		HashSet<String> set = new HashSet<String>();
		for(SummaryGraphEdge edge : graph.edgeSet()) {
			System.out.println( edge.getEdgeLabel()  + "\t");
			String subj = this.getResourceUri(edge.getSource());
			System.out.println(subj);
			String obj = this.getResourceUri(edge.getTarget());
			System.out.println(obj);
			set.add(subj);
			set.add(obj);
			System.out.println();
		}
		System.out.println("Edge=>Node: "+set.size()+"\tNode: "+graph.vertexSet().size());
	}
	/**
	 * just for graph print
	 * @param ele
	 * @return
	 */
	public String getResourceUri(SummaryGraphElement ele) {
		if(ele.getType() == SummaryGraphElement.CONCEPT) {
			return  "C ^ " + ((NamedConcept)ele.getResource()).getUri() + "[" + ele.getEF() + "]";
		}
		else if(ele.getType() == SummaryGraphElement.ATTRIBUTE || ele.getType() == SummaryGraphElement.RELATION){
			return "P ^ " + ((Property)ele.getResource()).getUri() + "[" + ele.getEF() + "]";
		}
		else {
			return "D ^ " + ele.getResource().getLabel() + "[" + ele.getEF() + "]";
		}
	}
	
	/**
	 * test
	 * @param args
	 */
	public static void main(String[] args) {
		SummaryGraphIndexServiceForBTFromNT s = new SummaryGraphIndexServiceForBTFromNT();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = s.readGraphIndexFromFile("D:\\semplore\\swrc-summary.obj.split");
//		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph1 = s.readGraphIndexFromFile("D:\\semplore\\10.15\\schemaObjsRoot\\swrc-schema.obj");
//		s.writeSummaryGraphAsRDF(graph, "D:\\semplore\\objbackup\\freebase-schema.rdf");
		System.out.println(graph.edgeSet().size());
		System.out.println(graph.vertexSet().size());
//		System.out.println(graph1.edgeSet().size());
//		System.out.println(graph1.vertexSet().size());
//		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = new SummaryGraphIndexServiceForBTFromNT().readGraphIndexFromFile("D:\\semplore\\summaryObjsRoot\\dblp-summary.obj");
//		for(SummaryGraphEdge edge: graph.edgeSet())
//			System.out.println(edge.toString());
//		SummaryGraphIndexServiceForBTFromNT nt = new SummaryGraphIndexServiceForBTFromNT();
//		String line = "<http://www.freebase.com/resource/%21%21%21wichtiger_Warnhinweis%21%21%21/guid/9202a8c04000641f8000000001dabab9> <http://www.freebase.com/property/name> \"!!!wichtiger Warnhinweis!!!\"@en .";
//		String[] part = Util4NT.processTripleLine(line);
//		if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2 || part[2].length()<2)
//			return;
////		System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
//		String subj = part[0].substring(1, part[0].length()-1);
//		String pred = part[1].substring(1, part[1].length()-1);
//
//		String obj = part[2];
//		if(!obj.startsWith("<"))
//			obj = "";
//		else obj = part[2].substring(1, part[2].length()-1);
//		if(!subj.startsWith("http") || !pred.startsWith("http"))
//			return;
//		System.out.println(subj+"\t"+pred+"\t"+obj);
//		if(nt.getSubjectType(pred, obj).equals(INDIVIDUAL) && nt.getPredicateType(pred, obj).equals(DATATYPEPROP) && nt.getObjectType(pred, obj).equals(LITERAL))
//		{
//			System.out.println("ok");
//		}
	}
}
