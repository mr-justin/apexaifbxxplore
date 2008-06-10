package org.aifb.xxplore.action;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.StoredQueryView;
import org.aifb.xxplore.storedquery.IStoredQueryListElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

public class DeleteQueryAction extends Action {

	private final StoredQueryView view;
	
	public DeleteQueryAction(StoredQueryView view) {
		this.view = view;
		setText("Delete Query");
		setToolTipText("Delete a new Query");
        setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/delete.gif"));
	}
	
	public void run(){ 
		IStructuredSelection selection = (IStructuredSelection)view.getViewer().getSelection();
		if (selection.getFirstElement() instanceof IStoredQueryListElement){
			IStoredQueryListElement entry = (IStoredQueryListElement)selection.getFirstElement();
			view.getQueryList().deleteQuery(entry);
		}
		this.view.getViewer().refresh();
	}
}
