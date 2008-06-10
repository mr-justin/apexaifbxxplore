/**
 * 
 */
package org.aifb.xxplore.core.service;


import java.util.Collection;

import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.ISchema;

public class KbContentProviderService implements IContentProviderService{

	
	public KbContentProviderService(){init();}

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub

	}



	public void disposeService() {
		// TODO Auto-generated method stub

	}



	public void init(Object... params) {}



	public Collection<IResource> getModel(IModelDefinition definition) {
		//TODO 
		//convert mode definition to sparql query 


		//evaluate sparql query --> see test()
		return null;
	}


	public IHierarchicalSchema getHierarchicalSchema(IOntology ontology) {
		return ontology.getHierarchicalSchema();

	}

	public ISchema getSchema(IOntology ontology) {
		return ontology.getSchema();
	}

}