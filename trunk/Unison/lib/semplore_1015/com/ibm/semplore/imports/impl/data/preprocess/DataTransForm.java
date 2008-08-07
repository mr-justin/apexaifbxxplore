package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

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
		BufferedReader reader;
		BufferedOutputStream writer; 
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
			System.out.println(outputFileName);
			outputFile = new File(outputFileName);
			outputFile.createNewFile();
			writer = new BufferedOutputStream(new FileOutputStream(outputFileName));
			reader = new BufferedReader(new FileReader(inputDir+tempFile.getName()));	
			String temp;
			int id;
			while ((temp = reader.readLine())!=null){
				System.out.println(temp);
				id = new Integer(temp).intValue();
				writer.write((id>>>24)&0xFF);
				writer.write((id>>>16)&0xFF);
				writer.write((id>>>8)&0xFF);
				writer.write((id>>>0)&0xFF);
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
