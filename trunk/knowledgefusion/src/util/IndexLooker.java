package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import main.Blocker;
import main.Clusterer;
import main.Indexer;
import main.KeyIndDealer;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;

import basic.AsciiUtils;
import basic.IOFactory;

public class IndexLooker {
	
	public static String refIndex = Indexer.indexFolder+"refIndex";
	public static String basicFeatureIndex = Indexer.indexFolder+"basicFeatureIndex";
	
	public static void main(String[] args) throws Exception {
//		lookBasicFeature();
//		batchGenerateRefIndexHtml(Blocker.workFolder+"extended60miss.txt", 0, 50000, 
//				KeyIndDealer.domainDBpedia, KeyIndDealer.domainDblp, 
//				Blocker.workFolder+"extendedDBpediaDBLP/");
//		batchGenerateRefIndexHtml(Blocker.workFolder+"extended60miss.txt", 0, 50000, 
//				KeyIndDealer.domainDblp, KeyIndDealer.domainDblp, 
//				Blocker.workFolder+"extendedDBLP/");
//
//		batchGenerateRefIndexHtml(Blocker.workFolder+"basic60miss.txt", 0, 40000, 
//				KeyIndDealer.domainDBpedia, KeyIndDealer.domainDblp, 
//				Blocker.workFolder+"basicDBpediaDBLP/");
//		batchGenerateRefIndexHtml(Clusterer.workFolder+"cluster2&1.1domain2label.txt", 0, 5000, 
//				KeyIndDealer.domainDblp, KeyIndDealer.domainDblp, 
//				Clusterer.workFolder+"basicDBLP2label/");
//		batchGenerateRefIndexHtml(Clusterer.workFolder+"cluster2&1.1domain2label.txt", 0, 5000, 
//				KeyIndDealer.domainDBpedia, KeyIndDealer.domainGeonames, 
//				Clusterer.workFolder+"basicDBpediaGeonames2label/");
		String[] keywords = {"An", "Ben", "Cross", "Cruz",
				"David", "der", "El", "George", "Giorgio", "Hall", "John", "la", "Little", "Mount",
				"Pietro", "Port", "Santiago", "Spring", "Torre", "Douglas", "Luis", "White", "Maria", 
				"Pedro", "Robert", "Antonio", "M.", "Carlos", "Juan", "Jos\\u00E9"};
		
//		An	36	649	1
//		Ben	29	145	1
//		Cross	36	230	1
//		Cruz	39	206	1
//		David	26	353	1
//		der	99	471	1
//		El	173	854	1
//		George	39	756	1
//		Giorgio	28	192	1
//		Hall	43	641	1
//		John	35	1421	1
//		la	167	936	1
//		Little	107	823	1
//		Mills	55	184	1
//		Mount	688	2412	1
//		Pietro	37	229	1
//		Port	192	1003	1
//		Santiago	33	194	1
//		Spring	67	278	1
//		Torre	40	233	1
//		Douglas	27	212	2
//		Luis	27	136	2
//		White	66	634	2
//		Maria	55	490	3
//		Pedro	34	215	3
//		Robert	25	580	3
//		Antonio	27	285	4
//		M.	27	475	5
//		Carlos	32	170	7
//		Juan	58	411	7
//		Jos\u00E9	61	360	9

		for (String keyword : keywords) {
			batchGenerateRefIndexHtml(Clusterer.workFolder+"clusterKeyword="+keyword+"Sn=6.txt", 0, 5000, 
					Clusterer.workFolder+keyword+"/");
		}
	}
	
	public static void batchGenerateRefIndexHtml(String tolabel, int start, int end, 
			String outputFolder) throws Exception {
		File labeldir = new File(outputFolder);
		if (!labeldir.exists() || !labeldir.isDirectory()) labeldir.mkdir(); 
		IndexReader ireader = IndexReader.open(refIndex);
		BufferedReader br = IOFactory.getBufferedReader(tolabel);
		for (int i = 0; i < start; i++) br.readLine();
		int htmlNum = 0;
		for (int i = start; i < end; i++) {
			String line = br.readLine();
			if (line == null) break;
			String[] parts = line.split(" ");
			for (int j = 0; j < parts.length; j++) for (int k = j+1; k < parts.length; k++) 
				if (generateRefIndexHtml(ireader, Integer.parseInt(parts[j]), 
						Integer.parseInt(parts[k]), htmlNum, 
						outputFolder)) htmlNum++;
			if ((i+1) % 100 == 0) System.out.println((i+1)+"");
		}
		ireader.close();
	}

	/**
	 * convert id pairs from tolabel to html files describing entity information
	 * @param tolabel
	 * @param start starting line number
	 * @param end ending line number
	 * @throws Exception
	 */
	public static void batchGenerateRefIndexHtml(String tolabel, int start, int end, String domain1, String domain2, 
			String outputFolder) throws Exception {
		File labeldir = new File(outputFolder);
		if (!labeldir.exists() || !labeldir.isDirectory()) labeldir.mkdir(); 
		IndexReader ireader = IndexReader.open(refIndex);
		BufferedReader br = IOFactory.getBufferedReader(tolabel);
		for (int i = 0; i < start; i++) br.readLine();
		int htmlNum = 0;
		for (int i = start; i < end; i++) {
			String line = br.readLine();
			if (line == null) break;
			String[] parts = line.split(" ");
			if (generateRefIndexHtml(ireader, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), htmlNum, domain1, 
					domain2, outputFolder)) htmlNum++;
			if ((i+1) % 100 == 0) System.out.println((i+1)+"");
		}
		ireader.close();
	}
	
	public static boolean generateRefIndexHtml(IndexReader ireader, int id1, int id2, int num, 
			String outputFolder) throws Exception {
		String uri1 = ireader.document(id1).get("URI");
		String uri2 = ireader.document(id2).get("URI");
		PrintWriter pw = IOFactory.getPrintWriter(outputFolder + num+".html");
		pw.println("<html><body>");
		if (num > 0) pw.println("<a href=" + (num-1) + ".html>previous</a>");
		pw.println("<a href=" + (num+1) + ".html>next</a>");
		pw.println("<table border=1><tr><td width=50% valign=top>");
		
		pw.println(id1+"<br>");
		pw.println("<b>&lt;<a href=" + uri1.substring(1, uri1.length()-1) + ">" + uri1.substring(1, uri1.length()-1) + 
				"</a>&gt;</b>");
		pw.println("<p>");
		List fieldList = ireader.document(id1).getFields();
		for (Object obj : fieldList) {
			Field field = (Field)obj;
			if (!field.name().equals("URI") && !field.name().endsWith("to") && !field.name().endsWith("from")) 
				pw.println(
						AsciiUtils.unicodeEncode(
							field.name().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + 
							" : " + field.stringValue().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "<br>"
						)
					);
			
		}
		
		pw.println("</td><td>");
		
		pw.println(id2+"<br>");
		pw.println("<b>&lt;<a href=" + uri2.substring(1, uri2.length()-1) + ">" + uri2.substring(1, uri2.length()-1) + 
				"</a>&gt;</b>");
		pw.println("<p>");
		fieldList = ireader.document(id2).getFields();
		for (Object obj : fieldList) {
			Field field = (Field)obj;
			if (!field.name().equals("URI") && !field.name().endsWith("to") && !field.name().endsWith("from")) 
				pw.println(
						AsciiUtils.unicodeEncode(
							field.name().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + 
							" : " + field.stringValue().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "<br>"
						)
					);
			
		}
		pw.println("</td></tr></table>");
		if (num > 0) pw.println("<a href=" + (num-1) + ".html>previous</a>");
		pw.println("<a href=" + (num+1) + ".html>next</a>");
		pw.println("</body></html>");
		pw.close();
		return true;
	}

	/**
	 * generate information about a pair of entities, only generate pairs between certain domains
	 * @param ireader
	 * @param id1
	 * @param id2
	 * @param num
	 * @throws Exception
	 */
	public static boolean generateRefIndexHtml(IndexReader ireader, int id1, int id2, int num, 
			String domain1, String domain2, String outputFolder) throws Exception {
		String uri1 = ireader.document(id1).get("URI");
		String uri2 = ireader.document(id2).get("URI");
		if (!(uri1.contains(domain1) && uri2.contains(domain2) || uri1.contains(domain2) && uri2.contains(domain1))) return false;
		PrintWriter pw = IOFactory.getPrintWriter(outputFolder + num+".html");
		pw.println("<html><body>");
		if (num > 0) pw.println("<a href=" + (num-1) + ".html>previous</a>");
		pw.println("<a href=" + (num+1) + ".html>next</a>");
		pw.println("<table border=1><tr><td width=50% valign=top>");
		pw.println("<b>&lt;<a href=" + uri1.substring(1, uri1.length()-1) + ">" + uri1.substring(1, uri1.length()-1) + 
				"</a>&gt;</b>");
		pw.println("<p>");
		List fieldList = ireader.document(id1).getFields();
		for (Object obj : fieldList) {
			Field field = (Field)obj;
			if (!field.name().equals("URI") && !field.name().endsWith("to") && !field.name().endsWith("from")) 
				pw.println(
						AsciiUtils.unicodeEncode(
							field.name().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + 
							" : " + field.stringValue().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "<br>"
						)
					);
			
		}
		pw.println("</td><td>");
		pw.println("<b>&lt;<a href=" + uri2.substring(1, uri2.length()-1) + ">" + uri2.substring(1, uri2.length()-1) + 
				"</a>&gt;</b>");
		pw.println("<p>");
		fieldList = ireader.document(id2).getFields();
		for (Object obj : fieldList) {
			Field field = (Field)obj;
			if (!field.name().equals("URI") && !field.name().endsWith("to") && !field.name().endsWith("from")) 
				pw.println(
						AsciiUtils.unicodeEncode(
							field.name().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + 
							" : " + field.stringValue().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "<br>"
						)
					);
			
		}
		pw.println("</td></tr></table>");
		if (num > 0) pw.println("<a href=" + (num-1) + ".html>previous</a>");
		pw.println("<a href=" + (num+1) + ".html>next</a>");
		pw.println("</body></html>");
		pw.close();
		return true;
	}
	
	public static void lookBasicFeature() throws Exception {
		Scanner sc = new Scanner(System.in);
		IndexReader ireader = IndexReader.open(basicFeatureIndex);
		while (true) {
			int n = sc.nextInt();
			System.out.println(n);
			System.out.println(ireader.document(n).get("URI"));
			System.out.println(ireader.document(n).get("basic"));
		}
	}
	
	public static void lookRefIndex() throws Exception {
		Scanner sc = new Scanner(System.in);
		IndexReader ireader = IndexReader.open(refIndex);
		while (true) {
			int n = sc.nextInt();
			System.out.println(n);
			System.out.println(ireader.document(n).get("URI"));
			List fieldList = ireader.document(n).getFields();
			for (Object obj : fieldList) {
				Field field = (Field)obj;
				if (!field.name().equals("URI")) 
					System.out.println(field.name() + " : " + field.stringValue());
			}
		}
	}

}
