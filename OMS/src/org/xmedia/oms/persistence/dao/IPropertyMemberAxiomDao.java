package org.xmedia.oms.persistence.dao;

import java.util.List;
import java.util.Set;

import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.IReifiedElementDao;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.DatasourceException;


public interface IPropertyMemberAxiomDao extends IReifiedElementDao<IPropertyMember> {
	
	

	/** indvidual could be both source or target ***********************/
	public Set<IPropertyMember> findByIndividual(IIndividual individual) throws DatasourceException;
	public Set<IPropertyMember> findByIndividual(IIndividual individual, boolean includeInferred) throws DatasourceException;

	public Set<IPropertyMember> findBySourceIndividual(IIndividual individual) throws DatasourceException;
	public Set<IPropertyMember> findBySourceIndividual(IIndividual individual, boolean includeInferred) throws DatasourceException;
	
	public Set<IPropertyMember> findByTargetIndividual(IIndividual individual) throws DatasourceException;
	public Set<IPropertyMember> findByTargetIndividual(IIndividual individual, boolean includeInferred) throws DatasourceException;
	
	public Set<IPropertyMember> findByTargetValue(ILiteral literal) throws DatasourceException;
	public Set<IPropertyMember> findByTargetValue(ILiteral literal, boolean includeInferred) throws DatasourceException;

	public Set<IPropertyMember> findObjectPropertyMemberByIndividual(IIndividual individual) throws DatasourceException;
	public Set<IPropertyMember> findObjectPropertyMemberByIndividual(IIndividual individual, boolean includeInferred) throws DatasourceException;
	
	public Set<IPropertyMember> findObjectPropertyMemberBySource(IIndividual individual) throws DatasourceException;
	public Set<IPropertyMember> findObjectPropertyMemberBySource(IIndividual individual, boolean includeInferred) throws DatasourceException;
	
	public Set<IPropertyMember> findByProperty(IProperty property) throws DatasourceException;
	public Set<IPropertyMember> findByProperty(IProperty property, boolean includeInferred) throws DatasourceException;

	/**
	 * Find a given statement.
	 * 
	 * @param subject
	 * @param property
	 * @param object
	 * @return null if the intended statement isn't present in the IOntology.
	 * @throws DatasourceException
	 */
	public IPropertyMember find(IIndividual subject, IProperty property, IResource object) throws DatasourceException;
	public IPropertyMember find(IIndividual subject, IProperty property, IResource object, boolean includeInferred) throws DatasourceException;
	
    public List<IPropertyMember> findAll() throws DatasourceException;
    public List<IPropertyMember> findAll(boolean includeInferred, boolean includeProvenanceStatements) throws DatasourceException;
    
    public IPropertyMember insert(IIndividual subject, IProperty property, IResource object) throws BOInsertionException;
    public IPropertyMember insert(IIndividual subject, IProperty property, IResource object, IProvenance provenance) throws BOInsertionException;
    
    public void delete(IPropertyMember aPropertyMember) throws BODeletionException;
    public void delete(Set<IPropertyMember> propertyMembers) throws BOsDeletionException, BODeletionException;
    public void delete(IIndividual subject, IProperty property, IResource object) throws BODeletionException;
    
    public List<IPropertyMember> findAllObjectPropertyMember()throws DatasourceException;
    public List<IPropertyMember> findAllDataPropertyMember() throws DatasourceException;
    public int getNumberOfPropertyMember(IProperty property);

}
