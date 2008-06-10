package org.aifb.xxplore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.storedquery.Prefix;
import org.aifb.xxplore.storedquery.Query;
//import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.xmedia.oms.query.QueryWrapper;

public class SparqlQueryView extends ViewPart{

	
	private Text m_text;
	private HashMap<String,String> m_queryURIs;
	private long m_lastSelectionTime;
	private Composite m_parent;

	private Button m_saveButton;
	private Button m_clearButton;
	private Button m_searchButton;
	private Button m_metaSearchButton;
	
	
//	private ModelDefinitionContentProvider m_modelDefinitionProvider;
	private SparqlViewSelectionProvider m_sparqlViewSelectionProvider;
	
	private ISelectionListener m_defViewlistener = new ISelectionListener() {
		
		@SuppressWarnings("unchecked")
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			
			// listening only to selection changes in the DefinitionView
			if (sourcepart.getSite().getId().equals(DefinitionView.ID)) 
			{			   
				if((selection instanceof IStructuredSelection) && !selection.isEmpty())
				{										
					if((((StructuredSelection)selection).getFirstElement() instanceof Integer) &&
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.CLEAR))
					{
						clear();					
					}
					else if((((StructuredSelection)selection).getFirstElement() instanceof Integer) && (
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.F_SEARCH) ||
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.ADD) ||
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.META_SEARCH)))
					{						
						Iterator<Object> iter = ((StructuredSelection)selection).iterator();
						
						//first element of selection event is about the type of the selection
						iter.next();
						
						//get second element becuase query is in the second element
						QueryWrapper query = (QueryWrapper)iter.next();
						
						//get third element: selection time
						long selectionTime = (Long)iter.next();
						
						if(!alreadyAdded(selectionTime))
						{
							setText(query.getQuery());
							m_lastSelectionTime = selectionTime;
						}							
					}
				}
			}
		}
	};
	
	
	@SuppressWarnings("unused")
	private static Logger s_log = Logger.getLogger(SparqlQueryView.class);
	public static final String ID = "org.aifb.xxplore.sparqlqueryview";
	
	
	@Override
	public void createPartControl(Composite parent){
			
		m_parent = parent;
		m_queryURIs = new HashMap<String, String>();
//		m_modelDefinitionProvider = ModelDefinitionContentProvider.ModelDefinitionContentProviderSingleTonHolder.getInstance();
		
		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new FormLayout());
		
		m_sparqlViewSelectionProvider = new SparqlViewSelectionProvider();
		getSite().setSelectionProvider(m_sparqlViewSelectionProvider);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(m_defViewlistener);
		
		m_text = new Text(container, SWT.BORDER | SWT.MULTI);
		m_text.setEditable(true);
		
		m_text.addMouseListener(new MouseListener()
		{
			
			public void mouseDoubleClick(MouseEvent event){}
			public void mouseUp(MouseEvent event){}
			
			public void mouseDown(MouseEvent event){
				
				if(event.button == 1)
				{
					m_text.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
					
//					enable buttons
					
					m_metaSearchButton.setEnabled(true);
					m_searchButton.setEnabled(true);
					m_clearButton.setEnabled(true);
					m_saveButton.setEnabled(true);
					
				}
				
			}
		});


		m_searchButton = new Button(container, SWT.PUSH);
		m_searchButton.setEnabled(false);
		m_searchButton.setText("f-search");	    
		//add Listener
		m_searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
				String cleanedQuery = SparqlHelper.cleanQuery(m_text.getText());
				String[] vars = SparqlHelper.getVars(cleanedQuery);
								
				QueryWrapper newQueryWrapper = new QueryWrapper(cleanedQuery,vars);
				
				long selectiontime = System.currentTimeMillis();
				Object[] selection = {ExploreEnvironment.F_SEARCH,newQueryWrapper,selectiontime};
				
				m_sparqlViewSelectionProvider.setSelection(new StructuredSelection(selection));
				
				setText(cleanedQuery);
				
			}

		});
		
		m_metaSearchButton = new Button(container, SWT.PUSH);
		m_metaSearchButton.setEnabled(false);
		m_metaSearchButton.setText("m-search");	    
		//add Listener
		m_metaSearchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
				String cleanedQuery = SparqlHelper.cleanQuery(m_text.getText());
				String[] vars = SparqlHelper.getVars(cleanedQuery);
								
				QueryWrapper newQueryWrapper = new QueryWrapper(cleanedQuery,vars);
				
				long selectiontime = System.currentTimeMillis();
				Object[] selection = {ExploreEnvironment.META_SEARCH,newQueryWrapper,selectiontime,null};
				
				m_sparqlViewSelectionProvider.setSelection(new StructuredSelection(selection));
				
				setText(cleanedQuery);
			}

		});
				
		m_clearButton = new Button(container, SWT.PUSH);	
		m_clearButton.setEnabled(false);
		m_clearButton.setText("clear");	    
		//add Listener
		m_clearButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
				
				m_metaSearchButton.setEnabled(false);
				m_searchButton.setEnabled(false);
				m_clearButton.setEnabled(false);
				m_saveButton.setEnabled(false);
				
				long selectiontime = System.currentTimeMillis();
				Object[] selection = {ExploreEnvironment.CLEAR,selectiontime};
				
				m_sparqlViewSelectionProvider.setSelection(new StructuredSelection(selection));
				
//				fallback ...
				setText("");
			}

		});
		
		m_saveButton = new Button(container, SWT.PUSH); 
		m_saveButton.setEnabled(false);
		m_saveButton.setText("save");	    
		//add Listener
		m_saveButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
				String cleanedQuery = SparqlHelper.cleanQuery(m_text.getText());
				
				Query query = new Query();

				Stack<String[]> triples = SparqlHelper.extractTriples(cleanedQuery);
				query.setPredicates(triples);
				
				String[] var_array = SparqlHelper.getVars(cleanedQuery);
				query.setVariables(new HashSet<String>(Arrays.asList(var_array)));
				
				Stack<Prefix> prefixes = SparqlHelper.getPrefixes(cleanedQuery);
				query.setPrefixes(prefixes);
				
				long selectiontime = System.currentTimeMillis();
				
				Object[] selection = {ExploreEnvironment.STORE, query, selectiontime};
				m_sparqlViewSelectionProvider.setSelection(new StructuredSelection(selection));
				
				giveFeedback();
			}

		});
		
		
		FormData button1Data = new FormData();
		button1Data.left = new FormAttachment(0,-1);
		button1Data.right = new FormAttachment(35, -4);		    
		button1Data.bottom = new FormAttachment(100, 0);		    
		m_searchButton.setLayoutData(button1Data);
		
		FormData button2Data = new FormData();
		button2Data.left = new FormAttachment(35,-1);
		button2Data.right = new FormAttachment(70, -4);		    
		button2Data.bottom = new FormAttachment(100, 0);		    
		m_metaSearchButton.setLayoutData(button2Data);	

		FormData button3Data = new FormData();
		button3Data.left = new FormAttachment(70,-1);
		button3Data.right = new FormAttachment(85, -4);		    
		button3Data.bottom = new FormAttachment(100, 0);		    
		m_clearButton.setLayoutData(button3Data);
		
		FormData button4Data = new FormData();
		button4Data.left = new FormAttachment(85,-1);
		button4Data.right = new FormAttachment(100, -4);		    
		button4Data.bottom = new FormAttachment(100, 0);		    
		m_saveButton.setLayoutData(button4Data);
		
	    FormData textData = new FormData();
	    textData.left = new FormAttachment(0, 0);
	    textData.right = new FormAttachment(100, 0);
	    textData.top = new FormAttachment(0, 4);
	    textData.bottom = new FormAttachment(m_saveButton, -4);
	    m_text.setLayoutData(textData);
	}
	
	private void giveFeedback(){
		MessageBox mb = new MessageBox(m_parent.getShell(), SWT.ICON_WORKING | SWT.OK);
		mb.setText("SparqlQueryView");
		mb.setMessage("  Query saved! Please see StoredQueryView.  ");
		mb.open();
	}
	
	private Display getDisplay(){
		return m_parent.getDisplay();
	}
	
	private boolean alreadyAdded(long selectionTime){
		return (m_lastSelectionTime == selectionTime);
	}
	
	private void clear(){
		setText(null);
		m_lastSelectionTime = -1;
	}

	
	@Override
	public void setFocus() {
	}

	public void setText(String strg){
		
		if(strg != null) 
		{
			m_text.setText(truncateQuery(strg));
		}
		else
		{
			m_text.setText(new String());
		}
		
		m_text.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}
		
	private String truncateUri(String uri) {
		return uri.indexOf("#") >= 0 ? uri.substring(uri.indexOf("#")+1) : uri;
	}
	
	private String truncateQuery(String query) {
		
		m_queryURIs.clear();
		
		if(query.equals("")) {
			return new String();
		}
		
		StringBuffer prebuffer = new StringBuffer(query.substring(0, query.indexOf("SELECT")));
		StringBuffer selbuffer = new StringBuffer(query.substring(query.indexOf("SELECT")));
	
		int prefid = 97;
		
		while ((selbuffer.indexOf("<")!= -1) && (selbuffer.indexOf(">")!= -1)){
			
			int ind1, ind2;
			ind1 = selbuffer.indexOf("<");
			ind2 = selbuffer.indexOf(">");
			
			String substrg = selbuffer.substring(ind1+1, ind2).toString();
			
			if(!m_queryURIs.containsKey(substrg.substring(0, substrg.indexOf("#")+1)))
			{
				m_queryURIs.put(substrg.substring(0, substrg.indexOf("#")+1),Character.toString((char)prefid));
				prefid++;
			}

			String prefix = m_queryURIs.get(substrg.substring(0, substrg.indexOf("#")+1));
			selbuffer.replace(ind1, ind2+1, prefix+":"+truncateUri(substrg));
			
		}	
	
		if(selbuffer.indexOf("WHERE")!= -1)
		{
			int index = selbuffer.indexOf("WHERE");
			selbuffer.insert(index, "\n");
		}
		if(selbuffer.indexOf("{")!= -1)
		{
			int index = selbuffer.indexOf("{");
			selbuffer.insert(index+1, "\n\t");
		}
		
		int fromIndex = 0;
		
		while((selbuffer.indexOf(".",fromIndex+1)!= -1) && (selbuffer.indexOf(".",fromIndex+1)!= selbuffer.lastIndexOf(".")))
		{
			fromIndex = selbuffer.indexOf(".",fromIndex+1);
			selbuffer.insert(fromIndex+1, "\n\t");
		}
		if(selbuffer.lastIndexOf("}")!= -1)
		{
			int index = selbuffer.lastIndexOf("}");
			selbuffer.insert(index, "\n");
		}
		
		for(String uri : m_queryURIs.keySet())
		{
			String prefix = m_queryURIs.get(uri);
			selbuffer.insert(0,"PREFIX "+prefix+": "+"<"+uri+">"+" \n");
		}
		
		
		if(prebuffer.length()==0)
		{
			return selbuffer.toString();
		}
		else
		{
			//clean prebuffer
			while ((prebuffer.indexOf("> P")!= -1))
			{
				int idx = prebuffer.indexOf("> P");
				prebuffer.insert(idx+2, "\n");
			}
			prebuffer.append("\n");
			
			return selbuffer.insert(0, prebuffer).toString();
		}
	}
	


//	private Stack<String[]> getPredicates(String query){
//		Stack<String[]> predicates = new Stack<String[]>();
//		String predicate = query.substring(query.indexOf("{"), query.indexOf("}"));
//		
//		Pattern p = Pattern.compile("(\\?x\\d{1,})\\s+([a-z]:\\S+)\\s+(.+)\\.");
//		Matcher m = p.matcher(predicate);
//		while(m.find()){
//			String[] strs = new String[3]; 
//			strs[0] = m.group(1).trim();
//			strs[1] = m.group(2).trim();
//			strs[2] = m.group(3).trim();
//			predicates.add(strs);
//		}
//		
//		return predicates;
//	}
//	
//	private Stack<Prefix> getPrefixes(String query){
//		Stack<Prefix> prefixes = new Stack<Prefix>();
//		String prefix = query.substring(0, query.indexOf("SELECT"));
//		
//		Pattern p = Pattern.compile("PREFIX\\s+(.*):\\s*<(.*)>.*");
//		Matcher m = p.matcher(prefix);
//		while(m.find()){
//			prefixes.add(new Prefix(m.group(1), m.group(2)));
//		}
//		
//		return prefixes;
//	}
//	
//	private Set<String> getSelectedVariables(String query){
//		Set<String> vars = new HashSet<String>();
//		String select = query.substring(query.indexOf("SELECT"), query.indexOf("WHERE"));
//		
//		Pattern p = Pattern.compile("\\?x\\d{1,}");
//		Matcher m = p.matcher(select);
//		while(m.find()){
//			vars.add(m.group());
//		}
//		
//		return vars;
//	}
//	
//	private Set<String> getVariables(String query){
//		Set<String> vars = new HashSet<String>();
//		String where = query.substring(query.indexOf("WHERE"));
//		
//		Pattern p = Pattern.compile("\\?x\\d{1,}");
//		Matcher m = p.matcher(where);
//		while(m.find()){
//			vars.add(m.group());
//		}
//		
//		return vars;
//	}
	
	public static class SparqlHelper{
		
		
		public static final String SELECT = "SELECT";
		public static final String WHERE = "WHERE";
		public static final String PREFIX = "PREFIX";
		
			
		public static String[] getVars(String query){
			
			ArrayList<String> vars = new ArrayList<String>();
			
			String select = query.substring(query.indexOf(SELECT), query.indexOf(WHERE));
			Integer[] occurs = getAllOccurrences(select, "?");
			
			for(int occur : occurs)
			{
				int end = select.indexOf(" ", occur);
				if (end < 0) {
					end = select.length();
				}
				vars.add(select.substring(occur, end).trim());
			}
				
			return vars.toArray(new String[0]);
		}
		
		private static Integer[] getAllOccurrences(String input, String subString){
			
			ArrayList<Integer> locations = new ArrayList<Integer>();
			int count = 0;
			
			while(input.contains(subString))
			{
				locations.add(0,input.indexOf(subString)+count);
				count += input.indexOf(subString)+subString.length();
				input = input.substring(input.indexOf(subString)+subString.length());
			}
		
			return locations.toArray(new Integer[0]);
		}
		
		public static Stack<String[]> extractTriples(String query){
			
			Stack<String[]> triples = new Stack<String[]>();
			
			String where = query.substring(query.indexOf(WHERE)+5);
			Integer[] occurs = getAllOccurrences(where, ".");
			
//			find candidates
			ArrayList<String> triple_candidates = new ArrayList<String>();
			
			for(int i = 0; i < occurs.length; i++){
				
				String candidate;
				
				if(i == 0){
					
					candidate = where.substring(occurs[i+1], occurs[i]);
//					clean candidate
					candidate = cleanCandidate(candidate);								
					triple_candidates.add(candidate);
				}
				else if(i == occurs.length - 1){
					
					candidate = where.substring(0, occurs[i]);
//					clean candidate
					candidate = cleanCandidate(candidate);					
					triple_candidates.add(candidate);
				}
				else{
					
					candidate = where.substring(occurs[i]+1, occurs[i-1]);
//					clean candidate
					candidate = cleanCandidate(candidate);					
					triple_candidates.add(candidate);
				}
			}
			
//			validate candidate
			for(String candidate : triple_candidates){

				String[] words = getWords(candidate);
				if(words.length == 3){
//					valid triple
					triples.add(words);
				}
			}
					
			return triples;
		}
		
		private static String[] getWords(String triple){
			
			ArrayList<String> words = new ArrayList<String>();
			Integer[] occurs = getAllOccurrences(triple, " ");
			
			for(int i = 0; i < occurs.length; i++){
				
				String word;
				
				if(i == 0){
					
					word = triple.substring(occurs[i+1], occurs[i]);
					word.trim();
					words.add(word);
				}
				else if(i == occurs.length - 1){
					
					word = triple.substring(0, occurs[i]);
					word.trim();
					words.add(0, word);
				}
			}
			
//			add last word
			String word = triple.substring(occurs[0]);
			word.trim();
			words.add(word);
			
			return words.toArray(new String[0]);
		}
		
		private static final String[] TO_CLEAN_TRIPLE = {"\\{","\\."};
		
		private static String cleanCandidate(String candidate){
			
			for(String clean_me : TO_CLEAN_TRIPLE){
				candidate = candidate.replaceAll(clean_me,"");
			}
			
			return candidate.trim();			
		}
		
		private static final String[] TO_CLEAN_QUERY = {"\n","\r","\t"};
		
		public static String cleanQuery(String query){

			for(String clean_me : TO_CLEAN_QUERY){
				query = query.replaceAll(clean_me,"");
			}

			/////////////////////////////////////////////////////////////////////////////
			
//			THIS IS A HACK: SINCE STOREDQUERY VIEW IS NOT GIVING ME PROPER QUERIES, I HAVE TO FIX IT HERE THE DIRTY WAY ... 
			
			if(getVars(query).length == 0){
				
				String select = query.substring(query.indexOf(SELECT), query.indexOf(WHERE));
				Integer[] occurs = getAllOccurrences(select, "x");
				
				StringBuffer query_buffer = new StringBuffer(query);
				
				for(int occur : occurs)
				{
					query_buffer.insert(occur, "?");
				}
				
				query = query_buffer.toString();
			}
			
			/////////////////////////////////////////////////////////////////////////////

			return query;
		}
		
		public static Stack<Prefix> getPrefixes(String query){
			
			Stack<Prefix> prefixes = new Stack<Prefix>();
			String prefixes_strg = query.substring(0, query.indexOf(SELECT));
			
			Integer[] occurs = getAllOccurrences(prefixes_strg, PREFIX);			
			ArrayList<String> prefixes_substrings = new ArrayList<String>();
			
			for(int i = occurs.length-1; i >= 0; i--){
				
				String prefix_substring;
				
				if(i != 0){
					
					prefix_substring = prefixes_strg.substring(occurs[i],occurs[i-1]);
					prefix_substring.trim();
					prefixes_substrings.add(prefix_substring);
				}
				else{
					
					prefix_substring = prefixes_strg.substring(occurs[i]);
					prefix_substring.trim();
					prefixes_substrings.add(prefix_substring);
				}
			}
			
			for(String prefix_substring : prefixes_substrings){
				
				if(prefix_substring.contains(":")){
					
					String uri = prefix_substring.substring(prefix_substring.indexOf(":")+1).trim();
					uri = cleanPrefixUri(uri);
					String prefix = prefix_substring.substring(6,prefix_substring.indexOf(":")).trim();		
					prefixes.add(new Prefix(prefix,uri));
				}
			}
				
			return prefixes;
		}
		
		private static String cleanPrefixUri(String uri){
			
			if(uri.contains("<")){				
				uri = uri.substring(uri.indexOf("<")+1);				
			}
			if(uri.contains(">")){
				uri = uri.substring(0,uri.indexOf(">"));
			}
			
			return uri;
		}		
	}

	public class SparqlViewSelectionProvider implements ISelectionProvider{

		private StructuredSelection m_selection;
		private Collection<ISelectionChangedListener> m_selectionListeners;	


		public SparqlViewSelectionProvider() {
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
