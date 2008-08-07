package com.ibm.semplore.imports.impl.data.util;
/**
 * 
 * 比较大小重载...
 *
 */
public class TripleURI implements Comparable {
	public String sub, pred, obj; 
	public String sort;
	public TripleURI(String s, String p, String o, String sort) {
		sub = s; pred = p; obj = o; this.sort = sort;
	}
	public int compareTo(Object arg0) {
		TripleURI a = (TripleURI)arg0;
		if (sort.equals("SRO")) return compareTo(a.sub,sub, a.pred,pred, a.obj, obj);
		if (sort.equals("ORS")) return compareTo(a.obj,obj, a.pred,pred, a.sub, sub);
		if (sort.equals("RSO")) return compareTo(a.pred,pred, a.sub, sub, a.obj, obj);
		if (sort.equals("ROS")) return compareTo(a.pred, pred, a.obj, obj, a.sub, sub);
		return 0;
	}
	private int compareTo(String a0, String a1, String b0, String b1, String c0, String c1){
		try {
			int x;
			if ((x = new Integer(a0).intValue() - new Integer(a1).intValue()) != 0)
				return -x;
			else if ((x = new Integer(b0).intValue() - new Integer(b1).intValue()) != 0)
				return -x;
			else
				return -(new Integer(c0).intValue() - new Integer(c1).intValue());
		} catch (Exception e) {
			return 0;
		}
	}
}
