package org.apexlab.service.session.test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apexlab.service.session.datastructure.ArraySnippet;
import org.apexlab.service.session.datastructure.Attribute;
import org.apexlab.service.session.datastructure.Concept;
import org.apexlab.service.session.datastructure.ConceptSuggestion;
import org.apexlab.service.session.datastructure.Couple;
import org.apexlab.service.session.datastructure.Facet;
import org.apexlab.service.session.datastructure.GraphEdge;
import org.apexlab.service.session.datastructure.Instance;
import org.apexlab.service.session.datastructure.Keywords;
import org.apexlab.service.session.datastructure.Litteral;
import org.apexlab.service.session.datastructure.Query;
import org.apexlab.service.session.datastructure.QueryGraph;
import org.apexlab.service.session.datastructure.Relation;
import org.apexlab.service.session.datastructure.RelationSuggestion;
import org.apexlab.service.session.datastructure.ResultItem;
import org.apexlab.service.session.datastructure.ResultPage;
import org.apexlab.service.session.datastructure.SeeAlso;
import org.apexlab.service.session.datastructure.Source;
import org.apexlab.service.session.datastructure.Suggestion;
import org.apexlab.service.session.semplore.ConceptOperation;
import org.apexlab.service.session.semplore.ConceptSuggestionOperation;
import org.apexlab.service.session.semplore.KeywordsOperation;
import org.apexlab.service.session.semplore.Operation;
import org.apexlab.service.session.semplore.QueryGraphOperation;
import org.apexlab.service.session.semplore.RelationOperation;
import org.apexlab.service.session.semplore.RelationSuggestionOperation;
import org.apexlab.service.session.semplore.SemplorePool;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources;
import com.ibm.semplore.btc.impl.GraphImpl;
import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.EnumerationCategory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.util.LRUHashMap;
import com.ibm.semplore.xir.DocStream;

public class SearchSessionService {
	static Logger logger = Logger.getLogger(SearchSessionService.class);
	static LRUHashMap<String, ArraySnippet> snippetCache = new LRUHashMap<String, ArraySnippet>(10000);
	static LRUHashMap<String, SeeAlso> seealsoCache = new LRUHashMap<String, SeeAlso>(10000);
	private HashMap<String, Object>	session = new HashMap<String, Object>();
	
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
			LinkedList<GraphEdge> mappingList = qg.getMappingList();
			Graph graph = new GraphImpl();
			HashMap<Facet, Integer> indexMap = new HashMap<Facet, Integer>();
			int index = 0;
			for (Facet f : nodeList) {
				if (f.equals(targetNode)) {
					CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(CompoundCategory.TYPE_AND);
					if (f instanceof Concept) {
						Concept c = (Concept)f;
						cc.addComponentCategory(SchemaFactoryImpl.getInstance().createCategory(c.getURI()));
					} else if (f instanceof Attribute) {
						Attribute a = (Attribute)f;
						cc.addComponentCategory(SchemaFactoryImpl.getInstance().createAttributeKeywordCategory(a.getURI(), a.getLabel()));
					} else if (f instanceof Instance) {
						Instance i = (Instance)f;
						EnumerationCategory ec = SchemaFactoryImpl.getInstance().createEnumerationCategory()
							.addInstanceElement(SchemaFactoryImpl.getInstance()
									.createInstance(i.getURI()));
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
						graph.add(SchemaFactoryImpl.getInstance().createCategory(c.getURI()));
					} else if (f instanceof Attribute) {
						Attribute a = (Attribute)f;
						graph.add(SchemaFactoryImpl.getInstance().createAttributeKeywordCategory(a.getURI(), a.getLabel()));
					} else if (f instanceof Instance) {
						Instance i = (Instance)f;
						EnumerationCategory ec = SchemaFactoryImpl.getInstance().createEnumerationCategory()
							.addInstanceElement(SchemaFactoryImpl.getInstance()
								.createInstance(i.getURI()));
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
				graph.add(SchemaFactoryImpl.getInstance().createRelation(rel.getURI()), from, to);
			}
			for (GraphEdge e : mappingList) {
				Integer from = indexMap.get(e.getFromElement());
				Integer to = indexMap.get(e.getToElement());
				if (from != null && to != null)
					graph.addIEdges(new Edge(from, to, null));
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
			if (session != null) {
				session.put("operationHistory", operationHistory);
				session.put("currentGraph", graph);
				session.put("resultHistory", resultHistory);
			}
			ResultPage ret = transform(result, 1, nbResultsPerPage);
			return ret;
	
		} else if (query instanceof Keywords) {
			Graph graph = new GraphImpl();
			Keywords k = (Keywords)query;
			LinkedList<String> wordList = k.getWordList();
			if (wordList.isEmpty()) return null;
			String datasource = (String)Config.readDSConfigFile("config"+File.separatorChar+"datasrc.cfg").get("defaultDataSet");
			Iterator<String> it = wordList.iterator();
			String str = it.next();
			for (; it.hasNext(); ) {
				String s = it.next();
				if (s.startsWith("xxds")) datasource = s.substring(4);
				else str += " " + s;
			}
			CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(1);	// AND
			cc.addComponentCategory(SchemaFactoryImpl.getInstance().createKeywordCategory(str));
			graph.add(cc);	//0
			graph.setTargetVariable(0);
			graph.setDataSource(0, datasource);
			
			int id = SemplorePool.acquire();
			QueryEvaluator eval = SemplorePool.getEvaluator(id);
			XFacetedResultSetForMultiDataSources result = eval.evaluate(graph);
			SemplorePool.release(id);
			
			LinkedList<Operation> operationHistory = new LinkedList<Operation>();
			operationHistory.add(new KeywordsOperation(k));
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory 
				= new LinkedList<XFacetedResultSetForMultiDataSources>();
			resultHistory.add(result);
			if (session != null) {
				session.put("operationHistory", operationHistory);
				session.put("currentGraph", graph);
				session.put("resultHistory", resultHistory);
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
		if (session == null 
				|| session.get("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)session
				.get("resultHistory");
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
				if (semploreFacets[i].isInverseRelation()) continue;
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
		if (session == null 
				|| session.get("operationHistory") == null
				|| session.get("currentGraph") == null
				|| session.get("resultHistory") == null) return null;
		LinkedList<Operation> operationHistory = (LinkedList<Operation>)session
			.get("operationHistory");
		Graph currentGraph = (Graph)session.get("currentGraph");
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory
			= (LinkedList<XFacetedResultSetForMultiDataSources>)session
				.get("resultHistory");
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
			session.put("operationHistory", operationHistory);
			session.put("currentGraph", currentGraph);
			
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
			session.put("resultHistory", resultHistory);
			return ret;

		} else if (query instanceof Facet) {
			if (query instanceof Concept) {
				Concept c = (Concept)query;
				ConceptOperation co = new ConceptOperation(c);
				String ds = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = co.applyTo(currentGraph);
				operationHistory.add(co);
				session.put("operationHistory", operationHistory);
				session.put("currentGraph", currentGraph);
				
				Graph graph = new GraphImpl();
				graph.add(SchemaFactoryImpl.getInstance()
						.createCategory(c.getURI()));	//0
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
				session.put("resultHistory", resultHistory);
				return ret;

			} else if (query instanceof Relation) {
				Relation r = (Relation)query;
				RelationOperation ro = new RelationOperation(r);
				String ds = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = ro.applyTo(currentGraph);
				operationHistory.add(ro);
				session.put("operationHistory", operationHistory);
				session.put("currentGraph", currentGraph);
				
				Graph graph = new GraphImpl();
				graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
				graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
				graph.add(SchemaFactoryImpl.getInstance().createRelation(r.getURI()), 0, 1);
				graph.setTargetVariable(1);
				graph.setDataSource(0, ds);
				graph.setDataSource(1, ds);
				HashMap<Integer,DocStream> helper = new HashMap<Integer,DocStream>();
				helper.put(0, currentResult.getResultStream());

				int id = SemplorePool.acquire();
				QueryEvaluator eval = SemplorePool.getEvaluator(id);
				XFacetedResultSetForMultiDataSources newResult = eval.evaluate(graph, helper);
				SemplorePool.release(id);

				ResultPage ret = transform(newResult, 1, nbResultsPerPage);
				resultHistory.add(newResult);
				session.put("resultHistory", resultHistory);
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
				session.put("operationHistory", operationHistory);
				session.put("currentGraph", currentGraph);
				
				if (c.getSource().getName().equals(currentDataSource)) {
					Graph graph = new GraphImpl();
					graph.add(SchemaFactoryImpl.getInstance()
							.createCategory(c.getURI()));	//0
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
					session.put("resultHistory", resultHistory);
					return ret;
				} else {
					Graph graph = new GraphImpl();
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
					graph.addIEdges(new Edge(0, 1, null));
					graph.setTargetVariable(1);
					graph.setDataSource(0, c.getSource().getName());
					graph.setDataSource(1, c.getSource().getName());
					HashMap<Integer,DocStream> helper = new HashMap<Integer,DocStream>();
					helper.put(0, currentResult.getResultStream());
	
					int id = SemplorePool.acquire();
					QueryEvaluator eval = SemplorePool.getEvaluator(id);
					XFacetedResultSetForMultiDataSources newResult = eval.evaluate(graph, helper);
					SemplorePool.release(id);
					
					ResultPage ret = transform(newResult, 1, nbResultsPerPage);
					resultHistory.add(newResult);
					session.put("resultHistory", resultHistory);
					return ret;
					
				}
				
			} else if (query instanceof RelationSuggestion) {
				RelationSuggestion r = (RelationSuggestion)query;
				RelationSuggestionOperation rso = new RelationSuggestionOperation(r);
				String currentDataSource = currentGraph.getDataSource(currentGraph.getTargetVariable());
				currentGraph = rso.applyTo(currentGraph);
				operationHistory.add(rso);
				session.put("operationHistory", operationHistory);
				session.put("currentGraph", currentGraph);
				
				if (r.getSource().getName().equals(currentDataSource)) {
					Graph graph = new GraphImpl();
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
					graph.add(SchemaFactoryImpl.getInstance().createRelation(r.getURI()), 0, 1);
					graph.setTargetVariable(1);
					graph.setDataSource(0, currentDataSource);
					graph.setDataSource(1, currentDataSource);
					HashMap<Integer,DocStream> helper = new HashMap<Integer,DocStream>();
					helper.put(0, currentResult.getResultStream());
	
					int id = SemplorePool.acquire();
					QueryEvaluator eval = SemplorePool.getEvaluator(id);
					XFacetedResultSetForMultiDataSources newResult = eval.evaluate(graph, helper);
					SemplorePool.release(id);
	
					ResultPage ret = transform(newResult, 1, nbResultsPerPage);
					resultHistory.add(newResult);
					session.put("resultHistory", resultHistory);
					return ret;
				} else {
					Graph graph = new GraphImpl();
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
					graph.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
					graph.addIEdges(new Edge(0, 1, null));
					graph.setTargetVariable(1);
					graph.setDataSource(1, r.getSource().getName());
					HashMap<Integer,DocStream> helper = new HashMap<Integer,DocStream>();
					helper.put(0, currentResult.getResultStream());
	
					int id = SemplorePool.acquire();
					QueryEvaluator eval = SemplorePool.getEvaluator(id);
					XFacetedResultSetForMultiDataSources newResult = eval.evaluate(graph, helper);
					SemplorePool.release(id);
					
					Graph graph1 = new GraphImpl();
					graph1.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//0
					graph1.add(SchemaFactoryImpl.getInstance().createUniversalCategory());	//1
					graph1.add(SchemaFactoryImpl.getInstance().createRelation(r.getURI()), 0, 1);
					graph1.setTargetVariable(1);
					graph1.setDataSource(0, r.getSource().getName());
					graph1.setDataSource(1, r.getSource().getName());
					helper = new HashMap<Integer,DocStream>();
					helper.put(0, newResult.getResultStream());

					id = SemplorePool.acquire();
					eval = SemplorePool.getEvaluator(id);
					newResult = eval.evaluate(currentGraph);
					SemplorePool.release(id);
					
					ResultPage ret = transform(newResult, 1, nbResultsPerPage);
					resultHistory.add(newResult);
					session.put("resultHistory", resultHistory);
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
		if (session == null || session.get("resultHistory") == null) return null;
		LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
			(LinkedList<XFacetedResultSetForMultiDataSources>)session.get("resultHistory");
		LinkedList<Operation> operationHistory = (LinkedList<Operation>)session.get("operationHistory");
		Graph currentGraph = (Graph)session.get("currentGraph");
		
		if (resultHistory.isEmpty()) return null;
		if (operationHistory.isEmpty()) return null;
		if (currentGraph == null) return null;
		
		resultHistory.removeLast();
		Operation lastOperation = operationHistory.removeLast();
		currentGraph = lastOperation.undo(currentGraph);
		XFacetedResultSetForMultiDataSources result = resultHistory.getLast();
		ResultPage ret = transform(result, 1, nbResultsPerPage);
		session.put("resultHistory", resultHistory);
		session.put("operationHistory", operationHistory);
		session.put("currentGraph", currentGraph);
		return ret;

	}
	
	/**
	 * This method clears all results previously stored. This is typically done before a new search
	 * or when the users session expires.
	 */
	public void clear() {
		session.put("resultHistory", null);
		session.put("operationHistory", null);
		session.put("currentGraph", null);
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
		SeeAlso cached;
		if ((cached = seealsoCache.get(resultItemURL))!=null) return cached;
		ResultItem item = new ResultItem();
		item.setURL(resultItemURL);

		try {
			if (session == null || session.get("resultHistory") == null) return null;
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
				(LinkedList<XFacetedResultSetForMultiDataSources>)session.get("resultHistory");
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

			if (array.size() == 0) return null;
			
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

			seeAlso.setResultItem(item);
			seealsoCache.put(resultItemURL, seeAlso);
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
		ArraySnippet cached;
		if ((cached = snippetCache.get(resultItemURL))!=null) return cached;
		try {
			if (session == null || session.get("resultHistory") == null) return null;
			LinkedList<XFacetedResultSetForMultiDataSources> resultHistory = 
				(LinkedList<XFacetedResultSetForMultiDataSources>)session.get("resultHistory");
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
			
			ArraySnippet as = this.getSnippet(currentResult.getCurrentDataSource(), resultItemURL, snippet_str);
			snippetCache.put(resultItemURL, as);
			return as;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private ArraySnippet getSnippet(String dataSource, String resultItemURL, String snippet_str) {
		ResultItem item = new ResultItem();
		item.setURL(resultItemURL);
		Source source = new Source(dataSource, null, 0);
		LinkedList<Couple> rel = new LinkedList<Couple>();
		LinkedList<Couple> attr = new LinkedList<Couple>();
		LinkedList<Concept> cat = new LinkedList<Concept>();
		String[] labels = new String[3];
		int count = 0;
		
		if (snippet_str != null) {
			StringTokenizer tok = new StringTokenizer(snippet_str,"\n");
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				String type = Util4NT.checkSnippetType(token);
				String[] processed = Util4NT.processTripleLine("<a> "+token);
				for (int i = 1; i<=2; i++) {
					if ((i==1 && type==Util4NT.CATEGORY)||(i==2 && type==Util4NT.ATTRIBUTE)) continue;
					labels[i] = Util4NT.getDefaultLabel(processed[i]);
				}
				
				if (type==Util4NT.CATEGORY) {
					cat.add(new Concept(labels[2],processed[2],source));
				} else if (type==Util4NT.RELATION) {
					rel.add(new Couple(new Relation(labels[1],processed[1],source), new Instance(labels[2],processed[2],source)));
				} else if (type==Util4NT.ATTRIBUTE) {
					attr.add(new Couple(new Attribute(labels[1],processed[1],source), new Litteral(processed[2],null,source)));
				}
				count ++;
			}
		}
		logger.info(String.format("getArraySnippet [%s]%s : %d", dataSource, resultItemURL, count));
		return new ArraySnippet(item, rel, attr, cat);
	}
	
	public static void showResultPage(SearchSessionService sss, PrintWriter pw,
			ResultPage rp) {
		pw.println("****************");
		pw.println("Result Page:");
		Source s = rp.getSource();
		pw.println("\tSource:" + s.getName());
		LinkedList<Facet> facetList = s.getFacetList();
		pw.println("\t\tFacets:");
		for (Facet f : facetList) {
			pw.println("\t\t\t" + f.getLabel() + "\t" + f.getResultNb() + "\t"
					+ f.getURI());
		}
		pw.println("\t\tResult count:" + s.getResultCount());
		int pageNum = rp.getPageNum();
		pw.println("\tPage Num:" + pageNum);
		LinkedList<ResultItem> resultItemList = rp.getResultItemList();
		pw.println("\tResult Items:");
		for (ResultItem ri : resultItemList) {
			pw.println("\t\t" + ri.getScore() + "\t" + ri.getSnippet() + "\t"
					+ ri.getTitle() + "\t" + ri.getType() + "\t" + ri.getURL());
			ArraySnippet as = sss.getArraySnippet(ri.getURL());
			if (as != null) {
				pw.println("snippets:");
				LinkedList<Couple> attv = as.getAttribute_value();
				for (Couple c : attv)
					pw.println(c.getElement1().getLabel() + "["
							+ c.getElement1().getURI() + "]" + " , "
							+ c.getElement2().getLabel() + "["
							+ c.getElement2().getURI() + "]");
				LinkedList<Couple> rela = as.getRelation_attribute();
				for (Couple c : rela)
					pw.println(c.getElement1().getLabel() + "["
							+ c.getElement1().getURI() + "]" + " , "
							+ c.getElement2().getLabel() + "["
							+ c.getElement2().getURI() + "]");
				LinkedList<Concept> cl = as.getClasseList();
				for (Concept c : cl)
					pw.println(c.getLabel() + "[" + c.getURI() + "]");
			} else {
				pw.println("no snippets");
			}
			SeeAlso sa = sss.getSeeAlsoItem(ri.getURL());
			if (sa != null) {
				pw.println("see also:");
				LinkedList<Instance> li = sa.getFacetList();
				for (Instance i : li)
					pw.println(i.getURI());
			} else {
				pw.println("no seeAlso");
			}
		}

	}
	
	public static void main(String[] args) throws Exception {
		SearchSessionService sss = new SearchSessionService();
		PrintWriter pw = new PrintWriter(new FileWriter(
				"d:\\searchSessionLog.txt"),true);
		LinkedList<String> lls = new LinkedList<String>();
		lls.add("iswc");
		ResultPage rp = sss.search(new Keywords(lls), 10);
		pw.println("search \"iswc\"");
		showResultPage(sss, pw, rp);
		System.out.println("search finished");
		rp = sss.getPage(2, 10);
		pw.println("second page");
		showResultPage(sss, pw, rp);
		System.out.println("get page finished");
		Source s = rp.getSource();
		LinkedList<Facet> facetList = s.getFacetList();
		for (Facet f : facetList)
			if (f instanceof Relation) {
				rp = sss.refine(f, 10);
				pw.println("refine by \"" + f.getLabel() + "\"");
				showResultPage(sss, pw, rp);
				System.out.println("refine by \"" + f.getLabel()
						+ "\" finished");
				rp = sss.undoLastRefinement(10);
				pw.println("after undo");
				showResultPage(sss, pw, rp);
				System.out.println("undo \"" + f.getLabel() + "\" finished");
			}
		pw.close();
	}
}
