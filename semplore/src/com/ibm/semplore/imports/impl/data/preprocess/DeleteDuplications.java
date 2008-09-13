package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.ibm.semplore.config.Config;

public class DeleteDuplications {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		try{
			Config config = new Config();
//			String filename = config.dir+"sample.relsplit.relID";
//			String outputname = config.dir+"sample.relsplit.relID.gen";
			String filename = config.dir+"sample.catsplit.catID";
			String outputname = config.dir+"sample.catsplit.catID.gen";
			String temp;
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			PrintWriter writer = new PrintWriter(new FileWriter(outputname));
			String last = "";
			while ((temp = reader.readLine())!=null){
				if (temp.equals(last)) continue;
				writer.println(temp);
				last = temp;
			}
			writer.close();
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

}
