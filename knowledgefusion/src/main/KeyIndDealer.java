package main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import basic.AsciiUtils;
import basic.IDataSourceReader;
import basic.IOFactory;

public class KeyIndDealer {

	public static String dbpediaSameAs = 
		"smb://poseidon/team/semantic search/BillionTripleData/crude/dbpedia-v3.equ";
	public static String geonamesSameAs = 
		"smb://poseidon/team/semantic search/BillionTripleData/crude/geonames.equ";
	public static String dblpSameAs = 
		"smb://poseidon/team/semantic search/BillionTripleData/crude/swetodblp.equ";

	public static String domainDBpedia = "dbpedia.org";
	public static String domainGeonames = "sws.geonames.org";
	public static String domainDblp = "www.informatik.uni-trier.de";
//	public static int cheatLineNum = 89746;
//	public static int stdAnsSize = 88867;
//	public static int stdAnsSize = 2318;
//	public static int r03resultSize = 23359103;
	public static String keyInd = Indexer.indexFolder+"keyInd.txt";
	public static String ppjoinFolder = Indexer.indexFolder+"/ppjoin/";
		
	public static void main(String[] args) throws Exception {
//		extractSameAsByDomain(dbpediaSameAs, domainDBpedia, domainGeonames, 
//				Indexer.indexFolder+"dbpedia2geonames.equ");
//		extractSameAsByDomain(geonamesSameAs, domainGeonames, domainDBpedia, 
//				Indexer.indexFolder+"geonames2dbpedia.equ");
//		extractSameAsByDomain(dblpSameAs, domainDblp, domainDblp, 
//				Indexer.indexFolder+"dblp.equ");
//		toIDNonNull(new String[]{Indexer.indexFolder+"dbpedia2geonames.equ", 
//				Indexer.indexFolder+"geonames2dbpedia.equ", 
//				Indexer.indexFolder+"dblp.equ"}, Indexer.indexFolder+"nonNullSameAsID.txt"); // done
		// sort -n nonNullSameAsID.txt | uniq > sameAsID.txt // done
		// sameAsID.txt: no duplicate pairs, larger doc# comes first, sorted in ascending order
//		getIndFromPairs(Indexer.indexFolder+"sameAsID.txt", Indexer.indexFolder+"keyInd.txt"); // done
		// keyInd.txt: all doc#s of individuals appear in some sameAs pair, sorted in ascending order
//		dumpFeature(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder+"keyInd.txt"), 
//				Blocker.workFolder+"keyIndBasicFeature.txt"); // done
		// tokenizer keyIndBasicFeature.txt // done
		// ppjoin j 0.5 keyIndBasicFeature.txt.bin > r0.5.txt // done in 40s
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder+"keyInd.txt"), 
//				Blocker.workFolder+"r0.5.txt", Blocker.workFolder+"r0.5translated.txt"); // done
		// sort -n r0.5translated.txt > r0.5sorted.txt // done
//		evaluate(Blocker.workFolder+"r0.5sorted.txt", Indexer.indexFolder+"sameAsID.txt"); // done
//		getFailed(Blocker.workFolder+"r0.5sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				Blocker.workFolder+"r0.5failed.txt");
//		blockByRareWords(Blocker.workFolder+"keyIndBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"keyIndBasicFeature.txt"), 3, 
//				Blocker.workFolder+"rare3.txt"); // done
//		readBinaryFile(Blocker.workFolder+"keyIndBasicFeature.txt.bin"); // unexpected byte order!!!
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//		Analyzer.countLines(Indexer.indexFolder+"keyInd.txt"), 
//		Blocker.workFolder+"rare3.txt", Blocker.workFolder+"rare3translated.txt"); // done
		// sort -n rare3translated.txt > rare3sorted.txt // done
//		evaluate(Blocker.workFolder+"rare3sorted.txt", Indexer.indexFolder+"sameAsID.txt"); // done
//		blockByRareWords(Blocker.workFolder+"keyIndBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"keyIndBasicFeature.txt"), 1, 
//				Blocker.workFolder+"rare1.txt"); // done
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder+"keyInd.txt"), 
//				Blocker.workFolder+"rare1.txt", Blocker.workFolder+"rare1translated.txt"); // done
		// sort -n rare1translated.txt > rare1sorted.txt // done
//		evaluate(Blocker.workFolder+"rare1sorted.txt", Indexer.indexFolder+"sameAsID.txt"); // done
		// qtokenizer 5 keyIndBasicFeatureU.txt // done
		// ppjoin j 0.5 keyIndBasicFeatureU.txt.5gram.bin > u5j0.5.txt // done
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder+"keyInd.txt"), 
//				Blocker.workFolder+"u5j0.5.txt", Blocker.workFolder+"u5j0.5translated.txt"); // done
		// sort -n u5j0.5translated.txt > u5j0.5sorted.txt // done
//		evaluate(Blocker.workFolder+"u5j0.5sorted.txt", Indexer.indexFolder+"sameAsID.txt"); // done
//		blockByPrefix(Blocker.workFolder+"keyIndBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"keyIndBasicFeature.txt"), 0.1f, 
//				Blocker.workFolder+"prefix0.1.txt"); // too slow, aborted
//		blockByPrefixFast(Blocker.workFolder+"keyIndBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"keyIndBasicFeature.txt"), 0.1f, 
//				Blocker.workFolder+"prefix0.1.txt"); // done
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder + "keyInd.txt"),
//				Blocker.workFolder + "prefix0.1.txt", Blocker.workFolder+"prefix0.1translated.txt"); // done
		// sort -n prefix0.1translated.txt > prefix0.1sorted.txt // done
//		evaluate(Blocker.workFolder+"prefix0.1sorted.txt", Indexer.indexFolder+"sameAsID.txt"); // done: 5364/672897
//		blockByPrefixFast(Blocker.workFolder+"keyIndBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"keyIndBasicFeature.txt"), 0.2f, 
//				Blocker.workFolder+"prefix0.2.txt"); // done
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder + "keyInd.txt"),
//				Blocker.workFolder + "prefix0.2.txt", Blocker.workFolder+"prefix0.2translated.txt"); // done
		// sort -n prefix0.2translated.txt > prefix0.2sorted.txt // done
//		evaluate(Blocker.workFolder+"prefix0.2sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				Blocker.workFolder+"prefix0.2eval.txt"); // done: 32156/10936070
//		blockByPrefixFast(Blocker.workFolder+"keyIndBasicFeatureU.txt.5gram.bin", 
//				Analyzer.countLines(Blocker.workFolder+"keyIndBasicFeature.txt"), 0.1f, 
//				Blocker.workFolder+"prefixU5gram0.1.txt"); // done
//		translateDocNum(Indexer.indexFolder+"keyInd.txt", 
//				Analyzer.countLines(Indexer.indexFolder + "keyInd.txt"),
//				Blocker.workFolder + "prefixU5gram0.1.txt", Blocker.workFolder+"prefixU5gram0.1translated.txt"); // done
		// sort -n prefixU5gram0.1translated.txt | uniq > prefixU5gram0.1sorted.txt // done
//		evaluate(Blocker.workFolder+"prefixU5gram0.1sorted.txt", Indexer.indexFolder+"sameAsID.txt"); // done: 37904/11922201
//		evaluate(Blocker.workFolder+"blockP=25sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				Blocker.workFolder+"pr/blockP=25eval.txt");
//		evaluate(Blocker.workFolder+"blockP=30sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				Blocker.workFolder+"pr/blockP=30eval.txt");

//		getAvgFeatureLength(Blocker.workFolder+"keyIndBasicFeature.txt");
//		dumpFeature(Indexer.indexFolder+"nonNullInd.txt", 2560000, Indexer.indexFolder+"nonNullIndFeature.txt");
//		extractExtendedFeature(keyInd, Blocker.workFolder+"keyIndExtendedFeature.txt");
//		specificDealing(Blocker.workFolder+"test.txt", Blocker.workFolder+"testOptimized.txt");
//		specificDealing(Blocker.workFolder+"keyIndBasicFeature.txt", Blocker.workFolder+"keyIndBasicFeatureOptimized.txt"); // done
		getAvgFeatureLength(Blocker.workFolder+"keyIndExtendedFeature.txt"); // done
		// -1 done
	}

	/**
	 * unaccent, remove parentheses, hyphen separate
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	private static void specificDealing(String input, String output) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String unacc = AsciiUtils.convertNonAscii(line);
			StringBuilder sb = new StringBuilder();
			int n = unacc.length();
			for (int i = 0; i < n; i++) {
				if (unacc.charAt(i)=='-') sb.append(" ");
				else if (unacc.charAt(i)=='(' || unacc.charAt(i)==')') continue;
				else sb.append(unacc.charAt(i));
			}
			pw.println(sb.toString());
			lineCount++;
			if (lineCount%10000 == 0) System.out.println(lineCount);
		}
		pw.close();
		br.close();
	}

	/**
	 * extract extended feature for key individuals
	 * based on the 2nd- and 3rd- lap indexes, for each key individual, extract its attribute values and 
	 * its neighbors' attribute values
	 */
	public static void extractExtendedFeature(String indList, String target) throws Exception {
		System.out.println(new Date().toString() + " : start extracting extended feature for " + indList);
		BufferedReader br = IOFactory.getBufferedReader(indList);
		PrintWriter pw = IOFactory.getPrintWriter(target);
		IndexReader ireader2 = IndexReader.open(Indexer.refIndex);
		IndexReader ireader3 = IndexReader.open(Indexer.basicFeatureIndex);
		IndexSearcher isearcher3 = new IndexSearcher(ireader3);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			pw.print(line);
			int i = Integer.parseInt(line);
			Document doc = ireader3.document(i);
			String uri = doc.get("URI");
			String extended = doc.get("basic");
			Document toExtend = ireader2.document(i);
			if (!toExtend.get("URI").equals(doc.get("URI"))) {
				System.out.println("URI not matched!!!");
				System.exit(1);
			}
			List fieldList = toExtend.getFields();
			for (Object o : fieldList) {
				Field f = (Field)o;
				if (f.name().endsWith("from") || f.name().endsWith("to")) 
				if (!f.name().equals(Common.rdfType+"from") && !f.name().equals(Common.owlClass+"from") 
						&& !f.name().equals(Common.dbpediaSubject+"from")) {
					TopDocs tdbasic = isearcher3.search(new TermQuery(new Term("URI", 
							f.stringValue())), 1);
					if (tdbasic.scoreDocs.length > 0) {
						int idoc = tdbasic.scoreDocs[0].doc;
						String dbasic = ireader3.document(idoc).get("basic");
						extended += dbasic;
					}
				}
			}
			pw.println(extended);
			count++;
			if (count%2000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		pw.close();
		br.close();
		isearcher3.close();
		ireader2.close();
		ireader3.close();

	}

	public static void getAvgFeatureLength(String input) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		int lineCount = 0;
		int featureLength = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			lineCount++;
			featureLength += line.split(" ").length;
		}
		br.close();
		System.out.println((featureLength+0.0)/lineCount);
	}
	
	public static void readBinaryFile(String input) throws Exception {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));
		while (true) {
			System.out.println(dis.readUnsignedByte());
			System.in.read();
			System.in.read();
		}
	}
	
	/**
	 * words in each records in input is sorted by document frequency, if ceil(prefix*length)-prefix share
	 * at least one token, block them 
	 * @param input
	 * @param lines
	 * @param prefix
	 * @param output
	 * @throws Exception
	 */
	public static void blockByPrefixFast(String input, int lines, float prefix, String output) throws Exception {
		int[][] feature = new int[lines+1][];
		Common.getBinaryFeature(input, lines, feature);
		HashMap<Integer, ArrayList<Integer>> token2rec = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 1; i <= lines; i++) {
			for (int j = 0; j < (int)Math.ceil(feature[i].length*prefix); j++) {
				if (token2rec.containsKey(feature[i][j])) token2rec.get(feature[i][j]).add(i);
				else {
					ArrayList<Integer> value = new ArrayList<Integer>();
					value.add(i);
					token2rec.put(feature[i][j], value);
				}
			}
			if (i%10000 == 0) System.out.println(new Date().toString() + " : " + i + " lines indexed");
		}
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer i : token2rec.keySet()) {
			ArrayList<Integer> recs = token2rec.get(i);
			for (int j = 0; j < recs.size(); j++) for (int k = j+1; k < recs.size(); k++) 
				pw.println(recs.get(j).intValue() + " " + recs.get(k).intValue());
//			System.out.println(new Date().toString() + " : " + i + " lines written");
		}
		pw.close();
	}

	/**
	 * words in each records in input is sorted by document frequency, if the first n words of two records
	 * are the same, block them
	 * @param input
	 * @param n
	 * @param output
	 * @throws Exception
	 */
	private static void blockByRareWords(String input, int lines, int n, String output) throws Exception {
		int[][] feature = new int[lines+1][];
		Common.getBinaryFeature(input, lines, feature);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (int i = 1; i <= lines; i++) for (int j = 1; j < i; j++) 
			if (firstNSame(feature[i], feature[j], n)) pw.println(i + " " + j);
		pw.close();
	}
	
	private static boolean firstNSame(int[] a, int[] b, int n) {
		for (int i = 0; i < n && i < a.length && i < b.length; i++) if (a[i] != b[i]) return false;
		return true;
	}

	private static void getFailed(String result, String ans, String output) throws Exception {
		HashSet<String> resultSet = Common.getStringSet(result);
		BufferedReader br = IOFactory.getBufferedReader(ans);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (!resultSet.contains(line)) pw.println(line);
		}
		pw.close();
		br.close();
	}

	/**
	 * translate the result of ppjoin, replace line# with individual IDs, and remove similarity values
	 * @param lineListFile
	 * @param ppjoinResult
	 * @param output
	 */
	public static void translateDocNum(String lineListFile, int lineNum, String ppjoinResult, 
			String output) throws Exception {
		int[] lineList = new int[lineNum+1];
		BufferedReader br = IOFactory.getBufferedReader(lineListFile);
		for (int i = 1; i <= lineNum; i++) lineList[i] = Integer.parseInt(br.readLine());
		br.close();
		br = IOFactory.getBufferedReader(ppjoinResult);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int line1 = lineList[Integer.parseInt(parts[0])];
			int line2 = lineList[Integer.parseInt(parts[1])];
			if (line1 < line2) {
				int tmp = line1;
				line1 = line2;
				line2 = tmp;
			}
			String ans = line1 + " " + line2;
			pw.println(ans);
		}
		pw.close();
		br.close();
	}
	
	/**
	 * get all distinct ids from an id pair file, used to get key individuals 
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	public static void getIndFromPairs(String input, String output) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			ret.add(x);
			ret.add(y);
		}
		br.close();
		writeSet(ret, output);
	}
	
	/**
	 * remove standard answer pairs with individuals without basic features
	 * @param sameAsID
	 * @param nonNullInd
	 * @param output
	 * @throws Exception
	 */
	public static void removeNullSameAsPairs(String sameAsID, String nonNullInd, 
			String output) throws Exception {
		HashSet<Integer> indSet = Common.getIntSet(nonNullInd);
		BufferedReader br = IOFactory.getBufferedReader(sameAsID);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			if (indSet.contains(x) && indSet.contains(y)) pw.println(x + " " + y);
		}
		pw.close();
		br.close();
	}
	
	/**
	 * estimate precision and recall of the blocking result according to ppjoin output pairs
	 * @param lineListFile
	 * @param lineNum
	 * @param ppJoinResult
	 * @param stdAns
	 * @throws Exception
	 */
	public static void evaluate(String ppJoinResultTranslated, String stdAns, String output) throws Exception {
		HashSet<String> stdAnswers = getLines(stdAns);
		BufferedReader br = IOFactory.getBufferedReader(ppJoinResultTranslated);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (stdAnswers.contains(line)) count++;
		}
		br.close();
		Common.printResult(count, stdAns, Analyzer.countLines(ppJoinResultTranslated), output);
	}
	
	private static HashSet<String> getLines(String stdAns) throws Exception {
		HashSet<String> ret = new HashSet<String>();
		BufferedReader br = IOFactory.getBufferedReader(stdAns);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			ret.add(line);
		}
		return ret;
	}

	/**
	 * within each pair, larger ID comes first, then sort and uniq
	 * @param sameAsID
	 * @param output
	 * @throws Exception
	 */
	public static void normSameAsID(String sameAsID, String output) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(sameAsID);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			if (x < y) {
				int tmp = x;
				x = y;
				y = tmp;
			}
			pw.println(x + " " + y);
		}
		br.close();
		pw.close();
	}
	
//	/**
//	 * dump basic features of individuals listed in the lineListfile
//	 * @param lineListFile
//	 * @param numLines
//	 * @param output
//	 * @throws Exception
//	 */
//	public static void dumpFeature(String lineListFile, int numLines, String output) throws Exception {
//		Indexer.dumpClassFeature(lineListFile, numLines, output);
//	}
	
	/**
	 * obtain IDs from sameAsID that have non-null basic features
	 * @param nonNullInd
	 * @param sameAsID
	 * @throws Exception
	 */
	public static void getNonNullSameAsInd(String nonNullInd, String sameAsID, 
			String output) throws Exception {
		HashSet<Integer> nonNullIndSet = Common.getIntSet(nonNullInd);
		TreeSet<Integer> ret = new TreeSet<Integer>();
		BufferedReader br = IOFactory.getBufferedReader(sameAsID);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			if (nonNullIndSet.contains(x)) ret.add(x);
			if (nonNullIndSet.contains(y)) ret.add(y);
		}
		br.close();
		writeSet(ret, output);
	}
	
	private static void writeSet(TreeSet<Integer> ret, String output) throws Exception {
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer i : ret) pw.println(i.intValue());
		pw.close();
	}

	/**
	 * extract sameAs statements from domain1 to domain2 from input to output
	 * @param input
	 * @param domain1
	 * @param domain2
	 * @param output
	 * @throws Exception
	 */
	public static void extractSameAsByDomain(String input, String domain1, String domain2, 
			String output) throws Exception {
		System.out.println("extract to " + output);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		IDataSourceReader br = IOFactory.getReader(input);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			if (parts.length > 2) {
				String o1 = parts[0];
				String o2 = parts[2];
				if (o1.contains(domain1) && o2.contains(domain2)) pw.println(o1 + " " + o2);
			}
			count++;
			if (count % 3000000 == 0) System.out.println(
					new Date().toString() + " : " + count);
		}
		br.close();
		pw.close();
		System.out.println(count + " lines in all");
	}
	
	/**
	 * convert the sameAs individual pairs to doc#s in the basicFeatureIndex and write them to output
	 * @param inputs
	 * @param output
	 * @throws Exception
	 */
	public static void toIDNonNull(String[] inputs, String output) throws Exception {
		PrintWriter pw = IOFactory.getPrintWriter(output);
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		int count = 0;
		for (String fn : inputs) {
			IDataSourceReader br = IOFactory.getReader(fn);
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] parts = line.split(" ");
				int x = getDocNum(ireader, parts[0]);
				int y = getDocNum(ireader, parts[1]);
				if (ireader.document(x).get("basic").equals("") || 
						ireader.document(y).get("basic").equals(""))
					continue;
				if (x < y) {
					int t = x;
					x = y;
					y = t;
				}
				pw.println(x + " " + y);
				count++;
				if (count%10000 == 0) System.out.println(new Date().toString() + " : " + count);
			}
			br.close();
		}
		System.out.println(new Date().toString() + " : " + count + " lines in all");
		pw.close();
	}

	private static int getDocNum(IndexReader ireader, String string) throws Exception {
		TermDocs td = ireader.termDocs(new Term("URI", string));
		td.next();
		return td.doc();
	}
}
