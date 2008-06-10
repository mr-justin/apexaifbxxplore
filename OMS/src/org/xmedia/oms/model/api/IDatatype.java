package org.xmedia.oms.model.api;

import java.util.Set;

public interface IDatatype extends IResource{
	
	public Class getJavaClass();
	
	public Set<IProperty> getDataPropertiesTo();
	

}
