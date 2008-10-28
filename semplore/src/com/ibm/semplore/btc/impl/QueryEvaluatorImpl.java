/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.semplore.btc.DecomposedGraph;
import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.NodeInSubGraph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.QueryPlanner;
import com.ibm.semplore.btc.QuerySnippetDB;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSourcesImpl;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.btc.Visit;
import com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources;
import com.ibm.semplore.btc.mapping.MappingIndexReader;
import com.ibm.semplore.btc.mapping.MappingIndexReaderFactory;
import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.XFacetedSearchable;
import com.ibm.semplore.search.impl.DocStreamHintImpl;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.search.impl.XFacetedSearchableImpl;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.TreeSetDocStream;
import com.ibm.semplore.xir.impl.DebugIndex;
import com.ibm.semplore.xir.impl.IndexReaderImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;
import com.sleepycat.je.DatabaseException;

/**
 * @author xrsun
 *
 */
public class QueryEvaluatorImpl implements QueryEvaluator {
	SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	TermFactory termFactory = TermFactoryImpl.getInstance();
	SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	QueryConverter4SemploreImpl converter = new QueryConverter4SemploreImpl();
	static Hashtable<String, File> pathOfDataSource;
	static Hashtable<Integer, String> dataSources;

	//stores ResultSets inside SubGraphs
	private HashMap<SubGraph, HashMap<Integer, DocStream>> result;
	private XFacetedResultSet targetResult;
	private String targetDataSource;
	//visited subgraphs' IDs
	private HashSet<Integer> visited;
	private int thisSearchID;
	private boolean relax;
	
	static private File mappingIndex;
	static private boolean configed = false;

	static int searchID = 0;
	static Logger logger = Logger.getLogger(QueryEvaluatorImpl.class);
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.QueryEvaluator#evaluate(com.ibm.semplore.btc.QueryPlanner)
	 */
	@Override
	public XFacetedResultSetForMultiDataSources evaluate(QueryPlanner planner) throws Exception {
		return evaluate(planner, null);
	}

	private void traverseInit(QueryPlanner planner, HashMap<NodeInSubGraph, DocStream> startCache) {
		result = new HashMap<SubGraph, HashMap<Integer, DocStream>>();
		if (startCache!=null) {
			for (Entry<NodeInSubGraph, DocStream> i: startCache.entrySet()) {
				NodeInSubGraph n = i.getKey();
				HashMap<Integer, DocStream> map = result.get(
						planner.getDecomposedGraph().getSubGraph(n.getSubGraphID()));
				if(map==null) {
					map = new HashMap<Integer, DocStream>();
					result.put(
							planner.getDecomposedGraph().getSubGraph(n.getSubGraphID()), map);
				}
				map.put(n.getNodeID(), i.getValue());
			}
		}
		visited = new HashSet<Integer>();
		targetResult = null;
	}
	protected XFacetedResultSetForMultiDataSources evaluate(QueryPlanner planner, HashMap<NodeInSubGraph, DocStream> startCache) throws Exception {
		traverseInit(planner, startCache);
		relax = false;
		planner.startTraverse(new PreVisit(), new PostVisit());
		if (relax) {
			traverseInit(planner, startCache);
			planner.startTraverse(new PreVisit(), new PostVisit());
		}
		return new XFacetedResultSetForMultiDataSourcesImpl(targetDataSource, null, targetResult);
	}

	public XFacetedResultSetForMultiDataSources evaluate(Graph graph) throws Exception {
		synchronized(this) {
			searchID++;
			thisSearchID = searchID;
		}
		logger.info("search "+thisSearchID+"\n"+graph);
		QueryDecomposerImpl decomposer = new QueryDecomposerImpl();
		DecomposedGraph dgraph = decomposer.decompose(graph);
		QueryPlanner planner = new QueryPlannerImpl();
		planner.setDecomposedGraph(dgraph);
		return evaluate(planner);
	}

	public XFacetedResultSetForMultiDataSources evaluate(Graph graph, HashMap<Integer, DocStream> startCache) throws Exception {
		synchronized(this) {
			searchID++;
			thisSearchID = searchID;
		}
		logger.info("search with startCache "+thisSearchID+"\n"+graph);
		QueryDecomposerImpl decomposer = new QueryDecomposerImpl();
		DecomposedGraph dgraph = decomposer.decompose(graph);
		QueryPlanner planner = new QueryPlannerImpl();
		planner.setDecomposedGraph(dgraph);
		HashMap<NodeInSubGraph, DocStream> cache = new HashMap<NodeInSubGraph, DocStream>();
		for (Entry<Integer, DocStream> i: startCache.entrySet())
			cache.put(decomposer.convertToInternalID(i.getKey()), i.getValue());
		return evaluate(planner, cache);
	}

	private class PreVisit implements Visit {
		public void visit(Object p, Object o) {
			SubGraph parent = (SubGraph) p;
			SubGraph g = (SubGraph)o;
			if (result.get(g)==null)
				result.put(g, new HashMap<Integer, DocStream>());
		}
	}
	private class PostVisit implements Visit {
		public void visit(Object p, Object o) {
			SubGraph parent = (SubGraph) p;
			SubGraph g = (SubGraph)o;
			visited.add(g.getSubGraphID());
			
			try {
				XFacetedSearchable searcher = getSearcher(g.getDataSource());
				
				//pass mapping conditions to SearchHelper
				SearchHelper helper = searchFactory.createSearchHelper();
				HashMap<Integer, DocStream> mappings = result.get(g);
				for (Entry<Integer, DocStream> i: mappings.entrySet())
					helper.setHint(SearchHelper.START_CACHE_HINT, i.getKey(), new DocStreamHintImpl(i.getValue()));
				//after that, mapping conditions can be freed from memory
				mappings = null;
				result.remove(g); 
				
				if (parent==null) {
					//need facet if isRoot
					targetDataSource = g.getDataSource();
					XFacetedQuery q = converter.convertQuery(g, relax);
					long time = System.currentTimeMillis();
					targetResult = searcher.search(q, helper);
					relax = targetResult.getLength() == 0;
					logger.debug(String.format("%s: %d+%d ms", q.getQueryConstraint().toString(), targetResult.getResultTime(), targetResult.getFacetTime()));
					logger.info("search "+thisSearchID+ " complete: " + targetResult.getLength() + " results");
				}
				else {
					XFacetedQuery q = converter.convertQuery(g, relax);
					long time = System.currentTimeMillis();
					DocStream ans = searcher.evaluate(q, helper); 
					logger.debug(String.format("%s: %dms => %d", q.getQueryConstraint().toString(), System.currentTimeMillis()-time, ans.getLen()));
					
					//find the edge that link to its parent
					DocStream origResult = null;
					HashMap<Integer, DocStream> parentResults = null;
					NodeInSubGraph parentNode = null;
					for (int i=0; i<g.numOfNodes(); i++) {
						parentNode = g.getMappingConditions(i).next();
						if (parentNode.getSubGraphID() == parent.getSubGraphID()) {
							parentResults = result.get(parent);
							origResult = parentResults.get(parentNode.getNodeID());
							break;
						}
					}
					//convert ans from this subgraph's ID's to its parent's
					time = System.currentTimeMillis();
					int origLen = ans.getLen();
					ans = convertID(g.getDataSource()+"_"+parent.getDataSource()+"_index", ans, origResult);
					logger.debug(String.format("%s(%d)->%s(%d): %dms", 
							g.getDataSource(), origLen, 
							parent.getDataSource(),	ans.getLen(), System.currentTimeMillis()-time));
					
					parentResults.put(parentNode.getNodeID(), ans);

				}
			} catch (Exception e) { //search failed
				e.printStackTrace();
			}
		}
	}
	
	private DocStream convertID(String file, DocStream from, DocStream to) throws Exception {
		MappingIndexReader reader = MappingIndexReaderFactory.getMappingIndexReader(file);
		
		TreeSet<Integer> results = new TreeSet<Integer>();

		from.init();
		for (int i=0; i<from.getLen(); i++, from.next()) {
			int doc1 = from.doc();
			Iterator<Integer> itr = reader.getMappings(doc1);
			while (itr.hasNext()) results.add(itr.next());
		}
		
		if (to != null) {
			TreeSet<Integer> toset = new TreeSet<Integer>();
			to.init();
			for (int i=0; i<to.getLen(); i++, to.next()) {
				toset.add(to.doc());
			}
			results.retainAll(toset);
		}
		
		DocStream resultStream = new TreeSetDocStream(results);
		
		return resultStream;
	}
	
	private HashMap<String, Integer> computeDSFacet() throws Exception {
		HashMap<String, Integer> arr = new HashMap<String, Integer>();
		MappingIndexReader reader;
		try {
			reader = MappingIndexReaderFactory.getMappingIndexReader(targetDataSource+"_ds");
		} catch (Exception e) {
			return arr;
		}

		DocStream from = targetResult.getResultStream();
		from.init();
		for (int i=0; i<from.getLen(); i++, from.next()) {
			int doc1 = from.doc();
			Iterator<Integer> itr = reader.getMappings(doc1);
			while (itr.hasNext()) {
				String ds = dataSources.get(itr.next());
				if (arr.get(ds)==null) arr.put(ds,1);
				else arr.put(ds, arr.get(ds)+1);
			}
		}
		return arr;
	}

	private QueryEvaluatorImpl()  {
		
	}
	
	/**
	 * read datasrc configuration file for each datasource's index location AND mapping index location AND snippet index location 
	 * @param datasrc
	 * @throws IOException 
	 */
	public QueryEvaluatorImpl(File datasrc) throws IOException {
		if (!configed ) {
			configed = true;
			HashMap config = Config.readDSConfigFile(datasrc.getAbsolutePath());
			pathOfDataSource = new Hashtable<String, File>();
			dataSources = new Hashtable<Integer, String>();
			for (Object o :config.keySet()) {
				if (o instanceof Integer) dataSources.put((Integer)o, (String)config.get(o));
				else if (o instanceof String && (config.get(o) instanceof String)) pathOfDataSource.put((String)o, new File((String)config.get(o)));
			}
			QuerySnippetDB.init(pathOfDataSource.get("snippet").getAbsolutePath());
			mappingIndex = pathOfDataSource.get("mapping");
			MappingIndexReaderFactory.init(mappingIndex);
			PropertyConfigurator.configure(pathOfDataSource.get("logging").toURL());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.QueryEvaluator#setPathOfDataSource(java.util.Hashtable)
	 */
	@Override
	public void setPathOfDataSource(Hashtable<String, File> map) {
		pathOfDataSource = map;
	}

	private XFacetedSearchable getSearcher(String dataSource) throws Exception {
		Properties config = new Properties();
		config.setProperty(Config.THIS_DATA_SOURCE, dataSource);
		try {
			config.setProperty(Config.INDEX_PATH, pathOfDataSource.get(dataSource).getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("dataSource = " + dataSource);
		}
		XFacetedSearchService searchService = searchFactory
				.getXFacetedSearchService(config);
		return searchService.getXFacetedSearchable();
	}
	@Override
	public void setPathOfMappingIndex(File path) {
		mappingIndex = path;
	}
	
	@Override
	public String getArraySnippet(String dataSource, int docID, String URI) {
		try {
			return QuerySnippetDB.getSnippet(dataSource, URI).getData();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (NullPointerException e1) {
			return null;
		}
		return null;
	}

	@Override
	public ArrayList<SchemaObjectInfoForMultiDataSources> getSeeAlso(
			String dataSource, int docID, String URI)  {
		final FieldType[] types = new FieldType[]{FieldType.URI, FieldType.LABEL};

		ArrayList<SchemaObjectInfoForMultiDataSources> arr = new ArrayList<SchemaObjectInfoForMultiDataSources>();
		for (String s: dataSources.values()) 
			if (s!=dataSource) {
				IndexReader rd;
				MappingIndexReader reader;
				try {
					rd = getSearcher(s).getInsIndexReader();
					reader = MappingIndexReaderFactory.getMappingIndexReader(dataSource+"_"+s+"_index");

					Iterator<Integer> itr = reader.getMappings(docID);
					while (itr.hasNext()) {
						int id = itr.next();
						String[] values = rd.getFieldValues(id, types);
						arr.add(new SchemaObjectInfoForMultiDataSourcesImpl(values[0],s,id,values[1],null,null));
					}
				} catch (Exception e1) {
					continue;
				}
			}
		logger.info(String.format("getSeeAlso [%s]%s : %d", dataSource, URI, arr.size()));
		return arr;
	}
	
	public static void main(String[] args) throws Exception {
		SearchFactory searchFactory = SearchFactoryImpl.getInstance();
		SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
		TermFactory termFactory = TermFactoryImpl.getInstance();
		
		Properties config = Config.readConfigFile(args[0]);
		XFacetedSearchService searchService = searchFactory
				.getXFacetedSearchService(config);
		XFacetedSearchableImpl searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
		IndexReaderImpl indexReader = (IndexReaderImpl)searcher.getInsIndexReader();

		config = Config.readConfigFile(args[1]);
		searchService = searchFactory
				.getXFacetedSearchService(config);
		searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
		IndexReaderImpl indexReader2 = (IndexReaderImpl)searcher.getInsIndexReader();

		
		DocStream doc = indexReader.getDocStream(termFactory.createTermForInstances(schemaFactory.createKeywordCategory("Sherman")));
//		DocStream doc = indexReader.getDocStream(termFactory.createTermForInstances(schemaFactory.createAttributeKeywordCategory("label", "Talaat")));
//		DebugIndex.printDocStream(indexReader, doc);
		
		QueryEvaluatorImpl eva = new QueryEvaluatorImpl();
		eva.setPathOfMappingIndex(new File("."));
		DocStream result = eva.convertID("dbpedia_dblp_index", doc, null);
		DebugIndex.printDocStream(indexReader2, result);
	}

	@Override
	public Collection<String> getAvailableDatasources() {
		return dataSources.values();
	}

}
