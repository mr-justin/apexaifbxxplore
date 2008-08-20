package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.ibm.semplore.imports.impl.data.util.TripleURI;



public class TripleSort {
	public void sort(String tripleFile, String newFile, String sort) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(tripleFile));
		BufferedWriter wr = new BufferedWriter(new FileWriter(newFile));
		ArrayList<TripleURI> arr = new ArrayList<TripleURI>();
		String temp;
		int count = 0;
		
		System.out.println("Reading " + tripleFile + "...");
		while ((temp = rd.readLine()) != null) {
			String[] ts = temp.split("\t");
			count ++;
			if (count % 5000 == 0) System.out.println(count);
//			System.out.println(temp);
			try {
				arr.add(new TripleURI(ts[0], ts[1], ts[2], sort));
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
			TripleURI tu = arr.get(i);
			wr.write(tu.sub + "\t" + tu.pred + "\t" + tu.obj + "\n");
		}
		wr.close();
		rd.close();
	}
	
	public static void main(String[] args) {
		TripleSort ts = new TripleSort();
		
		try {
			ts.sort(args[0], args[1], args[2]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}