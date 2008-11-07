/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Override
	public int removeIEdge(Edge edge, int nodeIndex) {
		iedges.get(edge.getFromNode()).remove(edge);
		iedges.get(edge.getFromNode()).remove(edge.reverse());
		iedges.get(edge.getToNode()).remove(edge);
		iedges.get(edge.getToNode()).remove(edge.reverse());
		return edge.getFromNode()+edge.getToNode()-nodeIndex;
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
	
	/* 
	 * format the graph in a readable format
	 */
	public String toString() {
		String str = "target = " + targetVariable + "\n";
		for (int i=0; i<nodeCount; i++)
			str += String.format("Node %d: [%s]%s\n", i, getDataSource(i), getNode(i).toString());
		for (int i=0; i<nodeCount; i++) {
			Iterator<Edge> itr = getEdges(i);
			while (itr.hasNext()) {
				Edge e = itr.next();
				if (Math.min(e.getFromNode(), e.getToNode()) < i) continue;
				str += "Edge: " + e.toString() + "\n";
			}
		}
		for (int i=0; i<nodeCount; i++) {
			Iterator<Edge> itr = getIEdges(i);
			while (itr.hasNext()) {
				Edge e = itr.next();
				if (Math.min(e.getFromNode(), e.getToNode()) < i) continue;
				str += "IEdge: " + e.toString() + "\n";
			}
		}
		return str;
	}


	/* 
	 * load the graph from a file generated using GraphImpl.toString()
	 * @see GraphImpl.toString()
	 */
	public void load(File file) throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int target = Integer.MAX_VALUE;
		String line;
		while ((line = reader.readLine())!=null) {
			if (line.trim().equals("")) continue;
			Matcher m;
			m = Pattern.compile("^target = (\\d+)$").matcher(line);
			if (m.matches()) {
				if (target!=Integer.MAX_VALUE) throw new IOException("Wrong graph format: more than one targets");
				target = Integer.valueOf(m.group(1));
				continue;
			}
			m = Pattern.compile("^Node (\\d+): \\[(.*)\\](.*)$").matcher(line);
			if (m.matches()) {
				int nodeIndex = Integer.valueOf(m.group(1));
				String ds = m.group(2);
				String str = m.group(3);
				if (str.startsWith("KEYWORD OF")) add(schemaFactory.createKeywordCategory(str.substring(11)));
				else add(schemaFactory.createCategory(str));
				setDataSource(nodeIndex, ds);
				continue;
			}
			m = Pattern.compile("^Edge: \\((\\d+)->(\\d+)#(.*)\\)$").matcher(line);
			if (m.matches()) {
				int from = Integer.valueOf(m.group(1));
				int to = Integer.valueOf(m.group(2));
				String uri = m.group(3);
				add(schemaFactory.createRelation(uri), from, to);
				continue;
			}
			m = Pattern.compile("^IEdge: \\((\\d+)<=>(\\d+)\\)$").matcher(line);
			if (m.matches()) {
				int from = Integer.valueOf(m.group(1));
				int to = Integer.valueOf(m.group(2));
				addIEdges(new Edge(from,to,null));
				continue;
			}
			throw new IOException("Wrong graph format: " + line);
		}
		if (target == Integer.MAX_VALUE) throw new IOException("Wrong graph format: No target");
		setTargetVariable(target);
	}

	public static void main(String args[]) throws NumberFormatException, FileNotFoundException, IOException {
		File file = new File(args[0]);
		char[] buf = new char[ (int) file.length() ];
		new FileReader(file).read(buf);
		Graph g = new GraphImpl();
		g.load(file);
		
		String ans = new String(buf).trim();
		String out = g.toString().trim();
		if (!ans.equals(out)) {
			System.out.println("=== ans ===");
			System.out.println(ans);
			System.out.println("=== out ===");
			System.out.println(out);
		}
	}
}
