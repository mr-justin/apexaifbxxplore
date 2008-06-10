package org.aifb.xxplore;

import java.io.File;
import java.util.Calendar;

import org.aifb.xxplore.core.service.datafiltering.DataFilteringService;
import org.aifb.xxplore.core.service.datafiltering.ITaskPolicyDao;
import org.aifb.xxplore.model.ImageRegistry;
import org.aifb.xxplore.model.TaskListViewContentProvider;
import org.aifb.xxplore.task.DatePicker;
import org.aifb.xxplore.task.ITaskListElement;
import org.aifb.xxplore.task.TaskEditorInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;

public class TaskEditor extends EditorPart {
	private static final int DEFAULT_ESTIMATED_TIME = 1;
	private static final String DESCRIPTION_OVERVIEW = "Task Info";
	private static final String TaskListView_ID = "org.aifb.xxplore.tasklistview";
	
	private TaskListViewContentProvider m_contentProvider;
	private ITaskPolicyDao m_taskPolicyDao;
	private DataFilteringService m_filteringService;
	
	private DatePicker m_scheduledDate;
	private DatePicker m_dueDate;
	private ITaskListElement m_taskListElement;
	private TaskEditorInput m_editorInput;
	private Composite m_editorComposite;
	private Table m_table;
	private TableViewer m_tableViewer;		
	private Text m_pathText;
	private ScrolledForm m_sform;
	private Text m_name;
    private Text m_description;
    private Text m_notes;
    private Text m_agenten;
    private Spinner m_estimated;    
    private boolean m_isDirty = false;
    private Button m_change;
    private Combo m_combo;
	private String[] m_basePolicy;
    
    
	@Override
	public void doSave(IProgressMonitor monitor) {
		m_taskListElement.getTask().setName(m_name.getText());
		m_taskListElement.getTask().setDescription(m_description.getText());
		setPartName(m_editorInput.getLabel());
		m_taskListElement.getTask().setNotes(m_notes.getText());		
		String path = m_pathText.getText();		
		m_taskListElement.getTask().setPath(path);
		if (m_scheduledDate != null && m_scheduledDate.getDate() != null) {
			m_taskListElement.getTask().setCreationDate(m_scheduledDate.getDate().getTime());
		}
		if (m_dueDate != null && m_dueDate.getData() != null) {
			m_taskListElement.getTask().setEndDate(m_dueDate.getDate().getTime());
		}
		
		refreshTaskListView();
		markDirty(false);
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	private void refreshTaskListView() {
		IViewPart view = getSite().getPage().findView(TaskListView_ID);
		((TaskListView)view).getViewer().refresh();
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		this.m_editorInput = (TaskEditorInput)input;
		this.m_contentProvider = m_editorInput.getContentProvider();
		this.m_taskPolicyDao = m_contentProvider.getTaskPolicyDao();
		this.m_filteringService = m_contentProvider.getDataFilteringService();
		setPartName(m_editorInput.getLabel());
		setTitleToolTip(input.getToolTipText());
	}

	@Override
	public boolean isDirty() {
		return m_isDirty;
	}
	
	private void markDirty(boolean dirty) {
		m_isDirty = dirty;		
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		m_sform = toolkit.createScrolledForm(parent);
		m_sform.setText("New Task");
		m_sform.getBody().setLayout(new GridLayout());
		m_editorComposite = m_sform.getBody();
		
		GridLayout layout = new GridLayout();
		layout.marginBottom = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 5;
		m_editorComposite.setLayout(layout);	
		m_editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContent(m_editorComposite, toolkit);
		m_sform.reflow(true);
		m_sform.setFocus();
	}

	@Override
	public void setFocus() {
		m_sform.setFocus();
	}
	
	private Composite createContent(Composite parent, FormToolkit toolkit) {				
		TaskEditorInput taskEditorInput = (TaskEditorInput)getEditorInput();
		m_taskListElement = taskEditorInput.getTask();
		if (m_taskListElement == null) {
			MessageDialog.openError(parent.getShell(), "No such task", "No task exists with this id");
			return null;
		}		
       
		createTaskInfoSection(parent, toolkit);	
		createNotesSection(parent, toolkit);
		createPlanningSection(parent, toolkit);
		createTimeLensSection(parent, toolkit);
//		createTimePlanningSection(parent, toolkit);
		createResourceSection(parent, toolkit);
		
		return null;
	}
	
	private void createTaskInfoSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR | Section.TWISTIE );
		section.setText(DESCRIPTION_OVERVIEW);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				m_sform.reflow(true);
			}
			public void expansionStateChanged(ExpansionEvent e) {
				m_sform.reflow(true);
			}			
		});
		
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;						
		sectionClient.setLayout(layout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label1 = toolkit.createLabel(sectionClient, "Name:", SWT.NONE);
        label1.setForeground(toolkit.getColors().getColor(FormColors.TITLE));	        
        m_name = toolkit.createText(sectionClient, m_taskListElement.getTask().getName(), SWT.BORDER);
        m_name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label2 = toolkit.createLabel(sectionClient, "Description:", SWT.NONE);
        label2.setForeground(toolkit.getColors().getColor(FormColors.TITLE));	        
        m_description = toolkit.createText(sectionClient, m_taskListElement.getTask().getDescription(), SWT.BORDER);
        m_description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label3 = toolkit.createLabel(sectionClient, "Base Policy:", SWT.NONE);
        label3.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
        m_combo = new Combo(sectionClient, SWT.READ_ONLY);
        m_combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        m_basePolicy = (m_taskPolicyDao.findAllTaskPolicies()).toArray(new String[0]);
//        m_combo.setItems(m_basePolicy);
        if (!m_taskListElement.isDirectlyModifiable()) {
        	m_name.setEnabled(false);
        	m_description.setEnabled(false);
        } else {
        	m_description.addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
    			}			
    		});
        	m_name.addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
    			}			
    		});
    	}
	}	
	
	private void createResourceSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.EXPANDED | ExpandableComposite.TITLE_BAR | Section.TWISTIE);
		section.setText("Resources");
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				m_sform.reflow(true);
			}
			public void expansionStateChanged(ExpansionEvent e) {
				m_sform.reflow(true);
			}
		});

		Composite clientSection = toolkit.createComposite(section);
		section.setClient(clientSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;						
		clientSection.setLayout(layout);
		
        Label l2 = toolkit.createLabel(clientSection, "Task path:");
        l2.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
        m_pathText = toolkit.createText(clientSection, "", SWT.BORDER);
        m_pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        m_pathText.setEditable(false);        
        m_pathText.setEnabled(false);
        
        m_change = toolkit.createButton(clientSection, "Change", SWT.PUSH | SWT.CENTER);
        
		m_change.addSelectionListener(new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
		
				FileDialog dialog = new FileDialog(Display.getDefault()
							.getActiveShell(), SWT.OPEN);
				String[] ext = { "*.xml" };
				dialog.setFilterExtensions(ext);

				String resourceDir = "c:/";
				dialog.setFilterPath(resourceDir);

				String res = dialog.open();
				if (res != null) {
					m_pathText.setText("<Resource_Dir>/" + res);
					markDirty(true);
				}
			}
	  	});
		
	}

	private void createNotesSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent,  Section.TITLE_BAR | Section.EXPANDED);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setText("Notes");
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				m_sform.reflow(true);
			}
			public void expansionStateChanged(ExpansionEvent e) {
				m_sform.reflow(true);
			}			
		});
		Composite sectionClient = toolkit.createComposite(section);			
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
//		TableWrapLayout layout = new TableWrapLayout();
		sectionClient.setLayout(layout);
		m_notes = toolkit.createText(sectionClient, m_taskListElement.getTask().getNotes(), SWT.WRAP | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.heightHint = 80;
		ld.widthHint = 500;
//		TableWrapData ld = new TableWrapData(TableWrapData.FILL_GRAB);
//		ld.heightHint = 80;
		m_notes.setLayoutData(ld);
		m_notes.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}			
		});
	}

	private void createTimePlanningSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent,  Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE );
		section.setText("Time Planning");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
		layout.numColumns = 11;
		layout.makeColumnsEqualWidth = false;
		sectionClient.setLayout(layout);
		GridData clientDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		sectionClient.setLayoutData(clientDataLayout);

		Label scheduledLabel = toolkit.createLabel(sectionClient, "Scheduled for:");
		scheduledLabel.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_scheduledDate = new DatePicker(sectionClient, SWT.NONE, DatePicker.LABEL_CHOOSE);
		m_scheduledDate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		m_scheduledDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		Calendar newTaskSchedule = Calendar.getInstance();
		m_scheduledDate.setDate(newTaskSchedule);
		
		ImageHyperlink clearScheduled = toolkit.createImageHyperlink(sectionClient, SWT.NONE);
		clearScheduled.setImage(new ImageRegistry().getRemoveImage());
		clearScheduled.setToolTipText("Clear");
		clearScheduled.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				m_scheduledDate.setDate(null);
			}
		});

		Label dummy1 = toolkit.createLabel(sectionClient, "");
		GridData dummyLabelDataLayout1 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout1.horizontalSpan = 1;
		dummyLabelDataLayout1.widthHint = 10;
		dummy1.setLayoutData(dummyLabelDataLayout1);
		
		Label dueLabel = toolkit.createLabel(sectionClient, "Due:");
		dueLabel.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_dueDate = new DatePicker(sectionClient, SWT.NONE, DatePicker.LABEL_CHOOSE);
		m_dueDate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		m_dueDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		m_dueDate.setDate(newTaskSchedule);
		
		ImageHyperlink clearDue = toolkit.createImageHyperlink(sectionClient, SWT.NONE);
		clearDue.setImage(new ImageRegistry().getRemoveImage());
		clearDue.setToolTipText("Clear");
		clearDue.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				m_dueDate.setDate(null);
			}
		});
		
		Label dummy2 = toolkit.createLabel(sectionClient, "");
		GridData dummyLabelDataLayout2 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout2.horizontalSpan = 1;
		dummyLabelDataLayout2.widthHint = 10;
		dummy2.setLayoutData(dummyLabelDataLayout2);
		
		Label estimatedLabel = toolkit.createLabel(sectionClient, "Estimated hours:");
		estimatedLabel.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_estimated = new Spinner(sectionClient, SWT.NONE);
		m_estimated.setDigits(0);
		m_estimated.setMaximum(100);
		m_estimated.setMinimum(0);
		m_estimated.setIncrement(1);
		m_estimated.setSelection(DEFAULT_ESTIMATED_TIME);
		m_estimated.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridData estimatedDataLayout = new GridData();
		estimatedDataLayout.widthHint = 50;
		m_estimated.setLayoutData(estimatedDataLayout);
		
		ImageHyperlink clearEstimated = toolkit.createImageHyperlink(sectionClient, SWT.NONE);
		clearEstimated.setImage(new ImageRegistry().getRemoveImage());
		clearEstimated.setToolTipText("Clear");
		clearEstimated.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				m_estimated.setSelection(0);
			}
		});

		toolkit.paintBordersFor(sectionClient);
	}

	private void createTimeLensSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.EXPANDED | Section.TWISTIE | Section.TITLE_BAR);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setText("Time Lens");
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				m_sform.reflow(true);
			}
			public void expansionStateChanged(ExpansionEvent e) {
				m_sform.reflow(true);
			}			
		});
		Composite sectionClient = toolkit.createComposite(section);			
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = false;		
		sectionClient.setLayout(layout);
		
		Label StartLabel = toolkit.createLabel(sectionClient, "Start time:");		
		StartLabel.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		GridData gd = new GridData();
		gd.widthHint = 110;
		gd.horizontalAlignment = GridData.FILL;
		Text startTime = toolkit.createText(sectionClient,"", SWT.BORDER);
        startTime.setLayoutData(gd);
		startTime.setEditable(false);
        startTime.setEnabled(false);
        
		Label dummy = toolkit.createLabel(sectionClient, "");
		GridData dummyLabelDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout.horizontalSpan = 1;
		dummyLabelDataLayout.widthHint = 50;
		dummy.setLayoutData(dummyLabelDataLayout);
        
        Label endLabel = toolkit.createLabel(sectionClient, "End time:");		
		endLabel.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		Text endTime = toolkit.createText(sectionClient,"", SWT.BORDER);
		endTime.setLayoutData(gd);
		endTime.setEditable(false);
        endTime.setEnabled(false);
		
	}

	private void createPlanningSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR);
		section.setText("Planning");
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				m_sform.reflow(true);
			}
			public void expansionStateChanged(ExpansionEvent e) {
				m_sform.reflow(true);
			}			
		});
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		sectionClient.setLayout(layout);
		
		createProviderPart(sectionClient, toolkit);
		createAgentPart(sectionClient, toolkit);
	}
	
	protected void createProviderPart(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.EXPANDED);
		section.setText("Information Providers"); 
		section.marginWidth = 5;
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.createCompositeSeparator(section);
		Composite clientSection = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		layout.makeColumnsEqualWidth = false;
		clientSection.setLayout(layout);
		section.setClient(clientSection);
		m_table = toolkit.createTable(clientSection, SWT.NULL);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.heightHint = 200;
		ld.widthHint = 200;
		ld.verticalSpan = 6;
		m_table.setLayoutData(ld);
		m_tableViewer = new TableViewer(m_table);
		m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
//				fireSelectionChanged(spart, event.getSelection());
			}
		});
		m_tableViewer.setContentProvider(new MasterContentProvider());
		m_tableViewer.setLabelProvider(new MasterLabelProvider());
		toolkit.paintBordersFor(clientSection);
		Button add = toolkit.createButton(clientSection, "Add", SWT.PUSH); 
		add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		add.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}	
		});
		Button remove = toolkit.createButton(clientSection, "Remove", SWT.PUSH);
		remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		remove.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}	
		});
		Button up = toolkit.createButton(clientSection, "Up", SWT.PUSH);
		up.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		up.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}	
		});
		Button down = toolkit.createButton(clientSection, "Down", SWT.PUSH);
		down.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		down.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}	
		});
		Button properties = toolkit.createButton(clientSection, "Properties", SWT.PUSH);
		properties.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		properties.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}	
		});
	}
	
	private void createAgentPart (Composite parent, FormToolkit toolkit){
		Section section = toolkit.createSection(parent, Section.EXPANDED);
		section.setText("Agenten"); 
		section.marginWidth = 5;
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		section.setClient(client);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		m_agenten = toolkit.createText(client, "", SWT.WRAP | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.heightHint = 200;
		ld.widthHint = 200;
		m_agenten.setLayoutData(ld);
	}
	
	class MasterContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class MasterLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return obj.toString();
		}
		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}
	
}
