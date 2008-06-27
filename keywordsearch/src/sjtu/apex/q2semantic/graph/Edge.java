package sjtu.apex.q2semantic.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Edge implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -108051979240639503L;

	public String type;
//	static int size = 0;
	/*public Edge(Node f, Node t, String n ,String ty) {
		from = f ; to = t ; name = n; type = ty;
		id = ++size;
		edges.put(id, this);
	}*/
	public Edge(Node f, Node t, String n,String ty, int i) {
		from = f ; to = t ; name = n ; id = i; type =ty;times=0;
		edges = new HashMap<Integer,Edge>();
		from_id = f.id;
		to_id = t.id;
		edges.put(id, this);
	}
	public static Map<Integer,Edge> edges = null;
	
	public transient Node from ;
	public transient Node to ;
	public int  from_id;
	public int  to_id;
	public int  id ;
	public int  times;
	public String name ;
	public float weight ;
	public boolean originDirection = false;

	private Edge() {};
}
