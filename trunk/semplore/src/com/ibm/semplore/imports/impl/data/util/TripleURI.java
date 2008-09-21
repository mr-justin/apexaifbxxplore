package com.ibm.semplore.imports.impl.data.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * 比较大小重载...
 *
 */
public class TripleURI implements Comparable {
	public long sub, pred, obj; 
	public String sort;
	public TripleURI(long s, long p, long o, String sort) {
		sub = s; pred = p; obj = o; this.sort = sort;
	}
	public int compareTo(Object arg0) {
		TripleURI a = (TripleURI)arg0;
		if (sort.equals("SRO")) return compareTo(a.sub,sub, a.pred,pred, a.obj, obj);
		if (sort.equals("ORS")) return compareTo(a.obj,obj, a.pred,pred, a.sub, sub);
		if (sort.equals("RSO")) return compareTo(a.pred,pred, a.sub, sub, a.obj, obj);
		if (sort.equals("ROS")) return compareTo(a.pred, pred, a.obj, obj, a.sub, sub);
		throw new Error("sort="+sort);
	}
	private int compareTo(long a0, long a1, long b0, long b1, long c0, long c1){
		if (a1!=a0) return a1>a0?1:-1;
		if (b1!=b0) return b1>b0?1:-1;
		if (c1!=c0) return c1>c0?1:-1;
		return 0;
	}

	public static void main(String[] args) {
		ArrayList<TripleURI> arr = new ArrayList<TripleURI>();
		arr.add(new TripleURI(4675473174458233343l,-8754609171607444120l,-4603906380473991850l,"RSO"));
		arr.add(new TripleURI(4862355692397456787l,-8754609171607444120l,2659168930109911440l,"RSO"));
		arr.add(new TripleURI(-413620939553944700l,8441574982497024798l,1120342977635256628l,"RSO"));
		arr.add(new TripleURI(-5158064180516699198l,-8754609171607444120l,3847424603389965712l,"RSO"));
		arr.add(new TripleURI(350261081197924613l,-7076502849930521836l,-131684385676710998l,"RSO"));
		Collections.sort(arr);
		for (int i = 0; i < arr.size(); i++) {
			TripleURI tu = arr.get(i);
			System.out.print(tu.sub + "\t" + tu.pred + "\t" + tu.obj + "\n");
		}

	}
}
