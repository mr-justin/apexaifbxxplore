/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.ibm.semplore.btc.DecomposedGraph;
import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.NodeInSubGraph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.QueryPlanner;
import com.ibm.semplore.btc.SchemaObjectInfoForMultiDataSources;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.btc.Visit;
import com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources;
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
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.TreeSetDocStream;
import com.ibm.semplore.xir.impl.DebugIndex;
import com.ibm.semplore.xir.impl.IndexReaderImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author xrsun
 *
 */
public class QueryEvaluatorImpl implements QueryEvaluator {
	SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	TermFactory termFactory = TermFactoryImpl.getInstance();
	SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	QueryConverter4SemploreImpl converter = new QueryConverter4SemploreImpl();
	Hashtable<String, File> pathOfDataSource;
	Hashtable<Integer, String> dataSources;

	//stores ResultSets inside SubGraphs
	private HashMap<SubGraph, HashMap<Integer, DocStream>> result;
	private XFacetedResultSet targetResult;
	private String targetDataSource;
	//visited subgraphs' IDs
	private HashSet<Integer> visited;
	private File mappingIndex;
	//              ds1_ds2       docid1 -> pos_in_index_map
	private HashMap<String, HashMap<Integer, Integer>> indexhead_cache = new HashMap<String, HashMap<Integer, Integer>>();

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.QueryEvaluator#evaluate(com.ibm.semplore.btc.QueryPlanner)
	 */
	@Override
	public XFacetedResultSetForMultiDataSources evaluate(QueryPlanner planner) throws Exception {
		result = new HashMap<SubGraph, HashMap<Integer, DocStream>>();
		visited = new HashSet<Integer>();
		targetResult = null;
		planner.startTraverse(new PreVisit(), new PostVisit());
		return new XFacetedResultSetForMultiDataSourcesImpl(computeDSFacet(), targetResult);
	}

	private class PreVisit implements Visit {
		public void visit(Object p, Object o) {
			SubGraph parent = (SubGraph) p;
			SubGraph g = (SubGraph)o;
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
					XFacetedQuery q = converter.convertQuery(g);
					long time = System.currentTimeMillis();
					targetResult = searcher.search(q, helper);
					System.out.println(String.format("%s: %dms", q.getQueryConstraint().toString(), System.currentTimeMillis()-time));
				}
				else {
					XFacetedQuery q = converter.convertQuery(g);
					long time = System.currentTimeMillis();
					DocStream ans = searcher.evaluate(q, helper); 
					System.out.println(String.format("%s: %dms", q.getQueryConstraint().toString(), System.currentTimeMillis()-time));
					
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
					System.out.println(String.format("%s(%d)->%s(%d): %dms", 
							g.getDataSource(), origLen, 
							parent.getDataSource(),	ans.getLen(), System.currentTimeMillis()-time));
					
					parentResults.put(parentNode.getNodeID(), ans);

				}
			} catch (Exception e) { //search failed
				e.printStackTrace();
			}
		}
	}
	
	private HashMap<Integer, Integer> loadIndexHead(String file) throws IOException {
		if (indexhead_cache.get(file)!=null) indexhead_cache.get(file);
		DataInputStream fhead = new DataInputStream(new BufferedInputStream(new FileInputStream(
				mappingIndex.getPath() + File.separatorChar + file + ".head")));
		int len = fhead.available()/8;
		HashMap<Integer,Integer> map = new HashMap<Integer, Integer>();
		for (int i=0; i<len; i++) {
			int docid = fhead.readInt();
			int pos = fhead.readInt();
			map.put(docid, pos);
		}
		fhead.close();
		indexhead_cache.put(file, map);
		return map;
	}
	
	private DocStream convertID(String file, DocStream from, DocStream to) throws Exception {
		HashMap<Integer, Integer> head = loadIndexHead(file);
		TreeSet<Integer> results = new TreeSet<Integer>();
		RandomAccessFile fmap = new RandomAccessFile(
				mappingIndex.getPath() + File.separatorChar + file +".map", "r");

		from.init();
		for (int i=0; i<from.getLen(); i++, from.next()) {
			int doc1 = from.doc();
			Integer pos = head.get(doc1);
			if (pos==null) continue;
			fmap.seek(pos*4);
			int doc2;
			while ((doc2=fmap.readInt())!=-1)
				results.add(doc2);
		}
		fmap.close();
		
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
	
	private ArrayList<String> computeDSFacet() throws Exception {
		DocStream doc = convertID(targetDataSource+"_ds", targetResult.getResultStream(), null);
		
		ArrayList<String> arr = new ArrayList<String>();
		doc.init();
		for (int i=0; i<doc.getLen(); i++, doc.next()) {
			arr.add(dataSources.get( doc.doc() ));
		}
		return arr;
	}

	public QueryEvaluatorImpl()  {
		
	}
	
	/**
	 * read datasrc configuration file for each datasource's index location AND mapping index location 
	 * @param datasrc
	 * @throws IOException 
	 */
	public QueryEvaluatorImpl(File datasrc) throws IOException {
		HashMap config = Config.readDSConfigFile(datasrc.getAbsolutePath());
		pathOfDataSource = new Hashtable<String, File>();
		dataSources = new Hashtable<Integer, String>();
		for (Object o :config.keySet()) {
			if (o instanceof Integer) dataSources.put((Integer)o, (String)config.get(o));
			else if (o instanceof String) pathOfDataSource.put((String)o, new File((String)config.get(o)));
		}
		mappingIndex = pathOfDataSource.get("mapping");
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
		config.setProperty(Config.INDEX_PATH, pathOfDataSource.get(dataSource).getAbsolutePath());
		XFacetedSearchService searchService = searchFactory
				.getXFacetedSearchService(config);
		return searchService.getXFacetedSearchable();
	}
	@Override
	public void setPathOfMappingIndex(File path) {
		mappingIndex = path;
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

	public XFacetedResultSetForMultiDataSources evaluate(Graph graph) throws Exception {
		QueryDecomposerImpl decomposer = new QueryDecomposerImpl();
		DecomposedGraph dgraph = decomposer.decompose(graph);
		QueryPlanner planner = new QueryPlannerImpl();
		planner.setDecomposedGraph(dgraph);
		return evaluate(planner);
	}

	public XFacetedResultSetForMultiDataSources evaluate(Graph graph, HashMap<Integer, DocStream> startCache) throws Exception {
		QueryDecomposerImpl decomposer = new QueryDecomposerImpl();
		DecomposedGraph dgraph = decomposer.decompose(graph);
		QueryPlanner planner = new QueryPlannerImpl();
		planner.setDecomposedGraph(dgraph);
		//TODO use helper
		return evaluate(planner);
	}

	@Override
	public String getArraySnippet(String dataSource, int docID, String URI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<SchemaObjectInfoForMultiDataSources> getSeeAlso(
			String dataSource, int docID, String URI) {
		// TODO Auto-generated method stub
		return null;
	}

}
