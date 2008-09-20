package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import com.ibm.semplore.imports.impl.data.util.TripleURI;



public class TripleSort {
	public void sort(String tripleFile, String newFile, String sort) throws IOException {
		Scanner rd = new Scanner(new BufferedReader(new FileReader(tripleFile)));
		BufferedWriter wr = new BufferedWriter(new FileWriter(newFile));
		ArrayList<TripleURI> arr = new ArrayList<TripleURI>();
		String temp;
		int count = 0;
		String s,p,o;
		
		System.out.println("Reading " + tripleFile + "...");
		while (rd.hasNext()) {
//		while ((temp = rd.readLine()) != null) {
			s = rd.next(); p = rd.next(); o = rd.next();
			count ++;
			if (count % 50000 == 0) System.out.println(count);
//			System.out.println(temp);
			try {
				arr.add(new TripleURI(s,p,o, sort));
			}
			catch (Exception e) {
				System.out.println(count);
			}
		}
		Collections.sort(arr);
		System.out.println("Writing " + newFile + "...");
		count = 0;
		for (int i = 0; i < arr.size(); i++) {
			count ++;
			if (count % 50000 == 0) System.out.println(count);
			TripleURI tu = arr.get(i);
			wr.write(tu.sub + "\t" + tu.pred + "\t" + tu.obj + "\n");
		}
		wr.close();
		rd.close();
	}
	
	public static void main(String[] args) {
		String test = "1;2\t3;4\t5;6\n7;8\t9;-1\t2;3\n";
		Scanner rd = new Scanner(new StringReader(test));
		String s,p,o;
		while (rd.hasNext()) {
			s = rd.next();	p = rd.next();	o = rd.next();
			System.out.println(String.format("%s^%s^%s", s,p,o));
		}

	}
}
