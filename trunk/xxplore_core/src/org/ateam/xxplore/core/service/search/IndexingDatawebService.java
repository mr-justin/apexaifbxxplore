package org.ateam.xxplore.core.service.search;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.mapping.ExtractInstancesService;
import org.ateam.xxplore.core.service.mapping.MappingComputationService;
import org.ateam.xxplore.core.service.mapping.SchemaMapping;
import org.jgrapht.graph.WeightedPseudograph;
import org.openrdf.repository.RepositoryException;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyDeletionException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

import sun.print.resources.serviceui;


public class IndexingDatawebService {

	private IKbConnection m_con; 
	private ISessionFactory m_sessionFactory;

	private static String BASE_URI = "";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDF_NATIVE;

	// ontologies //
	private static String DBLP_URI = "dblp"; // repository name
	private static String DBLP_PATH = "res/BTC/dblp.rdf";
	private static String DBLP_SCHEMA_PATH = "res/BTC/dblp_schema.rdf";

	private static String SWRC_URI = "swrc"; // repository name
	private static String SWRC_PATH = "res/BTC/swrc.owl";
	private static String SWRC_SCHEMA_PATH = "res/BTC/swrc_schema.owl";


	private static String REPOSITORY_DIR = "res/BTC/repository";
	private static String STRUCTURE_INDEX_DIR = "res/BTC/structureIndex";
	// uris already reserved in OWL that can cause parsing problems with KAON2 while loading schema into FOAM
	//private static String[] NON_SUPPORTED_URIS = {};

	//for mapping //
	private static String OUTPUTDIR  = "res/BTC/mapping/mappingResult";
	private static String SCHEMA_MAPPING = "schema";
	private static String INSTANCE_MAPPING = "instance";

	//for extracting instances //
	private static String SCHEMA_MAPPING_FILE = "res/BTC/mapping/mappingResult/dblp_schema.rdf+swrc_schema.owl.mapping"; 	
	private static String DBLP_ENTITIES_PATH = "res/BTC/mapping/dblp_entities.rdf"; 
	private static String SWRC_ENTITIES_PATH = "res/BTC/mapping/swrc_entities.rdf"; 
	private static String DBLP_ENTITIES = "dblp_entities"; 
	private static String SWRC_ENTITIES = "swrc_entities"; 
	
	
	public static void main(String[] args) {
		Properties parameters = new Properties();
		parameters.setProperty(ExploreEnvironment.BASE_ONTOLOGY_URI, BASE_URI);
		parameters.setProperty(ExploreEnvironment.SERIALIZATION_FORMAT, LANGUAGE);
		parameters.setProperty(KbEnvironment.ONTOLOGY_TYPE, ONTOLOGY_TYPE);
		IndexingDatawebService service = new IndexingDatawebService(REPOSITORY_DIR);

		//index DBLP 		
//		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, DBLP_URI);
//		IOntology onto = service.loadOntology(parameters, DBLP_PATH);
//		service.index(parameters, DBLP_SCHEMA_PATH, onto); 
//		service.m_sessionFactory.getCurrentSession().close();
//		service.m_con.closeOntology(onto);

		//index swrc 
//		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, SWRC_URI);
//		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, SWRC_PATH);
//		onto = service.loadOntology(parameters, SWRC_PATH);
//		service.index(parameters, SWRC_SCHEMA_PATH, onto);
//		service.m_sessionFactory.getCurrentSession().close();
//		service.m_con.closeOntology(onto);

		//compute schema mappings for SWRC
		service.computeMappings(SWRC_SCHEMA_PATH, DBLP_SCHEMA_PATH, null);
		
		//extract DBLP instances 
//		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, DBLP_URI);
//		onto = service.loadOntology(parameters, DBLP_PATH);
//		service.computeInstanceForMappings(SCHEMA_MAPPING_FILE, DBLP_ENTITIES_PATH, onto);
//		service.m_sessionFactory.getCurrentSession().close();
//		service.m_con.closeOntology(onto);

		//extract SWRC instances 
//		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, SWRC_URI);
//		onto = service.loadOntology(parameters, SWRC_PATH);
//		service.computeInstanceForMappings(SCHEMA_MAPPING_FILE, SWRC_ENTITIES_PATH, onto);
//		service.m_sessionFactory.getCurrentSession().close();
//		service.m_con.closeOntology(onto);
		
		
		//compute instance mappings 
		service.computeMappings(SWRC_ENTITIES_PATH, DBLP_ENTITIES_PATH, null);
		
//		try {
//			((SesameConnection)service.m_con).deleteAllOntologies();
//			service.m_con.close();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	}

	public IndexingDatawebService(String repodir){
		initConnection(repodir);
	}

	private void index(Properties parameters, String schemaPath, IOntology onto){
		SummaryGraphIndexServiceWithSesame2 service = new SummaryGraphIndexServiceWithSesame2();
		WeightedPseudograph<KbVertex, KbEdge> sGraph = service.computeSummaryGraph(false, null);
		service.writeSummaryGraphAsRDF(sGraph, schemaPath);

		String path = (STRUCTURE_INDEX_DIR.endsWith(File.separator) ? STRUCTURE_INDEX_DIR + parameters.getProperty(KbEnvironment.ONTOLOGY_URI) + ".graph" : 
			STRUCTURE_INDEX_DIR + File.separator + parameters.getProperty(KbEnvironment.ONTOLOGY_URI) + ".graph");
		service.writeSummaryGraph(sGraph, path);

	}

	private void computeMappings(String ontology1, String ontology2, SchemaMapping mapping){
		MappingComputationService service = new MappingComputationService();
		if (mapping == null) service.computeSchemaMappings(ontology1, ontology2);
		else service.computeInstanceMappings(ontology1, ontology2, mapping);
	}

	private void computeInstanceForMappings(String schemaMappingFilePath, String entityFilePath, IOntology onto){
		ExtractInstancesService service = new ExtractInstancesService(schemaMappingFilePath, entityFilePath);
		service.extractInstances();
	}

	private void initConnection(String repodir){
		try {
			m_con = new SesameConnection(repodir);
			m_sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());

		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	private IOntology loadOntology(Properties parameters, String filepath){
		//load ontology
		IOntology onto = null;
		try {
			onto = m_con.loadOntology(PropertyUtils.convertToMap(parameters));
			
		} catch (OntologyLoadException e) {
			try {
				onto = m_con.createOntology(PropertyUtils.convertToMap(parameters));
				onto.importOntology(LANGUAGE, BASE_URI, new FileReader(filepath));
			} catch (MissingParameterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidParameterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OntologyCreationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyImportException e2) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		try {
			ISession session = m_sessionFactory.openSession(m_con, onto);
			PersistenceUtil.setSession(session);
			PersistenceUtil.setDaoManager(ExtendedSesameDaoManager.getInstance((SesameSession)session));
		} catch (OpenSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return onto;
	}

}
