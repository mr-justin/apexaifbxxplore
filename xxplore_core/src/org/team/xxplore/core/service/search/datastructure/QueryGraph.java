package org.team.xxplore.core.service.search.datastructure;

import org.dom4j.Document;
import org.jgrapht.Graphs;

/**
 * This class represents query graphs
 * @author tpenin
 */
public class QueryGraph extends Graphs implements Query {
   
	public String NLText;
	
	/**
	 * Default constructor
	 */
	public QueryGraph() {
		super();
   	}
	
	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
	}
}
