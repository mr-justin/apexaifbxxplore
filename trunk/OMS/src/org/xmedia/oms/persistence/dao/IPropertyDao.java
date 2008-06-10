package org.xmedia.oms.persistence.dao;

import java.util.Set;

import org.aifb.xxplore.shared.util.Pair;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.DatasourceException;


public interface IPropertyDao extends IEntityDao<IProperty>{

	/***** find by concept *************************************************************/
	public Set<IProperty> findProperties(INamedConcept concept) throws DatasourceException;
	
	public Set<IProperty> findPropertiesFrom(INamedConcept concept) throws DatasourceException;
	
	public Set<IProperty> findPropertiesTo(INamedConcept concept) throws DatasourceException;
	
	public Set<Pair> findPropertiesAndRangesFrom(INamedConcept concept) throws DatasourceException;
	
	/***** find by individual **********************************************************/
	public Set<IProperty> findProperties(INamedIndividual individual) throws DatasourceException;
	
	public Set<IProperty> findPropertiesFrom(INamedIndividual individual) throws DatasourceException;
	
	public Set<IProperty> findPropertiesTo(INamedIndividual individual) throws DatasourceException;	

}
