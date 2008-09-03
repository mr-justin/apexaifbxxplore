package org.ateam.xxplore.core.service.search;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.mapping.ExtractInstancesService;
import org.ateam.xxplore.core.service.mapping.InstanceMapping;
import org.ateam.xxplore.core.service.mapping.MappingComputationService;
import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.mapping.SchemaMapping;
import org.jgrapht.graph.WeightedPseudograph;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
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
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

import edu.unika.aifb.foam.input.MyOntology;


public class IndexingDatawebService {

	private IKbConnection m_con; 
	private ISessionFactory m_sessionFactory;
	private IDaoManager m_dao;

	private static String REPOSITORY_DIR = "repodir";
	private static String SOURCE_ONTO_URI = "source_onto_uri";
	private static String SOURCE_ONTO_PATH = "source_onto_path";
	private static String SOURCE_ONTO_SCHEMA_PATH = "source_onto_schema_path";
	private static String TARGET_ONTO_URI = "target_onto_uri";
	private static String TARGET_ONTO_PATH = "target_onto_path";
	private static String TARGET_ONTO_SCHEMA_PATH = "target_onto_schema_path";
	
	private static String BASE_URI = "";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDF_NATIVE;

	private static String TEMP_ENTITIES_PATH = "res/BTC/mapping/entities.temp";
	
	private MappingComputationService m_mapper = new MappingComputationService();
	private SummaryGraphIndexService m_summarizer = new SummaryGraphIndexService();
	private MappingIndexService m_indexer = null; 
	private ExtractInstancesService m_extractor = new ExtractInstancesService();

	private static String propertyFile = "res/params.prop";
	public static void main(String[] args) {
		//Properties parameters = PropertyUtils.readFromPropertiesFile(args[0]);
		
		Properties parameters = PropertyUtils.readFromPropertiesFile(propertyFile);
		parameters.setProperty(ExploreEnvironment.BASE_ONTOLOGY_URI, BASE_URI);
		parameters.setProperty(ExploreEnvironment.SERIALIZATION_FORMAT, LANGUAGE);
		parameters.setProperty(KbEnvironment.ONTOLOGY_TYPE, ONTOLOGY_TYPE);
		IndexingDatawebService service = new IndexingDatawebService(REPOSITORY_DIR);
		
		service.m_indexer = new MappingIndexService(parameters.getProperty(REPOSITORY_DIR));
		
		IOntology omsOnto = null;

		//index DBLP 		
		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(SOURCE_ONTO_URI));
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, parameters.getProperty(SOURCE_ONTO_PATH));
		omsOnto = service.loadOntology(parameters, parameters.getProperty(SOURCE_ONTO_PATH));
		service.index(parameters, parameters.getProperty(SOURCE_ONTO_SCHEMA_PATH), omsOnto); 
		service.m_sessionFactory.getCurrentSession().close();
		service.m_con.closeOntology(omsOnto);

		//index swrc 
		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(TARGET_ONTO_URI));
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, parameters.getProperty(TARGET_ONTO_PATH));
		omsOnto = service.loadOntology(parameters, parameters.getProperty(TARGET_ONTO_PATH));
		service.index(parameters, parameters.getProperty(TARGET_ONTO_SCHEMA_PATH), omsOnto);
		service.m_sessionFactory.getCurrentSession().close();
		service.m_con.closeOntology(omsOnto);


		//compute schema mappings for SWRC
		Collection<SchemaMapping> sMappings = service.m_mapper.computeSchemaMappings(
				parameters.getProperty(SOURCE_ONTO_SCHEMA_PATH), 
				parameters.getProperty(TARGET_ONTO_SCHEMA_PATH));
		if (sMappings != null && sMappings.size() > 0){
			for (SchemaMapping m : sMappings){
				service.m_indexer.indexMappings(m);

				parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(SOURCE_ONTO_URI));
				omsOnto = service.loadOntology(parameters, parameters.getProperty(SOURCE_ONTO_PATH));
				Ontology sourceOnto= service.m_extractor.extractInstances(m.getSource(), service.m_dao);
				service.m_sessionFactory.getCurrentSession().close();
				service.m_con.closeOntology(omsOnto);
				
				//mapping is indeed a concept mapping
				if(sourceOnto != null){
					try {
						parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(TARGET_ONTO_URI));
						omsOnto = service.loadOntology(parameters, parameters.getProperty(TARGET_ONTO_PATH));
						Ontology targetOnto = service.m_extractor.extractInstances(m.getTarget(), service.m_dao);
						File temp = new File(TEMP_ENTITIES_PATH);
						targetOnto.saveOntology(OntologyFileFormat.OWL_XML,temp,"ISO-8859-1");
						sourceOnto.importContentsFrom(temp, null);
						service.m_sessionFactory.getCurrentSession().close();
						service.m_con.closeOntology(omsOnto);
					} catch (KAON2Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//compute instance mappings 
					Collection<InstanceMapping> iMappings = service.m_mapper.computeInstanceMappings(new MyOntology(sourceOnto), m.getSourceDsURI(), m.getTargetDsURI(), m);
					if (iMappings != null && iMappings.size() > 0){
						for (InstanceMapping mi : iMappings){
							service.m_indexer.indexMappings(mi);
						}
					}
				}

			}
		}
	}

	public IndexingDatawebService(String repodir){
		initConnection(repodir);
	}

	private void index(Properties parameters, String schemaPath, IOntology onto){
		WeightedPseudograph<KbVertex, KbEdge> sGraph = m_summarizer.computeSummaryGraph(false, null);
		m_summarizer.writeSummaryGraphAsRDF(sGraph, schemaPath);
		String dir = parameters.getProperty(REPOSITORY_DIR);
		String path = (dir.endsWith(File.separator) ? dir + parameters.getProperty(KbEnvironment.ONTOLOGY_URI) + ".graph" : 
			dir + File.separator + parameters.getProperty(KbEnvironment.ONTOLOGY_URI) + ".graph");
		m_summarizer.writeSummaryGraph(sGraph, path);

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
			m_dao = ExtendedSesameDaoManager.getInstance((SesameSession)session);
			PersistenceUtil.setDaoManager(m_dao);
		} catch (OpenSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return onto;
	}

}
