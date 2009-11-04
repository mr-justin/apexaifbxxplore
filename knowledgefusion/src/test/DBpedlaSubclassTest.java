package test;

import java.io.BufferedReader;

import main.Common;
import basic.IOFactory;

public class DBpedlaSubclassTest {

	public static void main(String[] args) throws Exception {
		BufferedReader br = IOFactory.getGzBufferedReader(Common.dbpedia);
		int count = 0;
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
//			String[] parts = line.split(" ");
//			if (parts[0].contains("Category:")) {
//				System.out.println(line);
//				count++;
//				if (count%50 == 0) {
//					System.in.read();
//					System.in.read();
//				}
//			}
			if (line.contains(Common.dbpediaSubclass)) {
				System.out.println(line);
				count++;
				if (count%50 == 0) {
					System.in.read();
					System.in.read();
				}
			}
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(lineCount);
		}
	}
}
