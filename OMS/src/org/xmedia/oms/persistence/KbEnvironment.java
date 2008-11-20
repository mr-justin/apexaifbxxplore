package org.xmedia.oms.persistence;



/**
 * Provides access to the configuration info passed in <tt>Properties</tt> objects.
**/

public final class KbEnvironment {

	/**
	 * <tt>ConnectionProvider</tt> implementor to use when obtaining connections
	 */
	public static final String CONNECTION_PROVIDER_CLASS ="connection.provider";

	/**
	 * the default <tt>ConnectionProvider</tt> implementor to use when obtaining connections
	 */
	public static final String DEFAULT_CONNECTION_PROVIDER_CLASS ="org.xmedia.oms.adapter.kaon2.Kaon2ConnectionProvider";
	
	
	/**
	 * URI
	 */
	public static final String CONNECTION_URL ="connection.url";

	/**
	 * JDBC user
	 */
	public static final String USER ="connection.username";
	
	/**
	 * JDBC password
	 */
	public static final String PASS ="connection.password";
	
	/**
	 * autocommit mode
	 */
	public static final String AUTOCOMMIT ="connection.autocommit";
	
	/**
	 * Maximum number of connections for connection pool
	 */
	public static final String CONNECTION_POOL_SIZE ="connection.pool_size";
	
	/**
	 * Knowledge Representation Language 
	 */
	public static final String KR_LANGUAGE ="kr.language";
		
	/**
	 * the uri of the ontology
	 */
	public static final String PHYSICAL_ONTOLOGY_URI ="ontology.uri";
	
	public static final String ONTOLOGY_URI =PHYSICAL_ONTOLOGY_URI;
	
	public static final String IMPORTED_ONTOLOGY_URI = "imported.ontology.uri";
	
	public static final String DEFAULT_ONTOLOGY_ENCODING = "ISO-8859-1";
	
	/**
	 * the uri of the ontology
	 */
	public static final String LOGICAL_ONTOLOGY_URI ="logical.ontology.uri";

	public static final String ONTOLOGY = "ontology";
	
	
	/**
	 * the type of the ontology
	 */
	public static final String ONTOLOGY_TYPE ="ontology.type";
	
	/**
	 * the specific index for the ontology
	 */
	public static final String ONTOLOGY_INDEX ="ontology.index";
	
	/**
	 * the uri of the ontology
	 */
	public static final String ACTIVE_ONTOLOGY ="ontology";
		
	/**
	 * Maximum number of reasoners for reasoner pool
	 */
	public static final String REASONER_POOL_SIZE = "reasoner.pool_size";
	
	
	public static final String REASONER_ON = "reasoner.on";
		
	
	public static final String TRANSACTION_CLASS = "transaction_class";
	
	/**
	 * the product name of the database, e.g. mySQL, Oracle etc. 
	 */
	public static final String DB_PRODUCT_NAME = "db.product_name";
	
	
	/**
	 * the driver class for the database
	 */
	public static final String DB_DRIVER_CLASS = "db.driver_class";
	
	/**
	 * the driver class for the database
	 */
	public static final String NAMED_GRAPH_SYNTAX = "named.graph.syntax";
	
	
	/********** specific for the applications *****/
	
	public static final String ADAPTATION_ONTOLOGY_URI = "adaptation.ontology.uri";
	
	public static final String BASE_ADAPTATION_ONTOLOGY_URI = "adaptation.base_ontology.uri";

	/**
	 * The maximum number of triples returned per query
	 * Workaround until proper iteration over results is possible
	 * TODO: backward/forward iteration over results
	 */
	public static final int MAX_NUMBER_TRIPLES = 200;

			
}
