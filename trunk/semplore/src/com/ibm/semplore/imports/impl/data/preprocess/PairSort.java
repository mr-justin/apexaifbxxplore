package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import com.ibm.semplore.imports.impl.data.util.PairURI;



public class PairSort {
	public void sort(String pairFile, String newFile) throws IOException {
		sort(pairFile, newFile, true);
	}
	
	public void sort(String pairFile, String newFile, boolean unique) throws IOException {
		Scanner rd = new Scanner(new BufferedReader(new FileReader(pairFile)));
		BufferedWriter wr = new BufferedWriter(new FileWriter(newFile));
		ArrayList<PairURI> arr = new ArrayList<PairURI>();
		int count = 0;
		long ins;
		String temp;
		PairURI last = null;
		
		System.out.println("Reading " + pairFile + "...");
		while (rd.hasNext()) {
			ins = rd.nextLong();
			temp = rd.nextLine();
			count ++;
			if (count % 50000 == 0) System.out.println(count);
			arr.add(new PairURI(ins, temp));
		}
		Collections.sort(arr);
		System.out.println("Writing " + newFile + "...");
		count = 0;
		for (int i = 0; i < arr.size(); i++) {
			count ++;
			if (count % 50000 == 0) System.out.println(count);
			PairURI tu = arr.get(i);
			if (unique) {
				if (last==null || tu.compareTo(last)!=0) {
					last = tu;
					wr.write(tu.instance + /*"\t" +*/ tu.category + "\n");
				}
			} else
				wr.write(tu.instance + /*"\t" +*/ tu.category + "\n");
		}
		wr.close();
		rd.close();
	}
	
}
