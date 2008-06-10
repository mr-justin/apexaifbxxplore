package org.xmedia.oms.model.api;

public interface IHierarchicalSchema extends ISchema{
	
	/**
	 * 
	 * @return the top node of the schema 
	 */
	public IHierarchicalSchemaNode getTopNode();
	
}
