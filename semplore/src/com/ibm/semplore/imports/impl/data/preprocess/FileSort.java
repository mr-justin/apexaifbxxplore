package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.File;
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
			for (int i = 0; i < fileCount; i++) {
				new File(filename + i).delete();
				new File(filename + ".n" + i).delete();
			}
			System.out.println("Sort output: " + oFileName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sortPair(String filename, String output) throws Exception {
		try {
			com.ibm.semplore.imports.impl.data.preprocess.PairSort ps = new com.ibm.semplore.imports.impl.data.preprocess.PairSort();
			
			int fileCount = fs.splitFile(filename);
			for (int i = 0; i < fileCount; i++)
				ps.sort(filename + i, filename + ".n" + i);
			
			fm.mergePair(filename + ".n", fileCount, output);
			for (int i = 0; i < fileCount; i++) {
				new File(filename + i).delete();
				new File(filename + ".n" + i).delete();
			}
			System.out.println("Sort output: " + output);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sortHashTuple(String filename, String output) throws Exception {
		try {
			com.ibm.semplore.imports.impl.data.preprocess.HashTupleSort ps = new com.ibm.semplore.imports.impl.data.preprocess.HashTupleSort();
			
			int fileCount = fs.splitFile(filename);
			for (int i = 0; i < fileCount; i++)
				ps.sort(filename + i, filename + ".n" + i);
			
			fm.mergeHashTuple(filename + ".n", fileCount, output);
			for (int i = 0; i < fileCount; i++) {
				new File(filename + i).delete();
				new File(filename + ".n" + i).delete();
			}
			System.out.println("Sort output: " + output);
			
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
//			 * 要排序的文件
//			 */
////			fs.sortPair("data/dbpedia/category");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
}
