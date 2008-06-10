package org.aifb.xxplore.action;

import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.aifb.xxplore.views.definitionviewer.CriterionNode;
import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.eclipse.jface.action.Action;

public class EditDefinitionNodeAction extends Action {
	private ITreeNode m_node;
	
	public EditDefinitionNodeAction(ITreeNode node) {
		m_node = node;
	}
	
	public void run() {
		if (m_node instanceof CriterionNode) {
			CriterionNode crit = (CriterionNode)m_node;
			if (crit.getEditState() == CriterionNode.EDIT_ALL) {
				setChecked(false);
				crit.setEditState(CriterionNode.EDIT_OFF);
				crit.getContentProvider().refreshViewers(m_node);
			}
			else {
				setChecked(true);
				crit.setEditState(CriterionNode.EDIT_ALL);
				crit.getContentProvider().refreshViewers(m_node);
			}
		}
	}
	
	public String getText() {
		return "Edit";
	}
}
