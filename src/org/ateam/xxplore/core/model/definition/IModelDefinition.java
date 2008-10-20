package org.ateam.xxplore.core.model.definition;

import org.aifb.xxplore.shared.util.IModelChangeProvider;

public interface IModelDefinition extends  IModelChangeProvider, IDefinition{

//	public IDefinition getSubjectDefinition();

	public IRelationDefinition getRelationDefinition();
	
	public IDefinition getObjectDefinition();

//	public void setSubjectDefinition(IDefinition definition);
	
	public void setObjectDefinition(IDefinition definition);

	public void setRelationDefinition(IRelationDefinition definition);
	
	/**
	 * A complex model definition has at least one complex definition tuple. A definition tuple is complex 
	 * if its object is not simply an entity definition but a model definition. 
	 * In other words, a complex model definition is such that contains model definitions as part.   
	 * @return
	 */
	public boolean isComplex();

	public void setVariableName(String varName);

	public String getVariableName();

}
