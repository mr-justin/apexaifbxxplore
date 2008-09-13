package com.ibm.semplore.imports.impl.data.util;

import java.io.Reader;
import java.util.Scanner;



public class ReaderPair implements Comparable {
	public Scanner reader;
	public PairURI pair;
	
	public ReaderPair(Scanner rd, PairURI p) {
		reader = rd;
		pair = p;
	}

	public int compareTo(Object o) {
		return -this.pair.compareTo(((ReaderPair)o).pair);
	}

}
