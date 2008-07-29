package org.aifb.xxplore.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskList implements Serializable {

    private static final long serialVersionUID = 3618984485791021105L;
    
    private List<ITaskListElement> m_rootTasks = new ArrayList<ITaskListElement>();
    
    public void addRootTask(ITaskListElement task) {
    	m_rootTasks.add(task);
    }
    
    public void deleteTask(ITaskListElement task) {
    	boolean deleted = deleteTaskHelper(m_rootTasks, task);
	}
    
    private boolean deleteTaskHelper(List<ITaskListElement> tasks, ITaskListElement t) {
    	for (ITaskListElement task : tasks) {
    		if (task.getHandle().equals(t.getHandle())) {
    			tasks.remove(task);
    			return true;
    		}	
		}
    	return false;
	}
    
    public ITaskListElement getTaskForHandle(String handle) {
        return findTaskHelper(m_rootTasks, handle);
    } 
    
    private ITaskListElement findTaskHelper(List<? extends ITaskListElement> elements, String handle) {
        for (ITaskListElement element : elements) {
        	if(element instanceof ITaskListElement){
        		if(element.getHandle().compareTo(handle) == 0)
        			return (ITaskListElement)element;
        	}	
        }
        return null;
    }
    
    public List<ITaskListElement> getRootTasks() {
        return m_rootTasks;
    }

    public int findLargestTaskHandle() {
    	int max = 0;
    	max = Math.max(largestTaskHandleHelper(m_rootTasks), max);
    	return max;
    }
    
    private int largestTaskHandleHelper(List<ITaskListElement> tasks) {
    	int ihandle = 0;
    	int max = 0;
    	for (ITaskListElement t : tasks) {
    		String string = t.getHandle().substring(t.getHandle().indexOf('-')+1, t.getHandle().length());
    		if (string != "") {
    			ihandle = Integer.parseInt(string);
    		}
    		max = Math.max(ihandle, max);
    	}
    	return max;
    }
    
    public List<Object> getRoots() {
    	List<Object> roots = new ArrayList<Object>();
    	for (ITaskListElement t : m_rootTasks) roots.add(t);   			
    	return roots;
    }
    
	public void clear() {
		m_rootTasks.clear();
	}	
}
