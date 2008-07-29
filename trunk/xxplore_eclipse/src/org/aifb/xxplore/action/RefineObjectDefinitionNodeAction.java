package org.aifb.xxplore.action;

import org.aifb.xxplore.views.definitionviewer.CriterionNode;
import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.eclipse.jface.action.Action;

public class RefineObjectDefinitionNodeAction extends Action {
	
	private ITreeNode m_node;
	
	public RefineObjectDefinitionNodeAction(ITreeNode node) {
		m_node = node;
	}
	
	@Override
	public void run() {
		if (m_node instanceof CriterionNode) {
			CriterionNode crit = (CriterionNode)m_node;
			if (crit.getEditState() == CriterionNode.EDIT_REFINE_OBJECT) {
				setChecked(false);
				crit.setEditState(CriterionNode.EDIT_OFF);
				crit.getContentProvider().refreshViewers(m_node);
			}
			else if (crit.getEditState() == CriterionNode.EDIT_OFF) {
				setChecked(true);
				crit.setEditState(CriterionNode.EDIT_REFINE_OBJECT);
				crit.getContentProvider().refreshViewers(m_node);
			}
		}
	}
	
	public String getText() {
		return "Refine object";
	}
}
