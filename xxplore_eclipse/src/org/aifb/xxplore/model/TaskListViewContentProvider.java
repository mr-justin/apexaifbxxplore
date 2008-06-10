package org.aifb.xxplore.model;

import java.util.List;

import org.aifb.xxplore.TaskListView;
import org.aifb.xxplore.core.service.datafiltering.DataFilteringService;
import org.aifb.xxplore.core.service.datafiltering.ITaskPolicyDao;
import org.aifb.xxplore.task.TaskList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TaskListViewContentProvider implements ITreeContentProvider {

	private final TaskListView m_view;
	private ITaskPolicyDao m_taskPolicyDao;
	private DataFilteringService m_filteringService;
	
	public TaskListViewContentProvider(TaskListView view, ITaskPolicyDao taskPolicyDao, DataFilteringService filteringService){
		this.m_view = view;
		this.m_taskPolicyDao = taskPolicyDao;
		this.m_filteringService = filteringService;
	}
	
	public ITaskPolicyDao getTaskPolicyDao(){
		return m_taskPolicyDao;
	}
	
	public DataFilteringService getDataFilteringService(){
		return m_filteringService;
	}
	
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof TaskList){
			List list = ((TaskList)inputElement).getRootTasks();
			return list.toArray();
		} else
			return new Object[0];
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
