package sjtu.apex.q2semantic.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Graph implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 451326765269187438L;
	public Map<Integer,Node> nodes = new HashMap<Integer,Node>();
	public Map<Integer,Edge> edges = new HashMap<Integer,Edge>();
	public NodeFactory nodeFactory = new NodeFactory();
	public EdgeFactory edgeFactory = new EdgeFactory();
	public Graph(){
		nodes = new HashMap<Integer,Node>();
		edges = new HashMap<Integer,Edge>();
		nodeFactory = new NodeFactory();
		edgeFactory = new EdgeFactory();
		
	}
	/**
	 * @param f the edge's starts node
	 * @param t the edge's end node
	 * @param n the edge's name
	 */
	public Edge addEdge(Node f, Node t,String n,String ty) {
		
		Edge e = edgeFactory.newEdge(f, t, n, ty); 
		edges.put(e.id, e);
		f.edges.add(e);
		e.times = 1;
		return e;
	}
	public Edge addEdge(Node f, Node t,String n,String ty,int id) {
		
		Edge e = new Edge(f, t, n, ty,id); 
		edges.put(e.id, e);
		f.edges.add(e);
		
		return e;
	}
	
	/*
	 * Types of edge
	 */
	static public String Rel = "r";
	static public String Attr = "a";
	/*
	 * Types of nodes
	 */
	static public String Key = "k";
	static public String Typ = "t";
	/*
	 * Types of result types
	 */
	static public String Path = "p";
	static public String Root = "r";

	static public String Name = "*name1987*";
	static public String NamePre = "NNNN";
	public Node addNode(String n,String t) {
		Node e = nodeFactory.newNode(n, t);;
		nodes.put(e.id, e);
		return e;
	}
	public Node addNode(String n,String t, int id) {
		Node e = new Node(n,t,id);
		nodes.put(e.id, e);
		return e;
	}
	public Node getNode(int i) {
		return nodes.get(i);
	}
	public Edge getEdge(int i) {
		return edges.get(i);
	}
}
