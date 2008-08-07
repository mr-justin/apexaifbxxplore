package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.ibm.semplore.imports.impl.data.util.PairURI;
import com.ibm.semplore.imports.impl.data.util.ReaderPair;
import com.ibm.semplore.imports.impl.data.util.ReaderTriple;
import com.ibm.semplore.imports.impl.data.util.TripleURI;



public class FileMerger {

	public void mergeTriple(String prefix, int fileCount, String output, String sort) throws Exception {
		BufferedReader[] rd = new BufferedReader[fileCount];
		com.ibm.semplore.imports.impl.data.util.Heap h = new com.ibm.semplore.imports.impl.data.util.Heap();
		BufferedWriter wr = new BufferedWriter(new FileWriter(output));
		int count = 0;

		for (int i = 0; i < fileCount; i++) {
			rd[i] = new BufferedReader(new FileReader(prefix + i));
			String[] stri = rd[i].readLine().split("\t");
			h.insert(new ReaderTriple(rd[i], new TripleURI(stri[0], stri[1], stri[2], sort)));
		}

		while (h.size() != 0) {
			ReaderTriple rt = (ReaderTriple) h.remove();
			if (++count % 5000 == 0) System.out.println(count);
			wr.write(rt.triple.sub + "\t" + rt.triple.pred + "\t" + rt.triple.obj + "\n");
			String temp;
			if ((temp = ((BufferedReader)rt.reader).readLine()) != null) {
				String[] stri = temp.split("\t");

				h.insert(new ReaderTriple(rt.reader, new TripleURI(stri[0], stri[1], stri[2],sort)));
			}
		}

		for (int i = 0; i < fileCount; i++)
			rd[i].close();
		wr.close();
	}
	
	public void mergePair(String prefix, int fileCount, String output, String sort) throws Exception {
		BufferedReader[] rd = new BufferedReader[fileCount];
		com.ibm.semplore.imports.impl.data.util.Heap h = new com.ibm.semplore.imports.impl.data.util.Heap();
		BufferedWriter wr = new BufferedWriter(new FileWriter(output));
		int count = 0;

		for (int i = 0; i < fileCount; i++) {
			rd[i] = new BufferedReader(new FileReader(prefix + i));
			String[] stri = rd[i].readLine().split("\t");
			h.insert(new ReaderPair(rd[i], new PairURI(stri[0], stri[1], sort)));
		}

		while (h.size() != 0) {
			ReaderPair rt = (ReaderPair) h.remove();
			if (++count % 5000 == 0) System.out.println(count);
			wr.write(rt.pair.instance + "\t" + rt.pair.category + "\n");
			String temp;
			if ((temp = ((BufferedReader)rt.reader).readLine()) != null) {
				String[] stri = temp.split("\t");

				h.insert(new ReaderPair(rt.reader, new PairURI(stri[0], stri[1], sort)));
			}
		}

		for (int i = 0; i < fileCount; i++)
			rd[i].close();
		wr.close();
	}
}
