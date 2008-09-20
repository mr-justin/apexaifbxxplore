package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.ibm.semplore.imports.impl.data.util.HashTuple;
import com.ibm.semplore.imports.impl.data.util.PairURI;
import com.ibm.semplore.imports.impl.data.util.ReaderHashTuple;
import com.ibm.semplore.imports.impl.data.util.ReaderPair;
import com.ibm.semplore.imports.impl.data.util.ReaderTriple;
import com.ibm.semplore.imports.impl.data.util.TripleURI;



public class FileMerger {

	public void mergeTriple(String prefix, int fileCount, String output, String sort) throws Exception {
		Scanner[] rd = new Scanner[fileCount];
		com.ibm.semplore.imports.impl.data.util.Heap h = new com.ibm.semplore.imports.impl.data.util.Heap();
		BufferedWriter wr = new BufferedWriter(new FileWriter(output));
		int count = 0;
		String s,p,o;
		TripleURI last = null;

		for (int i = 0; i < fileCount; i++) {
			rd[i] = new Scanner(new BufferedReader(new FileReader(prefix + i)));
			s = rd[i].next(); p = rd[i].next(); o = rd[i].next();
			h.insert(new ReaderTriple(rd[i], new TripleURI(s,p,o, sort)));
		}

		while (h.size() != 0) {
			ReaderTriple rt = (ReaderTriple) h.remove();
			if (last==null || rt.triple.compareTo(last)!=0) {
				last = rt.triple;
				if (++count % 500000 == 0) System.out.println(count);
				wr.write(rt.triple.sub + "\t" + rt.triple.pred + "\t" + rt.triple.obj + "\n");
			}
			if (rt.reader.hasNext()) {
				s = rt.reader.next(); p = rt.reader.next(); o = rt.reader.next();
				h.insert(new ReaderTriple(rt.reader, new TripleURI(s,p,o,sort)));
			}
		}

		for (int i = 0; i < fileCount; i++)
			rd[i].close();
		wr.close();
	}
	
	public void mergePair(String prefix, int fileCount, String output) throws Exception {
		Scanner[] rd = new Scanner[fileCount];
		com.ibm.semplore.imports.impl.data.util.Heap h = new com.ibm.semplore.imports.impl.data.util.Heap();
		BufferedWriter wr = new BufferedWriter(new FileWriter(output));
		int count = 0;
		String ins;
		String temp;
		PairURI last = null;

		for (int i = 0; i < fileCount; i++) {
			rd[i] = new Scanner(new BufferedReader(new FileReader(prefix + i)));
			ins = rd[i].next(); temp = rd[i].next();
			h.insert(new ReaderPair(rd[i], new PairURI(ins, temp)));
		}

		while (h.size() != 0) {
			ReaderPair rt = (ReaderPair) h.remove();
			if (last==null || rt.pair.compareTo(last)!=0) {
				if (++count % 500000 == 0) System.out.println(count);
				wr.write(rt.pair.instance + "\t" + rt.pair.category + "\n");
				last = rt.pair;
			}
			if (rt.reader.hasNext()) {
				ins = rt.reader.next(); temp = rt.reader.next();
				h.insert(new ReaderPair(rt.reader, new PairURI(ins, temp)));
			}
		}

		for (int i = 0; i < fileCount; i++)
			rd[i].close();
		wr.close();
	}
	public void mergeHashTuple(String prefix, int fileCount, String output) throws Exception {
		Scanner[] rd = new Scanner[fileCount];
		com.ibm.semplore.imports.impl.data.util.Heap h = new com.ibm.semplore.imports.impl.data.util.Heap();
		BufferedWriter wr = new BufferedWriter(new FileWriter(output));
		int count = 0;
		long s,p;
		String o;
		String type;
		HashTuple last = null;

		for (int i = 0; i < fileCount; i++) {
			rd[i] = new Scanner(new BufferedReader(new FileReader(prefix + i)));
			s = rd[i].nextLong(); type = rd[i].next(); p = rd[i].nextLong(); 
			o = rd[i].nextLine();
			h.insert(new ReaderHashTuple(rd[i], new HashTuple(s,type,p,o)));
		}

		while (h.size() != 0) {
			ReaderHashTuple rt = (ReaderHashTuple) h.remove();
			if (last==null || rt.pair.compareTo(last)!=0) {
				if (++count % 500000 == 0) System.out.println(count);
				wr.write(rt.pair.sub + "\t" + rt.pair.type + "\t" + rt.pair.pred + rt.pair.obj + "\n");
				last = rt.pair;
			}
			if (rt.reader.hasNext()) {
				s = rt.reader.nextLong(); type = rt.reader.next(); p = rt.reader.nextLong(); 
				o = rt.reader.nextLine();
				h.insert(new ReaderHashTuple(rt.reader, new HashTuple(s,type,p,o)));
			}
		}

		for (int i = 0; i < fileCount; i++)
			rd[i].close();
		wr.close();
	}
}
