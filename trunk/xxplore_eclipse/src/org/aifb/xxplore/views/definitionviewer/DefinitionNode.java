package org.aifb.xxplore.views.definitionviewer;

import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;

public abstract class DefinitionNode extends AbstractTreeNode {
	
	protected IModelDefinition m_modelDefinition;
	
	protected String varName;
	
	public DefinitionNode(ITreeNode parent, ModelDefinitionContentProvider contentProvider, IModelDefinition definition, String varName) {
		super(parent, contentProvider);
		m_modelDefinition = definition;
		this.varName = varName;
	}
	
	public IModelDefinition getModelDefinition() {
		return m_modelDefinition;
	}
	
	public void setModelDefinition(IModelDefinition def) {
		m_modelDefinition = def;
	}
	
	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	public String getVarName() {
		return varName;
	}
}
