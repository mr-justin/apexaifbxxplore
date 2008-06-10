package org.aifb.xxplore.task;

import java.io.File;

import org.aifb.xxplore.task.TaskList;
import org.aifb.xxplore.TaskListView;

public class TaskListManager {
    	
    private File taskListFile;
    private TaskList taskList = new TaskList();
    private int nextTaskId;    
    
    public TaskListManager(File file) {
        this.taskListFile = file;
        nextTaskId = 1;
    }    
        
    public TaskList createNewTaskList() {
    	taskList = new TaskList();
        return taskList;
    } 

    public String genUniqueTaskId() {
        return "task-" + nextTaskId++;
    }
    
//    public boolean readTaskList() {
//    	MylarTasklistPlugin.getDefault().getTaskListExternalizer().initExtensions();
//        try { 
//        	if (taskListFile.exists()) {
//        		MylarTasklistPlugin.getDefault().getTaskListExternalizer().readTaskList(taskList, taskListFile);
//        		int maxHandle = taskList.findLargestTaskHandle();
//        		if (maxHandle >= nextTaskId) {
//        			nextTaskId = maxHandle + 1;
//        		}
//        	} else {
//        		MylarTasklistPlugin.getTaskListManager().createNewTaskList();
//        	}
//        	if (TaskListView.getDefault() != null) {
//        		TaskListView.getDefault().getViewer().refresh();
//        	}
//        } catch (Exception e) { 
//        	MylarPlugin.log(e, "Could not read task list");
//        	return false;
//        } 
//		return true;
//    }

//    public void saveTaskList() {
//        try {   
//        	MylarTasklistPlugin.getDefault().getTaskListExternalizer().writeTaskList(taskList, taskListFile);
//            MylarPlugin.getDefault().getPreferenceStore().setValue(MylarTasklistPlugin.TASK_ID, nextTaskId);
//        } catch (Exception e) {
//            MylarPlugin.fail(e, "Could not save task list", true);
//        }
//    } 
    
    public TaskList getTaskList() {
        return taskList;
    }
    
    public void setTaskList(TaskList taskList) {
        this.taskList = taskList;
    }

    public void addRootTask(ITaskListElement task) {
    	taskList.addRootTask(task);
    }
        
    public void deleteTask(ITaskListElement task) {
        taskList.deleteTask(task);
    }
    
    public void setTaskListFile(File f) {
    	if (this.taskListFile.exists()) {
    		this.taskListFile.delete();
    	}
    	this.taskListFile = f;
    }
    
    public ITaskListElement getTaskForHandle(String handle) {
    	return taskList.getTaskForHandle(handle);
    }

//	public String toXmlString() {
//		try {   
//        	return MylarTasklistPlugin.getDefault().getTaskListExternalizer().getTaskListXml(taskList);
//        } catch (Exception e) {
//            MylarPlugin.fail(e, "Could not save task list", true);
//        }
//        return null;
//	}
}
