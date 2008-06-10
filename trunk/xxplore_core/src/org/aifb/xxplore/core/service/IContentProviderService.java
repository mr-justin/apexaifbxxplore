package org.aifb.xxplore.core.service;

import java.util.Collection;

import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.ISchema;

public interface IContentProviderService extends IService {
	
	/**
	 * 
	 * @param definition the definition of the model 
	 * @return the model of the given definition
	 */
	public Collection<IResource> getModel(IModelDefinition definition);	
	
	public ISchema getSchema(IOntology ontology);
	
	public IHierarchicalSchema getHierarchicalSchema(IOntology ontology);
	

}
