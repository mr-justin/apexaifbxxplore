package com.ibm.semplore.btc;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;

import com.ibm.semplore.btc.impl.GraphImpl;
import com.ibm.semplore.btc.impl.QueryDecomposerImpl;
import com.ibm.semplore.btc.impl.QueryEvaluatorImpl;
import com.ibm.semplore.btc.impl.QueryPlannerImpl;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.test.TestSearch;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

public class TestEvaluator {
	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	public static void main(String[] args) throws Exception {
		Graph graph = new GraphImpl();
		//Concepts
		graph.add(schemaFactory.createKeywordCategory("Princeton"));	//0
		graph.add(schemaFactory.createUniversalCategory());				//1
		graph.add(schemaFactory.createUniversalCategory());				//2
		graph.add(schemaFactory.createUniversalCategory());				//3
		//Relations
		graph.add(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID(
				"<http://dbpedia.org/property/workInstitutions>")), 1, 0);
//		graph.add(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID(
//				"<http://lsdis.cs.uga.edu/projects/semdis/opus#author>")), 3, 2);
		//IEdge
		graph.addIEdges(new Edge(1,2,null));
		//target
		graph.setTargetVariable(2);
		//datasource
		graph.setDataSource(0, "dbpedia");
		graph.setDataSource(1, "dbpedia");
		graph.setDataSource(2, "dblp");
		graph.setDataSource(3, "dblp");
		
		QueryDecomposerImpl decomposer = new QueryDecomposerImpl();
		DecomposedGraph dgraph = decomposer.decompose(graph);
		QueryPlanner planner = new QueryPlannerImpl();
		planner.setDecomposedGraph(dgraph);
		QueryEvaluator eval = new QueryEvaluatorImpl();

		Hashtable<String, File> dsmap = new Hashtable<String,File>();
		dsmap.put("dbpedia", new File("y:\\btc\\dbpedia_s\\index"));
		dsmap.put("dblp", new File("y:\\btc\\dblp\\index"));
		File mappath = new File("y:\\SSModel");
		eval.setPathOfDataSource(dsmap);
		eval.setPathOfMappingIndex(mappath);
		
		XFacetedResultSet result = eval.evaluate(planner);
		TestSearch.showResultSet(result, null);
	}
	
	

}
