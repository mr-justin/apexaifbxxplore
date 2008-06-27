package sjtu.apex.q2semantic.graph;

import java.io.Serializable;

public class NodeFactory implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1524514278544283222L;
	int size = 0;
	public Node newNode(String c, String t) {
		return new Node(c,t,++size);
	}
//	public Node newNode(String c, String t) {
//		return new Node
//	}
}
