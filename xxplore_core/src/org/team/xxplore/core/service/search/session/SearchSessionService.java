package org.team.xxplore.core.service.search.session;

import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.team.xxplore.core.service.search.datastructure.*;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.search.XFacetedResultSet;

public class SearchSessionService {

	private XFacetedResultSet currentResult;
	
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
	public ResultPage search(Query query, int nbResultsPerPage) {
		return null;
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
	 *            Number identifying the page to be served.
	 * @param nbResultsPerPage
	 *            The number of results that should appear on each page.
	 * @param needFacets
	 *            True if the facets associated with the source shall be provided.
	 * @return a page of results that matches the current query for the source specified.
	 */
	public ResultPage getPage(String source, int pageNum, int nbResultsPerPage, Boolean needFacets) {
		return null;
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
		return null;
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
		return null;
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
