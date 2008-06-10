package org.xmedia.oms.metaknow;

import java.util.Date;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.impl.Resource;

public class Provenance extends Resource implements IProvenance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Double m_cDegree;
	
	private INamedIndividual m_agent;
	
	private IEntity m_source;
	
	private Date m_creationDate;
	
	private String m_uri;
	
	public Provenance(){}
	
	public Provenance(Double confidenceDegree, INamedIndividual agent, IEntity source, Date creationDate){
		m_cDegree = confidenceDegree;
		m_agent = agent;
		m_source = source;
		m_creationDate = creationDate;
	}
	public Provenance(String uri, Double confidenceDegree, INamedIndividual agent, IEntity source, Date creationDate){
		m_uri = uri;
		m_cDegree = confidenceDegree;
		m_agent = agent;
		m_source = source;
		m_creationDate = creationDate;
	}
	
	public void setCreationDate(Date date){
		m_creationDate = date;
	}
	
	public void setAgent(INamedIndividual agent){
		m_agent = agent;
	}
	
	public void setSource(IEntity source) {
		
		m_source = source;
	}
	
	public String getUri() {
		return m_uri;
	}
	
	public void setUri(String uri) {
		m_uri = uri;
	}
	
	public void setConfidenceDegree(double degree){
		m_cDegree = degree;
	}
	
	public INamedIndividual getAgent() {
		
		return m_agent;
	}

	public Double getConfidenceDegree() {

		return m_cDegree;
	}

	public Date getCreationDate() {

		return m_creationDate;
	}

	public IEntity getSource() {

		return m_source;
	}

}
