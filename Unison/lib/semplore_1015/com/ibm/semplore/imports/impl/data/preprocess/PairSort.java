package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.ibm.semplore.imports.impl.data.util.PairURI;



public class PairSort {
	
	public void sort(String pairFile, String newFile, String sort) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(pairFile));
		BufferedWriter wr = new BufferedWriter(new FileWriter(newFile));
		ArrayList<PairURI> arr = new ArrayList<PairURI>();
		String temp;
		int count = 0;
		
		System.out.println("Reading " + pairFile + "...");
		while ((temp = rd.readLine()) != null) {
			String[] ts = temp.split("\t");
			count ++;
			if (count % 5000 == 0) System.out.println(count);
//			System.out.println(temp);
			try {
				arr.add(new PairURI(ts[0], ts[1], sort));
			}
			catch (Exception e) {
				System.out.println(temp);
			}
		}
		Collections.sort(arr);
		System.out.println("Writing " + newFile + "...");
		count = 0;
		for (int i = 0; i < arr.size(); i++) {
			count ++;
			if (count % 5000 == 0) System.out.println(count);
			PairURI tu = arr.get(i);
			wr.write(tu.instance + "\t" + tu.category + "\n");
		}
		wr.close();
		rd.close();
	}
	
}
