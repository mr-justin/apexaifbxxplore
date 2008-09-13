package com.ibm.semplore.imports.impl.data.util;

import java.io.Reader;
import java.util.Scanner;



public class ReaderTriple implements Comparable{
	public Scanner reader;
	public TripleURI triple;
	
	public ReaderTriple(Scanner rd, TripleURI tri) {
		reader = rd; triple = tri;
	}
	
	public int compareTo(Object arg0) {
		return -this.triple.compareTo(((ReaderTriple)arg0).triple);
	}
	
	
	
}
