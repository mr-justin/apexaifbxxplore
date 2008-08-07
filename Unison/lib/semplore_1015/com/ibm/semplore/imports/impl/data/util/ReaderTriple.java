package com.ibm.semplore.imports.impl.data.util;

import java.io.Reader;



public class ReaderTriple implements Comparable{
	public Reader reader;
	public TripleURI triple;
	
	public ReaderTriple(Reader rd, TripleURI tri) {
		reader = rd; triple = tri;
	}
	
	public int compareTo(Object arg0) {
		return -this.triple.compareTo(((ReaderTriple)arg0).triple);
	}
	
	
	
}
