package com.ibm.semplore.xir.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;

public class SemploreTermReader4Attributes extends Reader {
	private Iterator<AttributeValue> itr;
	protected int nowTerm = -1;

	public SemploreTermReader4Attributes(LinkedList<AttributeValue> attrVals) {
		itr = attrVals.iterator();
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String next() {
		if (itr.hasNext()) {
			AttributeValue attrVal = itr.next();
			String term = attrVal.getCombination();
			return term;
		}
		else return null;
	}
}
