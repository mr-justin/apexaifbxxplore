package org.team.xxplore.core.service.search.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.team.xxplore.core.service.search.datastructure.ArraySnippet;
import org.team.xxplore.core.service.search.datastructure.Concept;
import org.team.xxplore.core.service.search.datastructure.Facet;
import org.team.xxplore.core.service.search.datastructure.Instance;
import org.team.xxplore.core.service.search.datastructure.Keywords;
import org.team.xxplore.core.service.search.datastructure.Query;
import org.team.xxplore.core.service.search.datastructure.QueryGraph;
import org.team.xxplore.core.service.search.datastructure.Relation;
import org.team.xxplore.core.service.search.datastructure.ResultItem;
import org.team.xxplore.core.service.search.datastructure.ResultPage;
import org.team.xxplore.core.service.search.datastructure.SeeAlso;
import org.team.xxplore.core.service.search.datastructure.Source;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources;
import com.ibm.semplore.btc.impl.GraphImpl;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.xir.DocStream;

import flex.messaging.FlexContext;

public class SearchSessionService {

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
			graph.setDataSource(0, "wordnet");
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			if (eval == null) System.err.println("Evaluator not exist");
			XFacetedResultSetForMultiDataSources result = eval.evaluate(graph);
			SemplorePool.release(id);
			LinkedList<XFacetedResultSetForMultiDataSources> toSession = new LinkedList<XFacetedResultSetForMultiDataSources>();
			toSession.add(result);
			ResultPage ret = transform(result, 1, nbResultsPerPage);
			if (FlexContext.getFlexSession() != null) FlexContext.getFlexSession().setAttribute("resultHistory", toSession);
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
		if (FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession().getAttribute("resultHistory");
		XFacetedResultSetForMultiDataSources currentResult = resultHistory.getLast();
		ResultPage ret = transform(currentResult, pageNum, nbResultsPerPage);
		return ret;
		
	}

	protected ResultPage transform(XFacetedResultSetForMultiDataSources xres, int pageNum, int nbResultsPerPage) {
		try {
			//set active source
			Source activeSource = new Source(xres.getCurrentDataSource(), null, xres.getLength());
			LinkedList<Facet> facetList = new LinkedList<Facet>();
			//add category facets
			com.ibm.semplore.search.Facet[] semploreFacets = xres.getCategoryFacets();
			for (int i=0; i<semploreFacets.length; i++) {
				SchemaObjectInfo info = semploreFacets[i].getInfo();
				Facet facet = new Concept(info.getLabel(), info.getURI(), activeSource);
				facet.setResultNb(semploreFacets[i].getCount());
				facetList.add(facet);
			}
			//add relation facets
			semploreFacets = xres.getRelationFacets();
			for (int i=0; i<semploreFacets.length; i++) {
				SchemaObjectInfo info = semploreFacets[i].getInfo();
				Facet facet = new Relation(info.getLabel(), info.getURI(), activeSource);
				facet.setResultNb(semploreFacets[i].getCount());
				facetList.add(facet);
			}
			activeSource.setFacetList(facetList);
			
			//always make the active source the first element of the source list
			LinkedList<Source> sourceList = new LinkedList<Source>();
			sourceList.add(activeSource);
			
			//set the other sources of the source list
			for (Entry<String, Integer> entry:xres.getDataSourceFacets().entrySet()) {
				Source s = new Source(entry.getKey(),null,entry.getValue());
				sourceList.add(s);
			}
			
			//set result item list
			LinkedList<ResultItem> resultItemList = new LinkedList<ResultItem>();
			//e.g. pageNum=2, nbResultsPerPage=5 ==> start=5, end=10
			int start = (pageNum-1)*nbResultsPerPage;
			int end = start+nbResultsPerPage;
			for (int i=start; i<end && i < xres.getLength(); i++) {
				SchemaObjectInfo info = xres.getResult(i);
				ResultItem item = new ResultItem(info.getURI(), xres.getScore(i), activeSource, "text document", info.getLabel(), xres.getSnippet(i));
				resultItemList.add(item);
			}
			
			ResultPage page = new ResultPage(resultItemList, sourceList, activeSource, pageNum);
			return page;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
	public ResultPage refine(Query query, int nbResultsPerPage) throws Exception {
		if (FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession().getAttribute("resultHistory");
		XFacetedResultSetForMultiDataSources currentResult = resultHistory.getLast();
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
			HashMap<Integer,DocStream> helper = new HashMap<Integer,DocStream>();
			helper.put(0, currentResult.getResultStream());
			
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			XFacetedResultSetForMultiDataSources newResult = eval.evaluate(graph, helper);
			SemplorePool.release(id);
			
			ResultPage ret = transform(newResult, 1, nbResultsPerPage);
			resultHistory.add(newResult);
			FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
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
		if (FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession().getAttribute("resultHistory");
		if (resultHistory.isEmpty()) return null;
		resultHistory.removeLast();
		XFacetedResultSetForMultiDataSources result = resultHistory.getLast();
		ResultPage ret = transform(result, 1, nbResultsPerPage);
		FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
		return ret;

	}
	
	/**
	 * This method clears all results previously stored. This is typically done before a new search
	 * or when the users session expires.
	 */
	public void clear() {
		FlexContext.getFlexSession().setAttribute("resultHistory", null);
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
		try {
			if (FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
				(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession().getAttribute("resultHistory");
			XFacetedResultSetForMultiDataSources currentResult = resultHistory.getLast();
			
			ArrayList<ResultItem> result = getResultList(currentResult);
			int index;
			for(index = 0;index < result.size();index++) {
				if(result.get(index).getURL().equals(resultItemURL)) {
					break;
				}
			}
			
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			ArrayList<SchemaObjectInfoForMultiDataSources> array = eval.getSeeAlso(currentResult.getCurrentDataSource(), currentResult.getDocID(index), resultItemURL);
			SemplorePool.release(id);
			
			SeeAlso seeAlso = new SeeAlso();
			
			LinkedList ll = new LinkedList();
			for(SchemaObjectInfoForMultiDataSources s : array) {
				Instance ins = new Instance();
				ins.setLabel(s.getLabel());
				Source source = new Source();
				source.setName(s.getDataSource());
				ins.setSource(source);
				ins.setURI(s.getURI());
				ll.add(ins);
			}
			seeAlso.setFacetList(ll);

			return seeAlso;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ArrayList<ResultItem> getResultList(XFacetedResultSetForMultiDataSources xres) {
		try {
			//set active source
			Source activeSource = new Source(xres.getCurrentDataSource(), null, xres.getLength());
			LinkedList<Facet> facetList = new LinkedList<Facet>();
			//add category facets
			com.ibm.semplore.search.Facet[] semploreFacets = xres.getCategoryFacets();
			for (int i=0; i<semploreFacets.length; i++) {
				SchemaObjectInfo info = semploreFacets[i].getInfo();
				Facet facet = new Facet(info.getLabel(), info.getURI(), activeSource);
				facetList.add(facet);
			}
			//add relation facets
			semploreFacets = xres.getRelationFacets();
			for (int i=0; i<semploreFacets.length; i++) {
				SchemaObjectInfo info = semploreFacets[i].getInfo();
				Facet facet = new Facet(info.getLabel(), info.getURI(), activeSource);
				facetList.add(facet);
			}
			activeSource.setFacetList(facetList);
			
			//always make the active source the first element of the source list
			LinkedList<Source> sourceList = new LinkedList<Source>();
			sourceList.add(activeSource);
			
			//set the other sources of the source list
			for (Entry<String, Integer> entry:xres.getDataSourceFacets().entrySet()) {
				Source s = new Source(entry.getKey(),null,entry.getValue());
				sourceList.add(s);
			}
			
			//set result item list
			ArrayList<ResultItem> resultItemList = new ArrayList<ResultItem>();
			//e.g. pageNum=2, nbResultsPerPage=5 ==> start=5, end=10

			for (int i=0; i<xres.getLength(); i++) {
				SchemaObjectInfo info = xres.getResult(i);
				ResultItem item = new ResultItem(info.getURI(), xres.getScore(i), activeSource, "text document", info.getLabel(), xres.getSnippet(i));
				resultItemList.add(item);
			}
			return resultItemList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
	public ArraySnippet getArraySnippet(String resultItemURL) {
		try {
			if (FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
				(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession().getAttribute("resultHistory");
			XFacetedResultSetForMultiDataSources currentResult = resultHistory.getLast();
			
			ArrayList<ResultItem> result = getResultList(currentResult);
			int index;
			for(index = 0;index < result.size();index++) {
				if(result.get(index).getURL().equals(resultItemURL)) {
					break;
				}
			}
			
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			String snippet_str = eval.getArraySnippet(currentResult.getCurrentDataSource(), currentResult.getDocID(index), resultItemURL);
			SemplorePool.release(id);
			
			ArraySnippet as = this.getSnippet(snippet_str);
			return as;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private ArraySnippet getSnippet(String snippet_str) {
		return null;
	}
	
	public static void testSearch(String keywords) throws Exception {
		LinkedList<String> kwords = new LinkedList<String>();
		kwords.add(keywords);
		ResultPage rp = new SearchSessionService().search(new Keywords(kwords), 10);
		System.out.println("Result Page:");
		Source s = rp.getActiveSource();
		System.out.println("\tActive Source:" + s.getName());
		LinkedList<Facet> facetList = s.getFacetList();
		System.out.println("\t\tFacets:");
		for (Facet f : facetList) {
			System.out.println("\t\t\t" + f.getLabel() + "\t" + f.getResultNb() + "\t" + f.getURI());
		}
		System.out.println("\t\tResult count:" + s.getResultCount());
		int pageNum = rp.getPageNum();
		System.out.println("\tPage Num:" + pageNum);
		LinkedList<ResultItem> resultItemList = rp.getResultItemList();
		System.out.println("\tResult Items:");
		for (ResultItem ri : resultItemList) {
			System.out.println("\t\t" + ri.getScore() + "\t" + ri.getSnippet() + "\t" + ri.getTitle() + "\t" + ri.getType() + "\t" + ri.getURL());
		}
		LinkedList<Source> sourceList = rp.getSourceList();
		System.out.println("\tSources:");
		for (Source ss : sourceList) {
			System.out.println("\t\t" + ss.getName());
			LinkedList<Facet> fl = ss.getFacetList();
			System.out.println("\t\tFacets:");
			for (Facet f : fl) {
				System.out.println("\t\t\t" + f.getLabel() + "\t" + f.getResultNb() + "\t" + f.getURI());
			}
			System.out.println("\t\tResult count:" + ss.getResultCount());
		}

	}
	
	public static void main(String[] args) throws Exception {
		testSearch("test");
	}
}
