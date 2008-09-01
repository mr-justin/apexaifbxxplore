package org.ateam.xxplore.core.service.search;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NoInitialContextException;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.KbVertex;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.graph.WeightedPseudograph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.uris.impl.XMURIFactoryInsulated;


public class SummaryGraphIndexServiceWithSesame2 {

	private static Logger s_log = Logger.getLogger(SummaryGraphIndexServiceWithSesame2.class);

	private int DEFAULT_EF_TOP = 2; 
	private int DEFAULT_EF_SUBCLASS = 0; 

	public SummaryGraphIndexServiceWithSesame2() {}

	public WeightedPseudograph<KbVertex, KbEdge> computeSummaryGraph(boolean withScores, List<String> foamReserveURIs) {

		WeightedPseudograph<KbVertex, KbEdge>resourceGraph = new WeightedPseudograph<KbVertex,KbEdge>(KbEdge.class);

		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		List<IPropertyMember> propertyMembers = propertyMemberDao.findAll();	

		for(IPropertyMember propMem : propertyMembers) {
			IResource source = propMem.getSource();
			IResource target = propMem.getTarget();
			IProperty property = propMem.getProperty();
			
			if (foamReserveURIs != null && foamReserveURIs.contains(property.getUri())) continue;
				
			if(property instanceof IObjectProperty && !propMem.equals(Property.IS_INSTANCE_OF) ) {
				if(source instanceof IIndividual && target instanceof IIndividual) {
					Set<IConcept> sources = ((IIndividual)source).getTypes();
					Set<IConcept> targets = ((IIndividual)target).getTypes();
					Set<KbVertex> sourceVertices = new HashSet<KbVertex>();
					Set<KbVertex> targetVertices = new HashSet<KbVertex>();

					if(sources != null && sources.size() != 0 ) {
						for(IConcept scon : sources) {
							sourceVertices.add(new KbVertex(new NamedConcept(((INamedConcept)scon).getUri()), KbElement.CVERTEX));
						}
					} else {
						sourceVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, DEFAULT_EF_TOP));
					}

					if(targets != null && targets.size() != 0 ) {
						for(IConcept tcon : targets) {
							targetVertices.add(new KbVertex(new NamedConcept(((INamedConcept)tcon).getUri()), KbElement.CVERTEX));
						}
					} else {
						targetVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, DEFAULT_EF_TOP));
					}

					if (sourceVertices != null && sourceVertices.size() != 0 && targetVertices != null && targetVertices.size() != 0) {
						for (KbVertex sourceVertex : sourceVertices) {
							for (KbVertex targetVertex : targetVertices) {
								addGraphElement(sourceVertex, resourceGraph);
								addGraphElement(targetVertex, resourceGraph);
								addGraphElement(sourceVertex, targetVertex, property, resourceGraph, -1);
							}
						}
					}					
				}
			}
		}
		if(withScores) resourceGraph = computeEFScores(resourceGraph);
		return resourceGraph;	
	}
	
	
	private WeightedPseudograph<KbVertex, KbEdge> computeEFScores(WeightedPseudograph<KbVertex, KbEdge> graph){
		Emergency.checkPrecondition((graph.vertexSet() != null || graph.vertexSet().size() > 0 || graph.edgeSet() != null || graph.edgeSet().size() > 0) , "Graph " + graph  + " is empty!");
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();

		int numIndividual = onto.getNumberOfIndividual();
		s_log.debug("number of Individual: " + numIndividual);

		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
		s_log.debug("number of ObjectPropertyMember: " + numoPropertyMember);

		int noIndividuals = numIndividual;
		int noPropertyMembers = numoPropertyMember;
		
		
		for (KbVertex vertex : graph.vertexSet()){
			vertex.setCost(computeEF((INamedConcept)vertex.getResource(), noIndividuals));
		}

		for (KbEdge edge : graph.edgeSet()){
			edge.setCost(computeEF((IProperty)edge.getProperty(), noPropertyMembers));
		}

		
		return graph;
	}
	
	
	public void writeSummaryGraph(WeightedPseudograph<KbVertex, KbEdge> graph, String filepath){
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



	public void writeSummaryGraphAsRDF(WeightedPseudograph<KbVertex, KbEdge> graph, String rdffile){
		new File(rdffile).getParentFile().mkdirs();
		BufferedWriter bw = null;
		RDFXMLWriter writer = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rdffile),"UTF-8"));
			writer = new RDFXMLWriter(bw);
			writer.startRDF();

			Set<KbVertex> nodes = graph.vertexSet();
			if (nodes != null){
				for (KbVertex node : nodes){
					Emergency.checkPrecondition(node.getResource() instanceof NamedConcept, node.getResource().getLabel() + " not instance of NamedConcept");
					URI cvertex = new URIImpl(((NamedConcept)node.getResource()).getUri());
					writer.handleStatement(new StatementImpl(cvertex, RDF.TYPE, OWL.CLASS));
				}
			}

			Set<KbEdge> edges = graph.edgeSet();
			if (edges != null){
				for (KbEdge edge : edges){
					URI predicate = new URIImpl(edge.getProperty().getUri());					
					Emergency.checkPrecondition(edge.getVertex1().getResource() instanceof NamedConcept, edge.getVertex1().getResource().getLabel() + " not instance of NamedConcept");
					Emergency.checkPrecondition(edge.getVertex2().getResource() instanceof NamedConcept, edge.getVertex2().getResource().getLabel() + " not instance of NamedConcept");
					URI domain = new URIImpl(((INamedConcept)edge.getVertex1().getResource()).getUri());
					URI range = new URIImpl(((INamedConcept)edge.getVertex2().getResource()).getUri());
					

					writer.handleStatement(new StatementImpl(predicate,	RDF.TYPE, OWL.OBJECTPROPERTY));					
					writer.handleStatement(new StatementImpl(predicate, RDFS.DOMAIN, domain));
					writer.handleStatement(new StatementImpl(predicate, RDFS.RANGE, range));

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

	public WeightedPseudograph<KbVertex, KbEdge> readGraphIndexFromFile(String filepath){
		// retrieve graphIndex
		ObjectInputStream in;
		WeightedPseudograph<KbVertex,KbEdge> newResourceGraph = null;
		try {
			in = new ObjectInputStream(new FileInputStream(filepath));
			newResourceGraph = (WeightedPseudograph<KbVertex,KbEdge>)in.readObject(); 
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

		System.out.println("\n" + "new graph:");
		for(KbVertex vertex : newResourceGraph.vertexSet()){
			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
		}
		for(KbEdge edge : newResourceGraph.edgeSet()){
			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
		}

		return newResourceGraph;
	}


	private boolean addGraphElement(KbVertex vertex1, KbVertex vertex2, IProperty property, WeightedPseudograph<KbVertex,KbEdge> graph, double efProp){
		boolean addEdge = false; 
		KbEdge edge = null;
		IObjectProperty objectProperty = new ObjectProperty(property.getUri());
		Emergency.checkPrecondition(vertex2.getType() == KbElement.CVERTEX && vertex1.getType() == KbElement.CVERTEX, "vertex2.getType() == KbElement.CVERTEX && vertex1.getType() == KbElement.CVERTEX");
		if(objectProperty.equals(Property.SUBCLASS_OF)) {
			edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.REDGE, DEFAULT_EF_SUBCLASS);
		} else {
			edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.REDGE, efProp);
		}

		if(!(graph.containsEdge(edge))){
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}

		return addEdge;
	}

	private boolean addGraphElement(KbVertex vertex, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addVertex = false;
		addVertex = graph.addVertex(vertex);
		if(addVertex) {
			s_log.debug("Vertex " + vertex + " is added to the graph!");
		} else {
			s_log.debug("Vertex " + vertex + " is already in the graph!");
		}

		return addVertex;
	}

	private boolean addGraphElement(KbEdge edge, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		if(!(graph.containsEdge(edge))){
			KbVertex vertex1 = edge.getVertex1();
			if(!(graph.containsVertex(vertex1))) {
				graph.addVertex(vertex1);
			}
			KbVertex vertex2 = edge.getVertex2();
			if(!(graph.containsVertex(vertex2))) {
				graph.addVertex(vertex2);
			}
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}

		return addEdge;
	}

	private double computeEF(INamedConcept concept, int noInds){
		int numIndividual = concept.getNumberOfIndividuals();
		if(numIndividual == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return numIndividual/noInds;
	}

	private double computeEF(IProperty property, int noPropMembers){
		int numProMem = property.getNumberOfPropertyMember();
		if(numProMem == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return numProMem/noPropMembers;
	}  


}
