package org.aifb.xxplore.action;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.TaskListView;
import org.aifb.xxplore.task.ITaskListElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

public class DeleteTaskAction extends Action {
	
	private final TaskListView view;
	
	public DeleteTaskAction(TaskListView view) {
		this.view = view;
		setText("Delete Task");
		setToolTipText("Delete a new Task");
        setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/delete.gif"));
	}
	
	public void run(){ 
		IStructuredSelection selection = (IStructuredSelection)view.getViewer().getSelection();
		if (selection.getFirstElement() instanceof ITaskListElement){
			ITaskListElement entry = (ITaskListElement)selection.getFirstElement();
			view.getTaskList().deleteTask(entry);
		}
		this.view.getViewer().refresh();
	}
}
