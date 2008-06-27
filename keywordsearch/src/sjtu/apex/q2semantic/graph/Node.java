package sjtu.apex.q2semantic.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Node implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8127785694014630691L;
	public String type ;
	/*public Node(String c,String t){
	//	id = ++size;
		name = c;
		type = t;
	}*/
	public Node(String c ,String t, int i ) {
		id = i ;
		name = c;
		type =t;
		rtype = "u";
		times = 0;
		edges = new ArrayList<Edge>();
	}
	/**
	 * id . the node's identifier, it will be increased from 1 to size.
	 */
	public int id;
	/**
	 * name this is the node's category information
	 */
	public String name;
	/**
	 * the times that note appear
	 */
	public int times;
	
	/*
	 * for diji
	 */
	public Map<Node,Double> near = null;
	public Map<Node,Node>   prev = null;
	/*
	 * edge from this node
	 */
	public List<Edge> edges = null;
	
	public transient List<Edge> tEdges = null;
	
	
	/*
	 * rtype : result type .see more in Graph.
	 */
	public String rtype;
	public float weight;
	public int oid;
	private Node() {}
}
