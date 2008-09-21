package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Scanner;

import com.ibm.semplore.config.Config;

public class DataTransForm {

	/**
	 * @param args
	 */
	public void transForm(String dir) {
		try{
		Config config = new Config();
		String inputDir = dir;
		String outputDir = dir;
		File tempFile ;
		File outputFile;
		String outputFileName;
		File list[] = new File(dir).listFiles();
		Scanner reader;
		DataOutputStream writer; 
		for (int i = 0; i<list.length; i++){
			tempFile = list[i];
			outputFileName = tempFile.getName();
			if ((outputFileName.charAt(0) == 'O' || outputFileName.charAt(0) == 'S') && outputFileName.charAt(1)=='_')
				outputFileName = dir + "R"+outputFileName;
			else if (outputFileName.equals("relationTemp"))
				outputFileName = dir + "relation";
			else if (outputFileName.equals("categoryTemp"))
				outputFileName =dir + "category";
			else continue;
			if (tempFile.isDirectory()) continue;
			System.out.println("transform to " + outputFileName);
			outputFile = new File(outputFileName);
			outputFile.createNewFile();
			writer = new DataOutputStream(new FileOutputStream(outputFileName));
			reader = new Scanner(new BufferedReader(new FileReader(inputDir+tempFile.getName())));	
			String temp;
			while (reader.hasNext()) {
				writer.writeLong(reader.nextLong());
			}
			writer.close();
		}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String args[]){
		DataTransForm dtf = new DataTransForm();
		dtf.transForm("E:/User/AllisQM/Semplore/data/dbpedia sample/temp/pass/");
	}
}
