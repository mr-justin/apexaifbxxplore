package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.KbVertex;
import org.jgrapht.graph.WeightedPseudograph;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;


public class SummaryGraphIndexService {
	
	private static Logger s_log = Logger.getLogger(SummaryGraphIndexServiceWithSesame2.class);
	
	private int TOTAL_NUMBER_OF_INDIVIDUAL = 1; 
	private int TOTAL_NUMBER_OF_PROPERTYMEMBER = 1;
	
	public SummaryGraphIndexService() {}
	
	public void indexSummaryGraph(String datasourceUri) {
		
		if(!isIndexingRequired(datasourceUri)) return;
		
		computeTotalNumber();
		
		WeightedPseudograph<KbVertex,KbEdge> resourceGraph = new WeightedPseudograph<KbVertex,KbEdge>(KbEdge.class);
		
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		List<IPropertyMember> propertyMembers = propertyMemberDao.findAll();
		int numPropertyMember = propertyMembers.size();
		
		for(IPropertyMember propMem : propertyMembers) {
			IResource source = propMem.getSource();
			IResource target = propMem.getTarget();
			IProperty property = propMem.getProperty();
			
			if(property instanceof IObjectProperty && !propMem.equals(Property.IS_INSTANCE_OF) ) {
				if(source instanceof IIndividual && target instanceof IIndividual) {
					Set<IConcept> sources = ((IIndividual)source).getTypes();
					Set<IConcept> targets = ((IIndividual)target).getTypes();
					Set<KbVertex> sourceVertices = new HashSet<KbVertex>();
					Set<KbVertex> targetVertices = new HashSet<KbVertex>();
					
					if(sources != null && sources.size() != 0 ) {
						for(IConcept scon : sources) {
								sourceVertices.add(new KbVertex(new NamedConcept(((INamedConcept)scon).getUri()), KbElement.CVERTEX, computeWeight((INamedConcept)scon)));
						}
					} else {
						sourceVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, 2));
					}
					
					if(targets != null && targets.size() != 0 ) {
						for(IConcept tcon : targets) {
							targetVertices.add(new KbVertex(new NamedConcept(((INamedConcept)tcon).getUri()), KbElement.CVERTEX, computeWeight((INamedConcept)tcon)));
						}
					} else {
						targetVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, 2));
					}
					
					if (sourceVertices != null && sourceVertices.size() != 0 && targetVertices != null && targetVertices.size() != 0) {
						for (KbVertex sourceVertex : sourceVertices) {
							for (KbVertex targetVertex : targetVertices) {
								addGraphElement(sourceVertex, resourceGraph);
								addGraphElement(targetVertex, resourceGraph);
								addGraphElement(sourceVertex, targetVertex, property, resourceGraph);
							}
						}
					}
				}
			}
		}
		
		// save graphIndex into the file *.graph
		File graphIndex = new File(ExploreEnvironment.GRAPH_INDEX_DIR, datasourceUri + ".graph");
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
			out.writeObject(resourceGraph);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(KbVertex vertex : resourceGraph.vertexSet()){
			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
		}
		for(KbEdge edge : resourceGraph.edgeSet()){
			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
		}
		
	}
	
	private boolean isIndexingRequired(String datasourceUri){
		File file = new File(ExploreEnvironment.GRAPH_INDEX_DIR, datasourceUri + ".graph");
		
		if (!file.exists()) {
			return true;
		}
		
		return false;
	}
	
	public boolean addGraphElement(KbVertex vertex1, KbVertex vertex2, IProperty property, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		KbEdge edge = null;
		IObjectProperty objectProperty = new ObjectProperty(property.getUri());
		if(vertex2.getType() == KbElement.CVERTEX){
			if(objectProperty.equals(Property.SUBCLASS_OF)) {
				edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.REDGE, 0);
			} else {
				edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.REDGE, computeWeight(property));
			}
		}
		else {
			edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.AEDGE, 1);
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
	
	public boolean addGraphElement(KbEdge edge, WeightedPseudograph<KbVertex,KbEdge> graph){
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
	
	private void computeTotalNumber(){
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();
		
		int numIndividual = onto.getNumberOfIndividual();
		System.out.println("number of Individual: " + numIndividual);
		
		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
		System.out.println("number of ObjectPropertyMember: " + numoPropertyMember);
		
		TOTAL_NUMBER_OF_INDIVIDUAL = numIndividual;
		
		TOTAL_NUMBER_OF_PROPERTYMEMBER = numoPropertyMember;
	}
	
	private double computeEdgeWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2);
	}
	
	private double computeVertexWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2); 
	}
	
	private double computeWeight(INamedConcept concept){
		int numIndividual = concept.getNumberOfIndividuals();
		return computeVertexWeight(numIndividual,TOTAL_NUMBER_OF_INDIVIDUAL);
	}
	
	private double computeWeight(IProperty property){
		int numProMem = property.getNumberOfPropertyMember();
		return computeEdgeWeight(numProMem,TOTAL_NUMBER_OF_PROPERTYMEMBER);
	}  

}
