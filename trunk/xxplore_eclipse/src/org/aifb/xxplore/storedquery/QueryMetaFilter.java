package org.aifb.xxplore.storedquery;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;

public class QueryMetaFilter implements IQueryMetaFilter {
	private Double m_confidence;
	private Date m_date;
	private Set<INamedIndividual> m_agents;
	private Set<IEntity> m_sources;
	private boolean m_requireProvenances;
	
	public QueryMetaFilter(Double m_confidence, Date m_date, Set<INamedIndividual> m_agents, Set<IEntity> m_sources) {
		super();
		this.m_confidence = m_confidence;
		this.m_date = m_date;
		this.m_agents = m_agents;
		this.m_sources = m_sources;
	}

	public QueryMetaFilter() {
		super();
		m_agents = new HashSet<INamedIndividual>();
		m_sources = new HashSet<IEntity>();
	}

	public Set<INamedIndividual> getAgents() {
		return m_agents;
	}

	public Double getConfidenceDegree() {
		return m_confidence;
	}

	public Date getDate() {
		return m_date;
	}

	public Set<IEntity> getSources() {
		return m_sources;
	}

	public void setConfidenceDegree(Double m_confidence) {
		this.m_confidence = m_confidence;
	}

	public void setDate(Date m_date) {
		this.m_date = m_date;
	}

	public boolean getRequireProvenances() {
		return m_requireProvenances;
	}

	public void setRequireProvenances(boolean provenances) {
		m_requireProvenances = provenances;
	}
}
