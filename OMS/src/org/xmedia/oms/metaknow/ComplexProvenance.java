package org.xmedia.oms.metaknow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;

public class ComplexProvenance extends Provenance implements IComplexProvenance{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8863330360269406663L;

	private Collection<Double> m_degrees;
	
	private Collection<Date> m_creationDates;
	
	private Set<INamedIndividual> m_agents;
	
	protected Set<IEntity> m_sources;
	
	
	public ComplexProvenance(Collection<Double> degrees, Collection<Date> creationDates, Set<INamedIndividual> agents, Set<IEntity> sources) {
			m_degrees = degrees;
			m_creationDates = creationDates;
			m_agents = agents;
			m_sources = sources;
	}

	public ComplexProvenance(Double confidenceDegree, INamedIndividual agent, IEntity source, Date creationDate){
		m_degrees = new ArrayList<Double>();
		m_degrees.add(confidenceDegree);
		m_agents = new HashSet<INamedIndividual>();
		m_agents.add(agent);
		m_sources = new HashSet<IEntity>();
		m_sources.add(source);
		m_creationDates = new ArrayList<Date>();
		m_creationDates.add(creationDate);
	}
	
	public void setCreationDate(Date date){
		if (m_creationDates == null){
			m_creationDates = new ArrayList<Date>();
		}
		m_creationDates.add(date);
	}
	
	public void setAgent(INamedIndividual agent){
		if (m_agents == null){
			m_agents = new HashSet<INamedIndividual>();
		}
		m_agents.add(agent);
	}
	
	public void setSource(IEntity source) {
		if (m_sources == null){
			m_sources = new HashSet<IEntity>();
		}
		m_sources.add(source);	
	}
	
	
	public void setConfidenceDegree(double degree){
		if (m_degrees == null){
			m_degrees = new ArrayList<Double>();
		}
		m_degrees.add(degree);
	}
	
	public INamedIndividual getAgent() {
		if (m_agents == null){
			return null;
		}
		return m_agents.iterator().next(); 
	}

	public Double getConfidenceDegree() {
		if (m_degrees == null){
			return null;
		}
		return m_degrees.iterator().next(); 	}

	public Date getCreationDate() {
		if (m_creationDates == null){
			return null;
		}
		return m_creationDates.iterator().next(); 	}

	public IEntity getSource() {
		if (m_sources == null){
			return null;
		}
		return m_sources.iterator().next(); 
	}

	public Set<INamedIndividual> getComplexAgent() {
		return m_agents;
	}

	public Collection<Double> getComplexConfidenceDegree() {
		return m_degrees;
	}

	public Collection<Date> getComplexCreationDate() {
		return m_creationDates;
	}

	public Set<IEntity> getComplexSource() {
		return m_sources;
	}

	public String toString(){
		
		String out = new String();
		
		out += "ComplexProvenance@";
		out += this.hashCode();
		out += "[";
		
		Double[] degrees = (Double[])m_degrees.toArray(new Double[0]);
		Date[] dates = (Date[])m_creationDates.toArray(new Date[0]);
		INamedIndividual[] agents = (INamedIndividual[])m_agents.toArray(new INamedIndividual[0]);
		IEntity[] sources = (IEntity[])m_sources.toArray(new IEntity[0]);
		
		for(int i = 0; i < degrees.length; i++)
		{
			
			out += "degree: "+degrees[i]+", ";
			out += "date: "+dates[i]+", ";
			out += "agent: "+agents[i]+", ";
			out += "source: "+sources[i];
			
			if(i != degrees.length-1) out += "| ";

		}
	
		out += "]";
		
		return out;
	}	
}
