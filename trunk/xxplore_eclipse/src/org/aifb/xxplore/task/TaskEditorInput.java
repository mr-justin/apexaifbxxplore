package org.aifb.xxplore.task;

import org.aifb.xxplore.model.TaskListViewContentProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class TaskEditorInput implements IEditorInput {
	
	private ITaskListElement taskListElement;
	
	private TaskListViewContentProvider m_contentProvider;
	
	private String id;
	
	private boolean isNewTask = false;

	public TaskEditorInput(ITaskListElement task, TaskListViewContentProvider contentProvider, boolean isNewtask) {
		this.taskListElement = task;
		this.m_contentProvider = contentProvider;
		this.id = task.getHandle();
		this.isNewTask = isNewtask;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return taskListElement.getLabel();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public ITaskListElement getTask() {
		return taskListElement;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return taskListElement.getLabel();
	}
	
	public TaskListViewContentProvider getContentProvider(){
		return m_contentProvider;
	}

	public boolean isNewTask() {
		return isNewTask;
	}
	
//	public boolean equals(Object o) {
//		if (o instanceof TaskEditorInput) {
//			TaskEditorInput input = (TaskEditorInput) o;
//			return getId() == input.getId();
//		}
//		return false;
//	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TaskEditorInput other = (TaskEditorInput) obj;
		if (taskListElement == null) {
			if (other.taskListElement != null)
				return false;
		} else if (!taskListElement.equals(other.taskListElement))
			return false;
		return true;
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((taskListElement == null) ? 0 : taskListElement.hashCode());
		return result;
	}
}
