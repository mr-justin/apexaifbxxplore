package org.ateam.xxplore.core.model.definition;

import org.xmedia.oms.model.api.IProperty;

public interface IRelationDefinition extends IDefinition {
	
	public IProperty getDefinition();
	
	public void setDefinition(IProperty definition);

}

