package org.aifb.xxplore.action;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.task.ITaskListElement;
import org.aifb.xxplore.task.TaskEditorInput;
import org.aifb.xxplore.task.TaskListElement;
import org.aifb.xxplore.TaskListView;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.aifb.xxplore.core.service.datafiltering.Task;

public class CreateTaskAction extends Action {

	private final TaskListView view;
	
	public CreateTaskAction(TaskListView view) {
		this.view = view;
		setText("Add Task");
		setToolTipText("Add a new Task");
        setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/task-new.gif"));
	}
	
	public void run(){ 
		String editorId = "org.aifb.xxplore.taskeditor";
		IWorkbenchPage workbenchPage = view.getViewSite().getPage();
		ITaskListElement newTaskListElement = new TaskListElement(new Task(), view.getTaskListManager().genUniqueTaskId());
		view.getTaskListManager().getTaskList().addRootTask(newTaskListElement);
		try {
			workbenchPage.openEditor(new TaskEditorInput(newTaskListElement, view.getContentProvider(), true), editorId);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		this.view.getViewer().refresh();
		System.out.println("Test : New Task Handle is " + newTaskListElement.getHandle());
	}
}
