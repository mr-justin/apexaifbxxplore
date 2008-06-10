package org.aifb.xxplore.action;

import org.aifb.xxplore.ExploreEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * Action for the toolbar button to change the view mode (Complete, Instances, Conceps)
 * of the explore editor. 
 *
 */
public class ChangeViewModeAction implements IEditorActionDelegate {

	private ExploreEditor m_activeEditor = null;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof ExploreEditor) {
			m_activeEditor = (ExploreEditor)targetEditor;
		}
	}

	public void run(IAction action) {
//		if (m_activeEditor != null) {
//			GraphViewer viewer = (GraphViewer)m_activeEditor.getViewer();
//			((GraphControl)viewer.getControl()).switchModeAction();
//		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
