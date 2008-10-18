package org.team.xxplore.core.service.search.session;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.team.xxplore.core.service.search.datastructure.ArraySnippet;
import org.team.xxplore.core.service.search.datastructure.Attribute;
import org.team.xxplore.core.service.search.datastructure.Concept;
import org.team.xxplore.core.service.search.datastructure.ConceptSuggestion;
import org.team.xxplore.core.service.search.datastructure.Couple;
import org.team.xxplore.core.service.search.datastructure.Facet;
import org.team.xxplore.core.service.search.datastructure.GraphEdge;
import org.team.xxplore.core.service.search.datastructure.Instance;
import org.team.xxplore.core.service.search.datastructure.Keywords;
import org.team.xxplore.core.service.search.datastructure.Litteral;
import org.team.xxplore.core.service.search.datastructure.Query;
import org.team.xxplore.core.service.search.datastructure.QueryGraph;
import org.team.xxplore.core.service.search.datastructure.Relation;
import org.team.xxplore.core.service.search.datastructure.RelationSuggestion;
import org.team.xxplore.core.service.search.datastructure.ResultItem;
import org.team.xxplore.core.service.search.datastructure.ResultPage;
import org.team.xxplore.core.service.search.datastructure.SeeAlso;
import org.team.xxplore.core.service.search.datastructure.Source;
import org.team.xxplore.core.service.search.datastructure.Suggestion;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources;
import com.ibm.semplore.btc.impl.GraphImpl;
import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.EnumerationCategory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;
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
			QueryGraph qg = (QueryGraph)query;
			Facet targetNode = qg.getTargetVariable();
			LinkedList<Facet> nodeList = qg.getVertexList();
			LinkedList<GraphEdge> edgeList = qg.getEdgeList();
			Graph graph = new GraphImpl();
			HashMap<Facet, Integer> indexMap = new HashMap<Facet, Integer>();
			int index = 0;
			for (Facet f : nodeList) {
				if (f.equals(targetNode)) {
					CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(1);	// AND
					if (f instanceof Concept) {
						Concept c = (Concept)f;
						cc.addComponentCategory(SchemaFactoryImpl.getInstance().createCategory(Md5_BloomFilter_64bit.URItoID(c.getURI())));
					} else if (f instanceof Attribute) {
						Attribute a = (Attribute)f;
						cc.addComponentCategory(SchemaFactoryImpl.getInstance().createAttributeKeywordCategory(a.getURI(), a.getLabel()));
					} else if (f instanceof Instance) {
						Instance i = (Instance)f;
						EnumerationCategory ec = SchemaFactoryImpl.getInstance().createEnumerationCategory()
							.addInstanceElement(SchemaFactoryImpl.getInstance()
									.createInstance(Md5_BloomFilter_64bit.URItoID(i.getURI())));
						cc.addComponentCategory(ec);
					} else if (f instanceof Litteral) {
						Litteral l = (Litteral)f;
						cc.addComponentCategory(SchemaFactoryImpl.getInstance().createKeywordCategory(l.getLabel()));
					}
					graph.add(cc);
					graph.setTargetVariable(index);
					graph.setDataSource(index, f.getSource().getName());
				} else {
					if (f instanceof Concept) {
						Concept c = (Concept)f;
						graph.add(SchemaFactoryImpl.getInstance().createCategory(Md5_BloomFilter_64bit.URItoID(c.getURI())));
					} else if (f instanceof Attribute) {
						Attribute a = (Attribute)f;
						graph.add(SchemaFactoryImpl.getInstance().createAttributeKeywordCategory(a.getURI(), a.getLabel()));
					} else if (f instanceof Instance) {
						Instance i = (Instance)f;
						EnumerationCategory ec = SchemaFactoryImpl.getInstance().createEnumerationCategory()
							.addInstanceElement(SchemaFactoryImpl.getInstance()
								.createInstance(Md5_BloomFilter_64bit.URItoID(i.getURI())));
						graph.add(ec);
					} else if (f instanceof Litteral) {
						Litteral l = (Litteral)f;
						graph.add(SchemaFactoryImpl.getInstance().createKeywordCategory(l.getLabel()));
					}
					graph.setDataSource(index, f.getSource().getName());
				}
				indexMap.put(f, index);
				index++;
			}
			for (GraphEdge e : edgeList) {
				int from = indexMap.get(e.getFromElement());
				int to = indexMap.get(e.getToElement());
				Relation rel = (Relation)e.getDecorationElement();
				graph.add(SchemaFactoryImpl.getInstance().createRelation(Md5_BloomFilter_64bit.URItoID(rel.getURI())), from, to);
			}
			
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			XFacetedResultSetForMultiDataSources result = eval.evaluate(graph);
			SemplorePool.release(id);

			LinkedList<Operation> operationHistory = new LinkedList<Operation>();
			operationHistory.add(new QueryGraphOperation(new GraphImpl(), graph));
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory 
				= new LinkedList<XFacetedResultSetForMultiDataSources>();
			resultHistory.add(result);
			if (FlexContext.getFlexSession() != null) {
				FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
				FlexContext.getFlexSession().setAttribute("currentGraph", graph);
				FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
			}
			ResultPage ret = transform(result, 1, nbResultsPerPage);
			return ret;
	
		} else if (query instanceof Keywords) {
			Graph graph = new GraphImpl();
			Keywords k = (Keywords)query;
			LinkedList<String> wordList = k.getWordList();
			if (wordList.isEmpty()) return null;
			Iterator<String> it = wordList.iterator();
			String str = it.next();
			for (; it.hasNext(); ) str += " " + it.next();
			CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(1);	// AND
			cc.addComponentCategory(SchemaFactoryImpl.getInstance().createKeywordCategory(str));
			graph.add(cc);	//0
			graph.setTargetVariable(0);
			graph.setDataSource(0, (String)Config.readDSConfigFile("config"+File.separatorChar+"datasrc.cfg").get("defaultDataSet"));
			
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			XFacetedResultSetForMultiDataSources result = eval.evaluate(graph);
			SemplorePool.release(id);
			
			LinkedList<Operation> operationHistory = new LinkedList<Operation>();
			operationHistory.add(new KeywordsOperation(k));
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory 
				= new LinkedList<XFacetedResultSetForMultiDataSources>();
			resultHistory.add(result);
			if (FlexContext.getFlexSession() != null) {
				FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
				FlexContext.getFlexSession().setAttribute("currentGraph", graph);
				FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
			}
			ResultPage ret = transform(result, 1, nbResultsPerPage);
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
	 * @param pageNum
	 *            Number identifying the page to be served. Number 1 is the first page
	 * @param nbResultsPerPage
	 *            The number of results that should appear on each page.
	 * @return a page of results that matches the current query for the source specified.
	 */
	public ResultPage getPage(int pageNum, int nbResultsPerPage) throws Exception {
		if (FlexContext.getFlexSession() == null 
				|| FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession()
				.getAttribute("resultHistory");
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
			HashMap<String, Integer> sourceFacets = xres.getDataSourceFacets();
			if (sourceFacets != null) for (Entry<String, Integer> entry:sourceFacets.entrySet()) {
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
				ResultItem item = new ResultItem(info.getURI(), xres.getScore(i), "text document", info.getLabel(), xres.getSnippet(i));
				resultItemList.add(item);
			}
			
			ResultPage page = new ResultPage(resultItemList, activeSource, pageNum);
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
		if (FlexContext.getFlexSession() == null 
				|| FlexContext.getFlexSession().getAttribute("operationHistory") == null
				|| FlexContext.getFlexSession().getAttribute("currentGraph") == null
				|| FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
		LinkedList<Operation> operationHistory = (LinkedList<Operation>)FlexContext.getFlexSession()
			.getAttribute("operationHistory");
		Graph currentGraph = (Graph)FlexContext.getFlexSession().getAttribute("currentGraph");
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory
			= (LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession()
				.getAttribute("resultHistory");
		XFacetedResultSetForMultiDataSources currentResult = resultHistory.getLast();

		if (query instanceof QueryGraph) {
			//TODO
			return null;
		} else if (query instanceof Keywords) {
			Keywords k = (Keywords)query;
			KeywordsOperation ko = new KeywordsOperation(k);
			String ds = currentGraph.getDataSource(currentGraph.getTargetVariable());
			currentGraph = ko.applyTo(currentGraph);
			operationHistory.add(ko);
			FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
			FlexContext.getFlexSession().setAttribute("currentGraph", currentGraph);
			
			Graph graph = new GraphImpl();
			LinkedList<String> wordList = k.getWordList();
			if (wordList.isEmpty()) return null;
			Iterator<String> it = wordList.iterator();
			String str = it.next();
			for (; it.hasNext(); ) str += " " + it.next();
			graph.add(SchemaFactoryImpl.getInstance().createKeywordCategory(str));	//0
			graph.setTargetVariable(0);
			graph.setDataSource(0, ds);
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

		} else if (query instanceof Facet) {
			if (query instanceof Concept) {
				Concept c = (Concept)query;
				ConceptOperation co = new ConceptOperation(c);
				String ds = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = co.applyTo(currentGraph);
				operationHistory.add(co);
				FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
				FlexContext.getFlexSession().setAttribute("currentGraph", currentGraph);
				
				Graph graph = new GraphImpl();
				graph.add(SchemaFactoryImpl.getInstance()
						.createCategory(Md5_BloomFilter_64bit.URItoID(c.getURI())));	//0
				graph.setTargetVariable(0);
				graph.setDataSource(0, ds);
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

			} else if (query instanceof Relation) {
				Relation r = (Relation)query;
				RelationOperation ro = new RelationOperation(r);
				String ds = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = ro.applyTo(currentGraph);
				operationHistory.add(ro);
				FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
				FlexContext.getFlexSession().setAttribute("currentGraph", currentGraph);
				
				Graph graph = new GraphImpl();
				graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
				graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
				graph.add(SchemaFactoryImpl.getInstance().createRelation(Md5_BloomFilter_64bit.URItoID(r.getURI())), 0, 1);
				graph.setTargetVariable(1);
				graph.setDataSource(1, ds);
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
		} else if (query instanceof Suggestion) {
			if (query instanceof ConceptSuggestion) {
				ConceptSuggestion c = (ConceptSuggestion)query;
				ConceptSuggestionOperation cso = new ConceptSuggestionOperation(c);
				String currentDataSource = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = cso.applyTo(currentGraph);
				operationHistory.add(cso);
				FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
				FlexContext.getFlexSession().setAttribute("currentGraph", currentGraph);
				
				if (c.getSource().getName().equals(currentDataSource)) {
					Graph graph = new GraphImpl();
					graph.add(SchemaFactoryImpl.getInstance()
							.createCategory(Md5_BloomFilter_64bit.URItoID(c.getURI())));	//0
					graph.setTargetVariable(0);
					graph.setDataSource(0, currentDataSource);
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
					int id = SemplorePool.acquire();
					QueryEvaluator eval = SemplorePool.getEvaluator(id);
					XFacetedResultSetForMultiDataSources newResult = eval.evaluate(currentGraph);
					SemplorePool.release(id);
					
					ResultPage ret = transform(newResult, 1, nbResultsPerPage);
					resultHistory.add(newResult);
					FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
					return ret;
					
				}
				
			} else if (query instanceof RelationSuggestion) {
				RelationSuggestion r = (RelationSuggestion)query;
				RelationSuggestionOperation rso = new RelationSuggestionOperation(r);
				String currentDataSource = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = rso.applyTo(currentGraph);
				operationHistory.add(rso);
				FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
				FlexContext.getFlexSession().setAttribute("currentGraph", currentGraph);
				
				if (r.getSource().getName().equals(currentDataSource)) {
					Graph graph = new GraphImpl();
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
					graph.add(SchemaFactoryImpl.getInstance().createRelation(Md5_BloomFilter_64bit.URItoID(r.getURI())), 0, 1);
					graph.setTargetVariable(1);
					graph.setDataSource(1, currentDataSource);
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
					int id = SemplorePool.acquire();
					QueryEvaluator eval = SemplorePool.getEvaluator(id);
					XFacetedResultSetForMultiDataSources newResult = eval.evaluate(currentGraph);
					SemplorePool.release(id);
					
					ResultPage ret = transform(newResult, 1, nbResultsPerPage);
					resultHistory.add(newResult);
					FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
					return ret;
					
				}
			} else {
				return null;
			}
		}
		else {
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
		if (FlexContext.getFlexSession() == null || FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)FlexContext.getFlexSession().getAttribute("resultHistory");
		LinkedList<Operation> operationHistory = (LinkedList<Operation>)FlexContext.getFlexSession().getAttribute("operationHistory");
		Graph currentGraph = (Graph)FlexContext.getFlexSession().getAttribute("currentGraph");
		
		if (resultHistory.isEmpty()) return null;
		if (operationHistory.isEmpty()) return null;
		if (currentGraph == null) return null;
		
		resultHistory.removeLast();
		Operation lastOperation = operationHistory.removeLast();
		currentGraph = lastOperation.undo(currentGraph);
		XFacetedResultSetForMultiDataSources result = resultHistory.getLast();
		ResultPage ret = transform(result, 1, nbResultsPerPage);
		FlexContext.getFlexSession().setAttribute("resultHistory", resultHistory);
		FlexContext.getFlexSession().setAttribute("operationHistory", operationHistory);
		FlexContext.getFlexSession().setAttribute("currentGraph", currentGraph);
		return ret;

	}
	
	/**
	 * This method clears all results previously stored. This is typically done before a new search
	 * or when the users session expires.
	 */
	public void clear() {
		FlexContext.getFlexSession().setAttribute("resultHistory", null);
		FlexContext.getFlexSession().setAttribute("operationHistory", null);
		FlexContext.getFlexSession().setAttribute("currentGraph", null);
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
			if (FlexContext.getFlexSession() == null || FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
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
			HashMap<String, Integer> sourceFacets = xres.getDataSourceFacets();
			if (sourceFacets != null) for (Entry<String, Integer> entry:sourceFacets.entrySet()) {
				Source s = new Source(entry.getKey(),null,entry.getValue());
				sourceList.add(s);
			}
			
			//set result item list
			ArrayList<ResultItem> resultItemList = new ArrayList<ResultItem>();
			//e.g. pageNum=2, nbResultsPerPage=5 ==> start=5, end=10

			for (int i=0; i<xres.getLength(); i++) {
				SchemaObjectInfo info = xres.getResult(i);
				ResultItem item = new ResultItem(info.getURI(), xres.getScore(i), "text document", info.getLabel(), xres.getSnippet(i));
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
			if (FlexContext.getFlexSession() == null || FlexContext.getFlexSession().getAttribute("resultHistory") == null) return null;
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
			
			ArraySnippet as = this.getSnippet(resultItemURL, snippet_str);
			return as;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private ArraySnippet getSnippet(String resultItemURL, String snippet_str) {
		ResultItem item = new ResultItem();
		item.setURL(resultItemURL);
		LinkedList<Couple> rel = new LinkedList<Couple>();
		LinkedList<Couple> attr = new LinkedList<Couple>();
		LinkedList<Concept> cat = new LinkedList<Concept>();
		String[] labels = new String[3];
		
		if (snippet_str != null) {
			StringTokenizer tok = new StringTokenizer(snippet_str,"\n");
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				String type = Util4NT.checkSnippetType(token);
				String[] processed = Util4NT.processTripleLine("<a> "+token);
				for (int i = 1; i<=2; i++) {
					if ((i==1 && type==Util4NT.CATEGORY)||(i==2 && type==Util4NT.ATTRIBUTE)) continue;
					if (processed[i].startsWith("<")) processed[i] = processed[i].substring(1);
					if (processed[i].endsWith(">")) processed[i] = processed[i].substring(0, processed[i].length()-1);
					labels[i] = Util4NT.getDefaultLabel(processed[i]);
				}
				
				if (type==Util4NT.CATEGORY) {
					cat.add(new Concept(labels[2],processed[2],null));
				} else if (type==Util4NT.RELATION) {
					rel.add(new Couple(new Relation(labels[1],processed[1],null), new Instance(labels[2],processed[2],null)));
				} else if (type==Util4NT.ATTRIBUTE) {
					attr.add(new Couple(new Attribute(labels[1],processed[1],null), new Litteral(processed[2],null,null)));
				}
			}
		}
		return new ArraySnippet(item, rel, attr, cat);
	}
	
	public static void testSearch(String keywords) throws Exception {
		LinkedList<String> kwords = new LinkedList<String>();
		kwords.add(keywords);
		ResultPage rp = new SearchSessionService().search(new Keywords(kwords), 10);
		System.out.println("Result Page:");
		Source s = rp.getSource();
		System.out.println("\tSource:" + s.getName());
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

	}
	
	public static void main(String[] args) throws Exception {
		testSearch("test");
	}
}
