package org.ateam.xxplore.core.service.search;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.jgrapht.graph.Pseudograph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;


public class SummaryGraphIndexService {

	private static Logger s_log = Logger.getLogger(SummaryGraphIndexService.class);

	private int DEFAULT_EF_TOP = 2; 

	public SummaryGraphIndexService() {}

	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> computeSummaryGraph(boolean withScores) {

		Pseudograph<SummaryGraphElement, SummaryGraphEdge>resourceGraph = new Pseudograph<SummaryGraphElement,SummaryGraphEdge>(SummaryGraphEdge.class);

		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		List<IPropertyMember> propertyMembers = propertyMemberDao.findAll();	

		for(IPropertyMember propMem : propertyMembers) {
			IResource source = propMem.getSource();
			IResource target = propMem.getTarget();
			IProperty property = propMem.getProperty();

			if(property instanceof IObjectProperty && !propMem.equals(Property.IS_INSTANCE_OF) ) {
				if(source instanceof IIndividual && target instanceof IIndividual) {
					Set<IConcept> sources = ((IIndividual)source).getTypes();
					Set<IConcept> targets = ((IIndividual)target).getTypes();
					Set<SummaryGraphElement> sourceVertices = new HashSet<SummaryGraphElement>();
					Set<SummaryGraphElement> targetVertices = new HashSet<SummaryGraphElement>();

					if(sources != null && sources.size() != 0 ) {
						for(IConcept scon : sources) {
							sourceVertices.add(new SummaryGraphElement(new NamedConcept(((INamedConcept)scon).getUri()), SummaryGraphElement.CONCEPT));
						}
					} else {
						sourceVertices.add(new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT));
					}

					if(targets != null && targets.size() != 0 ) {
						for(IConcept tcon : targets) {
							targetVertices.add(new SummaryGraphElement(new NamedConcept(((INamedConcept)tcon).getUri()), SummaryGraphElement.CONCEPT));
						}
					} else {
						targetVertices.add(new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT));
					}

					if (sourceVertices != null && sourceVertices.size() != 0 && targetVertices != null && targetVertices.size() != 0) {
						for (SummaryGraphElement sourceVertex : sourceVertices) {
							for (SummaryGraphElement targetVertex : targetVertices) {
								resourceGraph.addVertex(sourceVertex);
								resourceGraph.addVertex(targetVertex);
								addGraphElement(sourceVertex, targetVertex, property, resourceGraph);
							}
						}
					}					
				}
			}
		}

		IConceptDao conDao = (IConceptDao)PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		List<IConcept> cons = (List<IConcept>)conDao.findAll();	
		for (IConcept con : cons){
			if(con instanceof INamedConcept){
				SummaryGraphElement conE = new SummaryGraphElement(con, SummaryGraphElement.CONCEPT);
				resourceGraph.addVertex(conE);
				Collection<IConcept> subCons = conDao.findSubconcepts((INamedConcept)con);
				for(IConcept subcon : subCons) {
					SummaryGraphElement subConE = new SummaryGraphElement(subcon, SummaryGraphElement.CONCEPT);
					resourceGraph.addVertex(subConE);
					addGraphElement(subConE, conE, new ObjectProperty(RDFS.SUBCLASSOF.toString()), resourceGraph);
				}
			}
		}


		if(withScores) resourceGraph = computeEFScores(resourceGraph);
		return resourceGraph;	
	}

	/**
	 * Unlike the summary graph which is used for query interpretation, this schema graph is use for schema mathcing. It contains
	 * not only of R-edges and C-vertices but also A-edges and Datatypes.
	 * @param withScores
	 * @param foamReserveURIs
	 * @return
	 */
	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> computeSchemaGraph(List<String> foamReserveURIs) {

		Pseudograph<SummaryGraphElement, SummaryGraphEdge> resourceGraph = computeSummaryGraph(false);

		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		List<IPropertyMember> propertyMembers = propertyMemberDao.findAll();	
		for(IPropertyMember propMem : propertyMembers) {
			IResource source = propMem.getSource();
			IProperty property = propMem.getProperty();

			if (foamReserveURIs != null && foamReserveURIs.contains(property.getUri())) continue;

			if(property instanceof IDataProperty && !propMem.equals(Property.IS_INSTANCE_OF) ) {
				if(source instanceof IIndividual) {
					Set<IConcept> sources = ((IIndividual)source).getTypes();
					Set<SummaryGraphElement> sourceVertices = new HashSet<SummaryGraphElement>();

					if(sources != null && sources.size() != 0 ) {
						for(IConcept scon : sources) {
							sourceVertices.add(new SummaryGraphElement(new NamedConcept(((INamedConcept)scon).getUri()), SummaryGraphElement.CONCEPT));
						}
					} else {
						sourceVertices.add(new SummaryGraphElement(NamedConcept.TOP, SummaryGraphElement.CONCEPT));
					}

					if (sourceVertices != null && sourceVertices.size() != 0) {
						for (SummaryGraphElement sourceVertex : sourceVertices) {
							resourceGraph.addVertex(sourceVertex);
							addGraphElement(sourceVertex, new SummaryGraphElement(new org.xmedia.oms.model.impl.Resource(
									SummaryGraphElement.DUMMY_DATATYPE_LABEL), SummaryGraphElement.DATATYPE), 
									property, resourceGraph);
						}
					}					
				}
			}
		}

		return resourceGraph; 

	}


	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> computeEFScores(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph){
		Emergency.checkPrecondition((graph.vertexSet() != null || graph.vertexSet().size() > 0 || graph.edgeSet() != null || graph.edgeSet().size() > 0) , "Graph " + graph  + " is empty!");
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();
		
		//it is not efficient
		int numIndividual = onto.getNumberOfIndividual();
		s_log.debug("number of Individual: " + numIndividual);

		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
		s_log.debug("number of ObjectPropertyMember: " + numoPropertyMember);

		int noIndividuals = numIndividual;
		int noPropertyMembers = numoPropertyMember;


		for (SummaryGraphElement vertex : graph.vertexSet()){
			IResource res = vertex.getResource();
			if (vertex.equals(SummaryGraphElement.SUBCLASS)) vertex.setCost(SummaryGraphElement.SUBCLASS_ELEMENT_DEFAULT_SCORE);
			else if(res.equals(NamedConcept.TOP)) vertex.setCost(DEFAULT_EF_TOP);			
			else if (res instanceof INamedConcept){
				vertex.setCost(computeEF((INamedConcept)res, noIndividuals));
			}
			else if (res instanceof IProperty){
				vertex.setCost(computeEF((IProperty)res, noPropertyMembers));
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
						Emergency.checkPrecondition(edges.size() == 2, "A subclass vertex should have only one outhoing and one ingoing edge");
						URI sourceURI = null; 
						URI targetURI = null;
						for (SummaryGraphEdge edge : edges){
							if (edge.getEdgeLabel().equals(SummaryGraphEdge.SUBCLASS_EDGE)){
								sourceURI = new URIImpl(((NamedConcept)edge.getSource().getResource()).getUri());
							}
							//must be super class edge
							else {
								targetURI = new URIImpl(((NamedConcept)edge.getTarget().getResource()).getUri());
							}
						}
						writer.handleStatement(new StatementImpl(sourceURI, RDFS.SUBCLASSOF, targetURI));
					}
				}
			}

			Set<SummaryGraphEdge> edges = graph.edgeSet();
			if (edges != null){
				for (SummaryGraphEdge edge : edges){
					String type = edge.getEdgeLabel();
					URI predicate = null;
					URI domain = null;
					URI range = null;
					if(type == SummaryGraphEdge.DOMAIN_EDGE){
						predicate = new URIImpl(((IProperty)edge.getTarget().getResource()).getUri());
						domain = new URIImpl(((INamedConcept)edge.getSource().getResource()).getUri());
					}
					else if(type == SummaryGraphEdge.RANGE_EDGE){
						predicate = new URIImpl(((IProperty)edge.getSource().getResource()).getUri());
						range = new URIImpl(((INamedConcept)edge.getTarget().getResource()).getUri());
					}

					Emergency.checkPostcondition(domain != null && range != null && predicate != null, "domain != null && range != null && predicate != null"); 
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

	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> readGraphIndexFromFile(String filepath){
		// retrieve graphIndex
		ObjectInputStream in;
		Pseudograph<SummaryGraphElement,SummaryGraphEdge> newResourceGraph = null;
		try {
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
		IObjectProperty objectProperty = new ObjectProperty(property.getUri());
		Emergency.checkPrecondition((vertex2.getType() == SummaryGraphElement.CONCEPT || vertex2.getType() == SummaryGraphElement.DATATYPE) && vertex1.getType() == SummaryGraphElement.CONCEPT, "vertex2.getType() == KbElement.CVERTEX && vertex1.getType() == KbElement.CVERTEX");
		if(objectProperty.equals(Property.SUBCLASS_OF)) {
			graph.addEdge(vertex1, SummaryGraphElement.SUBCLASS, 
					new SummaryGraphEdge(vertex1, SummaryGraphElement.SUBCLASS, SummaryGraphEdge.SUBCLASS_EDGE));
			graph.addEdge(SummaryGraphElement.SUBCLASS, vertex2,  
					new SummaryGraphEdge(SummaryGraphElement.SUBCLASS, vertex2, SummaryGraphEdge.SUPERCLASS_EDGE));

		} else {
			SummaryGraphElement prop = new SummaryGraphElement(objectProperty, SummaryGraphElement.RELATION);
			graph.addEdge(vertex1, prop, 
					new SummaryGraphEdge(vertex1, prop, SummaryGraphEdge.DOMAIN_EDGE));
			graph.addEdge(prop, vertex2,  
					new SummaryGraphEdge(prop, vertex2, SummaryGraphEdge.RANGE_EDGE));

		}
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
