package org.xmedia.oms.persistence;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xmedia.oms.model.api.IOntology;


public abstract class AbstractConnection implements IKbConnection{

	private Map<String, OntologyEntry> m_ontos = new HashMap<String, OntologyEntry>() ; 
	

	public boolean closeOntology(IOntology onto){
				
		//remove from the managed list
		m_ontos.remove(onto);
		onto = null;

		return true;

	}

	public IOntology findOntologyByUri(String uri) {

		if (m_ontos.get(uri) != null)
			return m_ontos.get(uri).getOntology();		
		else return null;
	}


	public Set findAllOntologies() {

		Set<IOntology> ontos = new HashSet<IOntology>();

		synchronized (m_ontos) {
			for (OntologyEntry onto : m_ontos.values()) {
				ontos.add(onto.getOntology());
			}
		}		
		return ontos;
	}

	
	/**
	 * adds the ontology to the existing datasources
	 * @return the id of the ontology returned 
	 */
	public void addOntology(IOntology onto) {

		// check if ontology is already added 
		if (findOntologyByUri(onto.getUri()) == null) {
		
			m_ontos.put(onto.getUri(), new OntologyEntry(onto.getUri(), onto));

		}
	}

	public void close() throws SQLException {

		m_ontos.clear();

	}

	
	
	class OntologyEntry implements Comparable<OntologyEntry>{
		protected String m_uri;
		protected IOntology m_onto;

		public OntologyEntry (String uri, IOntology dSource) {
			m_uri = uri;
			m_onto = dSource;
		}

		IOntology getOntology(){
			return m_onto;
		}

		String getName(){
			return m_uri;
		}

		public int compareTo(OntologyEntry o) {			
			if ((m_uri.compareTo(o.m_uri))!=0){
				return m_onto.hashCode()-o.m_onto.hashCode();
			}
			return 0;
		}

		public boolean equals(Object obj) {
			if (! (obj instanceof OntologyEntry))return false;
			OntologyEntry onto = (OntologyEntry)obj;
			if (m_uri.equals(obj) && m_onto.equals(onto.m_onto))return true;
			return false;
		}

		public int hashCode() {
			return m_uri.hashCode()+m_onto.hashCode();
		}
	}


	
}
