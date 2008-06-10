/**
 * 
 */
package org.aifb.xxplore.storedquery;

public class Prefix {
	private String m_prefix;
	private String m_ontology;
	
	public Prefix(String prefix, String ontology) {
		m_prefix = prefix;
		m_ontology = ontology;
	}
	
	public String getOntology() {
		return m_ontology;
	}
	public void setOntology(String m_ontology) {
		this.m_ontology = m_ontology;
	}
	public String getPrefix() {
		return m_prefix;
	}
	public void setPrefix(String m_prefix) {
		this.m_prefix = m_prefix;
	}
	
}