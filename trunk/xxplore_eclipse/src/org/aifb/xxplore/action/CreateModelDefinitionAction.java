package org.aifb.xxplore.action;

import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.aifb.xxplore.views.definitionviewer.AbstractTreeNode;
import org.aifb.xxplore.views.definitionviewer.DefinitionTupleObjectNode;
import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.aifb.xxplore.views.definitionviewer.ObjectNode;
import org.eclipse.jface.action.Action;

public class CreateModelDefinitionAction extends Action {

	private ITreeNode m_node;
	
	public CreateModelDefinitionAction(ITreeNode node) {
		m_node = node;
	}
	
	public void run() {
		if (m_node instanceof ObjectNode) {
			ObjectNode on = (ObjectNode)m_node;
			ModelDefinition md = (ModelDefinition)on.getModelDefinition();
			String varName = on.getVarName();
			
			ModelDefinition newDefinition = new ModelDefinition(md.getDataSource());
			newDefinition.setVariableName(newDefinition.getNextVarName());
			md.setVariableName(varName);
			md.setObjectDefinition(newDefinition);
			on.resetChildren();
			on.getContentProvider().refreshViewers(m_node);
		}

//		if (m_node instanceof DefinitionTupleObjectNode) {
//			DefinitionTupleObjectNode on = (DefinitionTupleObjectNode)m_node;
//
//			ModelDefinition newDefinition = new ModelDefinition(on.getDefinitionTuple().getModelDefinition().getDataSource());
//			newDefinition.setSuperDefinition(on.getDefinitionTuple().getModelDefinition());
//			on.getDefinitionTuple().setObjectDefinition(newDefinition);
//			on.getContentProvider().refreshViewers(m_node,ModelDefinitionContentProvider.EXPAND);
//		}
	}
	
	public String getText() {
		return "Create model definition";
	}
}
