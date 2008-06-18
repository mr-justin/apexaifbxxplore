/**
 * 
 */
package org.xmedia.oms.adapter.kaon2.ex;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConceptDao;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2PropertyMemberDao;
import org.xmedia.oms.adapter.kaon2.query.SparqlQueryEvaluator;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.IReifiedElement;
import org.xmedia.oms.metaknow.IReifiedElementDao;
import org.xmedia.oms.model.api.IAxiom;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.ITransaction;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryWrapper;

/**
 * @author Administrator
 *
 */
public class MetaqueryExample {

	private static IOntology m_onto;
	private static IOntology m_metaview;
	
	private static IOntology loadOntology(Kaon2ConnectionProvider provider, Properties props, String uri, boolean isLogicalURI) throws DatasourceException, MissingParameterException, InvalidParameterException, OntologyLoadException {

		/********** load ontology using the provider object  ************/
		if (isLogicalURI) 
			props.setProperty(KbEnvironment.LOGICAL_ONTOLOGY_URI, uri);
		else
			props.setProperty(KbEnvironment.PHYSICAL_ONTOLOGY_URI, uri);
		return provider.getConnection().loadOntology(PropertyUtils.convertToMap(props));
	}
	
	/**
	 * Init the connection with Kaon2. 
	 * Here, you need to provide connection parameter to a store, e.g. a hsqldb, if the statements are to be stored persistently. The connection provider
	 * can be also configured to run in memory. Via the connection provider, ontologies can be loaded. Wihtin a session and using data access objects  
	 * provided by a DAO manager, retrieval operations can be performed on the loaded ontologies.
	 */
	
	private static void init(){

		/********** create connection provider ************/
		//String providerClazz = parameters.getProperty(KbEnvironment.CONNECTION_PROVIDER_CLASS);
		Kaon2ConnectionProvider provider = new Kaon2ConnectionProvider();

		/********** configure connection provider ************/
		Properties props = new Properties();
		//set connection url, e.g. jdbc:hsqldb:hsql://localhost/  
		props.setProperty(KbEnvironment.CONNECTION_URL,  "");
		props.setProperty(KbEnvironment.USER, "");
		props.setProperty(KbEnvironment.PASS, "");
		
		//as kaon2 currently runs in memory only, no connection data to the db is required
		//the product name of the database used by jena store, e.g. HSQL 
		props.setProperty(KbEnvironment.DB_PRODUCT_NAME, "");
		//the driver class used for connection to the database, e.g. org.hsqldb.jdbcDriver 
		props.setProperty(KbEnvironment.DB_DRIVER_CLASS, "");
		
		props.setProperty(KbEnvironment.TRANSACTION_CLASS, "org.xmedia.oms.adapter.kaon2.persistence.Kaon2Transaction");
		provider.configure(props);

		try {
			m_onto = loadOntology(provider, props, "file:res/metaknow-example.xml", false);
			//need to load metaview extension and imports as well to access the metaview
			loadOntology(provider, props, "file:../OMS/res/org/xmedia/oms/model/onto/metaknow/metaknow.owl", false);
			loadOntology(provider, props, "file:res/metaknowledge-mw-ext.xml", false);
			m_metaview = loadOntology(provider, props, "ax:http://kaon2.semanticweb.org/example10-ontology", true);
		} catch (DatasourceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MissingParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OntologyLoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/********** create a session ************/
		ISessionFactory factory = SessionFactory.getInstance();
		//not much to configure now
		factory.configure(PropertyUtils.convertToMap(props));
		//set session factory
		PersistenceUtil.setSessionFactory(factory); 
		//open a new session with the ontology
		try {
			factory.openSession(provider.getConnection(), m_onto);
		} catch (DatasourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OpenSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/********** set a dao manager ************/
		// the dao manager provides the daos to be use to access the knowledge base
		PersistenceUtil.setDaoManager(Kaon2DaoManager.getInstance());

	}	


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		//init the connection 
		init();

		//get the initialized session 		
		SparqlQueryEvaluator eval = new SparqlQueryEvaluator();
		String query =
			"PREFIX  domain:  <http://kaon2.semanticweb.org/example10-ontology#>\n" +
			"PREFIX  mv:  <ax:http://kaon2.semanticweb.org/example10-ontology#>\n" +
			"PREFIX  metaex:  <http://www.x-media.org/metaknow-ext#>\n" + 
			"PREFIX  meta:  <http://www.x-media.org/ontologies/metaknow#>\n" + 
			"PREFIX  axmv:  <http://www.cs.man.ac.uk/AxiomMetaview#>\n" +
			"PREFIX  rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n\n" +
			"SELECT ?a ?axiom ?prov\n" +
			"FROM NAMED <http://kaon2.semanticweb.org/example10-ontology>\n" +
			"FROM NAMED <http://www.x-media.org/metaknow-ext>\n" +
			"WHERE\n" +
			"{\n" +
			"  GRAPH domain:\n" +
			"  {\n" +
			"    ?a domain:hasRelation domain:Eve \n" +
			"  } .\n" +
			"  GRAPH mv:\n" +
			"  {\n" +
			"      ?axiom axmv:sourceIndividual ?a .\n" +
			"      ?axiom axmv:objectProperty domain:hasRelation .\n" +
			"      ?axiom axmv:targetIndividual domain:Eve .\n" +
			"      ?axiom meta:has_Provenance ?prov .\n" +
			"      ?prov meta:source metaex:SomeSource \n" +
			"  }\n" +
			"}\n";
		IQueryResult result = eval.evaluateWithProvenance(new QueryWrapper(query, new String[] {"?a", "?axiom", "?prov"}));
		printResources(result);
	}
	
	private static void printResources(IQueryResult result) {
		Set<ITuple> tuples = result.getResult();
		for (ITuple tuple : tuples) {
			System.out.println(" " + tuple);
		}
	}

}
