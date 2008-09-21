package com.ibm.semplore.imports.impl.data.util;

import java.util.ArrayList;
import java.util.Collections;

public class PairURI implements Comparable{
	public long instance;
	public String category;
	public PairURI(long ins, String cat) {
		instance = ins;
		category = cat;
	}
	public int compareTo(Object arg0) {
		PairURI a = (PairURI)arg0;
		if (instance != a.instance) return instance > a.instance ? 1 : -1;
		else return category.compareTo(a.category);
	}
	
	public static void main(String[] args) {
		ArrayList<PairURI> arr = new ArrayList<PairURI>();
		arr.add(new PairURI(1, "2"));
		arr.add(new PairURI(1, "1"));
		arr.add(new PairURI(3, "3"));
		Collections.sort(arr);
		for (int i = 0; i < arr.size(); i++) {
			PairURI tu = arr.get(i);
			System.out.print(tu.instance + "\t" + tu.category + "\n");
		}

	}
}
