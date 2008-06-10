package org.xmedia.oms.model.api;

import java.util.Set;

public interface ILiteral extends IResource{
	
	public Object getValue();
		
	public String getLiteral();
	
	public String getLanguage();
	
	public Set<IDatatype> getDatatypes();
	
	/**
	 * 
	 * @return a set of property members, each containing this literal as the target 
	 */	
	public Set<IPropertyMember> getPropertyToValues();
		
}
