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
import java.util.regex.Pattern;

import com.ibm.semplore.imports.impl.data.util.HashTuple;
import com.ibm.semplore.imports.impl.data.util.TripleURI;

public class HashTupleSort {
	public void sort(String tripleFile, String newFile) throws IOException {
		Scanner rd = new Scanner(new BufferedReader(new FileReader(tripleFile)));
		BufferedWriter wr = new BufferedWriter(new FileWriter(newFile));
		ArrayList<HashTuple> arr = new ArrayList<HashTuple>();
		String temp;
		int count = 0;
		long s,p,o;
		String type, obj;
		
		System.out.println("Reading " + tripleFile + "...");
		while (rd.hasNextLong()) {
//		while ((temp = rd.readLine()) != null) {
			try {
			s = rd.nextLong(); type = rd.next(); p = rd.nextLong(); obj = rd.nextLine();
			count ++;
			if (count % 50000 == 0) System.out.println(count);
//			System.out.println(temp);
				arr.add(new HashTuple(s,type, p,obj));
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
			HashTuple tu = arr.get(i);
			wr.write(tu.sub + "\t" + tu.type + "\t" + tu.pred + tu.obj + "\n");
		}
		wr.close();
		rd.close();
	}
	public static void main(String[] args) {
		String a = "1\ta\t2\t3\n1\ta\t2\t3\n";
		Scanner rd = new Scanner(new StringReader(a));
		Pattern pattern = Pattern.compile(".");
		long s,p,o;
		String type, obj;
		s = rd.nextLong(); type = rd.next(); p = rd.nextLong(); 
		rd.next(pattern); 
		obj = rd.nextLine();
		System.out.println(s);
		System.out.println(type);
		System.out.println(p);
		System.out.println(obj);
	}
}
