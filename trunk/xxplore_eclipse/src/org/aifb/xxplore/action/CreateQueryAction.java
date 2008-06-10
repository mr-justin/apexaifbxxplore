package org.aifb.xxplore.action;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.StoredQueryView;
import org.aifb.xxplore.storedquery.IStoredQueryListElement;
import org.aifb.xxplore.storedquery.Query;
import org.aifb.xxplore.storedquery.StoredQueryEditorInput;
import org.aifb.xxplore.storedquery.StoredQueryListElement;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class CreateQueryAction extends Action {

	private final StoredQueryView view;
	
	public CreateQueryAction(StoredQueryView view) {
		this.view = view;
		setText("Add Query");
		setToolTipText("Add a new Query");
        setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/task-new.gif"));
	}
	
	public void run(){ 
		String editorId = "org.aifb.xxplore.storedqueryeditor";
		IWorkbenchPage workbenchPage = view.getViewSite().getPage();
		IStoredQueryListElement newQueryListElement = new StoredQueryListElement(new Query(), view.getQueryListManager().genUniqueTaskId());
		view.getQueryListManager().getQueryList().addQuery(newQueryListElement);
		try {
			workbenchPage.openEditor(new StoredQueryEditorInput(newQueryListElement, view.getContentProvider(), true), editorId);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		this.view.getViewer().refresh();
		System.out.println("Test : New Query Handle is " + newQueryListElement.getHandle());
	}
	
}
