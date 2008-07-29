package org.aifb.xxplore;

import java.io.File;

import org.aifb.xxplore.action.CreateTaskAction;
import org.aifb.xxplore.action.DeleteTaskAction;
import org.aifb.xxplore.model.TaskListViewContentProvider;
import org.aifb.xxplore.model.TaskListViewLabelProvider;
import org.aifb.xxplore.task.ITaskListElement;
import org.aifb.xxplore.task.TaskEditorInput;
import org.aifb.xxplore.task.TaskList;
import org.aifb.xxplore.task.TaskListManager;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.service.datafiltering.DataFilteringService;
import org.ateam.xxplore.core.service.datafiltering.ITaskPolicyDao;
import org.ateam.xxplore.core.service.datafiltering.TaskPolicyDao;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class TaskListView extends ViewPart {
	public static final String ID = "org.aifb.xxplore.tasklistview";

	private static Logger s_log = Logger.getLogger(TaskListView.class);
	
	private TreeViewer m_taskListViewer;

	private TaskListViewContentProvider m_contentProvider;
	private TaskListViewLabelProvider m_labelProvider;
	ITaskPolicyDao m_taskPolicyDao; 
	DataFilteringService m_filteringService;
	
	private CreateTaskAction m_createTaskAction;
	private static TaskListManager m_taskListManager;
	
	public TaskListView(){
        String path = "c:/tasklistfile";        
        File taskListFile = new File(path);
        if (m_taskListManager == null)
        	m_taskListManager = new TaskListManager(taskListFile);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		m_taskListViewer = new TreeViewer(container, SWT.BORDER);
		m_taskPolicyDao = TaskPolicyDao.getInstance();
		m_filteringService = new DataFilteringService();
		m_contentProvider = new TaskListViewContentProvider(this, m_taskPolicyDao, m_filteringService);

		m_taskListViewer.setContentProvider(m_contentProvider);
		m_labelProvider = new TaskListViewLabelProvider();
		m_taskListViewer.setLabelProvider(m_labelProvider);
		m_taskListViewer.setInput(m_taskListManager.getTaskList());
		m_taskListViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event){
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				String editorId = "org.aifb.xxplore.taskeditor";
				IWorkbenchPage workbenchPage = getViewSite().getPage();
				if (selection.getFirstElement() instanceof ITaskListElement){
					ITaskListElement taskListElement = (ITaskListElement)selection.getFirstElement();
					try {
						workbenchPage.openEditor(new TaskEditorInput(taskListElement,m_contentProvider, true), editorId);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
		fillViewAction();
		fillContextMenu();
	}

	@Override
	public void setFocus() {
		
	}
	
	public void fillViewAction(){
		m_createTaskAction = new CreateTaskAction(this);
		m_createTaskAction.setText("Add Task");
		m_createTaskAction.setToolTipText("Add a new Task");
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBar = bars.getToolBarManager();
		toolBar.add(m_createTaskAction);
	}
	
	public void fillContextMenu(){
		MenuManager menuManager = new MenuManager();
		menuManager.add(new DeleteTaskAction(this));
		Tree tree = m_taskListViewer.getTree();
		Menu menu = menuManager.createContextMenu(tree);
		tree.setMenu(menu);
	}
	
	public TaskListManager getTaskListManager(){
		return m_taskListManager;
	}
	
	public TaskList getTaskList(){
		return m_taskListManager.getTaskList();
	}
	
	public TreeViewer getViewer(){
		return m_taskListViewer;
	}
	
	public TaskListViewContentProvider getContentProvider(){
		return m_contentProvider;
	}
}
