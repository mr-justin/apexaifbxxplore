package org.xmedia.oms.persistence.dao;

import java.util.Set;

import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.persistence.DatasourceException;


public interface IConceptDao extends IEntityDao<INamedConcept> {
	
	/***** find by concept **********************************************************/
	public Set<IConcept> findSubconcepts(INamedConcept concept) throws DatasourceException;
		
	public Set<IConcept> findSuperconcepts(INamedConcept concept) throws DatasourceException;
	
	public Set<IConcept> findDisjointConcepts(INamedConcept concept) throws DatasourceException;
	
	public Set<IConcept> findEquivalentConcepts(INamedConcept concept) throws DatasourceException;

	public Set<IConcept> findSubconcepts(INamedConcept concept, boolean includeInferred) throws DatasourceException;
	
	public Set<IConcept> findSuperconcepts(INamedConcept concept, boolean includeInferred) throws DatasourceException;
	
	/***** find by property  *********************************************************/	
	public Set<INamedConcept> findDomains(IProperty property) throws DatasourceException;
	
	public Set<INamedConcept> findConceptRanges(IObjectProperty property) throws DatasourceException;
			
	
	/***** find by individual **********************************************************/
	
	public Set<IConcept> findTypes(INamedIndividual individual) throws DatasourceException;
	

}
