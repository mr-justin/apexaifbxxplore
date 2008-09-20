package com.ibm.semplore.imports.impl.data.util;

import java.util.ArrayList;
import java.util.Collections;

import com.ibm.semplore.util.HashID;

/**
 * 
 * 比较大小重载...
 *
 */
public class TripleURI implements Comparable {
	public HashID sub, pred, obj; 
	public String sort;
	public TripleURI(String s, String p, String o, String sort) {
		sub = new HashID(s); pred = new HashID(p); obj = new HashID(o); this.sort = sort;
	}
	public int compareTo(Object arg0) {
		TripleURI a = (TripleURI)arg0;
		if (sort.equals("SRO")) return compareTo(a.sub,sub, a.pred,pred, a.obj, obj);
		if (sort.equals("ORS")) return compareTo(a.obj,obj, a.pred,pred, a.sub, sub);
		if (sort.equals("RSO")) return compareTo(a.pred,pred, a.sub, sub, a.obj, obj);
		if (sort.equals("ROS")) return compareTo(a.pred, pred, a.obj, obj, a.sub, sub);
		throw new Error("sort="+sort);
	}
	private int compareTo(HashID a0, HashID a1, HashID b0, HashID b1, HashID c0, HashID c1){
		int c;
		if ((c=a1.compareTo(a0))!=0) return c;
		if ((c=b1.compareTo(b0))!=0) return c;
		if ((c=c1.compareTo(c0))!=0) return c;
		return 0;
	}
	public static void main(String[] args) {
		ArrayList<TripleURI> arr = new ArrayList<TripleURI>();
		arr.add(new TripleURI("1;2","3;4","5;6","RSO"));
		arr.add(new TripleURI("1;2","-1;4","5;6","RSO"));
		arr.add(new TripleURI("5;2","3;4","5;6","RSO"));
		Collections.sort(arr);
		for (int i = 0; i < arr.size(); i++) {
			TripleURI tu = arr.get(i);
			System.out.print(tu.sub + "\t" + tu.pred + "\t" + tu.obj + "\n");
		}
	}
}