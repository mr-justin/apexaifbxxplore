package org.aifb.xxplore.task;

import org.aifb.xxplore.core.service.datafiltering.ITask;
import org.eclipse.swt.graphics.Image;

public class TaskListElement implements ITaskListElement {

	private String m_handle;
	private ITask m_task;
	
	public TaskListElement(ITask task, String handle) {
		m_task = task;
		m_handle = handle;
	}
	
	public String getHandle() {
		return m_handle;
	}

	public Image getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStringForSortingDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		return m_task.getDescription();
	}

	public boolean isDirectlyModifiable() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setHandle(String id) {
		m_handle = id;
	}

	public ITask getTask() {
		return m_task;
	}

	public String getLabel() {
		return m_task.getName();
	}

}
