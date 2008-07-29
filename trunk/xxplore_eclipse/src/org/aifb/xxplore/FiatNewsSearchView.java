package org.aifb.xxplore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.aifb.xxplore.views.graphviewer.GraphViewer;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.query.QueryWrapper;

public class FiatNewsSearchView extends ViewPart{

	private FiatNewsSearchViewSelectionProvider m_selectionProvider;
	private SparqlQueryHelper m_SparqlQueryHelper;
//	private boolean m_isDirty;
//	private FormToolkit m_toolkit;
	private ScrolledForm m_sform;
//	private Composite m_editorComposite;

//	private static String PART_TITLE = "FIAT Query Editor";
//	private static String PART_TOOLTIP_TEXT = "";

	public static final String ID = "org.aifb.xxplore.fiatnewssearchview";
	
	@Override
	public void createPartControl(Composite parent) {

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		m_sform = toolkit.createScrolledForm(parent);
		m_sform.setText("FIAT News Search");
		m_sform.getBody().setLayout(new GridLayout());
		Composite editorComposite = m_sform.getBody();

		GridLayout layout = new GridLayout();
		layout.marginBottom = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 5;
		editorComposite.setLayout(layout);	
		editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContent(editorComposite,toolkit);
		m_sform.reflow(true);
		m_sform.setFocus();
		
		m_selectionProvider = new FiatNewsSearchViewSelectionProvider();
		getSite().setSelectionProvider(m_selectionProvider);
		m_SparqlQueryHelper = new SparqlQueryHelper();
	
	}

	private void createContent(Composite editorComposite, FormToolkit toolkit){

//		Create sections

		createSearchSection(editorComposite,toolkit);
		createQueryPreferencesSection(editorComposite,toolkit);
		createButtonSection(editorComposite,toolkit);

	}

	private Text m_text_freeSearch;
	private Combo m_combo_news_state;
	
	private void createSearchSection(Composite editorComposite, FormToolkit toolkit){

		Section section = toolkit.createSection(editorComposite, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE );
		section.setText("Search");
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

		Label label_freeSearch = toolkit.createLabel(sectionClient, "Query", SWT.NONE);
		label_freeSearch.setForeground(toolkit.getColors().getColor(FormColors.TITLE));	       
		m_text_freeSearch = toolkit.createText(sectionClient,new String(), SWT.BORDER);
		m_text_freeSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_text_freeSearch.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}			
		});
		
		Label label_state = toolkit.createLabel(sectionClient, "State");
		label_state.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_news_state = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_news_state.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			m_combo_news_state.setItems(FieldDataHelper.getStates());
		} catch (Exception e1) {
			m_combo_news_state.setEnabled(false);
			label_state.setEnabled(false);
		}
		
	}


	private Combo m_combo_market;
	private Combo m_combo_segment;
	private Combo m_combo_technology;
	private Combo m_combo_source;
	private Combo m_combo_language;
	private Combo m_combo_maker;
	private Combo m_combo_model;
	private Combo m_combo_concept;
	private Text m_text_date_from;
	private Text m_text_date_to;
	
	private void createQueryPreferencesSection(Composite editorComposite, FormToolkit toolkit){

		Section section = toolkit.createSection(editorComposite, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setText("Query Preferences");
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

		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;						
		sectionClient.setLayout(layout);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		String[] fielddata;
		
//		Market
		Label label_market = toolkit.createLabel(sectionClient, "Market");
		label_market.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_market = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_market.setLayoutData(gridData);
		
		try {
			fielddata = FieldDataHelper.getMarkets();
			if(fielddata.length == 0){
				m_combo_market.setEnabled(false);
				label_market.setEnabled(false);
			}
			else{
				m_combo_market.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_market.setEnabled(false);
			label_market.setEnabled(false);
		}	
		
//		Segment
		Label label_segment = toolkit.createLabel(sectionClient, "Segment");
		label_segment.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_segment = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_segment.setLayoutData(gridData);
		
		try {
			fielddata = FieldDataHelper.getSegments();
			if(fielddata.length == 0){
				m_combo_segment.setEnabled(false);
				label_segment.setEnabled(false);
			}
			else{
				m_combo_segment.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_segment.setEnabled(false);
			label_segment.setEnabled(false);
		}	
		
//		Maker
		Label label_maker = toolkit.createLabel(sectionClient, "Maker");
		label_maker.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_maker = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_maker.setLayoutData(gridData);
		
		try {
			fielddata = FieldDataHelper.getMakers();
			if(fielddata.length == 0){
				m_combo_maker.setEnabled(false);
				label_maker.setEnabled(false);
			}
			else{
				m_combo_maker.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_maker.setEnabled(false);
			label_maker.setEnabled(false);
		}	
		

//		Model
		Label label_model = toolkit.createLabel(sectionClient, "Model");
		label_model.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_model = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_model.setLayoutData(gridData);
		
		try {
			fielddata = FieldDataHelper.getModels();
			
			if(fielddata.length == 0){
				m_combo_model.setEnabled(false);
				label_model.setEnabled(false);
			}
			else{
				m_combo_model.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_model.setEnabled(false);
			label_model.setEnabled(false);
		}	
		
//		Concept
		Label label_concept = toolkit.createLabel(sectionClient, "Concept");
		label_concept.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_concept = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_concept.setLayoutData(gridData);
		
		try {
			fielddata = FieldDataHelper.getConcepts();
			
			if(fielddata.length == 0){
				m_combo_concept.setEnabled(false);
				label_concept.setEnabled(false);
			}
			else{
				m_combo_concept.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_concept.setEnabled(false);
			label_concept.setEnabled(false);
		}	
		
		
//		Technology
		Label label_technology = toolkit.createLabel(sectionClient, "Technology");
		label_technology.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_technology = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_technology.setLayoutData(gridData);

		try {
			fielddata = FieldDataHelper.getTechnology();
			if(fielddata.length == 0){
				m_combo_technology.setEnabled(false);
				label_technology.setEnabled(false);
			}
			else{
				m_combo_technology.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_technology.setEnabled(false);
			label_technology.setEnabled(false);
		}	
		
		
//		Date
		Label label_date_from = toolkit.createLabel(sectionClient, "Date (from)");
		label_date_from.setForeground(toolkit.getColors().getColor(FormColors.TITLE));		
		m_text_date_from = toolkit.createText(sectionClient,new String(), SWT.BORDER);
		m_text_date_from.setLayoutData(gridData);
		m_text_date_from.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}			
		});
		
//		TODO: Dates currently not supported
		label_date_from.setEnabled(false);
		m_text_date_from.setEnabled(false);
		
		Label label_date_to = toolkit.createLabel(sectionClient, "Date (to)");
		label_date_to.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_text_date_to = toolkit.createText(sectionClient,new String(), SWT.BORDER);
		m_text_date_to.setLayoutData(gridData);
		m_text_date_to.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}			
		});
//		TODO: Dates currently not supported
		label_date_to.setEnabled(false);
		m_text_date_to.setEnabled(false);
		
//		Source
		Label label_source = toolkit.createLabel(sectionClient, "Source");
		label_source.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_source = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_source.setLayoutData(gridData);

		try {
			fielddata = FieldDataHelper.getSources();			
			if(fielddata.length == 0){
				m_combo_source.setEnabled(false);
				label_source.setEnabled(false);
			}
			else{
				m_combo_source.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_source.setEnabled(false);
			label_source.setEnabled(false);
		}	
			
//		Language
		Label label_language = toolkit.createLabel(sectionClient, "Language");
		label_language.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		m_combo_language = new Combo(sectionClient, SWT.DROP_DOWN);
		m_combo_language.setLayoutData(gridData);

		try {
			fielddata = FieldDataHelper.getLanguages();
			if(fielddata.length == 0){
				m_combo_language.setEnabled(false);
				label_language.setEnabled(false);
			}
			else{
				m_combo_language.setItems(fielddata);
			}
		} catch (Exception e1) {
			m_combo_language.setEnabled(false);
			label_language.setEnabled(false);
		}		
	}

	private void createButtonSection(Composite editorComposite, FormToolkit toolkit){
		
		Section section = toolkit.createSection(editorComposite, ExpandableComposite.NO_TITLE);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite buttonSection = toolkit.createComposite(section);
		section.setClient(buttonSection);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonSection.setLayout(layout);
		
		Button search_button = toolkit.createButton(buttonSection,"Search",SWT.PUSH | SWT.CENTER);
		search_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				m_sform.reflow(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
				
				String sparql_query = m_SparqlQueryHelper.constructQuery();		
				String[] vars = {"?news"};
				QueryWrapper query = new QueryWrapper(sparql_query,vars);
				
				long selectiontime = System.currentTimeMillis();	
				Object[] selection = {ExploreEnvironment.F_SEARCH, query, selectiontime, null};
				m_selectionProvider.setSelection(new StructuredSelection(selection));
				
			}
		});

//		TODO combine NewsSearch with StoredQueryView. a 'save' option would be nice ... 
//		Button save_button = toolkit.createButton(buttonSection,"Save",SWT.PUSH | SWT.CENTER);
//		save_button.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {

//				m_sform.reflow(true);
//				markDirty(true);
//				firePropertyChange(IEditorPart.PROP_DIRTY);
//			}
//		});
	}

	@Override
	public void setFocus() {}

	private static class FieldDataHelper{

//		Fields for the contained data
		private static List<String> s_markets;
		private static List<String> s_segments;
		private static List<String> s_technology;
		private static List<String> s_sources;
		private static List<String> s_languages;
		private static List<String> s_makers;
		private static List<String> s_models;
		private static List<String> s_concepts;
		private static List<String> s_state;

		
//		Some URIs needed ...
		private static final String s_market_uri = "http://www.x-media-project.org/fiat#Market";
		private static final String s_segment_uri = "http://www.x-media-project.org/fiat#Segment";
		private static final String s_technology_uri = "http://www.x-media-project.org/fiat#Technology";
		private static final String s_maker_uri = "http://www.x-media-project.org/fiat#VehicleMake";
		private static final String s_model_uri = "http://www.x-media-project.org/fiat#ModelReleased";
//		private static final String s_concepts_uri = "http://www.x-media-project.org/fiat#ModelReleased";
		private static final String s_newssource_uri = "http://www.x-media-project.org/fiat#NewsSource";
		private static final String s_newssource_state_seen_uri = "http://www.x-media-project.org/fiat/news#newsStateSeen";
		private static final String s_newssource_state_unseen_uri = "http://www.x-media-project.org/fiat/news#newsStateUnseen";
		
		private static Logger s_log = Logger.getLogger(GraphViewer.class.getCanonicalName());
		
		
		private static List<String> getMemberIndividuals(String concept_uri) throws Exception{
			
			List<String> members = new ArrayList<String>();
			IDaoManager daoManager = PersistenceUtil.getDaoManager();
			
			try {
				
				IConceptDao conceptDao = daoManager.getConceptDao();
				INamedConcept namedConcept = conceptDao.findByUri(concept_uri);
				IIndividualDao individualDao = daoManager.getIndividualDao();
				
				if(namedConcept != null){
					
					Set<IIndividual> memberIndividuals = individualDao.findMemberIndividuals(namedConcept);
					
					for(IIndividual member : memberIndividuals){
						members.add(member.toString());
					}
				}
			} 
			catch (DaoUnavailableException e) {
				s_log.debug("DaoUnavailableException: Error while trying to get daos ...");
				e.printStackTrace();
			}
			
			return members;
		}
			
		private static String[] getMarkets() throws Exception{

			if(s_markets == null){				
				s_markets = new ArrayList<String>();			
				s_markets.addAll(getMemberIndividuals(s_market_uri));
			}

			return s_markets.toArray(new String[0]);
		}

		private static String[] getSegments() throws Exception{

			if(s_segments == null){				
				s_segments = new ArrayList<String>();			
				s_segments.addAll(getMemberIndividuals(s_segment_uri));
			}

			return s_segments.toArray(new String[0]);
		}
		
		private static String[] getTechnology() throws Exception{
			
			if(s_technology == null){
				s_technology = new ArrayList<String>();
				s_segments.addAll(getMemberIndividuals(s_technology_uri));
			}

			return s_technology.toArray(new String[0]);
		}
		
		private static String[] getLanguages() throws Exception{
			
			if(s_languages == null){
				s_languages = new ArrayList<String>();
				s_languages.add("en");
				s_languages.add("it");
			}

			return s_languages.toArray(new String[0]);
		}
		
		private static String[] getMakers() throws Exception{
			
			if(s_makers == null){
				s_makers = new ArrayList<String>();
				s_makers.addAll(getMemberIndividuals(s_maker_uri));
			}

			return s_makers.toArray(new String[0]);
		}
		
		private static String[] getModels() throws Exception{
			
			if(s_models == null){
				s_models = new ArrayList<String>();
				s_models.addAll(getMemberIndividuals(s_model_uri));
			}

			return s_models.toArray(new String[0]);
		}
		
		private static String[] getConcepts() throws Exception{
			
			if(s_concepts == null){
				s_concepts = new ArrayList<String>();
//				s_concepts.addAll(getMemberIndividuals(s_concepts_uri));
//				TODO
			}

			return s_concepts.toArray(new String[0]);
		}
		
		private static String[] getSources() throws Exception{
			
			if(s_sources == null){
				s_sources = new ArrayList<String>();
				s_sources.addAll(getMemberIndividuals(s_newssource_uri));
			}

			return s_sources.toArray(new String[0]);
		}
		
		private static String[] getStates() throws Exception{
			
			if(s_state == null){
				s_state = new ArrayList<String>();
				s_state.add(s_newssource_state_seen_uri);
				s_state.add(s_newssource_state_unseen_uri);
			}

			return s_state.toArray(new String[0]);
		}
	}
	
	private class SparqlQueryHelper{
	

		private String constructQuery(){
			
			String language = new String();
			String query = new String();
			StringBuffer query_whereClause = new StringBuffer();
			
			query += "SELECT ?news ";
			query += "WHERE { { ?news <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.x-media-project.org/fiat#News> ";
			query_whereClause.append("{ ?news <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.x-media-project.org/fiat#News> ");
			
			if((m_combo_news_state != null) && !m_combo_news_state.getText().equals("")){
				query += " . ?news <http://www.x-media-project.org/fiat#newsState> <"+m_combo_news_state.getText()+"> ";
				query_whereClause.append(" . ?news <http://www.x-media-project.org/fiat#newsState> <"+m_combo_news_state.getText()+"> ");
			}
			
			if((m_combo_market != null) && !m_combo_market.getText().equals("")){
				query += " . <"+m_combo_market.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ";
				query_whereClause.append(" . <"+m_combo_market.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ");
			}
			
			if((m_combo_model != null) && !m_combo_model.getText().equals("")){
				query += " . <"+m_combo_model.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ";
				query_whereClause.append(" . <"+m_combo_model.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ");
			}
			
			if((m_combo_segment != null) && !m_combo_segment.getText().equals("")){
				query += " . <"+m_combo_segment.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ";
				query_whereClause.append(" . <"+m_combo_segment.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ");
			}
			
			if((m_combo_maker != null) && !m_combo_maker.getText().equals("")){
				query += " . <"+m_combo_maker.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ";
				query_whereClause.append(" . <"+m_combo_maker.getText()+"> <http://www.x-media.org/ontologies/metaknow#source> ?news ");
			}
			
			if((m_combo_source != null) && !m_combo_source.getText().equals("")){
				query += " . ?news <http://www.x-media-project.org/fiat#hasSource> <"+m_combo_source.getText()+"> ";
				query_whereClause.append(" . ?news <http://www.x-media-project.org/fiat#hasSource> <"+m_combo_source.getText()+"> ");
			}
			
			if((m_combo_language != null) && !m_combo_language.getText().equals("")){
				language = m_combo_language.getText();
			}
			else{
//				default value
				language = "en";
			}

			if((m_text_freeSearch != null) && !m_text_freeSearch.getText().equals("")){
				
				ArrayList<String> newsArticles = findNewsArticles(parseKeywords(m_text_freeSearch.getText()));
				
				for(int i = 0; i < newsArticles.size(); i++){
					
					if(i == newsArticles.size()-1){
						query += " . ?news <http://www.x-media-project.org/fiat#hasLink> \""+newsArticles.get(i)+"\"@"+language+" } }";
					}
					else{
						query += " . ?news <http://www.x-media-project.org/fiat#hasLink> \""+newsArticles.get(i)+"\"@"+language+" } UNION ";
						query += query_whereClause.toString();
					}
				}
			}
			else{
				
				if((m_combo_language != null) && !m_combo_language.getText().equals("")){
					query += " . ?news <http://www.x-media-project.org/fiat#hasLink> \"*\"@"+m_combo_language.getText()+" } }";
				}
				else{
					query += "} } ";
				}
			}
			
			return query;
		}
		
		
		
		private ArrayList<String> m_allNews;
		private String m_news_uri = "http://www.x-media-project.org/fiat#News";
	
		private ArrayList<String> findNewsArticles(ArrayList<String> keywords){
			
			ArrayList<String> matches = new ArrayList<String>();

//			retrieve all news article ...
			
			if(m_allNews == null){
				
				m_allNews = new ArrayList<String>();
				IDaoManager daoManager = PersistenceUtil.getDaoManager();

				try {

					IConceptDao conceptDao = daoManager.getConceptDao();
					INamedConcept namedConcept = conceptDao.findByUri(m_news_uri);
					IIndividualDao individualDao = daoManager.getIndividualDao();
					Set<IIndividual> memberIndividuals = individualDao.findMemberIndividuals(namedConcept);

					for(IIndividual member : memberIndividuals){
						m_allNews.add(member.toString());
					}
				} 
				catch (DaoUnavailableException e) {
					e.printStackTrace();
				}
			}
			
//			scan all news articles for given keywords
			
			boolean is_match = true;
			
			for(String article : m_allNews){
				
				is_match = true;
				
				for(String keyword : keywords){
					
					if(!article.toLowerCase().contains(keyword)){
						is_match = false;
						break;
					}
				}
				
				if(is_match){
					matches.add(article);
				}
			}
			
			return matches;
		}
		
		private ArrayList<String> parseKeywords(String search_strg){
			
			ArrayList<String> keywords = new ArrayList<String>();	
			StringTokenizer tokenizer = new StringTokenizer(search_strg.toLowerCase(), "AND");
			
			while(tokenizer.hasMoreTokens()){
				keywords.add(tokenizer.nextToken());
			}
			
			return keywords;
		}
	}
	private class FiatNewsSearchViewSelectionProvider implements ISelectionProvider{

		private StructuredSelection m_selection;
		private Collection<ISelectionChangedListener> m_selectionListeners;	


		private FiatNewsSearchViewSelectionProvider() {
			super();
		}


		/**
		 * 
		 * @return a Collection which contains all added selectionChangedListeners
		 */
		private Collection<ISelectionChangedListener> getSelectionListeners(){

			if (m_selectionListeners == null) {
				m_selectionListeners = new ArrayList<ISelectionChangedListener>();
			}

			return m_selectionListeners;
		}

		/**
		 * fires a SelectionChangedEvent to all added SelectionChangedListeners
		 *
		 */
		private void fireSelectionChanged(){

			Iterator iter = getSelectionListeners().iterator();

			while(iter.hasNext()) 
			{
				ISelectionChangedListener listener = (ISelectionChangedListener) iter.next();
				listener.selectionChanged(new SelectionChangedEvent(this, getSelection()));
			}
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			getSelectionListeners().add(listener);
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			return m_selection;
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener){
			getSelectionListeners().remove(listener);
		}

		/**
		 * only accepts StructuredSelections
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection(ISelection selection){

			m_selection = (StructuredSelection) selection;
			fireSelectionChanged();			

		}
	}
}