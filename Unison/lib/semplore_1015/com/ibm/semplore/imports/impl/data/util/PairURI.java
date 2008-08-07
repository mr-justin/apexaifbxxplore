package com.ibm.semplore.imports.impl.data.util;

public class PairURI implements Comparable{
	public String instance, category;
	String sort;
	public PairURI(String ins, String cat, String sort) {
		instance = ins;
		category = cat;
		this.sort = sort;
		}
	public int compareTo(Object arg0) {
		PairURI a = (PairURI)arg0;
		if (sort.equals("C")) return compareTo(a.category, category, a.instance, instance);
		else return compareTo(a.instance, instance, a.category, category);
	}
	int compareTo(String a0, String a1, String b0, String b1){
		int x;
		if ((x = new Integer(a0).compareTo(new Integer(a1))) != 0)
			return x;
		else
			return new Integer(b0).compareTo(new Integer(b1));
	}
}
