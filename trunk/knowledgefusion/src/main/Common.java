package main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import basic.IOFactory;

public class Common {

	public static final String gzFolder = "smb://poseidon/team/semantic search/BillionTripleData/gz/";
	public static final String wordnet = gzFolder+"wordnet.gz"; // 1942887 triples
	public static final String dblp = gzFolder+"dblp.gz"; // 14936600 triples
	public static final String dbpedia = gzFolder+"dbpedia.gz"; // 110241463 triples
	public static final String geonames = gzFolder+"geonames.gz"; // 69778255 triples
	public static final String uscensus = gzFolder+"uscensus.gz"; // 445752172 triples
	public static final String foaf = gzFolder+"foaf.gz"; // 54000000 triples
	public static final String mbi = gzFolder+"mb.instance.gz"; // 8772612 triples
	public static final String mbr = gzFolder+"mb.relation.gz"; // 13591684 triples
	public static final String mba = gzFolder+"mb.attribute.gz"; // 16721842 triples
	
	public static final String rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	public static final String owlClass = "<http://www.w3.org/2002/07/owl#Class>";
	public static final String dbpediaSubject = "<http://www.w3.org/2004/02/skos/core#subject>";
	public static final String sameAs = "<http://www.w3.org/2002/07/owl#sameAs>";
	public static final String dbpediaSubclass = "<http://www.w3.org/2004/02/skos/core#broader>";

	public static void main(String[] args) {
		String[] result = sortUnique("111 as soon as possible yes as soon as possible", 1);
		for (String s : result) System.out.println(s);
	}
	
	/**
	 * get integer set from input, each line in input is an integer
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static HashSet<Integer> getIntSet(String input) throws Exception {
		HashSet<Integer> ret = new HashSet<Integer>();
		BufferedReader br = IOFactory.getBufferedReader(input);
		for (String line = br.readLine(); line != null; line = br.readLine()) 
			ret.add(Integer.parseInt(line));
		return ret;
	}

	/**
	 * get line set of input
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static HashSet<String> getStringSet(String input) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		HashSet<String> ret = new HashSet<String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) ret.add(line);
		return ret;
	}

	/**
	 * get integers from a string, neighboring integers are separated with a whitespace
	 * result array are sorted in ascending order
	 * @param line
	 * @return
	 */
	public static int[] getNumsInLineSorted(String line) {
		String[] parts = line.split(" ");
		int[] ret = new int[parts.length];
		for (int i = 0; i < parts.length; i++) ret[i] = Integer.parseInt(parts[i]);
		Arrays.sort(ret);
		return ret;
	}

	/**
	 * print precision/recall result
	 * @param overlap
	 * @param stdAns
	 * @param canSize
	 * @throws Exception
	 */
	public static void printResult(int overlap, String stdAns, int canSize, String output) throws Exception {
		PrintWriter pw = IOFactory.getPrintWriter(output);
//		System.out.println(overlap + " lines overlap");
		pw.println(overlap + " lines overlap");
		int stdSize = Analyzer.countLines(stdAns);
//		System.out.println("standard answer size: " + stdSize);
//		System.out.println("recall: " + (overlap+0.0)/stdSize);
//		System.out.println("result size: " + canSize);
//		System.out.println("precision: " + (overlap+0.0)/canSize);
		System.out.println((overlap+0.0)/canSize + "\t" + (overlap+0.0)/stdSize);
		pw.println("standard answer size: " + stdSize);
		pw.println("recall: " + (overlap+0.0)/stdSize);
		pw.println("result size: " + canSize);
		pw.println("precision: " + (overlap+0.0)/canSize);
		pw.close();
		pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
		pw.println((overlap+0.0)/canSize + "\t" + (overlap+0.0)/stdSize);
		pw.close();
	}

	/**
	 * index tokens in the prefixes of individuals
	 * input: canonicalized individual features, one individual per line, each line begins with the individual
	 * ID, followed by a whitespace, followed by tokens separated with a whitespace 
	 * @param input
	 * @param lines
	 * @param prefix
	 * @param indexFolder
	 * @throws Exception
	 */
	public static void indexPrefix(String input, int lines, float prefix, int maxPrefixLength, 
			String indexFolder) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		Directory dir = FSDirectory.getDirectory(indexFolder);
		IndexWriter iwriter = new IndexWriter(dir, new WhitespaceAnalyzer(), true, 
				IndexWriter.MaxFieldLength.UNLIMITED);
		int lineCount = 0;
		for (String line = br.readLine(); line != null && lineCount < lines; line = br.readLine()) {
			try {
				String[] parts = line.split(" ");
				int idx = Integer.parseInt(parts[0]);
				int n = parts.length-1;
				String tokens = "";
				for (int j = 1; (j <= (int)Math.ceil(n*prefix) || (j < 3 && j < n)) && j <= maxPrefixLength; j++) 
					tokens += " "+parts[j];
				Document doc = new Document();
				doc.add(new Field("id", ""+idx, Field.Store.YES, Field.Index.NO));
				doc.add(new Field("words", tokens, Field.Store.YES, Field.Index.ANALYZED));
				iwriter.addDocument(doc);
				lineCount++;
				if (lineCount%1000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
			} catch (Exception e) {
				continue;
			}
		}
		iwriter.optimize();
		iwriter.close();
		br.close();
		dir.close();
	}

	/**
	 * index features from .bin file generated by tokenizer
	 * @param input
	 * @param lines
	 * @param feature
	 * @throws Exception
	 */
	public static void indexBinaryFeature(String input, int lines, float prefix,
			String indexFolder) throws Exception {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));
		Directory dir = FSDirectory.getDirectory(indexFolder);
		IndexWriter iwriter = new IndexWriter(dir, new WhitespaceAnalyzer(), true, 
				IndexWriter.MaxFieldLength.UNLIMITED);
		for (int i = 1; i <= lines; i++) {
			try {
				int idx = readBigEndianInt(dis);
				int n = readBigEndianInt(dis);
				String tokens = "";
				int j;
				for (j = 0; j < (int)Math.ceil(n*prefix) || (j < 3 && j < n); j++) tokens += " "+readBigEndianInt(dis);
				for (; j < n; j++) readBigEndianInt(dis);
				Document doc = new Document();
				doc.add(new Field("line", ""+idx, Field.Store.YES, Field.Index.NO));
				doc.add(new Field("words", tokens, Field.Store.YES, Field.Index.ANALYZED));
				iwriter.addDocument(doc);
//				if (i % 100 == 0) System.out.println(new Date().toString() + " : " + i + " lines indexed");
			} catch (Exception e) {
				continue;
			}
		}
		iwriter.optimize();
		iwriter.close();
		dis.close();
		dir.close();
	}

	/**
	 * get features from .bin file generated by tokenizer
	 * @param input
	 * @param lines
	 * @param feature
	 * @throws Exception
	 */
	public static void getBinaryFeature(String input, int lines,
			int[][] feature) throws Exception {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));
		for (int i = 1; i <= lines; i++) {
			int idx = readBigEndianInt(dis);
			int n = readBigEndianInt(dis);
			feature[idx] = new int[n];
			for (int j = 0; j < n; j++) feature[idx][j] = readBigEndianInt(dis);
		}
		dis.close();
	}

	private static int readBigEndianInt(DataInputStream dis) throws Exception {
		return dis.readUnsignedByte()+(dis.readUnsignedByte()<<8)+(dis.readUnsignedByte()<<16)+
			(dis.readUnsignedByte()<<24);
	}

	/**
	 * sort and unique tokens in str, from the start-th token, duplicated tokens are assigned unique aliases
	 * @param str
	 * @param start
	 * @return
	 */
	public static String[] sortUnique(String str, int start) {
		String[] tokens = str.split(" ");
		Arrays.sort(tokens, start, tokens.length);
		for (int i = start; i < tokens.length; i++) 
			for (int j = i+1; j < tokens.length && tokens[j].equals(tokens[i]); j++) tokens[j] += ("."+(j-i));
		return tokens;
	}

	/**
	 * delete a directory
	 * @param dir
	 * @throws Exception
	 */
	public static void deleteFolder(File dir) throws Exception {
		if (!dir.isDirectory()) {
			System.out.println("deleteFolder: " + dir.getAbsolutePath() + " is not a directory!");
			return;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) deleteFolder(f);
			else f.delete();
		}
		dir.delete();
	}
	
}
