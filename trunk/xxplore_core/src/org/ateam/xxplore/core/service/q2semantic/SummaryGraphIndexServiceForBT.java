package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ateam.xxplore.core.service.search.SummaryGraphEdge;
import org.ateam.xxplore.core.service.search.SummaryGraphElement;
import org.jgrapht.graph.Pseudograph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;


public class SummaryGraphIndexServiceForBT {

	private static String PREFIX = "http://www.w3.org/2001/XMLSchema#";
	private int DEFAULT_EF_TOP = 2; 

	public SummaryGraphIndexServiceForBT() {}

	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> computeSummaryGraph(String repositoryPath, boolean withScores) throws Exception {

		Pseudograph<SummaryGraphElement, SummaryGraphEdge>resourceGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);
		
		SesameDao sd = new SesameDao(repositoryPath);
		SesameDao sdd = new SesameDao(repositoryPath);
		
		sd.findAllTriples();
		int count = 0;
		while(sd.hasNext())
		{
			count++;
			if(count%10000==0)
				System.out.println(count);
			sd.next();
			String source = sd.getSubject();
			String target = sd.getObject();
			String property = sd.getPredicate();

			if(sd.getPredicateType().equals(SesameDao.OBJPROP) /*&& !sd..isEdgeTypeOf(Property.IS_INSTANCE_OF)/*!propMem.equals(Property.IS_INSTANCE_OF) */) {
				if(sd.getSubjectType().equals(SesameDao.INDIVIDUAL) && sd.getObjectType().equals(SesameDao.INDIVIDUAL)) {
					Set<SummaryGraphElement> sourceVertices = new HashSet<SummaryGraphElement>();
					Set<SummaryGraphElement> targetVertices = new HashSet<SummaryGraphElement>();

					//System.out.println(source);
					sdd.findTypes(source);
					if(sdd.hasNext()) {
						while(sdd.hasNext()) {
							sdd.next();
							sourceVertices.add(new SummaryGraphElement(new NamedConcept(sdd.getObject()), SummaryGraphElement.CONCEPT));
						}
					} else {
						sourceVertices.add(new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT));
					}
					//sdd.close();
					sdd.findTypes(target);
					if(sdd.hasNext()) {
						while(sdd.hasNext()) {
							sdd.next();
							targetVertices.add(new SummaryGraphElement(new NamedConcept(sdd.getObject()), SummaryGraphElement.CONCEPT));
						}
					} else {
						targetVertices.add(new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT));
					}

					if (sourceVertices != null && sourceVertices.size() != 0 && targetVertices != null && targetVertices.size() != 0) {
						for (SummaryGraphElement sourceVertex : sourceVertices) {
							for (SummaryGraphElement targetVertex : targetVertices) {
								resourceGraph.addVertex(sourceVertex);
								resourceGraph.addVertex(targetVertex);
								addGraphElement(sourceVertex, targetVertex, new ObjectProperty(property), resourceGraph);
							}
						}
					}					
				}
			}
		}

		sd.findAllConcepts();
		while(sd.hasNext())
		{
			sd.next();
			{
				SummaryGraphElement conE = new SummaryGraphElement(new NamedConcept(sd.getObject()), SummaryGraphElement.CONCEPT);
				resourceGraph.addVertex(conE);
				sdd.findSubConcepts(sd.getObject());
				while(sdd.hasNext())
				{
					sdd.next();
					SummaryGraphElement subConE = new SummaryGraphElement(new NamedConcept(sdd.getObject()), SummaryGraphElement.CONCEPT);
					resourceGraph.addVertex(subConE);
					addGraphElement(subConE, conE, new ObjectProperty(RDFS.SUBCLASSOF.toString()), resourceGraph);
				}
			}
		}


		if(withScores) resourceGraph = computeEFScores(repositoryPath, resourceGraph);
		return resourceGraph;	
	}

	/**
	 * Unlike the summary graph which is used for query interpretation, this schema graph is use for schema mathcing. It contains
	 * not only of R-edges and C-vertices but also A-edges and Datatypes.
	 * @param withScores
	 * @param foamReserveURIs
	 * @return
	 * @throws Exception 
	 */
	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> computeSchemaGraph(String repositoryPath, Pseudograph graph, List<String> foamReserveURIs) throws Exception {

		Pseudograph<SummaryGraphElement, SummaryGraphEdge> resourceGraph = graph==null?computeSummaryGraph(repositoryPath, false):graph;

		SesameDao sd = new SesameDao(repositoryPath);
		SesameDao sdd = new SesameDao(repositoryPath);
		sd.findAllTriples();
		int count = 0;
		while(sd.hasNext())
		{
			count++;
			if(count%10000 ==0)
				System.out.println(count);
			sd.next();
			String source = sd.getSubject();
			String property = sd.getPredicate();

			if (foamReserveURIs != null && foamReserveURIs.contains(property)) continue;

			if(sd.getPredicateType().equals(SesameDao.DATATYPEPROP)/* && !sd.isEdgeTypeOf(Property.IS_INSTANCE_OF)/*!propMem.equals(Property.IS_INSTANCE_OF) */) {
				if(sd.getSubjectType().equals(SesameDao.INDIVIDUAL)) {

					String datatype = sd.getObject().substring(sd.getObject().lastIndexOf('"')+1);

					if(!datatype.equals("") && datatype.charAt(0)=='^')
					{
						datatype = datatype.substring(datatype.lastIndexOf('^')+1);
					}
					else datatype = PREFIX+"label";
					
					sdd.findTypes(source);
					Set<SummaryGraphElement> sourceVertices = new HashSet<SummaryGraphElement>();

					if(sdd.hasNext()) {
						while(sdd.hasNext()) {
							sdd.next();
							//http://www.w3.org/2001/XMLSchema#
							sourceVertices.add(new SummaryGraphElement(new NamedConcept(sdd.getObject()), SummaryGraphElement.CONCEPT));
						}
					} else {
						sourceVertices.add(new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT));
					}

					if (sourceVertices != null && sourceVertices.size() != 0) {
						for (SummaryGraphElement sourceVertex : sourceVertices) {
							resourceGraph.addVertex(sourceVertex);
							SummaryGraphElement datatypeNode = new SummaryGraphElement(new Datatype(datatype), SummaryGraphElement.DATATYPE);
							resourceGraph.addVertex(datatypeNode);
							addGraphElement(sourceVertex, datatypeNode, new DataProperty(property), resourceGraph);
						}
					}					
				}
			}
		}

		return resourceGraph; 

	}


	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> computeEFScores(String repositoryPath, Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph)throws Exception{
		//Emergency.checkPrecondition((graph.vertexSet() != null || graph.vertexSet().size() > 0 || graph.edgeSet() != null || graph.edgeSet().size() > 0) , "Graph " + graph  + " is empty!");

		SesameDao sd = new SesameDao(repositoryPath);
		sd.findAllTriples();
		Set indivSet = new HashSet<String>();
		int propSize = 0;
		while(sd.hasNext())
		{
			sd.next();

			if(sd.getPredicate().equals(Property.IS_INSTANCE_OF.getUri()))
				indivSet.add(sd.getSubject());
			else if(sd.getPredicateType().equals(SesameDao.OBJPROP) || sd.getPredicateType().equals(SesameDao.DATATYPEPROP))
				propSize++;
		}
		int noIndividuals = indivSet.size();
		int noPropertyMembers = propSize;
		System.out.println("indiv:"+noIndividuals+" | prop:"+noPropertyMembers);

		for (SummaryGraphElement vertex : graph.vertexSet()){
			IResource res = vertex.getResource();
			if (vertex.equals(SummaryGraphElement.SUBCLASS))
			{
				vertex.setCost(SummaryGraphElement.SUBCLASS_ELEMENT_DEFAULT_SCORE);
			}
			else if(res instanceof NamedConcept && ((NamedConcept)res).getUri().equals(NamedConcept.TOP.getUri()))
			{
				vertex.setCost(DEFAULT_EF_TOP);			
			}
			else if (res instanceof NamedConcept){
				vertex.setCost(computeEF((NamedConcept)res, noIndividuals, sd));
			}
			else if (res instanceof Property){
				vertex.setCost(computeEF((Property)res, noPropertyMembers, sd));
			}
		}
		return graph;
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
//			System.out.println(filepath);
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


	private void addGraphElement(SummaryGraphElement vertex1, SummaryGraphElement vertex2, IProperty property, Pseudograph<SummaryGraphElement,SummaryGraphEdge> graph){
		//Emergency.checkPrecondition(vertex2.getType() == SummaryGraphElement.CONCEPT && vertex1.getType() == SummaryGraphElement.CONCEPT, "vertex2.getType() == KbElement.CVERTEX && vertex1.getType() == KbElement.CVERTEX");
		if(property.getUri().equals(Property.SUBCLASS_OF.getUri())) {
			if(!graph.containsVertex(SummaryGraphElement.SUBCLASS))
				graph.addVertex(SummaryGraphElement.SUBCLASS);
			SummaryGraphEdge edge1 = new SummaryGraphEdge(vertex1, SummaryGraphElement.SUBCLASS, SummaryGraphEdge.SUBCLASS_EDGE);
			if(!graph.containsEdge(edge1))
				graph.addEdge(vertex1, SummaryGraphElement.SUBCLASS, edge1);
			SummaryGraphEdge edge2 = new SummaryGraphEdge(SummaryGraphElement.SUBCLASS, vertex2, SummaryGraphEdge.SUPERCLASS_EDGE);
			if(!graph.containsEdge(edge2))
				graph.addEdge(SummaryGraphElement.SUBCLASS, vertex2,  edge2);

		} else if(property instanceof ObjectProperty){
			IObjectProperty objectProperty = new ObjectProperty(property.getUri());
			SummaryGraphElement prop = new SummaryGraphElement(objectProperty, SummaryGraphElement.RELATION);
			graph.addVertex(prop);
			SummaryGraphEdge edge1 = new SummaryGraphEdge(vertex1, prop, SummaryGraphEdge.DOMAIN_EDGE);
			if(!graph.containsEdge(edge1))
				graph.addEdge(vertex1, prop,edge1);
			SummaryGraphEdge edge2 = new SummaryGraphEdge(prop, vertex2, SummaryGraphEdge.RANGE_EDGE);
			if(!graph.containsEdge(edge2))
			{
				graph.addEdge(prop, vertex2, edge2);
			}

		} else if(property instanceof DataProperty)
		{
			IDataProperty dataProperty = new DataProperty(property.getUri());
			SummaryGraphElement prop = new SummaryGraphElement(dataProperty, SummaryGraphElement.ATTRIBUTE);
			graph.addVertex(prop);
			SummaryGraphEdge edge1 = new SummaryGraphEdge(vertex1, prop, SummaryGraphEdge.DOMAIN_EDGE);
			if(!graph.containsEdge(edge1))
				graph.addEdge(vertex1, prop, edge1);
			SummaryGraphEdge edge2 = new SummaryGraphEdge(prop, vertex2, SummaryGraphEdge.RANGE_EDGE);
			if(!graph.containsEdge(edge2))
				graph.addEdge(prop, vertex2,edge2);
		}
	}

	private double computeEF(INamedConcept concept, int noInds, SesameDao sd)throws Exception{
		sd.findMemberIndividuals(concept.getUri());
		Set indivSet = new HashSet<String>();
		while(sd.hasNext())
		{
			sd.next();
			indivSet.add(sd.getSubject());
		}
		int numIndividual = indivSet.size();
		if(numIndividual == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return (double)numIndividual/noInds;
	}

	private double computeEF(IProperty property, int noPropMembers, SesameDao sd) throws Exception{
		sd.findMemberProperties(property.getUri());
		int numProMem = 0;
		while(sd.hasNext())
		{
			numProMem++;
			sd.next();
		}
		if(numProMem == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return (double)numProMem/noPropMembers;
	}  

	public static void visit(Pseudograph graph)
	{
		Set<SummaryGraphEdge> edges = graph.edgeSet();
		for(SummaryGraphEdge edge: edges)
		{
			try
			{
				System.out.print(((NamedConcept)edge.getSource().getResource()).getUri()+"\t");
				System.out.print(((NamedConcept)edge.getTarget().getResource()).getUri());
				System.out.println();
			}
			catch(Exception e)
			{
				System.out.println("guala~");
			}
		}
	}

}
