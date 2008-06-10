package org.xmedia.oms.persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.dao.IDaoManager;

public class StatelessSession implements ISession {

	private boolean closed = false;

	private IKbConnection m_conn;

	//the currently active ontology of the session
	private IOntology m_onto;

	private ITransaction m_transaction;

	/** The pool of reasoner for query */
	private IReasonerPool m_reasonerpool;

	private Map m_properties;

	/** is false by default, unless true is set **/ 
	boolean m_reasonerOn = false; 

	//private PersistenceContext temporaryPersistenceContext = new StatefulPersistenceContext( this );

	public StatelessSession(IKbConnection connection, IOntology onto) {
		m_conn = connection;

		try {
			setOntology(onto);
		} catch (OntologyLoadException e) {
			// TODO handle exception
		} catch (URISyntaxException e) {
			// TODO handle exception
		}

		m_properties = new HashMap<String, Object>();
		m_properties.putAll(connection.getConfiguration());

		String on = (String) connection.getConfiguration().get(KbEnvironment.REASONER_ON);
		if(on != null && on.contains("true"))
			m_reasonerOn = true; 
	}

	public boolean isOpen() {
		return !closed;
	}

	public void close() throws DatasourceException{
		try {
			m_conn.close();

			// rollback transaction if its still active
			if (m_transaction != null && m_transaction.isActive()){
				m_transaction.rollback();
			}

			m_transaction = null;
			m_onto = null;
			if (m_reasonerpool != null) {
				m_reasonerpool.dispose();
				m_reasonerpool = null;
			}

		} catch (SQLException e) {

			throw new DatasourceException(e);
		}
		closed = true;
	}



	public IKbConnection getConnection() {

		return m_conn;
	}

	public boolean isConnected() {
		if (m_conn != null){

			try {

				return !m_conn.isClosed();

			} catch (SQLException e) {
				return false;
			}

		}

		else return false;
	}


	/**
	 * Discard changes of current transtaction and start new one. 
	 */
	public ITransaction beginTransaction() {
		Emergency.checkPrecondition(m_onto != null, "m_onto != null");

		String transClass = (String)m_properties.get(KbEnvironment.TRANSACTION_CLASS);

		try {
			m_transaction = (ITransaction)Class.forName(transClass).newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		if(m_properties.get(KbEnvironment.ONTOLOGY) == null)
			m_properties.put((String)KbEnvironment.ONTOLOGY, m_onto);

		m_transaction.configure(m_properties);
		m_transaction.begin();
		return m_transaction; 
	}

	/**
	 * Commit last transtaction and begin new one.
	 * @param commitCurrent indicate whether current transaction shall be commited;
	 * @return
	 */
	public ITransaction beginTransaction(boolean commitCurrent){

		if (commitCurrent && m_transaction != null){
			if (m_transaction.isActive()) m_transaction.commit();
		}

		return beginTransaction();
	}


	public ITransaction getTransaction() {

		return m_transaction;
	}


	public void disconnect() throws DatasourceException {
		if (m_conn != null){
			try {
				m_conn.close();
			} catch (SQLException e) {

				throw new DatasourceException(e);
			}
		}
	}


	public void reconnect(IKbConnection connection) throws DatasourceException {
		m_conn = connection;
	}

	public void setOntology(IOntology onto) throws OntologyLoadException, URISyntaxException{
//		if (m_onto != null)
//			m_conn.closeOntology(m_onto);

		m_onto = onto;

		// check if onto is loaded
		if (m_conn.findOntologyByUri(m_onto.getUri()) == null){
			m_properties.put(KbEnvironment.PHYSICAL_ONTOLOGY_URI, m_onto.getUri());
			URI uri; 
			try {
				uri = new URI(m_onto.getUri());
			} catch (URISyntaxException e1) {
				throw e1;
			}

			try {
				m_conn.loadOntology(m_properties);
			} catch (Exception e) {
				throw new OntologyLoadException(uri, e);
			}

			m_properties.put(KbEnvironment.ONTOLOGY, onto);
		}

		//get new pool for ontology
		if(m_reasonerpool != null) m_reasonerpool.dispose();
		m_reasonerpool = new ReasonerPool(onto);		
		m_reasonerpool.configure(m_properties);

	}

	public IOntology getOntology(){
		return m_onto;
	}

	public Object getReasoner(){
		try {
			return m_reasonerpool.getAvailableReasoner();
		} catch (Exception e) {
			//TODO exception handling 
		}

		return null;
	}


	public void freeReasoner(Object reasoner){
		m_reasonerpool.freeReasoner(reasoner);
	}

	public boolean isReasoningOn(){
		return m_reasonerOn;
	}

	public void setReasoningOn(boolean on){
		m_reasonerOn = on;
	}

	public IDaoManager getDaoManager() {
		// TODO implement this by means of PersistenceUtils?
		return null;
	}

}


