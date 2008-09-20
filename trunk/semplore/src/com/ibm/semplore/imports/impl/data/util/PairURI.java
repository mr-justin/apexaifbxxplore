package com.ibm.semplore.imports.impl.data.util;

import java.util.ArrayList;
import java.util.Collections;

import com.ibm.semplore.util.HashID;

public class PairURI implements Comparable{
	public HashID instance;
	public String category;
	public PairURI(String ins, String cat) {
		instance = new HashID(ins);
		category = cat;
	}
	public int compareTo(Object arg0) {
		PairURI a = (PairURI)arg0;
		int c = instance.compareTo(a.instance);
		if (c!=0) return c;
		else return category.compareTo(a.category);
	}
	
	public static void main(String[] args) {
		ArrayList<PairURI> arr = new ArrayList<PairURI>();
		arr.add(new PairURI("1;2", "."));
		arr.add(new PairURI("1;3", "."));
		arr.add(new PairURI("-1;3", "."));
		Collections.sort(arr);
		for (int i = 0; i < arr.size(); i++) {
			PairURI tu = arr.get(i);
			System.out.print(tu.instance + "\t" + tu.category + "\n");
		}

	}
}
