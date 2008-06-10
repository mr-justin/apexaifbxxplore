package org.xmedia.oms.persistence;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.impl.Namespaces;


/**
 * Extends the notion of java.sql.connection to support also knowledge bases (ontologies).
 * A KB is database with a additional knowledge layer. 
 * Feature of this addtional layer is added value. That is, the knowledge can be regarded as  
 * normal data and be queried via SQL statements. Therefore, implementors must also support 
 * database-style access. 
 * @author dtr
 *
 */
public interface IKbConnection extends Connection {
	
	/**
	 * Load the ontology using the parameters.
	 * The ontology mus have been previously created. 
	 * 
	 * @param parameters information for opening the connection 
	 * @throws OntologyLoadException 
	 * @throws Exception 
	 */
	public IOntology loadOntology(java.util.Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyLoadException;
	
	/**
	 * 
	 * 
	 * @param parameters
	 * @return
	 * @throws MissingParameterException
	 * @throws InvalidParameterException
	 * @throws OntologyCreationException
	 */
	public IOntology createOntology(java.util.Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyCreationException;
	
	/**
	 * If the ontology exist, it is loaded; otherwise, it will be created before loading.
	 * 
	 * @param parameters
	 * @return
	 * @throws MissingParameterException
	 * @throws InvalidParameterException
	 * @throws OntologyCreationException
	 */
	public IOntology loadOrCreateOntology(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyCreationException;
	
	public void deleteOntology(String ontologyUri) throws OntologyDeletionException;
	
	public boolean closeOntology(IOntology onto);
		
	
//	public void setActiveReasoner(IReasoner reasoner);
	
	/**
	 * 
	 * @return a Set containing all ontology available through this kb connection. 
	 * Note that ontology is only available after beeing loaded.
	 * 
	 */
	public Set findAllOntologies();
	
	/**
	 * 
	 * @param uri
	 * @return 
	 */
	public IOntology findOntologyByUri(String uri);
	
	public Map getConfiguration();
	
	public Namespaces getNamespaces();

	
}
