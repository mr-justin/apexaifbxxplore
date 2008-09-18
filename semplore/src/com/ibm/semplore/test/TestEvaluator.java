package com.ibm.semplore.test;

import java.io.File;
import java.util.HashMap;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.QueryEvaluator;
import com.ibm.semplore.btc.impl.GraphImpl;
import com.ibm.semplore.btc.impl.QueryEvaluatorImpl;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.impl.DocStreamHintImpl;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;
import com.ibm.semplore.xir.DocStream;

public class TestEvaluator {
	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	public static void main(String[] args) throws Exception {
		Graph graph = new GraphImpl();
		//Concepts
		graph.add(schemaFactory.createKeywordCategory("University"));	//0
		graph.add(schemaFactory.createUniversalCategory());				//1
		graph.add(schemaFactory.createUniversalCategory());				//2
		graph.add(schemaFactory.createUniversalCategory());				//3
		//Relations
		graph.add(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID(
				"<http://dbpedia.org/property/workInstitutions>")), 1, 0);
		graph.add(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID(
				"<http://lsdis.cs.uga.edu/projects/semdis/opus#author>")), 3, 2);
		//IEdge
		graph.addIEdges(new Edge(1,2,null));
		//target
		graph.setTargetVariable(3);
		//datasource
		graph.setDataSource(0, "dbpedia");
		graph.setDataSource(1, "dbpedia");
		graph.setDataSource(2, "dblp");
		graph.setDataSource(3, "dblp");
		
		long time = System.currentTimeMillis();
		QueryEvaluator eval = new QueryEvaluatorImpl(new File(args[0])); //points to config/datasrc.cfg

		System.out.println("Begin Evaluation");
		XFacetedResultSet result = eval.evaluate(graph);
		System.out.println("Total Evaluation time(ms): "+(System.currentTimeMillis()-time));
		TestSearch.showResultSet(result, null);
		
		//use start cache
		graph = new GraphImpl();
		//Concepts
		graph.add(schemaFactory.createCategory(Md5_BloomFilter_64bit.URItoID(
			"<http://lsdis.cs.uga.edu/projects/semdis/opus#Article_in_Proceedings>")));	//0
		//target
		graph.setTargetVariable(0);
		//datasource
		graph.setDataSource(0, "dblp");			
		HashMap<Integer, DocStream> cache = new HashMap<Integer, DocStream>();
		cache.put(0, result.getResultStream());
		result = eval.evaluate(graph, cache);		
		TestSearch.showResultSet(result, null);		
	}
	
	

}
