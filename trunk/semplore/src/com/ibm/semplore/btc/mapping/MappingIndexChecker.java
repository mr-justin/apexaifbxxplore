package com.ibm.semplore.btc.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class MappingIndexChecker {

	public static void main(String[] args) throws NumberFormatException, IOException {
		MappingIndexReader mr = new MappingIndexReader(args[0]);
		
		BufferedReader fin = new BufferedReader(new InputStreamReader(System.in));

		int current = -1;
		Iterator<Integer> itr = null;
		
		long time = System.currentTimeMillis();
		
		String line;
		while ((line=fin.readLine())!=null) {
			String[] split = line.split("\\t");
			int d1 = Integer.parseInt(split[0]);
			int d2 = Integer.parseInt(split[1]);
			if (current!=d1) {
				itr = mr.getMappings(d1);
				current = d1;
			}
			if (!itr.hasNext()) { System.out.println("error! "+d1+","+d2); continue;}
			if (itr.next()!=d2) System.out.println("error2! "+d1+","+d2);
		}
		
		long time_e = System.currentTimeMillis();
		System.out.println(time_e-time);

	}
}
