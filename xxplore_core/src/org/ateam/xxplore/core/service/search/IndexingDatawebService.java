package org.ateam.xxplore.core.service.search;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.mapping.ExtractInstancesService;
import org.ateam.xxplore.core.service.mapping.InstanceMapping;
import org.ateam.xxplore.core.service.mapping.MappingComputationService;
import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.mapping.SchemaMapping;
import org.jgrapht.graph.Pseudograph;
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
	private static String KEYWORDINDEX_DIR = "keyword_index_dir";
	private static String MAPINGINDEX_DIR = "mapping_index_dir";

	private static String WRITE_SOURCE_ONTO_SCHEMA = "write_source_onto_schema";
	private static String WRITE_TARGET_ONTO_SCHEMA = "write_target_onto_schema";
	private static String WRITE_SOURCE_ONTO_SUMMARY = "write_source_onto_summary";
	private static String WRITE_TARGET_ONTO_SUMMARY = "write_target_onto_summary";
	private static String SOURCE_ONTO_URI = "source_onto_uri";
	private static String SOURCE_ONTO_PATH = "source_onto_path";
	private static String SOURCE_ONTO_SCHEMA_PATH = "source_onto_schema_path";
	private static String SOURCE_ONTO_SUMMARY_PATH = "source_onto_summary_path";
	private static String TARGET_ONTO_URI = "target_onto_uri";
	private static String TARGET_ONTO_PATH = "target_onto_path";
	private static String TARGET_ONTO_SCHEMA_PATH = "target_onto_schema_path";
	private static String TARGET_ONTO_SUMMARY_PATH = "target_onto_summary_path";

	private static String BASE_URI = "";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDF_NATIVE;

	private static String COMPUTE_MAPPING = "compute_mapping";
	private static String TEMP_ENTITIES_PATH = "res/BTC/mapping/entities.temp";

	private static Map<String,String> physicalURIsOfSummaryGraphs = new HashMap<String, String>();
	private KeywordIndexServiceForBT m_kIndexer = null;
	private MappingIndexService m_mIndexer = null;
	
	private static String propertyFile = "res/params.prop";
	public static void main(String[] args) {

		//Properties parameters = PropertyUtils.readFromPropertiesFile(args[0]);

		Properties parameters = PropertyUtils.readFromPropertiesFile(propertyFile);
		IndexingDatawebService service = new IndexingDatawebService(parameters.getProperty(REPOSITORY_DIR));
		service.process(parameters);


	}

	public IndexingDatawebService(String repodir){
		initConnection(repodir);
	}

	public void process(Properties parameters){
		parameters.setProperty(ExploreEnvironment.BASE_ONTOLOGY_URI, BASE_URI);
		parameters.setProperty(ExploreEnvironment.SERIALIZATION_FORMAT, LANGUAGE);
		parameters.setProperty(KbEnvironment.ONTOLOGY_TYPE, ONTOLOGY_TYPE);

		indexSummaries(parameters);
		indexElements(parameters);
		if(parameters.get(COMPUTE_MAPPING) == "true")
			computeMappings(parameters);
		
		QueryInterpretationService inter = new QueryInterpretationService();	
		inter.computeQueries(m_kIndexer.searchKb("thanh 2007", 0.9), m_mIndexer, 6, 10);
	}

	public static String getSummaryGraphFilePath(String datasource){
		return physicalURIsOfSummaryGraphs.get(datasource);
	}


	private void indexSummaries(Properties parameters){
		String sourcePath = parameters.getProperty(SOURCE_ONTO_PATH);
		String sourceURI = parameters.getProperty(SOURCE_ONTO_URI);
		String targetPath = parameters.getProperty(TARGET_ONTO_PATH);
		String targetURI = parameters.getProperty(TARGET_ONTO_URI);
		
		SummaryGraphIndexServiceForBT summarizer = new SummaryGraphIndexServiceForBT();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> sGraph = null;
		if(parameters.get(WRITE_SOURCE_ONTO_SUMMARY).equals("true")){			
			try {
				sGraph = summarizer.computeSummaryGraph(sourcePath, true);
				summarizer.writeSummaryGraph(sGraph, parameters.getProperty(SOURCE_ONTO_SUMMARY_PATH));
				physicalURIsOfSummaryGraphs.put(sourceURI, parameters.getProperty(SOURCE_ONTO_SUMMARY_PATH));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(parameters.get(WRITE_SOURCE_ONTO_SCHEMA).equals("true")){
			try {
				if(sGraph == null) sGraph = summarizer.computeSummaryGraph(sourcePath, true);
				Pseudograph<SummaryGraphElement, SummaryGraphEdge> schema 
				= summarizer.computeSchemaGraph(sourcePath, sGraph, null);
				summarizer.writeSummaryGraphAsRDF(schema, parameters.getProperty(SOURCE_ONTO_SCHEMA_PATH));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(parameters.get(WRITE_TARGET_ONTO_SUMMARY).equals("true")){			
			try {
				sGraph = summarizer.computeSummaryGraph(targetPath, true);
				summarizer.writeSummaryGraph(sGraph, parameters.getProperty(TARGET_ONTO_SUMMARY_PATH));
				physicalURIsOfSummaryGraphs.put(targetURI, parameters.getProperty(TARGET_ONTO_SUMMARY_PATH));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(parameters.get(WRITE_TARGET_ONTO_SCHEMA).equals("true")){
			try {
				if(sGraph == null) sGraph = summarizer.computeSummaryGraph(targetPath, true);
				Pseudograph<SummaryGraphElement, SummaryGraphEdge> schema 
				= summarizer.computeSchemaGraph(targetPath, sGraph, null);
				summarizer.writeSummaryGraphAsRDF(schema, parameters.getProperty(TARGET_ONTO_SCHEMA_PATH));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	private void indexElements(Properties parameters){
		String sourcePath = parameters.getProperty(SOURCE_ONTO_PATH);
		String sourceURI = parameters.getProperty(SOURCE_ONTO_URI);
		String targetPath = parameters.getProperty(TARGET_ONTO_PATH);
		String targetURI = parameters.getProperty(TARGET_ONTO_URI);
		String repoDir = parameters.getProperty(KEYWORDINDEX_DIR);

		m_kIndexer = new KeywordIndexServiceForBT(repoDir, true);
			
		try {
			String summary = getSummaryGraphFilePath(sourceURI);
			File graphIndex = new File(summary);
			ObjectInputStream in;
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = null;
			in = new ObjectInputStream(new FileInputStream(graphIndex));
			graph = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>)in.readObject();
			in.close();
			m_kIndexer.indexKeywords(sourcePath, sourceURI, graph, null);
			
			summary = getSummaryGraphFilePath(targetURI);
			graphIndex = new File(summary);
			in = new ObjectInputStream(new FileInputStream(graphIndex));
			graph = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>)in.readObject();
			in.close();
			m_kIndexer.indexKeywords(targetPath, targetURI, graph, null);

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}		
	}


	private void index(Properties parameters){
		IOntology omsOnto = null;

		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(SOURCE_ONTO_URI));
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, parameters.getProperty(SOURCE_ONTO_PATH));
		omsOnto = loadOntology(parameters, parameters.getProperty(SOURCE_ONTO_PATH));
		//TODO do the indexing with SummaryGraphIndexService
		m_sessionFactory.getCurrentSession().close();
		m_con.closeOntology(omsOnto);

		//index target
		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(TARGET_ONTO_URI));
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, parameters.getProperty(TARGET_ONTO_PATH));
		omsOnto = loadOntology(parameters, parameters.getProperty(TARGET_ONTO_PATH));
		//TODO do the indexing with SummaryGraphIndexService
		m_sessionFactory.getCurrentSession().close();
		m_con.closeOntology(omsOnto);
	}

	private void computeMappings(Properties parameters){
		MappingComputationService mapper = new MappingComputationService();
		m_mIndexer = new MappingIndexService(parameters.getProperty(MAPINGINDEX_DIR)); 
		ExtractInstancesService extractor = new ExtractInstancesService();

		Collection<SchemaMapping> sMappings = mapper.computeSchemaMappings(
				parameters.getProperty(SOURCE_ONTO_SCHEMA_PATH), 
				parameters.getProperty(TARGET_ONTO_SCHEMA_PATH));
		if (sMappings != null && sMappings.size() > 0){
			for (SchemaMapping m : sMappings){
				m_mIndexer.indexMappings(m);

				parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(SOURCE_ONTO_URI));
				IOntology omsOnto = loadOntology(parameters, parameters.getProperty(SOURCE_ONTO_PATH));
				Ontology sourceOnto= extractor.extractInstances(m.getSource(), m_dao);
				m_sessionFactory.getCurrentSession().close();
				m_con.closeOntology(omsOnto);

				//mapping is indeed a concept mapping
				if(sourceOnto != null){
					try {
						parameters.setProperty(KbEnvironment.ONTOLOGY_URI, parameters.getProperty(TARGET_ONTO_URI));
						omsOnto = loadOntology(parameters, parameters.getProperty(TARGET_ONTO_PATH));
						Ontology targetOnto = extractor.extractInstances(m.getTarget(), m_dao);
						File temp = new File(TEMP_ENTITIES_PATH);
						targetOnto.saveOntology(OntologyFileFormat.OWL_XML,temp,"ISO-8859-1");
						sourceOnto.importContentsFrom(temp, null);
						m_sessionFactory.getCurrentSession().close();
						m_con.closeOntology(omsOnto);
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
					Collection<InstanceMapping> iMappings = mapper.computeInstanceMappings(new MyOntology(sourceOnto), m.getSourceDsURI(), m.getTargetDsURI(), m);
					if (iMappings != null && iMappings.size() > 0){
						for (InstanceMapping mi : iMappings){
							m_mIndexer.indexMappings(mi);
						}
					}
				}
			}
		}
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
		if(m_con == null) initConnection(parameters.getProperty(REPOSITORY_DIR));
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


//	private boolean isIndexingRequired(String datasourceUri){
//	if (!m_indexedDS.contains(datasourceUri)){
//	File file = new File(ExploreEnvironment.KB_INDEX_DIR);
////	TODO check of the knowledgebase has been changed instead 
//	if ((file.list() == null) || (file.list().length <= 2)) {
//	return true;
//	}
//	}

//	return false;
//	}

}
