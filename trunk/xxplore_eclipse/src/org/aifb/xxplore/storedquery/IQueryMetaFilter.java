package org.aifb.xxplore.storedquery;

import java.util.Date;
import java.util.Set;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;

public interface IQueryMetaFilter {
	public Double getConfidenceDegree();
	
	public Date getDate();
	
	public Set<INamedIndividual> getAgents();
	
	public Set<IEntity> getSources();

	public void setConfidenceDegree(Double m_confidence);

	public void setDate(Date m_date);

	public boolean getRequireProvenances();

	public void setRequireProvenances(boolean provenances);
}
