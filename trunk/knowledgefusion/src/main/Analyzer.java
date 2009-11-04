package main;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import basic.IDataSourceReader;
import basic.IOFactory;


public class Analyzer {

	// group the col-th column of input (.gz file) and count the size of each group
	public static void summarize(String input, int col, String output) throws Exception {
		System.out.println("summarize to " + output);
		IDataSourceReader br = IOFactory.getReader(input);
		HashMap<String, Integer> summaryTable = new HashMap<String, Integer>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			if (parts.length > 2) {
				String toSum = parts[col];
				if (summaryTable.containsKey(toSum)) 
					summaryTable.put(toSum, summaryTable.get(toSum)+1);
				else summaryTable.put(toSum, 1);
			}
			count++;
			if (count % 3000000 == 0) System.out.println(
					new Date().toString() + " : " + count);
		}
		br.close();
		System.out.println(count + " lines in all");
		writeSummaryTable(summaryTable, output);
	}
	
	// group the input sameAs statements by http domain pairs and count the size of each group
	public static void sumDomain(String input, String output) throws Exception {
		System.out.println("summarize to " + output);
		IDataSourceReader br = IOFactory.getReader(input);
		HashMap<String, Integer> summaryTable = new HashMap<String, Integer>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			if (parts.length > 2) {
				String o1 = parts[0];
				String o2 = parts[2];
				String[] uriparts = o1.split("/");
				if (uriparts.length > 2) o1 = uriparts[2];
				uriparts = o2.split("/");
				if (uriparts.length > 2) o2 = uriparts[2];
				String toSum = o1+"->"+o2;
				if (summaryTable.containsKey(toSum)) 
					summaryTable.put(toSum, summaryTable.get(toSum)+1);
				else summaryTable.put(toSum, 1);
			}
			count++;
			if (count % 3000000 == 0) System.out.println(
					new Date().toString() + " : " + count);
		}
		br.close();
		System.out.println(count + " lines in all");
		writeSummaryTable(summaryTable, output);
	}
	
	// count concept sizes, utilizing "rdf:type" & "owl:Class" predicates
	public static void sumConcept(String input, String output) throws Exception {
		System.out.println("sumConcept to " + output);
		BufferedReader br = IOFactory.getGzBufferedReader(input);
		HashMap<String, Integer> summaryTable = new HashMap<String, Integer>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			if (parts.length > 2 && (parts[1].equals(Common.rdfType) || parts[1].equals(Common.owlClass)
					|| parts[1].equals(Common.dbpediaSubject))) {
				if (summaryTable.containsKey(parts[2]))
					summaryTable.put(parts[2], summaryTable.get(parts[2])+1);
				else summaryTable.put(parts[2], 1);
			}
			count++;
			if (count % 3000000 == 0) System.out.println(
					new Date().toString() + " : " + count);
		}
		br.close();
		System.out.println(count + " lines in all");
		writeSummaryTable(summaryTable, output);
	}
	
	private static void writeSummaryTable(HashMap<String, Integer> summaryTable, 
			String output) throws Exception {
		PrintWriter pw = IOFactory.getPrintWriter(output); // to ensure the encoding is utf-8
		for (String key : summaryTable.keySet()) {
			pw.println(key + " " + summaryTable.get(key));
		}
		pw.close();
	}

	// count attribute sizes, an attribute is indicated by a quotation mark at the beginning of 
	// the third part of a triple
	public static void sumAttribute(String input, String output) throws Exception {
		System.out.println("sumAttribute to " + output);
		BufferedReader br = IOFactory.getGzBufferedReader(input);
		HashMap<String, Integer> summaryTable = new HashMap<String, Integer>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			if (parts.length > 2 && parts[2].startsWith("\"")) {
				if (summaryTable.containsKey(parts[1]))
					summaryTable.put(parts[1], summaryTable.get(parts[1])+1);
				else summaryTable.put(parts[1], 1);
			}
			count++;
			if (count % 3000000 == 0) System.out.println(
					new Date().toString() + " : " + count);
		}
		br.close();
		System.out.println(count + " lines in all");
		writeSummaryTable(summaryTable, output);
	}
	
	// delete lines in file2 from file1, and write to result
	public static void diff(String file1, String file2, String result) throws Exception {
		System.out.println("diff to " + result);
		HashSet<String> lines = new HashSet<String>();
		IDataSourceReader idr = IOFactory.getReader(file1); // to ensure the encoding is utf-8
		for (String line = idr.readLine(); line != null; line = idr.readLine()) lines.add(line);
		idr.close();
		idr = IOFactory.getReader(file2);
		for (String line = idr.readLine(); line != null; line = idr.readLine()) lines.remove(line);
		idr.close();
		PrintWriter pw = IOFactory.getPrintWriter(result); // to ensure the encoding is utf-8
		for (String s : lines) pw.println(s);
		pw.close();
	}
	
	// find lines in gz input file containing keyword
	public static void find(String input, String keyword) throws Exception {
		String[] lines = new String[]{"", "", "", "", ""};
		BufferedReader br = IOFactory.getGzBufferedReader(input);
		int count = 0;
		int hit = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			for (int i = 0; i < 4; i++) lines[i] = lines[i+1];
			lines[4] = line;
			if (lines[2].contains(keyword)) {
				System.out.println();
				for (String s : lines) System.out.println(s);
//				System.out.println();
				hit++;
				if (hit%10 == 0) {
					System.out.println("press <ENTER> to continue...");
					System.in.read();
				}
			}
			count++;
			if (count%3000000 == 0) System.out.println(count);
		}
		br.close();
	}
	
	// find lines with less than 2 parts
	public static void findLineLessThan3Parts(String input) throws Exception {
		String[] lines = new String[]{"", "", "", "", ""};
		BufferedReader br = IOFactory.getGzBufferedReader(input);
		int count = 0;
//		int hit = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			for (int i = 0; i < 4; i++) lines[i] = lines[i+1];
			lines[4] = line;
			String[] parts = lines[2].split(" ");
			if (lines[2].length() != 0 && parts.length < 3) {
				System.out.println("**********");
				for (String s : lines) System.out.println(s);
//				System.out.println();
//				hit++;
//				if (hit%10 == 0) {
//					System.out.println("press <ENTER> to continue...");
//					System.in.read();
//				}
			}
			count++;
			if (count%3000000 == 0) System.out.println(count);
		}
		br.close();
	}

	// sort the lines in filename according to the values in the col-th column, and write the 
	// result to output
	public static void sort(String filename, int col, String output) throws Exception {
		TreeMap<Integer, ArrayList<String>> sorted = new TreeMap<Integer, ArrayList<String>>();
		IDataSourceReader br = IOFactory.getReader(filename); // to ensure the encoding is utf-8
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int key = Integer.parseInt(parts[col]);
			if (sorted.containsKey(key)) sorted.get(key).add(line);
			else {
				ArrayList<String> value = new ArrayList<String>();
				value.add(line);
				sorted.put(key, value);
			}
		}
		br.close();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer key : sorted.keySet()) for (String line : sorted.get(key)) pw.println(line);
		pw.close();
	}
	
	// observe the gz file from the startLine, display 40 lines
	public static void observe(String filename, int startLine) throws Exception {
		BufferedReader br = IOFactory.getGzBufferedReader(filename);
		for (int i = 0; i < startLine; i++) br.readLine();
		for (int i = startLine; i < startLine+40; i++) System.out.println(br.readLine());
		br.close();
	}
	
	// observe command line utility
	public static void mainObserve(String filename) throws Exception {
		Scanner sc = new Scanner(System.in);
		while (true) {
			int startLine = sc.nextInt();
			observe(filename, startLine);
		}
	}
	
	/**
	 * extract distinct URIs from gzfile and write them to output (also a gz file)
	 * @param gzfile
	 * @param output
	 * @throws Exception
	 */
	public static void extractURI(String gzfile, String output) throws Exception {
		System.out.println(new Date().toString() + "start extracting URIs from " + gzfile);
		BufferedReader br = IOFactory.getGzBufferedReader(gzfile);
		HashSet<String> uris = new HashSet<String>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			addURI2Set(line, uris);
			count++;
			if (count % 1000000 == 0) System.out.println(count);
		}
		br.close();
		System.out.println(new Date().toString() + " : extracted URIs from " + gzfile);
		writeSet(uris, output);
		System.out.println(new Date().toString() + " : write " + uris.size() + " URIs to " + output);
	}
	
	private static void addURI2Set(String line, HashSet<String> uris) {
		String[] parts = line.split(" ");
		uris.add(parts[0]);
		if (!parts[2].startsWith("\"")) uris.add(parts[2]);
	}

	/**
	 * extract distinct URIs with Lucene facilities, applicable to large input file not fitting into the memory
	 * @param gzfile
	 * @param output
	 * @throws Exception
	 */
	public static void extractURIWithLucene(String gzfile, String output) throws Exception {
		System.out.println(new Date().toString() + "start extracting URIs from " + gzfile + " with Lucene");
		BufferedReader br = IOFactory.getGzBufferedReader(gzfile);
		Directory directory = FSDirectory.getDirectory(Common.gzFolder+"temp");
		IndexWriter iwriter = new IndexWriter(directory, new SimpleAnalyzer(), true, 
				IndexWriter.MaxFieldLength.UNLIMITED);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			addURI2Index(line, iwriter);
			count++;
			if (count % 1000000 == 0) System.out.println(count);
		}
		br.close();
		iwriter.optimize();
		iwriter.close();
		directory.close();
		System.out.println(new Date().toString() + " : extracted URIs from " + gzfile);
		writeTerms(Common.gzFolder+"temp", output);
	}
	
	/**
	 * extract URIs from line and add them to the term set of iwriter
	 * @param line
	 * @param iwriter
	 */
	private static void addURI2Index(String line, IndexWriter iwriter) throws Exception {
		String[] parts = line.split(" ");
		Document doc = new Document();
		doc.add(new Field("URI", parts[0], Field.Store.NO, Field.Index.NOT_ANALYZED));
		iwriter.addDocument(doc);
		if (!parts[2].startsWith("\"")) {
			Document doc1 = new Document();
			doc1.add(new Field("URI", parts[2], Field.Store.NO, Field.Index.NOT_ANALYZED));
			iwriter.addDocument(doc1);
		}
	}

	/**
	 * write all the terms in indexFolder to output (in gz format), one per line
	 * @param indexFolder
	 * @param output
	 */
	private static void writeTerms(String indexFolder, String output) throws Exception {
		IndexReader ireader = IndexReader.open(indexFolder);
		TermEnum terms = ireader.terms();
		PrintWriter pw = IOFactory.getGzPrintWriter(output);
		int count = 0;
		while (terms.next()) {
			pw.println(terms.term().text());
			count++;
		}
		pw.close();
		ireader.close();
		System.out.println(count + " URIs written to " + output);
	}

	/**
	 * write elements in set to output (in gz format), one per line.
	 * @param set
	 * @param output
	 * @throws Exception
	 */
	private static void writeSet(HashSet<String> set, String output) throws Exception {
		PrintWriter pw = IOFactory.getGzPrintWriter(output);
		for (String s : set) pw.println(s);
		pw.close();
	}

	/**
	 * remove elements in col-th column in file2 from file1 (a one-column file), and write 
	 * the result to output (in gz format)
	 * @param file1
	 * @param file2
	 * @param col
	 * @param output
	 * @throws Exception
	 */
	public static void diff(String file1, String file2, int col, String output) throws Exception {
		IDataSourceReader idr = IOFactory.getReader(file2);
		HashSet<String> lines = new HashSet<String>();
		for (String line = idr.readLine(); line != null; line = idr.readLine()) 
			lines.add(line.split(" ")[col]);
		idr.close();
		
		idr = IOFactory.getReader(file1);
		PrintWriter pw = IOFactory.getGzPrintWriter(output);
		int count = 0;
		for (String line = idr.readLine(); line != null; line = idr.readLine()) if (!lines.contains(line)) {
			pw.println(line);
			count++;
		}
		idr.close();
		pw.close();
		System.out.println(new Date().toString() + " : diff completed, " + output + " : " + 
				count + " lines");
	}
	
	/**
	 * count the number of lines in a file
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static int countLines(String file) throws Exception {
		IDataSourceReader idr = IOFactory.getReader(file);
		int count = 0;
		for (String line = idr.readLine(); line != null; line = idr.readLine()) count++;
		idr.close();
		return count;
	}
	
	/**
	 * extract sameAs statements
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	public static void extractSameAs(String input, String output) throws Exception {
		IDataSourceReader idsr = IOFactory.getReader(input);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int count = 0;
		for (String line = idsr.readLine(); line != null; line = idsr.readLine()) {
			if (line.contains("sameAs")) pw.println(line);
			count++;
			if (count%10000000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		pw.close();
		idsr.close();
	}
	
	public static void main(String[] args) throws Exception {
		
//		find(Common.geonames, "<http://sws.geonames.org/1000023/>");
//		find(Common.dbpedia, "!!! (three exclamation marks,");
//		mainObserve("e:\\user\\fulinyun\\dbpediaPreprocessed.gz");
//		diff(Common.gzFolder+"dblp.individual.gz", Common.gzFolder+"dblp.attribute.txt", 0, 
//				Common.gzFolder+"dblp.individual-a.gz");
//		
//		diff(Common.gzFolder+"dblp.individual-a.gz", Common.gzFolder+"dblp.relation.txt", 0, 
//				Common.gzFolder+"dblp.individual-a-r.gz");
//		
//		diff(Common.gzFolder+"dbpedia.individual.gz", Common.gzFolder+"dbpedia.attribute.txt", 0, 
//				Common.gzFolder+"dbpedia.individual-a.gz");
//		
//		diff(Common.gzFolder+"dbpedia.individual-a.gz", Common.gzFolder+"dbpedia.relation.txt", 0, 
//				Common.gzFolder+"dbpedia.individual-a-r.gz");
//
//		diff(Common.gzFolder+"foaf.individual.gz", Common.gzFolder+"foaf.attribute.txt", 0, 
//				Common.gzFolder+"foaf.individual-a.gz");
//		
//		diff(Common.gzFolder+"foaf.individual-a.gz", Common.gzFolder+"foaf.relation.txt", 0, 
//				Common.gzFolder+"foaf.individual-a-r.gz");
//
//		diff(Common.gzFolder+"geonames.individual.gz", Common.gzFolder+"geonames.attribute.txt", 0, 
//				Common.gzFolder+"geonames.individual-a.gz");
//		
//		diff(Common.gzFolder+"geonames.individual-a.gz", Common.gzFolder+"geonames.relation.txt", 0, 
//				Common.gzFolder+"geonames.individual-a-r.gz");
//
//		diff(Common.gzFolder+"uscensus.individual.gz", Common.gzFolder+"uscensus.attribute.txt", 0, 
//				Common.gzFolder+"uscensus.individual-a.gz");
//		
//		diff(Common.gzFolder+"uscensus.individual-a.gz", Common.gzFolder+"uscensus.relation.txt", 0, 
//				Common.gzFolder+"uscensus.individual-a-r.gz");
//
//		diff(Common.gzFolder+"wordnet.individual.gz", Common.gzFolder+"wordnet.attribute.txt", 0, 
//				Common.gzFolder+"wordnet.individual-a.gz");
//		
//		diff(Common.gzFolder+"wordnet.individual-a.gz", Common.gzFolder+"wordnet.relation.txt", 0, 
//				Common.gzFolder+"wordnet.individual-a-r.gz");

//		String sameAsFolder = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\crude\\";
//		String dbpediaSameAs = sameAsFolder+"dbpedia-v3.equ";
//		String geonamesSameAs = sameAsFolder+"geonames.equ";
//		String dblpSameAs = sameAsFolder+"swetodblp.equ";
//		String foafSameAs = sameAsFolder+"foaf.equ";

//		sumDomain(foafSameAs, sameAsFolder+"foafSum.txt"); // do not need to run
//		extractSameAs(Common.foaf, foafSameAs); // run, no sameAs triples found!!!
//		sumDomain(dbpediaSameAs, sameAsFolder+"dbpediaSum.txt");
//		sumDomain(geonamesSameAs, sameAsFolder+"geonamesSum.txt");
//		sumDomain(dblpSameAs, sameAsFolder+"dblpSum.txt");

//		mainObserve(Common.wordnet);
//		String wordnet = "wordnet.gz", dblp = "dblp.gz", dbpedia = "dbpedia.gz", 
//		geonames = "geonames.gz", uscensus = "uscensus.gz", foaf = "foaf.gz";
		
//		findLineLessThan3Parts(Common.dbpedia); // done
//		findLineLessThan3Parts(Common.geonames); // done
//		findLineLessThan3Parts(Common.uscensus); // done
//		findLineLessThan3Parts(Common.foaf); // done
//		extractURI(Common.wordnet, Common.gzFolder+"wordnet.uri.gz");
//		diff(Common.gzFolder+"wordnet.uri.gz", Common.gzFolder+"wordnet.concept.txt", 0, 
//				Common.gzFolder+"wordnet.individual.gz");
//
//		extractURI(Common.dblp, Common.gzFolder+"dblp.uri.gz");
//		diff(Common.gzFolder+"dblp.uri.gz", Common.gzFolder+"dblp.concept.txt", 0, 
//				Common.gzFolder+"dblp.individual.gz");
		
//		extractURIWithLucene(Common.dbpedia, Common.gzFolder+"dbpedia.uri.gz");
//		diff(Common.gzFolder+"dbpedia.uri.gz", Common.gzFolder+"dbpedia.concept.txt", 0, 
//				Common.gzFolder+"dbpedia.individual.gz");
//		
//		extractURIWithLucene(Common.geonames, Common.gzFolder+"geonames.uri.gz");
//		diff(Common.gzFolder+"geonames.uri.gz", Common.gzFolder+"geonames.concept.txt", 0, 
//				Common.gzFolder+"geonames.individual.gz");
//
//		extractURIWithLucene(Common.uscensus, Common.gzFolder+"uscensus.uri.gz");
//		diff(Common.gzFolder+"uscensus.uri.gz", Common.gzFolder+"uscensus.concept.txt", 0, 
//				Common.gzFolder+"uscensus.individual.gz");
//
//		extractURIWithLucene(Common.foaf, Common.gzFolder+"foaf.uri.gz");
//		diff(Common.gzFolder+"foaf.uri.gz", Common.gzFolder+"foaf.concept.txt", 0, 
//				Common.gzFolder+"foaf.individual.gz");
		
//		extractURIPartial(workFolder+dbpedia, 12500000, 7500000, workFolder+"dbpedia.temp2.gz");
//		extractURIMultiLap(workFolder+dbpedia, 2500000, workFolder+"dbpedia.uri.gz");
//		diff(workFolder+"dbpedia.uri.gz", workFolder+"dbpedia.concept.txt", 0, 
//				workFolder+"dbpedia.individual.gz");
//
//		extractURIMultiLap(workFolder+geonames, 2500000, workFolder+"geonames.uri.gz");
//		diff(workFolder+"geonames.uri.gz", workFolder+"geonames.concept.txt", 0, 
//				workFolder+"geonames.individual.gz");
//
//		extractURIMultiLap(workFolder+uscensus, 2500000, workFolder+"uscensus.uri.gz");
//		diff(workFolder+"uscensus.uri.gz", workFolder+"uscensus.concept.txt", 0, 
//				workFolder+"uscensus.individual.gz");
//
//		extractURIMultiLap(workFolder+foaf, 2500000, workFolder+"foaf.uri.gz");
//		diff(workFolder+"foaf.uri.gz", workFolder+"foaf.concept.txt", 0, 
//				workFolder+"foaf.individual.gz");

//		find(workFolder+dbpedia, "http://musicbrainz.org/");
//		mainObserve("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\gz\\mb.relation.gz");
//		summarize(workFolder+foaf, 1, workFolder + "foaf.property.txt");
//		sumConcept(workFolder+foaf, workFolder + "foaf.concept.txt");
//		sumAttribute(workFolder+foaf, workFolder + "foaf.attribute.txt");
//		diff(workFolder + "foaf.property.txt", workFolder + "foaf.attribute.txt", 
//		workFolder + "foaf.relation.txt");
//
//		summarize(workFolder+wordnet, 1, workFolder + "wordnet.property.txt");
//		sumConcept(workFolder+wordnet, workFolder + "wordnet.concept.txt");
//		sumAttribute(workFolder+wordnet, workFolder + "wordnet.attribute.txt");
//		diff(workFolder + "wordnet.property.txt", workFolder + "wordnet.attribute.txt", 
//		workFolder + "wordnet.relation.txt");
//
//		summarize(workFolder+dbpedia, 1, workFolder + "dbpedia.property.txt");
//		sumConcept(workFolder+dbpedia, workFolder + "dbpedia.concept.txt");
//		sumAttribute(workFolder+dbpedia, workFolder + "dbpedia.attribute.txt");
//		diff(workFolder + "dbpedia.property.txt", workFolder + "dbpedia.attribute.txt", 
//		workFolder + "dbpedia.relation.txt");
//
//		summarize(workFolder+dblp, 1, workFolder + "dblp.property.txt");
//		sumConcept(workFolder+dblp, workFolder + "dblp.concept.txt");
//		sumAttribute(workFolder+dblp, workFolder + "dblp.attribute.txt");
//		diff(workFolder + "dblp.property.txt", workFolder + "dblp.attribute.txt", 
//		workFolder + "dblp.relation.txt");
//
//		summarize(workFolder+geonames, 1, workFolder + "geonames.property.txt");
//		sumConcept(workFolder+geonames, workFolder + "geonames.concept.txt");
//		sumAttribute(workFolder+geonames, workFolder + "geonames.attribute.txt");
//		diff(workFolder + "geonames.property.txt", workFolder + "geonames.attribute.txt", 
//		workFolder + "geonames.relation.txt");
//
//		summarize(workFolder+uscensus, 1, workFolder + "uscensus.property.txt");
//		sumConcept(workFolder+uscensus, workFolder + "uscensus.concept.txt");
//		sumAttribute(workFolder+uscensus, workFolder + "uscensus.attribute.txt");
//		diff(workFolder + "uscensus.property.txt", workFolder + "uscensus.attribute.txt", 
//		workFolder + "uscensus.relation.txt");
		
//		String dbpediaPredicates = workFolder + "dbpedia.property.txt"; // to run
//		sort(dbpediaPredicates, 1, workFolder+"dbpedia.property.sorted.txt"); // to run
//		System.out.println(countLines(Blocker.workFolder+"r0.3block.txt"));
//		mainObserve(Indexer.indexFolder+"dbpediaPreprocessed.gz");
//		System.out.println(countLines("/usr/fulinyun/blocker/keyIndBasicFeature.txt"));
	}
	
	
}

