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
		graph.add(schemaFactory.createKeywordCategory("ISWC"));	//0
		graph.add(schemaFactory.createCategory("<http://lsdis.cs.uga.edu/projects/semdis/opus#Proceedings>"));				//1
		graph.add(schemaFactory.createCategory("<http://swrc.ontoware.org/ontology#Proceedings>"));				//1
		graph.add(schemaFactory.createCategory("<http://swrc.ontoware.org/ontology#Topic>"));				//1
		graph.add(schemaFactory.createCategory("<http://swrc.ontoware.org/ontology#AssistantProfessor>"));				//1
		
		//Relations
		graph.add(schemaFactory.createRelation((
				"<http://lsdis.cs.uga.edu/projects/semdis/opus#book_title>")), 1, 0);
		graph.add(schemaFactory.createRelation((
		"<http://swrc.ontoware.org/ontology#publication>")), 4, 2);
		graph.add(schemaFactory.createRelation((
				"<http://swrc.ontoware.org/ontology#isWorkedOnBy>")), 3, 4);
		
		//IEdge
		graph.addIEdges(new Edge(1,2,null));
		//target
		graph.setTargetVariable(1);
		//datasource
		graph.setDataSource(0, "dblp");
		graph.setDataSource(1, "dblp");
		graph.setDataSource(2, "swrc");
		graph.setDataSource(3, "swrc");
		graph.setDataSource(4, "swrc");
		
		long time = System.currentTimeMillis();
		QueryEvaluator eval = new QueryEvaluatorImpl(new File(args[0])); //points to config/datasrc.cfg

		System.out.println("Begin Evaluation");
		XFacetedResultSet result = eval.evaluate(graph);
		System.out.println("Total Evaluation time(ms): "+(System.currentTimeMillis()-time));
		TestSearch.showResultSet(result, null);
		
		if (true) return ;
		//use start cache
		graph = new GraphImpl();
		//Concepts
		graph.add(schemaFactory.createCategory((
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
