package org.team.xxplore.core.service.search.datastructure;

import org.dom4j.Document;
import org.jgrapht.Graphs;
import org.jgrapht.graph.WeightedPseudograph;

/**
 * This class represents query graphs
 * @author tpenin
 */
public class QueryGraph extends Graphs implements Query {
   
	public String NLText;
	private WeightedPseudograph queryGraph;
	
	/**
	 * Default constructor
	 */
	public QueryGraph() {
		super();
   	}
	
	/**
	 * set the queryGraph built from Q2Semantic
	 * @param qg
	 */
	public void setQueryGraph(WeightedPseudograph qg)
	{
		queryGraph = qg;
	}
	
	/**
	 * get the queryGraph built from Q2Semantic
	 * @return
	 */
	public WeightedPseudograph getQUeryGraph()
	{
		return queryGraph;
	}
	
	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
	}
}
