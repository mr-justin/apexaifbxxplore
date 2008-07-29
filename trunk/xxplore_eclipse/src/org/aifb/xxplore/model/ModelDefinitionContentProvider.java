package org.aifb.xxplore.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aifb.xxplore.shared.util.URIHelper;
import org.aifb.xxplore.storedquery.IQuery;
import org.aifb.xxplore.storedquery.Prefix;
import org.aifb.xxplore.storedquery.Query;
import org.aifb.xxplore.views.definitionviewer.CheckboxDialog;
import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.aifb.xxplore.views.definitionviewer.ModelDefinitionNode;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.ateam.xxplore.core.service.IService;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.NextLuceneQueryService;
import org.ateam.xxplore.core.service.search.NextQueryIntepretationService;
import org.ateam.xxplore.core.service.search.QueryTranslationService;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.QueryWrapper;
import org.xmedia.oms.query.Variable;



public class ModelDefinitionContentProvider implements ITreeContentProvider{

	private ModelDefinition m_modeldefinition;

	private NextLuceneQueryService m_search_service;

	private NextQueryIntepretationService m_interpretator;

	private QueryTranslationService m_translator;

	private Set<Viewer> m_viewers;

	private ModelDefinitionNode m_modelDefinitionNode;
	
	private Collection<OWLPredicate> selectedQuery;

	private ModelDefinitionContentProvider(IService queryservice, NextQueryIntepretationService interpretor, QueryTranslationService translator) {
		super();
		m_viewers = new HashSet<Viewer>();
		m_search_service = (NextLuceneQueryService)queryservice;
		m_interpretator = interpretor;
		m_translator = translator;
	}


	public Object[] getElements(Object inputElement) {
		if (m_modeldefinition == inputElement) {
			return new Object[] { m_modelDefinitionNode };
		} else {
			return new Object[0];
		}
	}

	public Object[] getChildren(Object parentElement) {
		return ((ITreeNode)parentElement).getChildren().toArray();
	}

	public boolean hasChildren(Object element) {
		return ((ITreeNode)element).hasChildren();
	}

	public Object getParent(Object element) {
		//TODO
		return null;
	}

	public void dispose() {
		m_modeldefinition = null;
		m_search_service = null; 
		m_interpretator = null;
		m_translator = null; 
		m_viewers = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		if (newInput == null){
			m_modeldefinition = null;
		}

		//if not the same input
		if(oldInput != newInput){
			if((newInput != null) && (newInput instanceof ModelDefinition)){
				if (!m_viewers.contains(viewer)) {
					m_viewers.add(viewer);
				}

				m_modeldefinition = (ModelDefinition)newInput;
				m_modelDefinitionNode = new ModelDefinitionNode(this, m_modeldefinition);

				if(m_modeldefinition.getDataSource() instanceof IOntology){
					makeKbIndex(((IOntology)m_modeldefinition.getDataSource()).getUri());
					System.out.println(((IOntology)m_modeldefinition.getDataSource()).getUri());
				}	
			}
		}

		viewer.refresh();
		if (viewer instanceof TreeViewer) {
			((TreeViewer)viewer).expandAll();
		}		
	}

	public String truncateUri(String uri) {
		return uri.indexOf("#") >= 0 ? uri.substring(uri.indexOf("#")+1) : uri;
	}
	@SuppressWarnings("unchecked")
	public boolean openDialog(Collection<Collection<OWLPredicate>> queries){
		Iterator<Collection<OWLPredicate>> itr = queries.iterator();
		int n = queries.size();
		String[] ids = new String[n];
		String[] labels = new String[n];

		for(int j = 0; j < n; j++){
			Collection<OWLPredicate> query = itr.next();
			int num = query.size();
			String label = "";
			int p = 0;
			int q = 1;
			for(OWLPredicate predicate : query) {
				p++;
				String subLabel;
				String relLabel;
				String objLabel;
				if(predicate instanceof PropertyMemberPredicate) {
					subLabel = ((PropertyMemberPredicate)predicate).getFirstTerm().getLabel();
					objLabel = ((PropertyMemberPredicate)predicate).getSecondTerm().getLabel();
					relLabel = ((PropertyMemberPredicate)predicate).getProperty().getLabel();
					label = label + truncateUri(relLabel) + "(" + truncateUri(subLabel) + "," + truncateUri(objLabel) + ")";
				}
				else if(predicate instanceof ConceptMemberPredicate) {
					subLabel = ((ConceptMemberPredicate)predicate).getTerm().getLabel();
					objLabel = ((ConceptMemberPredicate)predicate).getConcept().getLabel();
					relLabel = "type";
					label = label + truncateUri(relLabel) + "(" + truncateUri(subLabel) + "," + truncateUri(objLabel) + ")";
				}
				
//				if(p < num && p%6 != 0 )
//					label = label + ", ";
//				else if(p < num && p%6 == 0)
//					label = label + ", :";
				
				if(p < num) {
					label = label + ", ";
				}
				if(label.length() >= 130*q){
					label = label + "$";
					q++;
				}
					
			}
			ids[j] = "" + (j+1);
			labels[j] = "" + (j+1) + ". " + label;
		}
		Set<String> initialIds = new HashSet<String>();
		initialIds.add("1");
		CheckboxDialog dialog = new CheckboxDialog(m_viewers.iterator().next().getControl().getShell(), "Results of Interpretation",
				"Selection options", "Select one of the options", ids, labels, initialIds,
				new CheckboxDialog.CheckboxValidator(){
			public String validate(Collection options){
				return (options.size() != 1) ? 
						"Select one option" : null;
			}
		});
		if (dialog.open() == Window.OK){
			Iterator itrator = queries.iterator();
			Set<String> result = dialog.getResult();
			String str = result.iterator().next();
			int index = Integer.parseInt(str);
			for (int p = 1; p < index; p++) {
				itrator.next();
			}
			selectedQuery = (Collection<OWLPredicate>)itrator.next();
			return true;
		} else {
			return false;
		}
	}
	
//	Modified version of updateIntepretation() below.
//	Updates definition with highest ranked query!
//	Used for XXPloreConsole only.
	public boolean updateIntepretationCommandlineVersion(){
		
		// listening only to selection changes in the DefinitionView
		//get keyword queries 
		String query = m_modeldefinition.getQuery();
		//map keywords to ontology elements
		Map<String,Collection<KbElement>> elements = m_search_service.searchKb(query);

		//connect ontology elements to compute possible definitions (queries) 
		Collection<Collection<OWLPredicate>> queries = m_interpretator.computeQueries(elements, 
																ExploreEnvironment.DEFAULT_WIDTH_FOR_TERMINTERPRETATION,
																ExploreEnvironment.DEFAULT_DEPTH_FOR_TERMINTERPRETATION);
		if(queries != null){	
				selectedQuery = queries.iterator().next();
				m_modeldefinition = m_translator.translate2ModelDefinition(selectedQuery,m_modeldefinition);
				return true;
			
		} else {
			return false;
		} 
	}
	
	public void updateIntepretation(){
		// listening only to selection changes in the DefinitionView
		//get keyword queries 
		String query = m_modeldefinition.getQuery();
		//map keywords to ontology elements
		Map<String,Collection<KbElement>> elements = m_search_service.searchKb(query);

		//connect ontology elements to compute possible definitions (queries) 
		Collection<Collection<OWLPredicate>> queries = m_interpretator.computeQueries(elements, 
				ExploreEnvironment.DEFAULT_WIDTH_FOR_TERMINTERPRETATION, ExploreEnvironment.DEFAULT_DEPTH_FOR_TERMINTERPRETATION);

		if(queries != null){ 
			boolean selected = true;
			//TODO: popup a menu for the user to choose the definition instead of the highest ranked one
			if (queries.size() > 1) {
				selected = openDialog(queries);
			} else {
				selectedQuery = queries.iterator().next();
			}

			if (selected == true){	
				//TODO instead of overwriting --> do a merge of the returned model definition with the current model definition
				m_modeldefinition = m_translator.translate2ModelDefinition(selectedQuery,m_modeldefinition);
			}
		} else {
			String message = "No results found. Please rephrase query.";
			String title = "DefinitionView";
			MessageBox mb = new MessageBox(m_viewers.iterator().next().getControl().getShell(), SWT.ICON_WORKING | SWT.OK);
			mb.setText(title);
			mb.setMessage(message);
			mb.open();
		} 
		
		for(Viewer viewer : m_viewers)
		{
			inputChanged(viewer,null,m_modeldefinition);
		}
	}

	public void updateResult(){
		if (selectedQuery != null)
			selectedQuery.clear();
		else
			selectedQuery = new LinkedHashSet<OWLPredicate>();
		m_modeldefinition.getSelectedQuery(m_modeldefinition,selectedQuery);
		QueryWrapper sparqlQuery = m_translator.translate2SparqlQuery(selectedQuery, m_interpretator.getRankedVariables(selectedQuery));
		m_modeldefinition.setDLQuery(sparqlQuery);
	}


	public void clear(){
		selectedQuery = null;
	}

	public void refreshViewers() {
		for (Viewer viewer : m_viewers) {
			viewer.refresh();
//			if (viewer instanceof TreeViewer) 
//			((TreeViewer)viewer).expandAll();
		}
	}

	public void refreshViewers(ITreeNode node) {
		for (Viewer viewer : m_viewers) {
			viewer.refresh();
			if (viewer instanceof TreeViewer) {
				((TreeViewer) viewer).expandToLevel(node, AbstractTreeViewer.ALL_LEVELS);
			}
		}
	}

	public void updateDefinition(String searchstring){
		if(m_modeldefinition != null)
		{
			m_modeldefinition.setQuery(searchstring);
		}
	}

	public ModelDefinition getModelDefinition(){
		return m_modeldefinition;
	}
	
//	used by XXPloreConsole only ...
	public void setModelDefinition(ModelDefinition modelDefinition){
		m_modeldefinition = modelDefinition;
	}
	
	public IQuery getQuery() {
		if (selectedQuery != null)
			selectedQuery.clear();
		else
			selectedQuery = new LinkedHashSet<OWLPredicate>();
		m_modeldefinition.getSelectedQuery(m_modeldefinition,selectedQuery);
		Set<String[]> triples = m_translator.translate2StringTriples(selectedQuery, m_interpretator.getRankedVariables(selectedQuery));

		Map<String,String> namespaces = new HashMap<String,String>();
		int c = 0;
		for (String[] t : triples) {
			for (int i = 0; i < t.length; i++) {
				if (t[i].startsWith("<") && t[i].endsWith(">")) {
					String uri = t[i].replaceAll("<", "");
					uri = uri.replaceAll(">", "");
					String ns = URIHelper.getNamespace(uri);
					if (ns != null) {
						if (!namespaces.containsKey(ns))
							namespaces.put(ns, "ns" + (c++));
						t[i] = uri.replace(ns, namespaces.get(ns) + ":");
					}
				}
			}
		}
		
		IQuery query = new Query();
		
		Stack<String[]> preds = new Stack<String[]>();
		preds.addAll(triples);
		query.setPredicates(preds);
		
		Stack<Prefix> prefixes = new Stack<Prefix>();
		for (String ns : namespaces.keySet())
			prefixes.add(new Prefix(namespaces.get(ns), ns));
		query.setPrefixes(prefixes);
		
		Set<String> vars = new HashSet<String>();
		for (Variable v : m_interpretator.getRankedVariables(selectedQuery)) {
			vars.add(v.getName());
		}
		query.setVariables(vars);
		
		return query;
	}

	private void makeKbIndex(final String datasourceUri){
		Display.getCurrent().asyncExec(new Runnable(){
			public void run(){
				m_search_service.indexDataSource(datasourceUri);
			}
		});
	}
	
//	used by XXPloreCommandLine only ...
	public void makeKbIndexCommandLineVersion(String datasourceUri){
		m_search_service.indexDataSource(datasourceUri);
	}
	
	public static class ModelDefinitionContentProviderSingleTonHolder {
		
		public static ModelDefinitionContentProvider m_modelDefinitionContentProvider;
		
		public static ModelDefinitionContentProvider getInstance(){
			
			if(m_modelDefinitionContentProvider == null){				
				NextQueryIntepretationService interpretator = new NextQueryIntepretationService();
				QueryTranslationService translator = new QueryTranslationService();
				m_modelDefinitionContentProvider = new ModelDefinitionContentProvider(NextLuceneQueryService.getInstance(), interpretator, translator);
			}
			
			return m_modelDefinitionContentProvider;
		}
	}
}
