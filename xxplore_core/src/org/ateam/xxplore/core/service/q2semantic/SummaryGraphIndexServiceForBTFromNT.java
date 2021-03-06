package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;

import com.ibm.semplore.imports.impl.data.load.Util4NT;


public class SummaryGraphIndexServiceForBTFromNT {

	//CONSTANT
	public static String CONCEPT="concept", 
						INDIVIDUAL="individual", 
						LITERAL="literal",
						PROPERTY="property", 
						DATATYPEPROP="datatypeprop", 
						OBJPROP="objprop", 
						RDFSPROP="rdfsprop";//predicate type
		
	public static String LABEL= "http://www.w3.org/2001/XMLSchema#label";
	public static String INDIV2CON = "indiv2con";
	public static String RELPOOL = "-splitRelPool";
	public static double TOP_ELEMENT_SCORE = 2.0, SUBCLASS_ELEMENT_SCORE = 0;
	
	public LuceneMap indiv2con;
	public TreeMap<String, Integer> propCount;
	public TreeMap<String, Integer> conceptCount;
	public TreeMap<String, Set<String>> cache;
	public TreeMap<String, SummaryGraphElement> elemPool;
	public int MAX_CACHE_SIZE = 10000000; 
	public int indivSize, propSize = 0;
	

	public static String getSubjectType(String pred, String obj)
	{
		if( (pred.equals(BuildQ2SemanticService.rdfsEdge[0]) && obj.equals(BuildQ2SemanticService.rdfsEdge[8])) || pred.equals(BuildQ2SemanticService.rdfsEdge[1]))
			return CONCEPT;
		else if( (pred.equals(BuildQ2SemanticService.rdfsEdge[4]) && obj.equals(BuildQ2SemanticService.rdfsEdge[7])) || pred.equals(BuildQ2SemanticService.rdfsEdge[2])
				|| pred.equals(BuildQ2SemanticService.rdfsEdge[3]))
			return PROPERTY;
		else if(pred.equals(BuildQ2SemanticService.rdfsEdge[0]) || getPredicateType(pred, obj).equals(OBJPROP) || getPredicateType(pred, obj).equals(DATATYPEPROP))
			return INDIVIDUAL;
//		System.err.println("subj~"+pred+"\t"+obj);
		return "";
	}

	public static String getObjectType(String pred, String obj)
	{
		if(pred.equals(BuildQ2SemanticService.rdfsEdge[0]) && !BuildQ2SemanticService.rdfsEdgeSet.contains(obj))
			return CONCEPT;
		else if(getPredicateType(pred, obj).equals(OBJPROP))
			return INDIVIDUAL;
		else if(getPredicateType(pred, obj).equals(DATATYPEPROP))
			return LITERAL;
//		System.err.println("obj~"+pred+"\t"+obj);
		return "";
	}

	// for example isEdgeTypeOf(Property.IS_INSTANCE_OF)
	public static String getPredicateType(String pred, String obj)
	{//System.out.println(obj);
		if(BuildQ2SemanticService.rdfsEdgeSet.contains(pred))
			return RDFSPROP;
		else if(obj.startsWith("http"))
			return OBJPROP;
		else if(obj.equals(""))
			return DATATYPEPROP;
//		System.err.println("pred~"+pred);
		return "";
	}

	public void buildGraphs(String path) throws Exception
	{	
		propCount = new TreeMap<String, Integer>();
		conceptCount = new TreeMap<String, Integer>();
		elemPool = new TreeMap<String, SummaryGraphElement>();
		indiv2con = new LuceneMap();
		
		//preparation
		firstScan(path);
		System.gc();
		//summary graph
		secondScan(path);
		System.gc();
		//schema graph
		thirdScan(path);
		System.gc();

	}
	
	public void firstScan(String path) throws Exception
	{
		System.out.println("=======firstScan==========");
		indiv2con.openWriter(path, true);
		indivSize = BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource)==null?
				-1:BuildQ2SemanticService.instNumMap.get(BuildQ2SemanticService.datasource);
		BufferedReader br = new BufferedReader(new FileReader(BuildQ2SemanticService.source));
		TreeSet<String> indivSet = new TreeSet<String>();
		String line;
		int count = 0;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
				System.out.println("1st scan\t"+count);

			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2 || part[2].length()<2)
				continue;

			if(!part[0].startsWith("<") || !part[1].startsWith("<"))
				continue;

			String subj = part[0].substring(1, part[0].length()-1);
			String pred = part[1].substring(1, part[1].length()-1);
			String obj = part[2];
			if(!obj.startsWith("<"))
				obj = "";
			else if(obj.length()>=2) obj = part[2].substring(1, part[2].length()-1);
			if(!subj.startsWith("http") || !pred.startsWith("http"))
					continue;

			if(indivSize ==-1 && getSubjectType(pred, obj).equals(INDIVIDUAL))
				indivSet.add(subj);
			if(indivSize ==-1 && getObjectType(pred, obj).equals(INDIVIDUAL))
				indivSet.add(obj);
			if(!getPredicateType(pred, obj).equals(RDFSPROP))
			{
				propSize++;
				Integer n = propCount.get(pred);
				if(n==null) n = Integer.valueOf(0);
				propCount.put(pred, n+1);
			}
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(CONCEPT))
			{
				indiv2con.put(subj, obj);
				Integer i = conceptCount.get(obj);
				if(i==null) i = Integer.valueOf(0);
				conceptCount.put(obj, i+1);
			}
		}
		if(indivSize ==-1)
		{
			indivSize = indivSet.size();
			indivSet.clear();
			indivSet = null;
		}
		br.close();
		indiv2con.closeWriter();
		System.out.println("indivSize: "+indivSize+"\tpropSize: "+propSize);
	}
	
	public void secondScan(String path) throws Exception
	{
		System.out.println("=======secondScan==========");
		indiv2con.openSearcher(path);
		cache = new TreeMap<String, Set<String>>();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);
		BufferedReader br = new BufferedReader(new FileReader(BuildQ2SemanticService.source));
		String line;
		int count = 0, hits = 0;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
				System.out.println("2nd scan\t"+count+"\tcache:"+cache.size()+"\thits:"+hits);
			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2 || part[2].length()<2)
				continue;
//			System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
			String subj = part[0].substring(1, part[0].length()-1);
			String pred = part[1].substring(1, part[1].length()-1);

			String obj = part[2];
			if(!obj.startsWith("<"))
				obj = "";
			else obj = part[2].substring(1, part[2].length()-1);
			if(!subj.startsWith("http") || !pred.startsWith("http"))
				continue;
			
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(OBJPROP))
			{
				Set<String> subjParent = null, objParent = null;
				subjParent = cache.get(subj);
				try 
				{
					if(subjParent == null || subjParent.size()==0)
						subjParent = indiv2con.search(subj);
					else hits++;
				} catch (Exception e) { e.printStackTrace(); continue; }
				
				if(subjParent == null || subjParent.size()==0)
				{
					subjParent = new TreeSet<String>();
					subjParent.add(NamedConcept.TOP.getUri());
				}
				if(!cache.containsKey(subj))
					cache.put(subj, subjParent);
//				System.out.println(obj);
				objParent = cache.get(obj);
				try 
				{
					if(objParent == null || objParent.size()==0)
						objParent = indiv2con.search(obj);
					else hits++;
				} catch (Exception e) { e.printStackTrace(); continue; }
				
				if(objParent == null || objParent.size()==0)
				{
					objParent = new TreeSet<String>();
					objParent.add(NamedConcept.TOP.getUri());
				}
				if(!cache.containsKey(obj))
					cache.put(obj, objParent);
				
				while(cache.size()>=MAX_CACHE_SIZE)
					cache.clear();
				
				for(String str: subjParent)
				{
					SummaryGraphElement s = getElemFromUri(str);
					for(String otr: objParent)
					{
						SummaryGraphElement o = getElemFromUri(otr);
						SummaryGraphElement p = getElem(pred, SummaryGraphElement.RELATION);
						Integer i = propCount.get(pred);
						if(i==null) p.setCost(Double.MAX_VALUE);
						else p.setCost(i.doubleValue()/propSize);
						summaryGraph.addVertex(s);
						summaryGraph.addVertex(o);
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
				SummaryGraphElement elem1 = getElem(subj, SummaryGraphElement.CONCEPT);
				if(!summaryGraph.containsVertex(elem1))
				{
					Integer i = conceptCount.get(subj);
					if(i==null) elem1.setCost(Double.MAX_VALUE);
					else elem1.setCost(i.doubleValue()/indivSize);
					summaryGraph.addVertex(elem1);
				}
				SummaryGraphElement elem2 = getElem(obj, SummaryGraphElement.CONCEPT);
				if(!summaryGraph.containsVertex(elem2))
				{
					Integer i = conceptCount.get(obj);
					if(i==null) elem2.setCost(Double.MAX_VALUE);
					else elem2.setCost(i.doubleValue()/indivSize);
					summaryGraph.addVertex(elem2);
				}
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
	
	public void thirdScan(String path) throws Exception
	{
		System.out.println("=======thirdScan==========");
		indiv2con.openSearcher(path);
		if(cache == null)
			cache = new TreeMap<String, Set<String>>();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph = readGraphIndexFromFile(BuildQ2SemanticService.summaryObj);
		TreeMap<String, TreeSet<String>> con2attr = new TreeMap<String, TreeSet<String>>();
		BufferedReader br = new BufferedReader(new FileReader(BuildQ2SemanticService.source));
		String line;
		int count = 0;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
				System.out.println("3rd scan\t"+count+"\tcache:"+cache.size());
			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2 || part[2].length()<2)
				continue;
//			System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
			String subj = part[0].substring(1, part[0].length()-1);
			String pred = part[1].substring(1, part[1].length()-1);

			String obj = part[2];
			if(!obj.startsWith("<"))
				obj = "";
			else obj = part[2].substring(1, part[2].length()-1);
			if(!subj.startsWith("http") || !pred.startsWith("http"))
				continue;
			
//			store attribute between concept and datatype
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(DATATYPEPROP) && getObjectType(pred, obj).equals(LITERAL))
			{
				Set<String> cons = null;
				cons = cache.get(subj);
				try {
					if(cons == null || cons.size()==0)
						cons = indiv2con.search(subj);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					continue;
				}
				
				if(cons==null || cons.size()==0)
				{
					cons = new TreeSet<String>();
					cons.add(NamedConcept.TOP.getUri());
				}
				cache.put(subj, cons);
				while(cache.size()>=MAX_CACHE_SIZE)
					cache.clear();

				for(String con: cons)
				{
					TreeSet<String> set = con2attr.get(con);
					if(set==null) set = new TreeSet<String>();
					set.add(pred);
					con2attr.put(con, set);
				}
				cons.clear();
				cons = null;
			}
		}
		br.close();
		
		for(String con: con2attr.keySet())
		{
//			System.out.println(con);
			for(String attr: con2attr.get(con))
			{
//				System.out.println("\t"+attr);
				SummaryGraphElement elem1 = getElem(con, SummaryGraphElement.CONCEPT);
				Integer i = conceptCount.get(con);
				if(i==null) elem1.setCost(Double.MAX_VALUE);
				else elem1.setCost(i.doubleValue()/indivSize);
				SummaryGraphElement prop = getElem(attr, SummaryGraphElement.ATTRIBUTE);
				i = propCount.get(attr);
				if(i==null) prop.setCost(Double.MAX_VALUE);
				else prop.setCost(i.doubleValue()/propSize);
				SummaryGraphElement elem2 = getElem(LABEL, SummaryGraphElement.DATATYPE);
				summaryGraph.addVertex(elem1);
				summaryGraph.addVertex(prop);
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
		
		System.out.println("write schema graph");
//		write schema graph
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.schemaObj);
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.schemaRDF);
		
//		print
		System.out.println("=========print schema===========");
		outputGraphInfo(summaryGraph);
		indiv2con.closeSearcher();
	}
	
	
	public SummaryGraphElement getElemFromUri(String uri)
	{
		if(uri.equals(NamedConcept.TOP.getUri()))
		{
			SummaryGraphElement elem = getElem(NamedConcept.TOP.getUri(), SummaryGraphElement.CONCEPT);
			elem.setCost(TOP_ELEMENT_SCORE);
			return elem;
		}
		else
		{
			double cost;
			Integer i = conceptCount.get(uri);
			if(i==null) cost = 0;
			else cost = i.doubleValue()/indivSize;
			
			SummaryGraphElement elem = getElem(uri, SummaryGraphElement.CONCEPT);
			elem.setCost(cost);
			return elem;
		}
	}
	
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


	public void writeSummaryGraph(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, String filepath){
		File graphIndex = new File(filepath);
		if(!graphIndex.exists()){
			graphIndex.getParentFile().mkdirs();
			try {
				graphIndex.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(graphIndex));
			out.writeObject(graph);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



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
//						System.out.println("ww"+((NamedConcept)node.getResource()).getUri());
						URI cvertex = new URIImpl(((NamedConcept)node.getResource()).getUri());
						writer.handleStatement(new StatementImpl(cvertex, RDF.TYPE, OWL.CLASS));
					}
					else if (node.equals(SummaryGraphElement.SUBCLASS)){
						Set<SummaryGraphEdge> edges = graph.edgesOf(node);
						//Emergency.checkPrecondition(edges.size() == 2, "A subclass vertex should have only one outhoing and one ingoing edge");
						URI sourceURI = null; 
						URI targetURI = null;
						for (SummaryGraphEdge edge : edges){
							if (edge.getEdgeLabel().equals(SummaryGraphEdge.SUBCLASS_EDGE)){
								if(sourceURI instanceof NamedConcept)
									sourceURI = new URIImpl(((NamedConcept)edge.getSource().getResource()).getUri());
								if(targetURI instanceof NamedConcept)	
									targetURI = new URIImpl(((NamedConcept)edge.getTarget().getResource()).getUri());
							}
							//must be super class edge
//							if(edge.getTarget().getResource() instanceof NamedConcept){
//								targetURI = new URIImpl(((NamedConcept)edge.getTarget().getResource()).getUri());
//							}
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

		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> readGraphIndexFromFile(String filepath){
		// retrieve graphIndex
		ObjectInputStream in;
		Pseudograph<SummaryGraphElement,SummaryGraphEdge> newResourceGraph = null;
		try {
			System.out.println(filepath);
			in = new ObjectInputStream(new FileInputStream(filepath));
			newResourceGraph = (Pseudograph<SummaryGraphElement,SummaryGraphEdge>)in.readObject(); 
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newResourceGraph;
	}

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
	
	public static void main(String[] args) {
		SummaryGraphIndexServiceForBTFromNT s = new SummaryGraphIndexServiceForBTFromNT();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = s.readGraphIndexFromFile("D:\\semplore\\freebase-summary.obj");
//		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph1 = s.readGraphIndexFromFile("D:\\semplore\\objbackup\\dblp-schema.obj");
//		s.writeSummaryGraphAsRDF(graph, "D:\\semplore\\objbackup\\freebase-schema.rdf");
//		System.out.println(graph.edgeSet().size());
//		System.out.println(graph.vertexSet().size());
//		System.out.println(graph1.edgeSet().size());
//		System.out.println(graph1.vertexSet().size());
//		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = new SummaryGraphIndexServiceForBTFromNT().readGraphIndexFromFile("D:\\semplore\\summaryObjsRoot\\dblp-summary.obj");
		for(SummaryGraphEdge edge: graph.edgeSet())
			System.out.println(edge.toString());
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
