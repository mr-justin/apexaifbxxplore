package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PartitionTool {

	public static void main(String[] args) throws Exception {
		String folder = "/usr/fulinyun/pay-as-you-go-matching/ssjoin-data/";
		partition(folder+"trec.raw.txt", 100, 30, folder);
	}
	
	public static void partition(String input, int linesPerFile, int maxFileNum, 
			String targetFolder) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		PrintWriter pw;
		for (int i = 0; i < maxFileNum; i++) {
			pw = new PrintWriter(new FileWriter(targetFolder+i+".raw"));
			for (int j = 0; j < linesPerFile; j++) pw.println(br.readLine());
			pw.close();
		}
		br.close();
	}
}
