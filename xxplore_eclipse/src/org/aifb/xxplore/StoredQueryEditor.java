package org.aifb.xxplore;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import org.aifb.xxplore.core.service.datafiltering.DataFilteringService;
import org.aifb.xxplore.core.service.datafiltering.ITaskPolicyDao;
import org.aifb.xxplore.model.ImageRegistry;
import org.aifb.xxplore.model.StoredQueryViewContentProvider;
import org.aifb.xxplore.storedquery.DatePicker;
import org.aifb.xxplore.storedquery.IQueryMetaFilter;
import org.aifb.xxplore.storedquery.IStoredQueryListElement;
import org.aifb.xxplore.storedquery.Prefix;
import org.aifb.xxplore.storedquery.StoredQueryEditorInput;
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
import org.eclipse.swt.widgets.MessageBox;
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
import org.eclipse.ui.part.EditorPart;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IEntityDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;

public class StoredQueryEditor extends EditorPart {
	private static final int DEFAULT_ESTIMATED_TIME = 1;
	private static final String DESCRIPTION_OVERVIEW = "Query Info";
	private static final String StoredQueryView_ID = "org.aifb.xxplore.storedqueryview";
	
	private StoredQueryViewContentProvider m_contentProvider;
	
/*
Query Info
	Name: [editiable field]
    Description: [editiable field]
    
Notes: [editiable field]  

Ontologies
	Ontology: [editiable field] 
	Prefix: [editable field] 
	[add prefix button] бн but click on add, a further line should be created

Query Predicates
	Subject: [editable field] 
	Predicate: [editable field] 
	Object: [editable field] 
	[add predicate button]

Select Variables
    Variable1: [drop down box showing possible variables] 
	[add variable button]
*/
	
	private DatePicker m_scheduledDate;
	private DatePicker m_dueDate;
	private Spinner m_estimated;   
	
	private FormToolkit m_toolkit;
	private Composite m_editorComposite;
	private ScrolledForm m_sform;
	private IStoredQueryListElement m_queryListElement;
	private StoredQueryEditorInput m_editorInput;
	
	private Composite m_ontologySection;
	private Stack<Text[]> m_prefixTexts = new Stack<Text[]>();
    private Stack<Label[]> m_prefixLabels = new Stack<Label[]>();
    private Button m_addPrefixButton;
    private Button m_deletePrefixButton;
    
    private Composite m_predicateSection;
	private List<Text[]> m_predicateTexts = new ArrayList<Text[]>();
	private List<Label[]> m_predicateLabels = new ArrayList<Label[]>();
	private List<Button> m_predicateDelButtons = new ArrayList<Button>();
	private Button m_addPredicateButton;
	
	private Composite m_varSection;
	private Combo m_combo;
	private Button m_addVariableButton;
	private Button m_deleteVariableButton;
	private Text m_varText;
	
	private Composite m_metaSection;
	private Text m_metaConfidence;
	private Text m_metaDate;
	private org.eclipse.swt.widgets.List m_metaAgents;
	private org.eclipse.swt.widgets.List m_metaSources;
	private Button m_metaRequireProvenancesButton;
		
	private Text m_name;
    private Text m_description;
    private Text m_notes;
  
    private boolean m_isDirty = false;
    
    private Text m_pathText;
    private Button m_change;
	private Text m_metaAgentAddText;
	private Text m_metaSourceAddText;
    
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		this.m_editorInput = (StoredQueryEditorInput)input;
		this.m_contentProvider = m_editorInput.getContentProvider();
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
		
		m_toolkit = new FormToolkit(parent.getDisplay());
		m_sform = m_toolkit.createScrolledForm(parent);
		m_sform.setText("New Query");
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
		createContent(m_editorComposite, m_toolkit);
		m_sform.reflow(true);
		m_sform.setFocus();
	}

	@Override
	public void setFocus() {
		m_sform.setFocus();
	}
	
	private void createContent(Composite parent, FormToolkit toolkit) {				
		StoredQueryEditorInput queryEditorInput = (StoredQueryEditorInput)getEditorInput();
		m_queryListElement = queryEditorInput.getQuery();
		if (m_queryListElement == null) {
			MessageDialog.openError(parent.getShell(), "No such query", "No query exists with this id");
		}		
       
		createQueryInfoSection(parent);	
		createNotesSection(parent);
		createOntologySection(parent);
		createPredicateSection(parent);
		createVariableSection(parent);
		createMetaFilterSection(parent);
		
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		setPartName(m_editorInput.getLabel());
		m_queryListElement.getQuery().setName(m_name.getText().trim());
		m_queryListElement.getQuery().setDescription(m_description.getText().trim());
		m_queryListElement.getQuery().setNotes(m_notes.getText().trim());	
		
		Stack<Prefix> prefixes = m_queryListElement.getQuery().getPrefixes();
		prefixes.clear();
		for (Text[] texts : m_prefixTexts) {
			prefixes.add(new Prefix(texts[0].getText(), texts[1].getText()));
		}
		
		Stack<String[]> predicates = m_queryListElement.getQuery().getPredicates();
		predicates.clear();
		Set<String> variables = m_queryListElement.getQuery().getVariables();
		variables.clear();
		Set<String> selectedVars = m_queryListElement.getQuery().getSelectedVariables();
		for(Text[] predicate : m_predicateTexts){
			String[] str = new String[3];
			str[0] = predicate[0].getText().trim();
			if(str[0].startsWith("?")) {
				variables.add(str[0]);
			}	
			str[1] = predicate[1].getText().trim();
			str[2] = predicate[2].getText().trim();
			if(str[2].startsWith("?")) {
				variables.add(str[2]);
			}	
			predicates.push(str);
		}
		m_combo.setItems(variables.toArray(new String[0]));
		selectedVars.retainAll(variables);
		m_varText.setText(selectedVars.toString());
		
		if (selectedVars.isEmpty()) {
			monitor.setCanceled(true);
			MessageBox mb = new MessageBox(m_editorComposite.getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Query Editor");
			mb.setMessage("Please select variables before saving the query.");
			mb.open();
			return;
		}
		
		m_queryListElement.getQuery().getMetaFilter().setRequireProvenances(m_metaRequireProvenancesButton.getSelection());
			
		
		IQueryMetaFilter meta = m_queryListElement.getQuery().getMetaFilter();
		meta.getAgents().clear();
		meta.getSources().clear();
		if (m_metaConfidence.getText() != null && !m_metaConfidence.getText().equals(""))
			meta.setConfidenceDegree(Double.valueOf(m_metaConfidence.getText()));
		if (m_metaDate.getText() != null && !m_metaDate.getText().equals("")) {
			try {
				meta.setDate(DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse(m_metaDate.getText()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			for (String agent : m_metaAgents.getItems()) {
				meta.getAgents().add(iDao.findByUri(agent));
			}
			
			for (String source : m_metaSources.getItems()) {
				SesameOntology o = (SesameOntology)PersistenceUtil.getSessionFactory().getCurrentSession().getOntology();
				System.out.println(o.findResourceByURI(source));
				meta.getSources().add(Ses2AK.getNamedIndividual(o.findResourceByURI(source), o));
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
//		String path = m_pathText.getText();		
//		m_queryListElement.getQuery().setPath(path);
//		if (m_scheduledDate != null && m_scheduledDate.getDate() != null) {
//			m_queryListElement.getQuery().setCreationDate(m_scheduledDate.getDate().getTime());
//		}
//		if (m_dueDate != null && m_dueDate.getData() != null) {
//			m_queryListElement.getQuery().setEndDate(m_dueDate.getDate().getTime());
//		}
		m_sform.reflow(true);
		refreshStoredQueryView();
		markDirty(false);
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	private void refreshStoredQueryView() {
		IViewPart view = getSite().getPage().findView(StoredQueryView_ID);
		((StoredQueryView)view).getViewer().refresh();
	}

	@Override
	public void doSaveAs() {

	}
	
	private void updatePossibleVariables() {
		Stack<String> vars = new Stack<String>();
		
		for (Text[] texts : m_predicateTexts) {
			String subject = texts[0].getText();
			String object = texts[2].getText();
			
			if (subject.startsWith("?"))
				vars.push(subject);
			
			if (object.startsWith("?"))
				vars.push(object);
		}
		
		m_combo.setItems(vars.toArray(new String[] {}));
	}

	private void createVariableSection(Composite parent){
		Section section = m_toolkit.createSection(parent, Section.EXPANDED | ExpandableComposite.TITLE_BAR | Section.TWISTIE);
		section.setText("Select Variables:");
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

		m_varSection = m_toolkit.createComposite(section);
		section.setClient(m_varSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;						
		m_varSection.setLayout(layout);
		
        Label l = m_toolkit.createLabel(m_varSection, "Possible Variables: ");
        l.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
        m_combo = new Combo(m_varSection, SWT.READ_ONLY);
        GridData gd = new GridData();
        gd.widthHint = 100;
        m_combo.setLayoutData(gd);
        m_combo.setItems(m_queryListElement.getQuery().getVariables().toArray(new String[0]));
        
        m_addVariableButton = m_toolkit.createButton(m_varSection, "Add a Variable", SWT.PUSH | SWT.CENTER);
        GridData gd1 = new GridData();
        gd1.horizontalIndent = 30;
        m_addVariableButton.setLayoutData(gd1);
        m_addVariableButton.addSelectionListener(new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str = m_combo.getText();
				if(str != null && str.length()!= 0){
					m_queryListElement.getQuery().getSelectedVariables().add(str);
					m_varText.setText(m_queryListElement.getQuery().getSelectedVariables().toString());
					m_sform.reflow(true);
					markDirty(true);
					firePropertyChange(IEditorPart.PROP_DIRTY);
				}	
			}
	  	});
        
        m_deleteVariableButton = m_toolkit.createButton(m_varSection, "Delete a Variable", SWT.PUSH | SWT.CENTER);
        m_deleteVariableButton.addSelectionListener(new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str = m_combo.getText();
				if(str != null && str.length()!= 0){
					m_queryListElement.getQuery().getSelectedVariables().remove(str);
					m_varText.setText(m_queryListElement.getQuery().getSelectedVariables().toString());
					m_sform.reflow(true);
					markDirty(true);
					firePropertyChange(IEditorPart.PROP_DIRTY);
				}	
			}
	  	});
        Label l1 = m_toolkit.createLabel(m_varSection, "Select Variables: ");
        l1.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
        m_varText = m_toolkit.createText(m_varSection, m_queryListElement.getQuery().getSelectedVariables().toString(),SWT.READ_ONLY);
        GridData gd2 = new GridData(GridData.FILL_HORIZONTAL );
        gd2.horizontalSpan = 3;
        m_varText.setLayoutData(gd2);
        m_varText.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
	}
	
	private void createPredicateSection(Composite parent){
		Section section = m_toolkit.createSection(parent, Section.EXPANDED | ExpandableComposite.TITLE_BAR | Section.TWISTIE);
		section.setText("Query Predicates");
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
		
		Composite sectionClient = m_toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		sectionClient.setLayout(layout);
		
		createPredicateFieldArea(sectionClient);
		createPredicateButtonArea(sectionClient);

	}
	
	private void createPredicateButtonArea(Composite parent) {
		Section section = m_toolkit.createSection(parent, Section.NO_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite buttonSection = m_toolkit.createComposite(section);
		section.setClient(buttonSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonSection.setLayout(layout);
		m_addPredicateButton = m_toolkit.createButton(buttonSection, "Add a Predicate",SWT.PUSH | SWT.CENTER);
		m_addPredicateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Label[] labels = new Label[3];
	        	Text[] texts = new Text[3];
	         	labels[0] = m_toolkit.createLabel(m_predicateSection, "Subject: ");
	        	labels[0].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
	        	texts[0] = m_toolkit.createText(m_predicateSection, "", SWT.BORDER);
	        	texts[0].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        	texts[0].addModifyListener(new ModifyListener() {
	        		public void modifyText(ModifyEvent e) {
	        			markDirty(true);
	        			firePropertyChange(IEditorPart.PROP_DIRTY);
	        		}			
	        	});
	        	labels[1] = m_toolkit.createLabel(m_predicateSection, "Predicate: ");
	        	labels[1].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
	        	texts[1] = m_toolkit.createText(m_predicateSection, "", SWT.BORDER);
	        	texts[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        	texts[1].addModifyListener(new ModifyListener() {
	        		public void modifyText(ModifyEvent e) {
	        			markDirty(true);
	        			firePropertyChange(IEditorPart.PROP_DIRTY);
	        		}			
	        	});
	        	labels[2] = m_toolkit.createLabel(m_predicateSection, "Object: ");
	        	labels[2].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
	        	texts[2] = m_toolkit.createText(m_predicateSection, "", SWT.BORDER);
	        	texts[2].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        	texts[2].addModifyListener(new ModifyListener() {
	        		public void modifyText(ModifyEvent e) {
	        			markDirty(true);
	        			firePropertyChange(IEditorPart.PROP_DIRTY);
	        		}			
	        	});
        		Button delButton = m_toolkit.createButton(m_predicateSection, "Delete", SWT.PUSH | SWT.CENTER);
        		delButton.setData(labels[0]);
        		delButton.addSelectionListener(new SelectionAdapter() {
        			@Override
        			public void widgetSelected(SelectionEvent e) {
        				Label label = (Label)((Button)e.getSource()).getData();
        				
        				int i = 0;
        				for (Label[] labels : m_predicateLabels) {
        					if (labels[0].equals(label))
        						break;
        					i++;
        				}
        				
        				Label[] labels = m_predicateLabels.remove(i);
        				for(Label l : labels){
        					l.dispose();
        				}
        				Text[] texts = m_predicateTexts.remove(i);
        				for(Text text : texts){
        					text.dispose();
        				}
        				m_predicateDelButtons.remove(i);
        				((Button)e.getSource()).dispose();
        				m_sform.reflow(true);
        				markDirty(true);
        				firePropertyChange(IEditorPart.PROP_DIRTY);
        			}
        		});
	        	m_predicateLabels.add(labels);
	        	m_predicateTexts.add(texts);
	        	m_predicateDelButtons.add(delButton);
				m_sform.reflow(true);
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		});

	}

	private void createPredicateFieldArea(Composite parent) {
		Section section = m_toolkit.createSection(parent, Section.NO_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		m_predicateSection = m_toolkit.createComposite(section);
		section.setClient(m_predicateSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 7;						
		m_predicateSection.setLayout(layout);
       
		Stack<String[]> predicates = m_queryListElement.getQuery().getPredicates();
		int i = 0;
        if(m_editorInput.isNewQuery() || predicates.size() == 0){
        	Label[] labels = new Label[3];
        	Text[] texts = new Text[3];
         	labels[0] = m_toolkit.createLabel(m_predicateSection, "Subject: ");
        	labels[0].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
        	texts[0] = m_toolkit.createText(m_predicateSection, "", SWT.BORDER);
        	texts[0].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	texts[0].addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			updatePossibleVariables();
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
        		}			
        	});
        	labels[1] = m_toolkit.createLabel(m_predicateSection, "Predicate: ");
        	labels[1].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
        	texts[1] = m_toolkit.createText(m_predicateSection, "", SWT.BORDER);
        	texts[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	texts[1].addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
        		}			
        	});
        	labels[2] = m_toolkit.createLabel(m_predicateSection, "Object: ");
        	labels[2].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
        	texts[2] = m_toolkit.createText(m_predicateSection, "", SWT.BORDER);
        	texts[2].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	texts[2].addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			updatePossibleVariables();
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
        		}			
        	});
    		Button delButton = m_toolkit.createButton(m_predicateSection, "Delete", SWT.PUSH | SWT.CENTER);
    		delButton.setData(labels[0]);
    		delButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				Label label = (Label)((Button)e.getSource()).getData();
    				
    				int i = 0;
    				for (Label[] labels : m_predicateLabels) {
    					if (labels[0].equals(label))
    						break;
    					i++;
    				}
    				
    				Label[] labels = m_predicateLabels.remove(i);
    				for(Label l : labels){
    					l.dispose();
    				}
    				Text[] texts = m_predicateTexts.remove(i);
    				for(Text text : texts){
    					text.dispose();
    				}
    				m_predicateDelButtons.remove(i);
    				((Button)e.getSource()).dispose();
    				m_sform.reflow(true);
    				markDirty(true);
    				firePropertyChange(IEditorPart.PROP_DIRTY);
    			}
    		});
    		
        	m_predicateLabels.add(labels);
        	m_predicateTexts.add(texts);
        	m_predicateDelButtons.add(delButton);
        	i++;
        }
        else {
        	for(String[] predicate : predicates) {
        		Label[] labels = new Label[3];
            	Text[] texts = new Text[3];
             	labels[0] = m_toolkit.createLabel(m_predicateSection, "Subject: ");
            	labels[0].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
            	texts[0] = m_toolkit.createText(m_predicateSection, predicate[0], SWT.BORDER);
            	texts[0].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            	texts[0].addModifyListener(new ModifyListener() {
            		public void modifyText(ModifyEvent e) {
            			updatePossibleVariables();
            			markDirty(true);
            			firePropertyChange(IEditorPart.PROP_DIRTY);
            		}			
            	});
            	labels[1] = m_toolkit.createLabel(m_predicateSection, "Predicate: ");
            	labels[1].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
            	texts[1] = m_toolkit.createText(m_predicateSection, predicate[1], SWT.BORDER);
            	texts[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            	texts[1].addModifyListener(new ModifyListener() {
            		public void modifyText(ModifyEvent e) {
            			markDirty(true);
            			firePropertyChange(IEditorPart.PROP_DIRTY);
            		}			
            	});
            	labels[2] = m_toolkit.createLabel(m_predicateSection, "Object: ");
            	labels[2].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
            	texts[2] = m_toolkit.createText(m_predicateSection, predicate[2], SWT.BORDER);
            	texts[2].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            	texts[2].addModifyListener(new ModifyListener() {
            		public void modifyText(ModifyEvent e) {
            			updatePossibleVariables();
            			markDirty(true);
            			firePropertyChange(IEditorPart.PROP_DIRTY);
            		}			
            	});

        		Button delButton = m_toolkit.createButton(m_predicateSection, "Delete", SWT.PUSH | SWT.CENTER);
        		delButton.setData(labels[0]);
        		delButton.addSelectionListener(new SelectionAdapter() {
        			@Override
        			public void widgetSelected(SelectionEvent e) {
        				Label label = (Label)((Button)e.getSource()).getData();
        				
        				int i = 0;
        				for (Label[] labels : m_predicateLabels) {
        					if (labels[0].equals(label))
        						break;
        					i++;
        				}
        				
        				Label[] labels = m_predicateLabels.remove(i);
        				for(Label l : labels){
        					l.dispose();
        				}
        				Text[] texts = m_predicateTexts.remove(i);
        				for(Text text : texts){
        					text.dispose();
        				}
        				m_predicateDelButtons.remove(i);
        				((Button)e.getSource()).dispose();
        				m_sform.reflow(true);
        				markDirty(true);
        				firePropertyChange(IEditorPart.PROP_DIRTY);
        			}
        		});
        		
        		m_predicateLabels.add(labels);
            	m_predicateTexts.add(texts);
            	m_predicateDelButtons.add(delButton);
            	i++;
        	}
        }
	}

	private void createOntologySection(Composite parent){
		Section section = m_toolkit.createSection(parent, Section.EXPANDED | ExpandableComposite.TITLE_BAR | Section.TWISTIE);
		section.setText("Ontology prefixes");
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
		
		Composite sectionClient = m_toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		sectionClient.setLayout(layout);
		
		createOntologyFieldArea(sectionClient);
		createOntologyButtonArea(sectionClient);
	}
	
	private void createOntologyFieldArea(Composite parent) {
		Section section = m_toolkit.createSection(parent, Section.NO_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		m_ontologySection = m_toolkit.createComposite(section);
		section.setClient(m_ontologySection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;						
		m_ontologySection.setLayout(layout);
       
        Stack<Prefix> prefixes = m_queryListElement.getQuery().getPrefixes();
        if(m_editorInput.isNewQuery() || prefixes.size() == 0){
        	prefixes = new Stack<Prefix>();
        	prefixes.add(new Prefix("", ""));
        }
        
    	for(Prefix prefix : prefixes) {
        	addPrefixEditRow(prefix);
        }
	}

	private void addPrefixEditRow(Prefix prefix) {
		Label[] labels = new Label[2];
		Text[] texts = new Text[2];
		
		labels[0] = m_toolkit.createLabel(m_ontologySection, "Prefix: ");
		labels[0].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
		texts[0] = m_toolkit.createText(m_ontologySection, prefix.getPrefix(), SWT.BORDER);
		texts[0].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		texts[0].addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}			
		});
		
		labels[1] = m_toolkit.createLabel(m_ontologySection, "Ontology URI: ");
		labels[1].setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
		texts[1] = m_toolkit.createText(m_ontologySection, prefix.getOntology(), SWT.BORDER);
		texts[1].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		texts[1].addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}			
		});

		m_prefixLabels.push(labels);
		m_prefixTexts.push(texts);
	}
	
	private void createOntologyButtonArea (Composite parent){
		Section section = m_toolkit.createSection(parent, Section.NO_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite buttonSection = m_toolkit.createComposite(section);
		section.setClient(buttonSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonSection.setLayout(layout);
		m_addPrefixButton = m_toolkit.createButton(buttonSection, "Add a Prefix",SWT.PUSH | SWT.CENTER);
		m_addPrefixButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPrefixEditRow(new Prefix("", ""));
				m_sform.reflow(true);
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		});

		m_deletePrefixButton = m_toolkit.createButton(buttonSection, "Delete a Prefix",SWT.PUSH | SWT.CENTER);
		m_deletePrefixButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Label[] labels = m_prefixLabels.pop();
				for (Label l : labels)
					l.dispose();
				Text[] texts = m_prefixTexts.pop();
				for (Text t : texts)
					t.dispose();
				m_sform.reflow(true);
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		});
		
	}
	
	private void createQueryInfoSection(Composite parent) {
		Section section = m_toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR | Section.TWISTIE );
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
		
		Composite sectionClient = m_toolkit.createComposite(section);
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;						
		sectionClient.setLayout(layout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label1 = m_toolkit.createLabel(sectionClient, "Name:", SWT.NONE);
        label1.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));	        
        m_name = m_toolkit.createText(sectionClient, m_queryListElement.getQuery().getName(), SWT.BORDER);
        m_name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label2 = m_toolkit.createLabel(sectionClient, "Description:", SWT.NONE);
        label2.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));	        
        m_description = m_toolkit.createText(sectionClient, m_queryListElement.getQuery().getDescription(), SWT.BORDER);
        m_description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (!m_queryListElement.isDirectlyModifiable()) {
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
	
	private void createResourceSection(Composite parent) {
		Section section = m_toolkit.createSection(parent, Section.TWISTIE);
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

		Composite clientSection = m_toolkit.createComposite(section);
		section.setClient(clientSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;						
		clientSection.setLayout(layout);
		
        Label l2 = m_toolkit.createLabel(clientSection, "Task path:");
        l2.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
        m_pathText = m_toolkit.createText(clientSection, "", SWT.BORDER);
        m_pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        m_pathText.setEditable(false);        
        m_pathText.setEnabled(false);
        
        m_change = m_toolkit.createButton(clientSection, "Change", SWT.PUSH | SWT.CENTER);
        
		m_change.addSelectionListener(new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
		
				FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
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

	private void createNotesSection(Composite parent) {
		Section section = m_toolkit.createSection(parent,  Section.TITLE_BAR | Section.EXPANDED);
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
		Composite sectionClient = m_toolkit.createComposite(section);			
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
//		TableWrapLayout layout = new TableWrapLayout();
		sectionClient.setLayout(layout);
		m_notes = m_toolkit.createText(sectionClient, m_queryListElement.getQuery().getNotes(), SWT.WRAP | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
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
	
	private void createMetaFilterSection(Composite parent) {
		Double degree = m_queryListElement.getQuery().getMetaFilter().getConfidenceDegree();
		String degreeText = degree == null ? "" : degree.toString();
		
		Date date = m_queryListElement.getQuery().getMetaFilter().getDate();
		String dateText = date == null ? "" : date.toLocaleString();
		
		Section section = m_toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR | Section.TWISTIE );
		section.setText("Meta filter");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				m_sform.reflow(true);
			}
			public void expansionStateChanged(ExpansionEvent e) {
				m_sform.reflow(true);
			}			
		});
		
		Composite sectionClient = m_toolkit.createComposite(section);
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;						
		sectionClient.setLayout(layout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		m_metaRequireProvenancesButton = m_toolkit.createButton(sectionClient, "Return only triples with provenances attached", SWT.CHECK);
		m_metaRequireProvenancesButton.setLayoutData(new GridData(0, 0, true, false, 4, 1));
		m_metaRequireProvenancesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				markDirty(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		});
		
		// confidence degree
		Label label1 = m_toolkit.createLabel(sectionClient, "Confidence degree:", SWT.NONE);
        label1.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));	        
        m_metaConfidence = m_toolkit.createText(sectionClient, degreeText, SWT.BORDER);
        m_metaConfidence.setLayoutData(new GridData(SWT.FILL, 0, true, false, 3, 1));
        m_metaConfidence.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			m_metaRequireProvenancesButton.setSelection(true);
    			markDirty(true);
    			firePropertyChange(IEditorPart.PROP_DIRTY);
    		}			
    	});
        
        // creation date
        Label label2 = m_toolkit.createLabel(sectionClient, "Creation date:", SWT.NONE);
        label2.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));	        
        m_metaDate = m_toolkit.createText(sectionClient, dateText, SWT.BORDER);
        m_metaDate.setLayoutData(new GridData(SWT.FILL, 0, true, false, 3, 1));
        m_metaDate.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			m_metaRequireProvenancesButton.setSelection(true);
    			markDirty(true);
    			firePropertyChange(IEditorPart.PROP_DIRTY);
    		}			
    	});
        
        // agents
        Label label3 = m_toolkit.createLabel(sectionClient, "Agents");
        label3.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));

		m_metaAgents = new org.eclipse.swt.widgets.List(sectionClient, SWT.BORDER);
        m_metaAgents.setLayoutData(new GridData(SWT.FILL, 0, true, true, 1, 2));
        
        m_metaAgentAddText = m_toolkit.createText(sectionClient, "", SWT.BORDER);
		m_metaAgentAddText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_metaAgentAddText.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			m_metaRequireProvenancesButton.setSelection(true);
    			markDirty(true);
    			firePropertyChange(IEditorPart.PROP_DIRTY);
    		}			
    	});
        
        Button b1 = m_toolkit.createButton(sectionClient, "Add agent", SWT.PUSH | SWT.CENTER);
        b1.setLayoutData(new GridData(0, 0, false, false, 1, 1));
        b1.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if (m_metaAgentAddText.getText() != null && !m_metaAgentAddText.getText().equals("")) {
        			m_metaAgents.add(m_metaAgentAddText.getText());
    				m_sform.reflow(true);
    				markDirty(true);
    				firePropertyChange(IEditorPart.PROP_DIRTY);
        		}
        	}
        });
        
        Button b2 = m_toolkit.createButton(sectionClient, "Delete agent", SWT.PUSH | SWT.CENTER);
        b2.setLayoutData(new GridData(0, 0, false, false, 2, 1));
        b2.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if (m_metaAgents.getSelectionIndex() != -1) {
        			m_metaAgents.remove(m_metaAgents.getSelectionIndex());
    				m_sform.reflow(true);
    				markDirty(true);
    				firePropertyChange(IEditorPart.PROP_DIRTY);
        		}
        	}
        });
        
        // sources
        Label label4 = m_toolkit.createLabel(sectionClient, "Sources");
        label4.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));	        

        m_metaSources = new org.eclipse.swt.widgets.List(sectionClient, SWT.BORDER | SWT.V_SCROLL);
        m_metaSources.setLayoutData(new GridData(SWT.FILL, 0, true, true, 1, 2));
        
        m_metaSourceAddText = m_toolkit.createText(sectionClient, "", SWT.BORDER);
		m_metaSourceAddText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_metaSourceAddText.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			m_metaRequireProvenancesButton.setSelection(true);
    			markDirty(true);
    			firePropertyChange(IEditorPart.PROP_DIRTY);
    		}			
    	});
        
        Button b3 = m_toolkit.createButton(sectionClient, "Add source", SWT.PUSH | SWT.CENTER);
        b3.setLayoutData(new GridData(0, 0, false, false, 1, 1));
        b3.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if (m_metaSourceAddText.getText() != null && !m_metaSourceAddText.getText().equals("")) {
        			m_metaSources.add(m_metaSourceAddText.getText());
    				m_sform.reflow(true);
    				markDirty(true);
    				firePropertyChange(IEditorPart.PROP_DIRTY);
        		}
        	}
        });
        
        Button b4 = m_toolkit.createButton(sectionClient, "Delete source", SWT.PUSH | SWT.CENTER);
        b4.setLayoutData(new GridData(0, 0, false, false, 2, 1));
        b4.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if (m_metaSources.getSelectionIndex() != -1) {
        			m_metaSources.remove(m_metaSources.getSelectionIndex());
    				m_sform.reflow(true);
    				markDirty(true);
    				firePropertyChange(IEditorPart.PROP_DIRTY);
        		}
        	}
        });
        
        if (!m_queryListElement.isDirectlyModifiable()) {
        	m_metaConfidence.setEnabled(false);
        	m_metaDate.setEnabled(false);
        	m_metaAgents.setEnabled(false);
        	m_metaSources.setEnabled(false);
        } else {
        	m_metaConfidence.addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
    			}			
    		});
        	m_metaDate.addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			markDirty(true);
        			firePropertyChange(IEditorPart.PROP_DIRTY);
    			}			
    		});
    	}
	}

	private void createTimePlanningSection(Composite parent) {
		Section section = m_toolkit.createSection(parent,  Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE );
		section.setText("Time Planning");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite sectionClient = m_toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
		layout.numColumns = 11;
		layout.makeColumnsEqualWidth = false;
		sectionClient.setLayout(layout);
		GridData clientDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		sectionClient.setLayoutData(clientDataLayout);

		Label scheduledLabel = m_toolkit.createLabel(sectionClient, "Scheduled for:");
		scheduledLabel.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
		m_scheduledDate = new DatePicker(sectionClient, SWT.NONE, DatePicker.LABEL_CHOOSE);
		m_scheduledDate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		m_scheduledDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		Calendar newTaskSchedule = Calendar.getInstance();
		m_scheduledDate.setDate(newTaskSchedule);
		
		ImageHyperlink clearScheduled = m_toolkit.createImageHyperlink(sectionClient, SWT.NONE);
		clearScheduled.setImage(new ImageRegistry().getRemoveImage());
		clearScheduled.setToolTipText("Clear");
		clearScheduled.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				m_scheduledDate.setDate(null);
			}
		});

		Label dummy1 = m_toolkit.createLabel(sectionClient, "");
		GridData dummyLabelDataLayout1 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout1.horizontalSpan = 1;
		dummyLabelDataLayout1.widthHint = 10;
		dummy1.setLayoutData(dummyLabelDataLayout1);
		
		Label dueLabel = m_toolkit.createLabel(sectionClient, "Due:");
		dueLabel.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
		m_dueDate = new DatePicker(sectionClient, SWT.NONE, DatePicker.LABEL_CHOOSE);
		m_dueDate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		m_dueDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		m_dueDate.setDate(newTaskSchedule);
		
		ImageHyperlink clearDue = m_toolkit.createImageHyperlink(sectionClient, SWT.NONE);
		clearDue.setImage(new ImageRegistry().getRemoveImage());
		clearDue.setToolTipText("Clear");
		clearDue.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				m_dueDate.setDate(null);
			}
		});
		
		Label dummy2 = m_toolkit.createLabel(sectionClient, "");
		GridData dummyLabelDataLayout2 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout2.horizontalSpan = 1;
		dummyLabelDataLayout2.widthHint = 10;
		dummy2.setLayoutData(dummyLabelDataLayout2);
		
		Label estimatedLabel = m_toolkit.createLabel(sectionClient, "Estimated hours:");
		estimatedLabel.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
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
		
		ImageHyperlink clearEstimated = m_toolkit.createImageHyperlink(sectionClient, SWT.NONE);
		clearEstimated.setImage(new ImageRegistry().getRemoveImage());
		clearEstimated.setToolTipText("Clear");
		clearEstimated.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				m_estimated.setSelection(0);
			}
		});

		m_toolkit.paintBordersFor(sectionClient);
	}

	private void createTimeLensSection(Composite parent) {
		Section section = m_toolkit.createSection(parent, Section.EXPANDED | Section.TWISTIE | Section.TITLE_BAR);
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
		Composite sectionClient = m_toolkit.createComposite(section);			
		section.setClient(sectionClient);		
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = false;		
		sectionClient.setLayout(layout);
		
		Label StartLabel = m_toolkit.createLabel(sectionClient, "Start time:");		
		StartLabel.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
		GridData gd = new GridData();
		gd.widthHint = 110;
		gd.horizontalAlignment = GridData.FILL;
		Text startTime = m_toolkit.createText(sectionClient,"", SWT.BORDER);
        startTime.setLayoutData(gd);
		startTime.setEditable(false);
        startTime.setEnabled(false);
        
		Label dummy = m_toolkit.createLabel(sectionClient, "");
		GridData dummyLabelDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout.horizontalSpan = 1;
		dummyLabelDataLayout.widthHint = 50;
		dummy.setLayoutData(dummyLabelDataLayout);
        
        Label endLabel = m_toolkit.createLabel(sectionClient, "End time:");		
		endLabel.setForeground(m_toolkit.getColors().getColor(FormColors.TITLE));
		Text endTime = m_toolkit.createText(sectionClient,"", SWT.BORDER);
		endTime.setLayoutData(gd);
		endTime.setEditable(false);
        endTime.setEnabled(false);
		
	}
}
