package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileSpliter {
	int maxLine;
	
	public FileSpliter(int m) {
		maxLine = m;
	}
	
	public int splitFile(String filename) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(filename));
		BufferedWriter wr = new BufferedWriter(new FileWriter(filename + "0"));
		String temp;
		int fileCount = 0, lineCount = 0;
		
		while ((temp = rd.readLine()) != null) {
			lineCount++;
			if (lineCount % 5000 == 0) System.out.println(lineCount);
			wr.write(temp + "\n");
			if (lineCount % maxLine == 0) {
				System.out.println("End of file " + fileCount);
				fileCount++;
				wr.close();
				wr = new BufferedWriter(new FileWriter(filename + fileCount));
			}
		}
		wr.close();
		rd.close();
		System.out.println("End of file " + fileCount);
		return fileCount + 1;
	}
}
