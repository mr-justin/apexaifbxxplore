package org.xmedia.oms.metaknow;

import java.util.Date;
import java.util.Set;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.dao.IEntityDao;

/**
 * A reified element is either an ontology axiom, e.g. property member or an entity, e.g. an individual. 
 * Right now, it is assumed that there exist only one single provenance object for  a reified element. That is, 
 * getProvenance returns one instance of Provenance or null. 
 * @author Thanh
 *
 */
public interface IReifiedElementDao<ReifiedResource extends IReifiedElement> extends IEntityDao<ReifiedResource> {
	
	public static int CONFIDENCE_DEGREE_STRICTLY_HIGHER = 0;
	
	public static int CONFIDENCE_DEGREE_STRICTLY_LOWER = 1;
	
	public static int CONFIDENCE_DEGREE_HIGHER = 2;
	
	public static int CONFIDENCE_DEGREE_LOWER = 3;
	
	public static int DATE_STRICTLY_NEWER = 0;

	public static int DATE_STRICTLY_OLDER = 1;
		
	public static int DATE_NEWER = 2;
	
	public static int DATE_OLDER = 3;
		
	public Set<ReifiedResource> findByConfidenceDegree(double degree, int degreeType) throws DatasourceException;
	
	public Set<ReifiedResource> findByConfidenceDegreeBetween(double lowerbound, int degreeTypeLower, long upperbound, int degreeTypeUpper) throws DatasourceException;
	
	public Set<ReifiedResource> findByAgent(IEntity agent) throws DatasourceException;
	
	public Set<ReifiedResource> findByAgent(String agentUri) throws DatasourceException;
	
	public Set<ReifiedResource> findBySource(IEntity source) throws DatasourceException;
	
	public Set<ReifiedResource> findBySource(String sourceUri) throws DatasourceException;

	public Set<ReifiedResource> findByCreationDate(Date creationDate, int type) throws DatasourceException;

	public Set<ReifiedResource> findByCreationDateBetween(Date before, int beforeType, Date after, int aftertype) throws DatasourceException;
	
	public Set<IProvenance> getProvenances(ReifiedResource res) throws ProvenanceUnknownException;
	
	public Set<INamedIndividual> getAgents(ReifiedResource res) throws ProvenanceUnknownException;
	
	public Set<IEntity> getSources(ReifiedResource res) throws ProvenanceUnknownException;
	
	public Set<Date> getCreationDates(ReifiedResource res) throws ProvenanceUnknownException;
	
	public Double[] getConfidenceDegrees(ReifiedResource res) throws ProvenanceUnknownException;
	
	/**
	 * Factory method for creating provenance.
	 * Values may be null.
	 * 
	 * @param agent
	 * @param confidenceDegree
	 * @param creationDate
	 * @param source
	 * @return
	 */
	public IProvenance createProvenance(
			INamedIndividual agent,
			Double confidenceDegree,
			Date creationDate,
			IEntity source);
}
