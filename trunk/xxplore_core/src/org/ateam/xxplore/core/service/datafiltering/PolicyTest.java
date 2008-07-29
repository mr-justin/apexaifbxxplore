package org.ateam.xxplore.core.service.datafiltering;
//package org.aifb.xxplore.core.service.datafiltering;
//
//import java.util.Properties;
//import java.util.Set;
//
//import org.aifb.xxplore.core.ExploreEnvironment;
//import org.aifb.xxplore.shared.util.PropertyUtils;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
//import org.xmedia.oms.model.api.IOntology;
//import org.xmedia.oms.model.api.IPropertyMember;
//import org.xmedia.oms.model.api.IResource;
//import org.xmedia.oms.persistence.DatasourceException;
//import org.xmedia.oms.persistence.IConnectionProvider;
//import org.xmedia.oms.persistence.ISessionFactory;
//import org.xmedia.oms.persistence.InvalidParameterException;
//import org.xmedia.oms.persistence.KbEnvironment;
//import org.xmedia.oms.persistence.MissingParameterException;
//import org.xmedia.oms.persistence.OntologyLoadException;
//import org.xmedia.oms.persistence.OpenSessionException;
//import org.xmedia.oms.persistence.PersistenceUtil;
//import org.xmedia.oms.persistence.SessionFactory;
//import org.xmedia.oms.persistence.StatelessSession;
//import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
//
///**
// * @author Administrator
// *
// */
//public class PolicyTest {
//
//	private static IOntology m_onto;
//	private static IOntology m_metaknow;
//	private static IOntology m_metaviewExtension;
//	private static IOntology m_policyOntology;
//	
//	private static IOntology loadOntology(IConnectionProvider provider, Properties props, String uri) throws DatasourceException, MissingParameterException, InvalidParameterException, OntologyLoadException {
//
//		/********** load ontology using the provider object  ************/
//		props.setProperty(KbEnvironment.PHYSICAL_ONTOLOGY_URI, uri);
//		
//		return provider.getConnection().loadOntology(PropertyUtils.convertToMap(props));
//	}
//	
//	private static void init(){
//
//		/********** create connection provider ************/
//		//String providerClazz = parameters.getProperty(KbEnvironment.CONNECTION_PROVIDER_CLASS);
//		IConnectionProvider provider = new Kaon2ConnectionProvider();
//
//		/********** configure connection provider ************/
//		Properties props = new Properties();
//		//set connection url, e.g. jdbc:hsqldb:hsql://localhost/  
//		props.setProperty(KbEnvironment.CONNECTION_URL,  "");
//		props.setProperty(KbEnvironment.USER, "http://www.applicationontologies/fiat/policy#testuser1");
//		props.setProperty(KbEnvironment.PASS, "");
//		props.setProperty(ExploreEnvironment.BASE_POLICY_ONTOLOGY_URI, "http://www.applicationontologies/fiat/policy");
//		
//		//as kaon2 currently runs in memory only, no connection data to the db is required
//		//the product name of the database used by jena store, e.g. HSQL 
//		props.setProperty(KbEnvironment.DB_PRODUCT_NAME, "");
//		//the driver class used for connection to the database, e.g. org.hsqldb.jdbcDriver 
//		props.setProperty(KbEnvironment.DB_DRIVER_CLASS, "");
//		
//		props.setProperty(KbEnvironment.TRANSACTION_CLASS, "org.xmedia.oms.adapter.kaon2.persistence.Kaon2Transaction");
////		props.setProperty(KbEnvironment.POLICY_USER, "http://www.domainontologies/hypermedia/odahs#testuser1");
////		props.setProperty(KbEnvironment.POLICY_ONTOLOGY_URI, "file:../OMS/res/org/xmedia/oms/model/onto/metaknow/TaskPolicyFiat.owl");
//
//		provider.configure(props);
//
//		try {
//			m_metaknow = loadOntology(provider, props, "file:../OMS/res/org/xmedia/oms/model/onto/metaknow/metaknow.owl");
//			m_metaviewExtension = loadOntology(provider, props, "file:../standard-kaon2/res/metaknowledge-mw-ext.xml");
//			m_onto = loadOntology(provider, props, "file:../standard-kaon2/res/metaknow-example.xml");
//			m_policyOntology = loadOntology(provider, props, "file:../OMS/res/org/xmedia/oms/model/onto/metaknow/TaskPolicyFiat.owl");
//		} catch (DatasourceException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (MissingParameterException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InvalidParameterException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (OntologyLoadException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		/********** create a session ************/
//		ISessionFactory factory = SessionFactory.getInstance();
//		//not much to configure now
//		factory.configure(PropertyUtils.convertToMap(props));
//		//set session factory
//		PersistenceUtil.setSessionFactory(factory); 
//		//open a new session with the ontology
//		try {
//			factory.openSession(provider.getConnection(), m_onto);
//		} catch (DatasourceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OpenSessionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		/********** set a dao manager ************/
//		// the dao manager provides the daos to be use to access the knowledge base
//		PersistenceUtil.setDaoManager(Kaon2DaoManager.getInstance());
//
//	}	
//
//
//	/**
//	 * @param args
//	 * @throws Exception 
//	 */
//	public static void main(String[] args) throws Exception {
//		
//		//init the connection 
//		init();
//
//		//get the initialized session 
//		StatelessSession session = (StatelessSession) PersistenceUtil.getSessionFactory().getCurrentSession();
//		//get the loaded ontology
//		IOntology onto = session.getOntology();
//		IPropertyMemberAxiomDao propMemberDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
//		System.out.println(propMemberDao.findAll());
//
////		session.setOntology(m_policyOntology);
//		ITaskPolicyDao policyDao = TaskPolicyDao.getInstance();
//		ITask t = policyDao.findTaskPolicyByUri("http://www.applicationontologies/fiat/policy#task1");
//		System.out.println(t);
//		System.out.println(policyDao.findAgentsForTask(t));
//		System.out.println(policyDao.findInformationProviderForTask(t));
//		System.out.println(policyDao.findAllTaskPolicies());
//
//		DataFilteringService dfs = new DataFilteringService();
////		dfs.applyOrganizationalFilter(m_onto.getUri(), m_policyOntology.getUri());
//		dfs.applyTaskFilter(m_onto.getUri(), m_policyOntology.getUri(), t);
//		
//		session.setOntology(m_onto);
//	}
//	
//	private static void printResources(Set<IPropertyMember> pms) {
//		for (IResource res : pms) {
//			System.out.println(" " + res);
//		}
//	}
//
//}
