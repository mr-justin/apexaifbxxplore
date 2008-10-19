/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;

/**
 * @author xrsun
 *
 */
public class GraphImpl implements Graph {
	private ArrayList< ArrayList<Edge> > iedges;
	private ArrayList<String> dataSources;
	private CatRelGraph graphs; 
	private SchemaFactory schemaFactory;
	private int targetVariable;
	private int nodeCount;

	public GraphImpl() {
		schemaFactory = SchemaFactoryImpl.getInstance();
		graphs = schemaFactory.createCatRelGraph();
		nodeCount = 0;
		iedges = new ArrayList<ArrayList<Edge>>();
		dataSources = new ArrayList<String>();
	}


	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.Graph#add(com.ibm.semplore.model.GeneralCategory)
	 */
	@Override
	public Graph add(GeneralCategory cat) {
		graphs.add(cat);
		iedges.add(new ArrayList<Edge>());
		dataSources.add(null);
		nodeCount ++;
		return this;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.Graph#add(com.ibm.semplore.model.Relation, int, int)
	 */
	@Override
	public Graph add(Relation rel, int fromNodeIndex, int toNodeIndex) {
		graphs.add(rel, fromNodeIndex, toNodeIndex);
		return this;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.Graph#addIEdges(Edge)
	 */
	@Override
	public Graph addIEdges(Edge edge) {
		iedges.get(edge.getFromNode()).add(edge);
		iedges.get(edge.getToNode()).add(edge.reverse());
		return this;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.Graph#getIEdges(int)
	 */
	@Override
	public Iterator<Edge> getIEdges(int nodeIndex) {
		return iedges.get(nodeIndex).iterator();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.Graph#getTargetVariable()
	 */
	@Override
	public int getTargetVariable() {
		return targetVariable;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.Graph#setTargetVariable(int)
	 */
	@Override
	public void setTargetVariable(int nodeIndex) throws IndexOutOfBoundsException {
		if (nodeIndex >= nodeCount) throw new IndexOutOfBoundsException("wrong nodeIndex");
		targetVariable = nodeIndex;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.model.CatRelGraph#getEdges(int)
	 */
	@Override
	public Iterator<Edge> getEdges(int nodeIndex) {
		return graphs.getEdges(nodeIndex);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.model.CatRelGraph#getNode(int)
	 */
	@Override
	public GeneralCategory getNode(int nodeIndex) {
		return graphs.getNode(nodeIndex);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.model.CatRelGraph#numOfNodes()
	 */
	@Override
	public int numOfNodes() {
		return nodeCount;
	}


	@Override
	public String getDataSource(int nodeIndex) {
		return dataSources.get(nodeIndex);
	}

	@Override
	public Graph setDataSource(int nodeIndex, String ds) {
		dataSources.set(nodeIndex, ds);
		return this;
	}

	public int removeRelation(Relation rel, int nodeIndex) {
		return graphs.removeRelation(rel, nodeIndex);
	}
	
	public String toString() {
		String str = "";
		for (int i=0; i<nodeCount; i++)
			str += String.format("Node %d: [%s]%s\n", i, getDataSource(i), getNode(i).toString());
		for (int i=0; i<nodeCount; i++) {
			Iterator<Edge> itr = getEdges(i);
			while (itr.hasNext()) {
				Edge e = itr.next();
				if (Math.min(e.getFromNode(), e.getToNode()) < i) continue;
				str += "Edge: " + itr.next().toString() + "\n";
			}
		}
		for (int i=0; i<nodeCount; i++) {
			Iterator<Edge> itr = getIEdges(i);
			while (itr.hasNext()) {
				Edge e = itr.next();
				if (Math.min(e.getFromNode(), e.getToNode()) < i) continue;
				str += "IEdge: " + itr.next().toString() + "\n";
			}
		}
		return str;
	}
}
