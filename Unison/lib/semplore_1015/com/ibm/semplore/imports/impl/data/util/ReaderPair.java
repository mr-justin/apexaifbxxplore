package com.ibm.semplore.imports.impl.data.util;

import java.io.Reader;



public class ReaderPair implements Comparable {
	public Reader reader;
	public PairURI pair;
	
	public ReaderPair(Reader rd, PairURI p) {
		reader = rd;
		pair = p;
	}

	public int compareTo(Object o) {
		return -this.pair.compareTo(((ReaderPair)o).pair);
	}

}
