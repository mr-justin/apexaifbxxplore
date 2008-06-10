package org.xmedia.oms.model.api;

import java.util.Set;

public interface IHierarchicalSchemaNode extends ISchemaNode{
	
	public Set<IHierarchicalSchemaNode>getAncestors();	
	
	public Set<IHierarchicalSchemaNode>getParents();	
	
	public Set<IHierarchicalSchemaNode>getDescendants();	
	
	public Set<IHierarchicalSchemaNode>getChilds();	
}
