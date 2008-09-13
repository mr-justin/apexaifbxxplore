package com.ibm.semplore.xir.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.semplore.model.Category;

public class SemploreTermReader extends Reader {
	private Iterator<Category> itr;
	protected int nowTerm = -1;

	public SemploreTermReader(ArrayList<Category> cats) {
		itr = cats.iterator();
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
		if (itr.hasNext()) return String.valueOf((itr.next()).getIDofURI());
		else return null;
	}
}
