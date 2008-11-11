package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.ibm.semplore.config.Config;

public class RelInfoDiv {

	/**
	 * @param args
	 */
	String[] split, last;

	String temp;

	
	String prefix;
	String filename;
	int columnNum; 
	public RelInfoDiv(String inFile, String prefix, int columnNum){
		this.prefix = prefix;
		this.filename = inFile;
		this.columnNum = columnNum-1;
	}
	 
	PrintWriter newFile(PrintWriter writer) {
		try {
			if (last != null && last[1].equals(split[1]))
				return writer;
			if (writer != null)
				writer.close();
			return new PrintWriter(new FileWriter(config.dir+prefix + split[1]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	void printInFile(PrintWriter writer) {
		if (last == null || !split[1].equals(last[1]) || !split[columnNum].equals(last[columnNum])){
			writer.println(split[columnNum]);
		}	
		last = split;
	}
	Config config = new Config();
	
	public void divide(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			PrintWriter writer = null;
			while ((temp = reader.readLine()) != null) {
//				System.out.println(temp);
				split = temp.split("\t");
				writer = newFile(writer);
				printInFile(writer);
			}
			writer.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
