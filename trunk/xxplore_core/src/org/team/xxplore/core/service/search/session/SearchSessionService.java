package org.team.xxplore.core.service.search.session;

import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.team.xxplore.core.service.search.datastructure.*;

import searchsession.SemplorePool;
import sjtu.apex.searchWebDB.dataStructures.Facet;
import sjtu.apex.searchWebDB.dataStructures.Keywords;
import sjtu.apex.searchWebDB.dataStructures.Query;
import sjtu.apex.searchWebDB.dataStructures.QueryGraph;
import sjtu.apex.searchWebDB.dataStructures.ResultItem;
import sjtu.apex.searchWebDB.dataStructures.ResultPage;
import sjtu.apex.searchWebDB.dataStructures.Source;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.btc.impl.GraphImpl;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.impl.DocStreamHintImpl;
import com.ibm.semplore.search.impl.SearchFactoryImpl;

public class SearchSessionService {

	private LinkedList<XFacetedResultSet> resultHistory = new LinkedList<XFacetedResultSet>();
	private XFacetedResultSet currentResult;
	private XFacetedResultSet lastResult;
	
	public void setString(String str) {
		AMFContext context = AMFContext.getCurrentContext();
		HttpSession HttpSession = context.getSession();
		ServletContext ServletContext = context.getServletContext();

		HttpServletRequest request = context.getRequest();
		HttpServletResponse response = context.getResponse();

		context.setSessionAttribute("attr",str);
	}
	
	public String getString() {
		AMFContext context = AMFContext.getCurrentContext();
		HttpSession HttpSession = context.getSession();
		ServletContext ServletContext = context.getServletContext();

		HttpServletRequest request = context.getRequest();
		HttpServletResponse response = context.getResponse();

		return (String)context.getSessionAttribute("attr");
	}
	
	/**
	 * This method returns a ResultPage object, representing the first page matching the query for
	 * the best source and respecting the number of result items per page. This ResultPage object
	 * contains a list of all sources that returned results, which in turn contain the number of
	 * results provided by each source. However, only the current source has associated facets
	 * elements. The number of results that should be given per page is also provided. In the case
	 * where the query has a set of blank nodes as result items, an empty result page is returned
	 * with the associated facets. Note: this method is only called for new searches. If there are
	 * no results at all, returns null. Parameters: query The Query object representing the query.
	 * nbResultsPerPage The number of results that should appear on each page.
	 * 
	 * @param query
	 *            The Query object representing the query.
	 * @param nbResultsPerPage
	 *            The number of results that should appear on each page.
	 * @return a ResultPage object, representing the first page matching the query for the best
	 *         source and respecting the number of result items per page.
	 */
	public ResultPage search(Query query, int nbResultsPerPage) throws Exception {
		if (query instanceof QueryGraph) {
			// TODO translate QueryGraph to GraphImpl for Semplore QueryEvaluator to execute
			return null;
		} else if (query instanceof Keywords) {
			Graph graph = new GraphImpl();
			Keywords k = (Keywords)query;
			LinkedList<String> wordList = k.getWordList();
			if (wordList.isEmpty()) return null;
			Iterator<String> it = wordList.iterator();
			String str = it.next();
			for (; it.hasNext(); ) str += " " + it.next();
			graph.add(SchemaFactoryImpl.getInstance().createKeywordCategory(str));	//0
			graph.setTargetVariable(0);
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			currentResult = eval.evaluate(graph);
			ArrayList<ResultItem> result = getResultList(currentResult);
			
			//TODO get facet and result count
			
			ResultPage ret = new ResultPage();
			ret.setActiveSource(new Source(graph.getDataSource(0), new LinkedList<Facet>(), 0));
			ret.setPageNum(1);
			LinkedList<ResultItem> resultItemList = new LinkedList<ResultItem>();
			for (int i = 0; i < nbResultsPerPage; i++) resultItemList.add(result.get(i));
			ret.setResultItemList(resultItemList);
			LinkedList<Source> sourceList = new LinkedList<Source>();
			HashSet<Source> sourceSet = new HashSet<Source>();
			for (Source s : sourceList) sourceSet.add(s);
			for (Source s : sourceSet) sourceList.add(s);
			ret.setSourceList(sourceList);
			SemplorePool.release(id);
			return ret;
		} else {
			return null;
		}
		
	}

	/**
	 * This method returns a page of results that matches the current query for the source
	 * specified. The number of the wanted page and the number of results that should be given per
	 * page are also provided. The last argument is set to true if the current source should include
	 * its associated facets. The other sources need not to be specified. Note: this method is only
	 * called for an existing search. If it is impossible to return the result page, then returns
	 * null.
	 * 
	 * @param source
	 *            The name of the source to consider.
	 * @param pageNum
	 *            Number identifying the page to be served. Number 1 is the first page
	 * @param nbResultsPerPage
	 *            The number of results that should appear on each page.
	 * @param needFacets
	 *            True if the facets associated with the source shall be provided.
	 * @return a page of results that matches the current query for the source specified.
	 */
	public ResultPage getPage(String source, int pageNum, int nbResultsPerPage, Boolean needFacets) throws Exception {
		ArrayList<ResultItem> result = getResultList(currentResult);
		ResultPage ret = new ResultPage();
		if (!needFacets) ret.setActiveSource(new Source(source, new LinkedList<Facet>(), 0));
		else {
			//TODO
		}
		ret.setPageNum(pageNum);
		LinkedList<ResultItem> resultItemList = new LinkedList<ResultItem>();
		for (int i = (pageNum-1)*nbResultsPerPage; i < pageNum*nbResultsPerPage; i++) resultItemList.add(result.get(i));
		ret.setResultItemList(resultItemList);
		return ret;
		
	}

	/**
	 * This method returns a ResultPage object representing the first page matching the query for
	 * the best source and respecting the number of result items per page. This ResultPage object
	 * contains a list of all sources that returned results, which in turn contain the number of
	 * results provided by each source. However, only the current source has associated facets
	 * elements. Note that this Query object is only the refinement element of an existing query
	 * kept into memory. The number of results that should be given per page is also provided. In
	 * the case where the query has a set of blank nodes as result items, an empty result page is
	 * returned with the associated facets. Note: this method is only called for existing searches.
	 * If there are no results at all, returns null.
	 * 
	 * @param query
	 *            The Query object representing the query refinement.
	 * @param nbResultsPerPage
	 *            The number of results that should appear on each page.
	 * @return
	 */
	public ResultPage refine(Query query, int nbResultsPerPage) {
		lastResult = currentResult;
		if (query instanceof QueryGraph) {
			//TODO
			return null;
		} else if (query instanceof Keywords) {
			Graph graph = new GraphImpl();
			Keywords k = (Keywords)query;
			LinkedList<String> wordList = k.getWordList();
			if (wordList.isEmpty()) return null;
			Iterator<String> it = wordList.iterator();
			String str = it.next();
			for (; it.hasNext(); ) str += " " + it.next();
			graph.add(SchemaFactoryImpl.getInstance().createKeywordCategory(str));	//0
			graph.setTargetVariable(0);
			SearchFactory searchFactory = SearchFactoryImpl.getInstance();
			SearchHelper helper = searchFactory.createSearchHelper();
			
			helper.setHint(SearchHelper.START_CACHE_HINT, 0, new DocStreamHintImpl(currentResult.getResultStream()));
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			currentResult = eval.evaluate(graph, helper);

			ArrayList<ResultItem> result = getResultList(currentResult);
			
			//TODO get facet and result count
			
			ResultPage ret = new ResultPage();
			ret.setActiveSource(new Source(graph.getDataSource(0), new LinkedList<Facet>(), 0));
			ret.setPageNum(1);
			LinkedList<ResultItem> resultItemList = new LinkedList<ResultItem>();
			for (int i = 0; i < nbResultsPerPage; i++) resultItemList.add(result.get(i));
			ret.setResultItemList(resultItemList);
			LinkedList<Source> sourceList = new LinkedList<Source>();
			HashSet<Source> sourceSet = new HashSet<Source>();
			for (Source s : sourceList) sourceSet.add(s);
			for (Source s : sourceSet) sourceList.add(s);
			ret.setSourceList(sourceList);
			SemplorePool.release(id);
			return ret;

		} else {
			return null;
		}
		
	}

	/**
	 * This method undoes the last refinement that was applied and returns a ResultPage object
	 * representing the first page matching the query for the best source and respecting the number
	 * of result items per page. This ResultPage object contains a list of all sources that returned
	 * results, which in turn contain the number of results provided by each source. However, only
	 * the current source has associated facets elements. In the case where the query has a set of
	 * blank nodes as result items, an empty result page is returned with the associated facets.
	 * Note: this method is only called for an existing search. If it is impossible to return the
	 * result page, then returns null.
	 * 
	 * @param nbResultsPerPage
	 *            The number of results that should appear on each page.
	 * @return
	 */
	public ResultPage undoLastRefinement(int nbResultsPerPage) {
		currentResult = lastResult;
		ArrayList<ResultItem> result = getResultList(currentResult);
		
		//TODO get facet and result count
		
		ResultPage ret = new ResultPage();
		ret.setActiveSource(new Source(graph.getDataSource(0), new LinkedList<Facet>(), 0));
		ret.setPageNum(1);
		LinkedList<ResultItem> resultItemList = new LinkedList<ResultItem>();
		for (int i = 0; i < nbResultsPerPage; i++) resultItemList.add(result.get(i));
		ret.setResultItemList(resultItemList);
		LinkedList<Source> sourceList = new LinkedList<Source>();
		HashSet<Source> sourceSet = new HashSet<Source>();
		for (Source s : sourceList) sourceSet.add(s);
		for (Source s : sourceSet) sourceList.add(s);
		ret.setSourceList(sourceList);

	}
	
	/**
	 * This method clears all results previously stored. This is typically done before a new search
	 * or when the user¡¯s session expires.
	 */
	public void clear() {
		
	}
	
	/**
	 * This method asks for the SeeAlso object associated with a result item. Note: this method is
	 * only called for an existing search. If it is impossible to return the SeeAlso object, then
	 * returns null.
	 * 
	 * @param resultItemURL
	 *            URL identifying the result item.
	 * @return the SeeAlso object associated with the result item.
	 */
	public SeeAlso getSeeAlsoItem(String resultItemURL) {
//		int id = SemplorePool.acquire();
//		QueryEvaluator eval = SemplorePool.getEvaluator(id);
//		ArrayList<SchemaObjectInfoForMultiDataSources> array = eval.getSeeAlso("dblp", 1, resultItemURL);
//		SemplorePool.release(id);
//		
//		SeeAlso seeAlso = new SeeAlso();
//		
//		LinkedList ll = new LinkedList();
//		for(SchemaObjectInfoForMultiDataSources s : array) {
//			Instance ins = new Instance();
//			ins.setLabel(s.getLabel());
//			Source source = new Source();
//			source.setName(s.getDataSource());
//			ins.setSource(source);
//			ins.setURI(s.getURI());
//			ll.add(ins);
//		}
//		seeAlso.setFacetList(ll);
//		ResultPage rp;
//		seeAlso.setResultItem(this.currentResult);
//		return seeAlso;
		return null;
	}

	/**
	 * This method asks for the ArraySnippet object associated with a result item. Note: this method
	 * is only called for an existing search. If it is impossible to return the ArraySnippet object,
	 * then returns null.
	 * 
	 * @param resultItemURL
	 *            URL identifying the result item.
	 * @return the ArraySnippet object associated with the result item.
	 */
	public ArraySnippet getArraySnippet(int resultItemURL) {
//		int id = SemplorePool.acquire();
//		QueryEvaluator eval = SemplorePool.getEvaluator(id);
//		
//		eval.getArraySnippet(, this.currentResult.getDocID(3), resultItemURL);
//		SemplorePool.release(id);
//		
//		ArraySnippet as = new ArraySnippet();
//		as.get
//		
		
		return null;
	}

}
