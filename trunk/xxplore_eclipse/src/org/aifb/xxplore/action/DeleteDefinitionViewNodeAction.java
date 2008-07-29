package org.aifb.xxplore.action;

import org.aifb.xxplore.views.definitionviewer.CriterionNode;
import org.aifb.xxplore.views.definitionviewer.DefinitionTupleObjectNode;
import org.aifb.xxplore.views.definitionviewer.DefinitionTupleRelationNode;
import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.aifb.xxplore.views.definitionviewer.ObjectNode;
import org.aifb.xxplore.views.definitionviewer.RelationNode;
import org.aifb.xxplore.views.definitionviewer.SubjectNode;
import org.ateam.xxplore.core.model.definition.EntityDefinition;
import org.ateam.xxplore.core.model.definition.RelationDefinition;
import org.eclipse.jface.action.Action;

public class DeleteDefinitionViewNodeAction extends Action {
	ITreeNode m_node;
	
	public DeleteDefinitionViewNodeAction(ITreeNode node) {
		m_node = node;
	}
	
	public void run() {
		if (m_node instanceof CriterionNode) {
			CriterionNode crit = (CriterionNode)m_node;
			((SubjectNode)m_node.getParent()).removeCriterion(crit);
			crit.getContentProvider().refreshViewers();
		}
		
		if (m_node instanceof RelationNode) {
			((RelationNode)m_node).getModelDefinition().setRelationDefinition(new RelationDefinition(((RelationNode)m_node).getModelDefinition().getDataSource()));
			((RelationNode)m_node).getContentProvider().refreshViewers();
		}
		
		if (m_node instanceof ObjectNode) {
			((ObjectNode)m_node).getModelDefinition().setObjectDefinition(new EntityDefinition(((ObjectNode)m_node).getModelDefinition().getDataSource(), EntityDefinition.OBJECT));
			((ObjectNode)m_node).getContentProvider().refreshViewers();
		}
		
		if (m_node instanceof DefinitionTupleRelationNode) {
			((DefinitionTupleRelationNode)m_node).getDefinitionTuple().setRelationDefinition(new RelationDefinition(((DefinitionTupleRelationNode)m_node).getDefinitionTuple().getModelDefinition().getDataSource()));
			((DefinitionTupleRelationNode)m_node).getContentProvider().refreshViewers();
		}

		if (m_node instanceof DefinitionTupleObjectNode) {
			((DefinitionTupleObjectNode)m_node).getDefinitionTuple().setObjectDefinition(new EntityDefinition(((DefinitionTupleObjectNode)m_node).getDefinitionTuple().getModelDefinition().getDataSource(), EntityDefinition.OBJECT));
			((DefinitionTupleObjectNode)m_node).getContentProvider().refreshViewers();
		}
}
	
	public String getText() {
		return "Delete";
	}
}
