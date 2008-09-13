package com.ibm.semplore.imports.impl.data.util;

import java.util.Scanner;

public class ReaderHashTuple implements Comparable{
	public Scanner reader;
	public HashTuple pair;
	
	public ReaderHashTuple(Scanner rd, HashTuple p) {
		reader = rd;
		pair = p;
	}

	public int compareTo(Object o) {
		return -this.pair.compareTo(((ReaderHashTuple)o).pair);
	}
}
