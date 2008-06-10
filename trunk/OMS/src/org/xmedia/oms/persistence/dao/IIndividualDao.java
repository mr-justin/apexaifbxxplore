package org.xmedia.oms.persistence.dao;

import java.util.Set;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.persistence.DatasourceException;



public interface IIndividualDao extends IEntityDao<INamedIndividual> {

	/***** find by concept **********************************************************/
	public Set<IIndividual> findMemberIndividuals(INamedConcept concept) throws DatasourceException;

	public Set<IIndividual> findMemberIndividuals(INamedConcept concept, boolean includeInferred) throws DatasourceException;
	
	public int getNumberOfIndividual(INamedConcept concept)throws DatasourceException;
	
	public int getNumberOfIndividual(INamedConcept concept, boolean includeInferred)throws DatasourceException;
	
}
