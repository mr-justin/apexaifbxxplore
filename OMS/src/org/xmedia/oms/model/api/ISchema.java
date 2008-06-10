package org.xmedia.oms.model.api;

public interface ISchema {
	
	/**
	 * Returns the node of the schema containing the given class.
	 * @param clazz
	 * @return the node of the schema containing the given class.
	 */
	public ISchemaNode getNode(INamedConcept clazz);
		
}
