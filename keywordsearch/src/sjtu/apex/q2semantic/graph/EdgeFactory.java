package sjtu.apex.q2semantic.graph;

import java.io.Serializable;

public class EdgeFactory implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7241718820165477953L;
	int size ;
	EdgeFactory(){size = 0 ; }
	public Edge newEdge(Node f, Node t, String n ,String ty) {
		return new Edge(f,t,n,ty,++size);
	}
}
