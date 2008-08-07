package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.IOException;

public class FileSort {
	
	int maxLine = 500000;
	FileSpliter fs = new FileSpliter(maxLine);
	FileMerger fm = new FileMerger();
	
	public void sortTriple(String filename, String oFileName, String sort) throws Exception {
		try {
			com.ibm.semplore.imports.impl.data.preprocess.TripleSort ts = new com.ibm.semplore.imports.impl.data.preprocess.TripleSort();
			
			int fileCount = fs.splitFile(filename);
//			int fileCount = 9;
			for (int i = 0; i < fileCount; i++)
				ts.sort(filename + i, filename + ".n" + i, sort);
			fm.mergeTriple(filename + ".n", fileCount, oFileName, sort);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sortPair(String filename, String output, String sort) throws Exception {
		try {
			com.ibm.semplore.imports.impl.data.preprocess.PairSort ps = new com.ibm.semplore.imports.impl.data.preprocess.PairSort();
			
			int fileCount = fs.splitFile(filename);
			for (int i = 0; i < fileCount; i++)
				ps.sort(filename + i, filename + ".n" + i, sort);
			
			fm.mergePair(filename + ".n", fileCount, output, sort);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		FileSort fs = new FileSort();
//		Config config = new Config();
//		try {
//			System.out.println(config.tempDir + "ori");
////			fs.sortTriple(config.tempDir+"ori", "RSO");
//			/**
//			 * Ҫ������ļ�
//			 */
////			fs.sortPair("data/dbpedia/category");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
}
