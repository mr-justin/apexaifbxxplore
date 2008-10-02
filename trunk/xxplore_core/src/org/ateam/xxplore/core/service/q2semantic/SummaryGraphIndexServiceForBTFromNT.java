package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	public static double TOP_ELEMENT_SCORE = 2.0, SUBCLASS_ELEMENT_SCORE = 0;
	
	public BerkeleyDB indiv2con;
	public HashMap<String, Integer> propCount;
	public HashMap<String, Integer> conceptCount;
	public int indivSize, propSize = 0;
	public String dbpath;
	

	public String getSubjectType(String pred, String obj)
	{
		if( (pred.equals(BuildQ2SemanticService.rdfsEdge[0]) && obj.equals(BuildQ2SemanticService.rdfsEdge[8])) || pred.equals(BuildQ2SemanticService.rdfsEdge[1]))
			return CONCEPT;
		else if( (pred.equals(BuildQ2SemanticService.rdfsEdge[2]) && obj.equals(BuildQ2SemanticService.rdfsEdge[7])) || pred.equals(BuildQ2SemanticService.rdfsEdge[3])
				|| pred.equals(BuildQ2SemanticService.rdfsEdge[4]))
			return PROPERTY;
		else if(pred.equals(BuildQ2SemanticService.rdfsEdge[0]) || getPredicateType(pred, obj).equals(OBJPROP) || getPredicateType(pred, obj).equals(DATATYPEPROP))
			return INDIVIDUAL;
//		System.err.println("subj~"+pred+"\t"+obj);
		return "";
	}

	public String getObjectType(String pred, String obj)
	{
		if(pred.equals(BuildQ2SemanticService.rdfsEdge[0]))
			return CONCEPT;
		else if(getPredicateType(pred, obj).equals(OBJPROP))
			return INDIVIDUAL;
		else if(getPredicateType(pred, obj).equals(DATATYPEPROP))
			return LITERAL;
//		System.err.println("obj~"+pred+"\t"+obj);
		return "";
	}

	// for example isEdgeTypeOf(Property.IS_INSTANCE_OF)
	public String getPredicateType(String pred, String obj)
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
		propCount = new HashMap<String, Integer>();
		conceptCount = new HashMap<String, Integer>();
		initDB(path);
		firstScan();
		closeDB();
		
		initDB(path);
		secondScan();
		closeDB();
	}
	public void initDB(String dbpath) throws Exception
	{
		this.dbpath = dbpath;
		indiv2con = new BerkeleyDB();
		indiv2con.openDB(dbpath, INDIV2CON);
	}
	public void closeDB() throws Exception
	{
		indiv2con.closeDB();
	}
	
	public void firstScan() throws Exception
	{
		System.out.println("=======firstScan==========");
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
				System.out.println(count);

			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2 || part[2].length()<2)
				continue;
//			System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
			if(!part[0].startsWith("<") || !part[1].startsWith("<"))
				continue;
//			System.out.println(part[0]+"\t"+part[1]+"\t"+part[2]);
			String subj = part[0].substring(1, part[0].length()-1);
			String pred = part[1].substring(1, part[1].length()-1);
			String obj = part[2];
			if(!obj.startsWith("<"))
				obj = "";
			else if(obj.length()>=2) obj = part[2].substring(1, part[2].length()-1);
			if(!subj.startsWith("http") || !pred.startsWith("http"))
					continue;
//			System.out.println(subj+"\t"+pred+"\t"+obj);
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
		System.out.println("indivSize: "+indivSize+"\tpropSize: "+propSize);
	}
	
	public void secondScan() throws Exception
	{
		System.out.println("=======secondScan==========");
		Pseudograph<SummaryGraphElement, SummaryGraphEdge>summaryGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);
		HashMap<String, HashSet<String>> con2attr = new HashMap<String, HashSet<String>>();
		BufferedReader br = new BufferedReader(new FileReader(BuildQ2SemanticService.source));
		String line;
		int count = 0;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
				System.out.println(count);
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
//			if(pred.contains("omasJ96"))System.out.println(obj);
//			System.out.println(getPredicateType(pred, obj));
//			add relation between concepts of individuals
//			if(getSubjectType(pred, obj).equals(INDIVIDUAL))System.out.println( getObjectType(pred, obj)+"\t"+getPredicateType(pred, obj));
			if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getObjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(OBJPROP))
			{
				HashSet<SummaryGraphElement> subjParent = new HashSet<SummaryGraphElement>(), objParent = new HashSet<SummaryGraphElement>();
				List<String> list;
				try {
					list= indiv2con.search(subj);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					continue;
				}
				
				
				if(list==null)
				{
					SummaryGraphElement elem = new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT);
					elem.setCost(TOP_ELEMENT_SCORE);
					subjParent.add(elem);
				}
				else
					for(String str: list)
					{
						SummaryGraphElement elem = new SummaryGraphElement(new NamedConcept(str), SummaryGraphElement.CONCEPT);
						Integer i = conceptCount.get(str);
						if(i==null) elem.setCost(Double.MAX_VALUE);
						else elem.setCost(i.doubleValue()/indivSize);
						subjParent.add(elem);
					}
//				System.out.println(obj);
				try {
					list = indiv2con.search(obj);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					continue;
				}
				
				if(list==null)
				{
					SummaryGraphElement elem = new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT);
					elem.setCost(TOP_ELEMENT_SCORE);
					objParent.add(elem);
				}
				else
					for(String str: list)
					{
						SummaryGraphElement elem = new SummaryGraphElement(new NamedConcept(str), SummaryGraphElement.CONCEPT);
						Integer i = conceptCount.get(str);
						if(i==null) elem.setCost(Double.MAX_VALUE);
						else elem.setCost(i.doubleValue()/indivSize);
						objParent.add(elem);
					}
				for(SummaryGraphElement s: subjParent)
					for(SummaryGraphElement o: objParent)
					{//if(o.getEF()==TOP_ELEMENT_SCORE && objParent.size()!=1)System.out.println(objParent.size());
						SummaryGraphElement p = new SummaryGraphElement(new ObjectProperty(pred), SummaryGraphElement.RELATION);
						Integer i = propCount.get(pred);
						if(i==null) p.setCost(Double.MAX_VALUE);
						else p.setCost(i.doubleValue()/propSize);
						summaryGraph.addVertex(s);
						summaryGraph.addVertex(o);
						summaryGraph.addVertex(p);
//						if(((Property)p.getResource()).getUri().contains("http://lsdis.cs.uga.edu/projects/semdis/opus#author") && ((NamedConcept)o.getResource()).getUri().contains("Person"))
//							System.out.println("1");
						SummaryGraphEdge edge1 = new SummaryGraphEdge(s, p, SummaryGraphEdge.DOMAIN_EDGE);
						SummaryGraphEdge edge2 = new SummaryGraphEdge(p, o, SummaryGraphEdge.RANGE_EDGE);
						if(!summaryGraph.containsEdge(edge1))
							summaryGraph.addEdge(s, p, edge1);
						if(!summaryGraph.containsEdge(edge2))
							summaryGraph.addEdge(p, o, edge2);
					}				
			}
//			add subclass relation between concepts
			else if(pred.equals(BuildQ2SemanticService.rdfsEdge[1]) && getSubjectType(pred, obj).equals(CONCEPT) && getObjectType(pred, obj).equals(CONCEPT))
			{
				SummaryGraphElement elem1 = new SummaryGraphElement(new NamedConcept(subj), SummaryGraphElement.CONCEPT);
				if(!summaryGraph.containsVertex(elem1))
				{
					Integer i = conceptCount.get(subj);
					if(i==null) elem1.setCost(Double.MAX_VALUE);
					else elem1.setCost(i.doubleValue()/indivSize);
					summaryGraph.addVertex(elem1);
				}
				SummaryGraphElement elem2 = new SummaryGraphElement(new NamedConcept(obj), SummaryGraphElement.CONCEPT);
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
//			store attribute between concept and datatype
			else if(getSubjectType(pred, obj).equals(INDIVIDUAL) && getPredicateType(pred, obj).equals(DATATYPEPROP) && getObjectType(pred, obj).equals(LITERAL))
			{
				HashSet<String> cons = new HashSet<String>();
				List<String> l;
				try {
					l= indiv2con.search(subj);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					continue;
				}
				
				if(l==null) cons.add(NamedConcept.TOP.getUri());
				else cons.addAll(l);
				for(String con: cons)
				{
					HashSet<String> set = con2attr.get(con);
					if(set==null) set = new HashSet<String>();
					set.add(pred);
					con2attr.put(con, set);
				}
			}
		}
		System.out.println("write summary graph");
//		writer summary graph
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.summaryObj);
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.summaryRDF);
//		System.out.println("=========print summary===========");
//		outputGraphInfo(summaryGraph);
//		construct schema graph
		for(String con: con2attr.keySet())
		{
			for(String attr: con2attr.get(con))
			{
				SummaryGraphElement elem1 = new SummaryGraphElement(new NamedConcept(con), SummaryGraphElement.CONCEPT);
				Integer i = conceptCount.get(con);
				if(i==null) elem1.setCost(Double.MAX_VALUE);
				else elem1.setCost(i.doubleValue()/indivSize);
				SummaryGraphElement prop = new SummaryGraphElement(new DataProperty(attr), SummaryGraphElement.ATTRIBUTE);
				i = propCount.get(attr);
				if(i==null) prop.setCost(Double.MAX_VALUE);
				else prop.setCost(i.doubleValue()/propSize);
				SummaryGraphElement elem2 = new SummaryGraphElement(new Datatype(LABEL), SummaryGraphElement.DATATYPE);
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
		
		System.out.println("write schema graph");
//		write schema graph
		writeSummaryGraph(summaryGraph, BuildQ2SemanticService.schemaObj);
		writeSummaryGraphAsRDF(summaryGraph, BuildQ2SemanticService.schemaRDF);
		
//		print
		System.out.println("=========print schema===========");
		outputGraphInfo(summaryGraph);
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
			HashMap<String, URI> predicateMap = new HashMap<String, URI>();
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
}
