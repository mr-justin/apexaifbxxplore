package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.KbVertex;
import org.ateam.xxplore.core.service.search.SummaryGraphIndexServiceWithSesame2;
import org.jgrapht.graph.WeightedPseudograph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.xmedia.accessknow.sesame.model.PropertyMember;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.accessknow.sesame.persistence.converter.DelegatesManager;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
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
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

public class ExtractInstancesServiceWithSesame2 {

	private static Logger s_log = Logger.getLogger(ExtractInstancesServiceWithSesame2.class);
	
	private IOntology m_onto;
	private ISession m_session;
	
	private String repositoryDir;
	private Properties parameters;
	
	private BufferedReader br;
	
	private BufferedWriter bw;
	private RDFXMLWriter writer;
	
	private static String ONTOLOGY_URI = "target"; // repository directory name
	private static String ONTOLOGY_FILE_PATH = "res/target.rdf";
	private static String ONTOLOGY_FILE_NAME = "target.rdf";
	private static String BASE_ONTOLOGY_URI = "http://www.example.org/example";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT;
	
	private static String REPOSITORY_DIR = "res/BTC/sampling/repository";
	private static String SCHEMA_MAPPING_FILE = "res/BTC/sampling/mapping/mappingResult/schema.rdf+swrc.owl.mapping"; 
	private static String EXTRACTED_ENTITIES = "res/BTC/target_entities.rdf"; 
	
	public static void main(String[] args) {
		Properties parameters = new Properties();
		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, ONTOLOGY_URI);
		parameters.setProperty(KbEnvironment.ONTOLOGY_TYPE, ONTOLOGY_TYPE);
		
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, ONTOLOGY_FILE_PATH);
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_NAME, ONTOLOGY_FILE_NAME);
		parameters.setProperty(ExploreEnvironment.BASE_ONTOLOGY_URI, BASE_ONTOLOGY_URI);
		parameters.setProperty(ExploreEnvironment.LANGUAGE, LANGUAGE);
		
		ExtractInstancesServiceWithSesame2 service = new ExtractInstancesServiceWithSesame2(parameters, REPOSITORY_DIR, SCHEMA_MAPPING_FILE, EXTRACTED_ENTITIES);
		service.extractInstances();
	} 
	
	public ExtractInstancesServiceWithSesame2(Properties parameters, String repositoryDir, String schemaMappingFile, String extractedEntities) {
		this.parameters = parameters;
		this.repositoryDir = repositoryDir;
		new File(extractedEntities).getParentFile().mkdirs();
		try {
			br = new BufferedReader(new FileReader(schemaMappingFile));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(extractedEntities),"UTF-8"));
			writer = new RDFXMLWriter(bw);
			writer.startRDF();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		init();	
	}
	
	public void extractInstances() {
		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		
		String line;
			try {
				while ((line = br.readLine()) != null) {
					String[] mapping = line.split(";");
					if (mapping.length == 3) {
						INamedConcept concept = conceptDao.findByUri(mapping[0]);
						if(concept != null){
							URI conceptUri = new URIImpl(concept.getUri());
							Set<IIndividual> individuals = individualDao.findMemberIndividuals(concept); 
							for(IIndividual individual : individuals){
								if(individual instanceof INamedIndividual) { 
									URI subjectUri = new URIImpl(((INamedIndividual)individual).getUri());
									writer.handleStatement(new StatementImpl(subjectUri, RDF.TYPE, conceptUri));
									
									Set<IPropertyMember> propMems = propertyMemberDao.findBySourceIndividual(individual);
									for(IPropertyMember propMem : propMems) {
										if(propMem.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
											IProperty dataProperty = propMem.getProperty();
											URI propertyUri = new URIImpl(dataProperty.getUri());
											IResource object = propMem.getTarget();
											if(object instanceof ILiteral) {
												LiteralImpl literal = new LiteralImpl(((ILiteral)object).getLiteral());
												writer.handleStatement(new StatementImpl(subjectUri, propertyUri, literal));
											}
										} else {
											IProperty objectProperty = propMem.getProperty();
											URI propertyUri = new URIImpl(objectProperty.getUri());
											IResource object = propMem.getTarget();
											if(object instanceof IIndividual) {
												if(object instanceof INamedIndividual) { 
													URI objectUri = new URIImpl(((INamedIndividual)object).getUri());
													writer.handleStatement(new StatementImpl(subjectUri, propertyUri, objectUri));
												} else {
													String objectNodeId = object.getLabel();
													if(objectNodeId.startsWith("_:")) {
														objectNodeId = objectNodeId.substring(2);
													}
													BNodeImpl objectBNode = new BNodeImpl(objectNodeId); 
													writer.handleStatement(new StatementImpl(subjectUri, propertyUri, objectBNode));
												}		
											}
										}
									}
								} else {
									String subjectNodeId = individual.getLabel();
									if(subjectNodeId.startsWith("_:")) {
										subjectNodeId = subjectNodeId.substring(2);
									}
									BNodeImpl subjectBNode = new BNodeImpl(subjectNodeId); 
									writer.handleStatement(new StatementImpl(subjectBNode, RDF.TYPE, conceptUri));
									
									Set<IPropertyMember> propMems = propertyMemberDao.findBySourceIndividual(individual);
									for(IPropertyMember propMem : propMems) {
										if(propMem.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
											IProperty dataProperty = propMem.getProperty();
											URI propertyUri = new URIImpl(dataProperty.getUri());
											IResource object = propMem.getTarget();
											if(object instanceof ILiteral) {
												LiteralImpl literal = new LiteralImpl(((ILiteral)object).getLiteral());
												writer.handleStatement(new StatementImpl(subjectBNode, propertyUri, literal));
											}
										} else {
											IProperty objectProperty = propMem.getProperty();
											URI propertyUri = new URIImpl(objectProperty.getUri());
											IResource object = propMem.getTarget();
											if(object instanceof IIndividual) {
												if(object instanceof INamedIndividual) { 
													URI objectUri = new URIImpl(((INamedIndividual)object).getUri());
													writer.handleStatement(new StatementImpl(subjectBNode, propertyUri, objectUri));
												} else {
													String objectNodeId = object.getLabel();
													if(objectNodeId.startsWith("_:")) {
														objectNodeId = objectNodeId.substring(2);
													}
													BNodeImpl objectBNode = new BNodeImpl(objectNodeId); 
													writer.handleStatement(new StatementImpl(subjectBNode, propertyUri, objectBNode));
												}		
											}
										}
									}	
								}	
							}
						}
					}
				}
				writer.endRDF();
				if(bw != null)
					bw.close();
				if(br != null)
					br.close();
			} catch (DatasourceException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (RDFHandlerException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	}
	
	private void init() {
		
//		load ontology	
		m_onto = null;
		SesameConnection ses_con = null;
		try {
			try {
				ses_con = new SesameConnection(repositoryDir);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			try {
				m_onto = ses_con.loadOntology(PropertyUtils.convertToMap(parameters));
			} catch (OntologyLoadException e) {

				m_onto = ses_con.createOntology(PropertyUtils.convertToMap(parameters));

				try {
					addFileToRepository(m_onto, PropertyUtils.convertToMap(parameters));
				} catch (MissingParameterException e1) {
					e1.printStackTrace();
				}
			}
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (MissingParameterException e) {
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (OntologyCreationException e) {
			e.printStackTrace();
		} 
		
		SesameSessionFactory sesame_factory = new SesameSessionFactory(new XMURIFactoryInsulated());
		ISession session = null;
		
		try {
			session = sesame_factory.openSession(ses_con, m_onto);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (OpenSessionException e) {
			e.printStackTrace();
		}
		//set dao manager
		PersistenceUtil.setDaoManager(ExtendedSesameDaoManager.getInstance((SesameSession)session));
		
		session.close();	
		
		ISessionFactory factory = SessionFactory.getInstance();
		PersistenceUtil.setSessionFactory(factory); 
		//open a new session with the ontology
		try {
			m_session = factory.openSession(ses_con,m_onto);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (OpenSessionException e) {
			e.printStackTrace();
		}
								
	}

	private static void addFileToRepository(IOntology onto, Map<String, Object> parameters)throws MissingParameterException{
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_PATH)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_PATH+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_NAME)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_NAME+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.BASE_ONTOLOGY_URI)) {
			throw new MissingParameterException(ExploreEnvironment.BASE_ONTOLOGY_URI+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.LANGUAGE)) {
			throw new MissingParameterException(ExploreEnvironment.LANGUAGE+" missing!");
		}
		
		String filePath = (String)parameters.get(ExploreEnvironment.ONTOLOGY_FILE_PATH);
		String baseUri = (String)parameters.get(ExploreEnvironment.BASE_ONTOLOGY_URI);
		String language = (String)parameters.get(ExploreEnvironment.LANGUAGE);
				
		try {
			onto.importOntology(language, baseUri, new FileReader(filePath));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (OntologyImportException e) {
			e.printStackTrace();
		}
	}
	
	
}
