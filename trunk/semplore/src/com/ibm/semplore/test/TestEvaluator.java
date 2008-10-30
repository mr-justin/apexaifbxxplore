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

	/**
	 * @param args[0] config/datasrc.cfg
	 * @param args[1] (optional) specify graph file
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Graph graph;
		
		if (args.length==2) {
			graph = new GraphImpl();
			graph.load(new File(args[1])); 
		}
		else 
			graph = prepareGraph();
		
		QueryEvaluator eval = new QueryEvaluatorImpl(new File(args[0])); //points to config/datasrc.cfg

		long time = System.currentTimeMillis();
		System.out.println("Begin Evaluation");
		XFacetedResultSet result = eval.evaluate(graph);
		System.out.println("Total Evaluation time(ms): "+(System.currentTimeMillis()-time));
		
		TestSearch.showResultSet(result, null);
		
		System.out.println(String.format("Time stats: %d, %d, %d", result.getResultTime(), result.getFacetTime(), (System.currentTimeMillis()-time)));
	}
	
	private static Graph prepareGraph() {
		Graph graph = new GraphImpl();
		//Concepts
		graph.add(schemaFactory.createUniversalCategory());
		graph.add(schemaFactory.createKeywordCategory("frank"));	//0
		graph.add(schemaFactory.createCategory("<http://swrc.ontoware.org/ontology#FullProfesso>"));
//		graph.add(schemaFactory.createCategory("<http://semanticweb.org/id/Category-3ASemantic_Web_topic>"));
	
		//Relations
		graph.add(schemaFactory.createRelation((
				"<http://swrc.ontoware.org/ontology#nam>")), 2, 1);
		graph.add(schemaFactory.createRelation((
		"<http://swrc.ontoware.org/ontology#publication>")), 2, 0);
//		graph.add(schemaFactory.createRelation((
//				"<http://swrc.ontoware.org/ontology#isWorkedOnBy>")), 3, 4);
		
		//IEdge
//		graph.addIEdges(new Edge(1,0,null));
		//target
		graph.setTargetVariable(0);
		//datasource
		graph.setDataSource(0, "swrc");
		graph.setDataSource(1, "swrc");
		graph.setDataSource(2, "swrc");
		
		return graph;
	}
	

}
