package org.ateam.xxplore.core.model.definition;

import java.util.Set;

import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;

public interface IEntityDefinition extends IDefinition {

	/**
	 * return the defined concept. 
	 * @return
	 */
	public IResource getDefinition();
	
	public boolean hasSubDefinition();
	
	public Set getSubDefinition();

	public void setDefinition(IResource definition);
	
	public static int OR_COMBINATION = 0;

	public static int AND_COMBINATION = 1;


	/**
	 * 
	 * @param definition
	 * @param type AND or OR combination of the specified definition and the existing definition
	 */
	public void addDefinition(IResource definition, int type);


	public static int SUBJECT = 0;

	public static int OBJECT = 1;

	public int getType();


}
