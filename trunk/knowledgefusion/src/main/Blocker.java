package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import basic.IDataSourceReader;
import basic.IOFactory;

/**
 * Implementation of the positional prefix filtering algorithm from 
 * C. Xiao et al. WWW 2008 paper
 * not necessary, since we reuse the programs from 
 * http://www.cse.unsw.edu.au/~weiw/project/simjoin.html
 * @author fulinyun
 *
 */
public class Blocker {
	
//	public static String workFolder = "/media/disk1/fulinyun/blocker/"; // for vidi018(192.168.4.18)
	public static String workFolder = "/home/fulinyun/blocker/"; // for vidi004(192.168.4.104)
//	public static String workFolder = "/usr/fulinyun/blocker/"; // for hadoop1(192.168.4.148)

	
	public static void main(String[] args) throws Exception {
//		findBlock(workFolder+"r0.3sorted.txt", workFolder+"r0.3block.txt");
//		findBlock(workFolder+"r0.4sorted.txt", workFolder+"r0.4block.txt");
//		evaluate(workFolder+"r0.3block.txt", workFolder+"nonNullSameAs.txt");
//		evaluate(workFolder+"r0.4block.txt", workFolder+"nonNullSameAs.txt");
		
//		findBlock(workFolder+"r0.5sorted.txt", workFolder+"r0.5block.txt"); // done
//		evaluate(workFolder+"r0.5block.txt", Indexer.indexFolder+"sameAsID.txt"); // done
//		prefixBlocking(Blocker.workFolder+"cheatBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"cheatBasicFeature.txt"), 0.2f, 
//				Blocker.workFolder+"prefix0.2block.txt"); // done
//		translateBlock(workFolder+"prefix0.2block.txt", Indexer.indexFolder+"keyInd.txt", workFolder+"prefix0.2blockTranslated.txt");
//		evaluate(workFolder+"prefix0.2blockTranslated.txt", Indexer.indexFolder+"sameAsID.txt");
//		prefixBlocking(Blocker.workFolder+"cheatBasicFeature.txt.bin", 
//				Analyzer.countLines(Blocker.workFolder+"cheatBasicFeature.txt"), 0.2f, 
//				Blocker.workFolder+"prefix0.2&3block.txt"); // done
//		translateBlock(workFolder+"prefix0.2&3block.txt", Indexer.indexFolder+"keyInd.txt", 
//				workFolder+"prefix0.2&3blockTranslated.txt"); // done
//		evaluate(workFolder+"prefix0.2&3blockTranslated.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"prefix0.2&3blockEval.txt"); // done
		
		// part of impact of mutual enhancement mechanism test, block classes
//		prefixBlocking(workFolder+"classFeatureDump.txt.bin", 
//				Analyzer.countLines(workFolder+"classFeatureDump.txt"), 0.2f, 
//				workFolder+"prefix0.2classBlock.txt"); // done
//		translateBlock(workFolder+"prefix0.2classBlock.txt", workFolder+"nonNullClass.txt", 
//				workFolder+"prefix0.2classBlockTranslated.txt"); // done

//		prefixBlocking(workFolder+"classFeatureDump.txt.bin", 
//				Analyzer.countLines(workFolder+"classFeatureDump.txt"), 0.1f, 
//				workFolder+"prefix0.1classBlock.txt"); // done
//		translateBlock(workFolder+"prefix0.1classBlock.txt", workFolder+"nonNullClass.txt", 
//				workFolder+"prefix0.1classBlockTranslated.txt"); // done

//		prefixBlocking(workFolder+"classFeatureDump.txt.bin", 
//				Analyzer.countLines(workFolder+"classFeatureDump.txt"), 0.05f, 
//				workFolder+"prefix0.05classBlock.txt"); // done
//		translateBlock(workFolder+"prefix0.05classBlock.txt", workFolder+"nonNullClass.txt", 
//				workFolder+"prefix0.05classBlockTranslated.txt"); // done
//		for (int p = 10; p <= 30; p += 5) for (int n = 10000; n <= 1250000; n *= 5) {
//			prefixBlockingWithLucene(ppjoinFolder+"indFeature126w"+n+".txt.bin", n, 
//					p/100.0f, workFolder+"temp"+p+"-"+n, 
//				workFolder+"temp.txt", workFolder+"blockP="+p+"n="+n+"speed.txt");
//		}
//		prefixBlocking(workFolder+"cheatBasicFeature10000.txt.bin", 10000, 0.1f, 
//				workFolder+"blockTemp.txt", workFolder+"tempReport.txt");
//		for (int p = 10; p <= 30; p += 5) for (int n = 10000; n <= 160000; n *= 2) {
//			prefixBlocking(workFolder+"cheatBasicFeature"+n+".txt.bin", n, p/100.0f, 
//					workFolder+"blockSpec/blockP="+p+"n="+n+".txt", workFolder+"report/blockP="+p+"n="+n+"memSpeed.txt");
//		}
		
//		for (int p = 10; p <= 30; p += 5) getBlockSizeDistribution(workFolder+"blockSpec/blockP="+p+"n=160000.txt",
//				workFolder+"blockSizeDistribution/blockSizeDistributionP="+p+"n=160000.txt");
//		for (int p = 10; p <= 30; p += 5) {
//			prefixBlocking(workFolder+"cheatBasicFeature.txt.bin", 
//					Analyzer.countLines(workFolder+"cheatBasicFeature.txt"), p/100.0f, 
//					workFolder+"blockP="+p+".txt", workFolder+"blockP="+p+"memSpeed.txt"); 
//		}
		
//		for (int p = 10; p <= 30; p += 5) {
//			translateBlock(workFolder+"blockP="+p+".txt", Indexer.indexFolder+"keyInd.txt", 
//					workFolder+"blockP="+p+"translated.txt"); 
//		}
		
//		for (int p = 10; p <= 30; p += 5) {
//			dumpCanPairs(workFolder+"blockP="+p+"translated.txt", workFolder+"blockP="+p+"dump.txt");
//		}
		
//		evaluate(workFolder+"blockP="+p+"translated.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"pr/blockP="+p+"eval.txt"); 
//		PrintWriter pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
//		pw.println(new Date().toString() + " blockP=0.10-0.30");
//		pw.close();

//		for (int p = 10; p <= 30; p += 5) {
//			Clusterer.evaluateWithDomain(workFolder+"blockP="+p+"translated.txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"pr/blockP="+p+"eval.txt");
//		}
		
//		canonicalize(Indexer.indexFolder+"nonNullIndFeature.txt", workFolder+"tempIndex", 
//				workFolder+"nonNullIndCaned.txt");
//		prefixBlockingWithLucene(workFolder+"nonNullIndCaned.txt", 0.2f, 100, workFolder+"tempIndex", 
//				workFolder+"nonNullIndBlocks0.2&100.txt", workFolder+"nonNullIndBlocking0.2&100Report.txt");
//		System.out.println(getRecall(workFolder+"nonNullIndBlocks0.2&100.txt", Indexer.indexFolder+"sameAsID.txt"));

//		canonicalize(workFolder+"keyIndExtendedFeature.txt", workFolder+"tempIndex", 
//				workFolder+"keyIndExtendedFeatureCaned.txt"); 
//		for (int p = 10; p <= 30; p += 5) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndExtendedFeatureCaned.txt", p/100.0f, 100, 10000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 10; p <= 30; p += 5) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		for (int p = 35; p <= 60; p += 5) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndExtendedFeatureCaned.txt", p/100.0f, 100, 10000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 35; p <= 60; p += 5) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		int p = 5;
//		String commonName = "keyIndExtendedFeatureP="+p;
//		prefixBlockingWithLucene(workFolder+"keyIndExtendedFeatureCaned.txt", p/100.0f, 100, 10000, 
//				workFolder+commonName+"TempIndex", 
//				workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+commonName+"PR.txt");
//		addID(workFolder+"keyIndBasicFeature.txt", Indexer.indexFolder+"keyInd.txt", 
//				workFolder+"keyIndBasicFeatureWithID.txt");
//		canonicalize(workFolder+"keyIndBasicFeature.txt", workFolder+"tempIndex", 
//				workFolder+"keyIndBasicFeatureCaned.txt");
//		for (int p = 5; p <= 60; p += 5) {
//			String commonName = "keyIndBasicFeatureP="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndBasicFeatureCaned.txt", p/100.0f, 100, 1000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 5; p <= 60; p += 5) {
//			String commonName = "keyIndBasicFeatureP="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		for (int p = 5; p <= 60; p += 5) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndExtendedFeatureCaned.txt", p/100.0f, 100, 1000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 5; p <= 60; p += 5) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		for (int p = 1; p <= 4; p += 1) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndExtendedFeatureCaned.txt", p/100.0f, 100, 1000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 1; p <= 4; p += 1) {
//			String commonName = "keyIndExtendedFeatureP="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		for (int p = 1; p <= 9; p += 1) {
//			String commonName = "keyIndExtendedFeature10P="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndExtendedFeatureCaned.txt", p/1000.0f, 100, 1000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 1; p <= 4; p += 1) {
//			String commonName = "keyIndExtendedFeature10P="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		for (int p = 5; p <= 9; p += 1) {
//			String commonName = "keyIndExtendedFeature10P="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		System.out.println("get missing pairs: extended60");
//		getMissingPairs(workFolder+"keyIndExtendedFeatureP=60.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"extended60miss.txt");
//		System.out.println("get missing pairs: basic60");
//		getMissingPairs(workFolder+"keyIndBasicFeatureP=60.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"basic60miss.txt"); // done
//		canonicalize(workFolder+"keyIndBasicFeatureOptimized.txt", workFolder+"tempIndex", 
//				workFolder+"keyIndBasicFeatureOpedCaned.txt");
//		for (int p = 5; p <= 60; p += 5) {
//			String commonName = "keyIndBasicFeatureOpedP="+p;
//			prefixBlockingWithLucene(workFolder+"keyIndBasicFeatureOpedCaned.txt", p/100.0f, 100, 1000, 
//					workFolder+commonName+"TempIndex", 
//					workFolder+commonName+".txt", workFolder+commonName+"Report.txt");
//		} // done
//		for (int p = 5; p <= 60; p += 5) {
//			String commonName = "keyIndBasicFeatureOpedP="+p;
//			Clusterer.evaluateWithDomain(workFolder+commonName+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+commonName+"PR.txt");
//		} // done
//		// pr.txt -> prBlockOped.txt // done
		
//		indexAll(workFolder+"keyIndBasicFeatureCaned.txt", workFolder+"keyIndBasicFeatureIndex"); // done
//		prefixBlocking(workFolder+"keyIndBasicFeatureIndex", 1000, workFolder+"keyIndBasicFeatureTh=1000.txt"); // done in 217450 ms
//		System.out.println(Analyzer.countLines(workFolder+"keyIndBasicFeatureTh=1000.txt")); // 417870 blocks
//		System.out.println("freqTh\tblockCount\ttime"); // done
//		for (int i = 1000; i < 11000; i += 1000) {
//			prefixBlocking(workFolder+"keyIndBasicFeatureIndex", i, workFolder+"keyIndBasicFeatureTh="+i+".txt");
//		} // done
//		blockSizeAll(workFolder+"keyIndBasicFeatureIndex", workFolder+"blockSizesSearch.txt"); // done
//		for (int i = 10; i < 100; i += 10) {
//			prefixBlocking(workFolder+"keyIndBasicFeatureIndex", i, workFolder+"keyIndBasicFeatureTh="+i+".txt");
//		} // running
		
//		classifyTermsAccording2freq(workFolder+"keyIndBasicFeatureIndex", workFolder+"termsFreq/termsFreq="); // done
		
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 1000, 50); // done
		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 2000, 50); // to run
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 3000, 50); // to run
//		
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 300000, 20); // to run
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 400000, 20); // to run
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 500000, 20); // to run
//
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 4000000, 10); // to run
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 5000000, 10); // to run
//		incrementalAddEntities(workFolder+"nonNullIndCaned.txt", 10000000, workFolder+"incExpIndex", 6000000, 10); // to run

	}
	
	public static void classifyTermsAccording2freq(String indexFolder, String outputPrefix) throws Exception {
		IndexReader ireader = IndexReader.open(indexFolder);
		TermEnum te = ireader.terms();
		int blockCount = 0;
		while (te.next()) {
			int freq = te.docFreq();
			PrintWriter pw = IOFactory.getPrintWriter(outputPrefix+freq/100+".txt", true);
			pw.println(te.term().text() + "\t" + freq);
			pw.close();
			blockCount++;
			if (blockCount%1000000 == 0) System.out.println(blockCount);
		}
		ireader.close();

	}
	
	/**
	 * record the time cost of incremental blocking
	 * @param allEntities
	 * @param allSize
	 * @param indexFolder
	 * @param incSize
	 * @param incNum
	 * @throws Exception
	 */
	public static void incrementalAddEntities(String allEntities, int allSize, String indexFolder, 
			int incSize, int incNum) throws Exception {
		TreeSet<Integer> lineNums = new TreeSet<Integer>();
		Random r = new Random(new Date().getTime());
		for (int i = 0; i < incNum; i++) {
			PrintWriter pw = IOFactory.getPrintWriter(workFolder+"inc"+i+".txt");
			lineNums.clear();
			for (int j = 0; j < incSize; j++) lineNums.add(r.nextInt(allSize));
			int lineNum = 0;
			BufferedReader br = IOFactory.getBufferedReader(allEntities);
			for (Integer k : lineNums) {
				for (int l = lineNum; l < k; l++) br.readLine();
				pw.println(br.readLine());
				lineNum = k+1;
			}
			br.close();
			pw.close();
		}
		System.out.println("incSize: " + incSize + "\tincNum: " + incNum);
		long startTime = new Date().getTime();
		indexAll(workFolder+"inc0.txt", indexFolder);
		long timeCost = new Date().getTime()-startTime;
		System.out.println(timeCost);
		for (int i = 1; i < incNum; i++) {
			startTime = new Date().getTime();
			addEntitiesIntoIndex(indexFolder, workFolder+"inc"+i+".txt");
			timeCost = new Date().getTime()-startTime;
			System.out.println(timeCost);
		}
	}
	
	/**
	 * add new entities to the original blocking index
	 * @param originalIndexFolder
	 * @param newEntities
	 * @throws Exception
	 */
	public static void addEntitiesIntoIndex(String originalIndexFolder, String newEntities) throws Exception {
		canonicalize(newEntities, workFolder+"tempIndex", workFolder+"tempCaned.txt");
		indexAll(workFolder+"tempCaned.txt", workFolder+"tempIndex");
		IndexWriter iwriter = new IndexWriter(originalIndexFolder, new WhitespaceAnalyzer(), false, 
				IndexWriter.MaxFieldLength.UNLIMITED);
		iwriter.addIndexesNoOptimize(new Directory[]{FSDirectory.getDirectory(workFolder+"tempIndex")});
		iwriter.optimize();
		iwriter.close();
		new File(workFolder+"tempCaned.txt").delete();
	}
	
	/**
	 * compute candidate blocks whose entities all match a keyword based on an existing blocking result
	 * @param keyword
	 * @param blockFile
	 * @param indexFolder
	 * @param output
	 * @throws Exception
	 */
	public static void computeCandidates(String keyword, String blockFile, 
			String indexFolder, String output) throws Exception {
		HashSet<Integer> hits = getKeywordHits(keyword, indexFolder);
		BufferedReader br = IOFactory.getBufferedReader(blockFile);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			boolean firstID = true;
			int[] entityIDs = Common.getNumsInLine(line);
			for (int i = 0; i < entityIDs.length; i++) if (hits.contains(entityIDs[i])) {
				if (firstID) {
					pw.print(entityIDs[i]);
					firstID = false;
				} else {
					pw.print(" " + entityIDs[i]);
				}
			}
			if (!firstID) pw.println();
		}
		pw.close();
		br.close();
	}
	
	/**
	 * return the IDs of all the entities with keyword as one of its features
	 * @param keyword
	 * @param indexFolder
	 * @return
	 * @throws Exception
	 */
	public static HashSet<Integer> getKeywordHits(String keyword, String indexFolder) throws Exception {
		IndexReader ireader = IndexReader.open(indexFolder);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		TopDocs td = isearcher.search(new TermQuery(new Term("words", keyword)), ireader.maxDoc());
		HashSet<Integer> ret = new HashSet<Integer>();
		for (int i = 0; i < td.scoreDocs.length; i++) 
			ret.add(Integer.parseInt(ireader.document(td.scoreDocs[i].doc).get("id")));
		isearcher.close();
//		TermDocs td = ireader.termDocs(new Term("words", keyword));
//		HashSet<Integer> ret = new HashSet<Integer>();
//		while (td.next()) ret.add(Integer.parseInt(ireader.document(td.doc()).get("id")));
		ireader.close();
		return ret;
	}
	
	/**
	 * Treat the inverted lists shorter than freqTh as blocks
	 * @param indexFolder
	 * @param freqTh
	 * @param output
	 * @throws Exception
	 */
	public static void prefixBlocking(String indexFolder, int freqTh, String output) throws Exception {
		long startTime = new Date().getTime();
		
		IndexReader ireader = IndexReader.open(indexFolder);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		TermEnum te = ireader.terms();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int blockCount = 0;
		while (te.next()) {
			TopDocs td = isearcher.search(new TermQuery(te.term()), freqTh+1);
			
			// discard blocks with only one individual or of too frequent tokens
			if (td.scoreDocs.length <= 1 || td.scoreDocs.length > freqTh) continue;
			
			pw.print(ireader.document(td.scoreDocs[0].doc).get("id"));
			for (int i = 1; i < td.scoreDocs.length; i++) {
				pw.print(" " + ireader.document(td.scoreDocs[i].doc).get("id"));
			}
			pw.println();
			blockCount++;
//			if (blockCount%1000 == 0) System.out.println(new Date().toString() + " : " + blockCount + " blocks");
		}
		pw.close();
		ireader.close();
		long time = new Date().getTime()-startTime;
		System.out.println(freqTh + "\t" + blockCount + "\t" + time); // for speed test
	}

	/**
	 * print the size of each block (actually the length of each inverted list in the index)
	 * @param indexFolder
	 * @param output
	 * @throws Exception
	 */
	public static void blockSizeAll(String indexFolder, String output) throws Exception {
		IndexReader ireader = IndexReader.open(indexFolder);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		TermEnum te = ireader.terms();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int blockCount = 0;
		while (te.next()) {
			TopDocs td = isearcher.search(new TermQuery(te.term()), 999999);
			pw.println(td.scoreDocs.length);
			blockCount++;
			if (blockCount % 10000 == 0) System.out.println(new Date().toString() + " : " + blockCount);
		}
		pw.close();
		isearcher.close();
		ireader.close();
	}
	
	/**
	 * index all features of all entities in the canonicalized input file
	 * @param input
	 * @param indexFolder
	 * @throws Exception
	 */
	public static void indexAll(String input, String indexFolder) throws Exception {
		indexAll(input, Integer.MAX_VALUE, Integer.MAX_VALUE, indexFolder);
	}
	
	/**
	 * index a certain length of each feature set of the first <lines> entities in the 
	 * canonicalized input file 
	 * @param input
	 * @param lines
	 * @param maxFeatureLength
	 * @param indexFolder
	 * @throws Exception
	 */
	public static void indexAll(String input, int lines, int maxFeatureLength, 
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
				for (int j = 1; j <= n && j <= maxFeatureLength; j++) 
					tokens += " "+parts[j];
				Document doc = new Document();
				doc.add(new Field("id", ""+idx, Field.Store.YES, Field.Index.NO));
				doc.add(new Field("words", tokens, Field.Store.YES, Field.Index.ANALYZED));
				iwriter.addDocument(doc);
				lineCount++;
//				if (lineCount%1000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
			} catch (Exception e) {
				continue;
			}
		}
		iwriter.optimize();
		iwriter.close();
		br.close();
		dir.close();
	}
	
	public static void getMissingPairs(String blockFile, String sameAsFile, String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(sameAsFile);
		BufferedReader br = IOFactory.getBufferedReader(blockFile);
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String toTest = docNums[i] + " " + docNums[j];
				if (stdSet.contains(toTest)) {
					stdSet.remove(toTest);
				}
			}
			lineCount++;
			if (lineCount%1000 == 0) System.out.println(lineCount);
		}
		br.close();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String s : stdSet) pw.println(s);
		pw.close();
	}
	
	private static void addID(String toAdd, String idFile, String output) throws Exception {
		BufferedReader idreader = IOFactory.getBufferedReader(idFile);
		BufferedReader toaddreader = IOFactory.getBufferedReader(toAdd);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String id = idreader.readLine(); id != null; id = idreader.readLine()) {
			String line = toaddreader.readLine();
			pw.println(id+line);
		}
		pw.close();
		toaddreader.close();
		idreader.close();
		
	}

	public static float getRecall(String blockFile, String stdAns) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		int ansSize = stdSet.size();
		System.out.println("answer size: " + ansSize);
		BufferedReader br = IOFactory.getBufferedReader(blockFile);
		int overlap = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String toTest = docNums[i] + " " + docNums[j];
				if (stdSet.contains(toTest)) {
					overlap++;
					if (overlap%10000 == 0) 
						System.out.println(new Date().toString() + " : " + overlap + " overlaps");
					stdSet.remove(toTest); // to avoid duplicate counting
				}
			}
		}
		br.close();
		System.out.println("overlap: " + overlap);
		return (overlap+0.0f)/ansSize;
	}
	
	public static void canonicalize(String input, String indexFolder, String output) throws Exception {
		canonicalize(input, Integer.MAX_VALUE, indexFolder, output);
	}
	
	/**
	 * Canonicalize the first maxLineNumber lines in input, i.e., sort the tokens by document frequency 
	 * in ascending order
	 * @param input
	 * @param maxLineNumber
	 * @param indexFolder
	 * @param output
	 * @throws Exception
	 */
	public static void canonicalize(String input, int maxLineNumber, String indexFolder, String output) throws Exception {
		Directory dir = FSDirectory.getDirectory(indexFolder);
		IndexWriter iwriter = new IndexWriter(dir, null, true, IndexWriter.MaxFieldLength.UNLIMITED);
		BufferedReader br = IOFactory.getBufferedReader(input);
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] tokens = Common.sortUnique(line, 1);
			for (int i = 1; i < tokens.length; i++) {
				String t = tokens[i];
				Document doc = new Document();
				doc.add(new Field("term", t, Field.Store.NO, Field.Index.NOT_ANALYZED));
				iwriter.addDocument(doc);
			}
			lineCount++;
//			if (lineCount % 100 == 0) 
//				System.out.println(new Date().toString() + " : " + lineCount + " lines indexed");
			if (lineCount == maxLineNumber) break;
		}
//		System.out.println(new Date().toString() + " : " + lineCount + " lines indexed");
		br.close();
		iwriter.optimize();
		iwriter.close();
//		System.out.println(new Date().toString() + " : indexing finished");
		final IndexReader ireader = IndexReader.open(dir);
		br = IOFactory.getBufferedReader(input);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] tokens = Common.sortUnique(line, 1);
			Arrays.sort(tokens, 1, tokens.length, new Comparator<String>() {
				public int compare(String a, String b) {
					try {
						int fa = ireader.docFreq(new Term("term", a));
						int fb = ireader.docFreq(new Term("term", b));
						if (fa > fb) return 1;
						else if (fa < fb) return -1;
						return 0;
					} catch (Exception e) {
						e.printStackTrace();
						return 0;
					}
				}
			});
			pw.print(tokens[0]);
			for (int i = 1; i < tokens.length; i++) pw.print(" " + tokens[i]);
			pw.println();
			lineCount++;
//			if (lineCount%100000 == 0) 
//				System.out.println(new Date().toString() + " : " + lineCount + " lines output");
		}
//		System.out.println(new Date().toString() + " : " + lineCount + " lines output");
		pw.close();
		br.close();
		ireader.close();
		dir.close();
		Common.deleteFolder(new File(indexFolder));
//		System.out.println(new Date().toString() + " : canonicalization finished");
	}
	
	/**
	 * for each n get how many blocks is of size n
	 * @param input
	 * @param output
	 */
	private static void getBlockSizeDistribution(String input, String output) throws Exception {
		TreeMap<Integer, Integer> size2num = new TreeMap<Integer, Integer>();
		BufferedReader br = IOFactory.getBufferedReader(input);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int key = line.split(" ").length;
			if (!size2num.containsKey(key)) size2num.put(key, 0);
			size2num.put(key, size2num.get(key)+1);
		}
		br.close();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer key : size2num.keySet()) pw.println(key + " " + size2num.get(key));
		pw.close();
	}

	public static void prefixBlockingWithLucene(String input, float prefix, int maxPrefixLength, 
			int maxBlockSize, String indexFolder, 
			String output, String report) throws Exception {
		prefixBlockingWithLucene(input, Integer.MAX_VALUE, prefix, maxPrefixLength, maxBlockSize, 
				indexFolder, output, report);
	}
	
	/**
 	 * words in each records in input is sorted by document frequency, if ceil(prefix*length)-prefix share
	 * at least one token, block them, 
	 * @param input
	 * @param lines number of lines to block
	 * @param prefix prefix parameter
	 * @param maxDocFreq max document frequency for a token to be considered a rare feature
	 * @param indexFolder temporary index folder
	 * @param output
	 * @param report
	 * @throws Exception
	 */
	public static void prefixBlockingWithLucene(String input, int lines, float prefix, int maxPrefixLength, 
			int maxDocFreq, String indexFolder, 
			String output, String report) throws Exception {
		long startTime = new Date().getTime();
		Common.indexPrefix(input, lines, prefix, maxPrefixLength, indexFolder);
		
		IndexReader ireader = IndexReader.open(indexFolder);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		TermEnum te = ireader.terms();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int maxBlockSize = 0;
		int totalBlockSize = 0;
		int blockCount = 0;
		while (te.next()) {
			TopDocs td = isearcher.search(new TermQuery(te.term()), maxDocFreq+1);
			
			// discard blocks with only one individual or of too frequent tokens
			if (td.scoreDocs.length <= 1 || td.scoreDocs.length > maxDocFreq) continue;
			
			if (td.scoreDocs.length > maxBlockSize) maxBlockSize = td.scoreDocs.length;
			totalBlockSize += td.scoreDocs.length;
			blockCount++;
			pw.print(ireader.document(td.scoreDocs[0].doc).get("id"));
			for (int i = 1; i < td.scoreDocs.length; i++) {
				pw.print(" " + ireader.document(td.scoreDocs[i].doc).get("id"));
			}
			pw.println();
			if (blockCount%1000 == 0) System.out.println(new Date().toString() + " : " + blockCount + " blocks");
		}
		pw.close();
		ireader.close();
		long time = new Date().getTime()-startTime;
		pw = IOFactory.getPrintWriter(report, true);
		pw.println(new Date().toString());
		pw.println("#individual: " + lines);
		pw.println("blocking parameter: " + prefix);
		pw.println("time: " + time);
		pw.println("#block: " + blockCount);
		pw.println("max block size: " + maxBlockSize);
		pw.println("avg block size: " + (totalBlockSize+0.0)/blockCount);
		pw.close();
		Common.deleteFolder(new File(indexFolder));
		System.out.println(prefix + "\t" + lines + "\t" + time); // for speed test
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
	public static void prefixBlockingInMem(String input, int lines, float prefix, String output, 
			String report) throws Exception {
		long startTime = new Date().getTime();
		int[][] feature = new int[lines+1][];
		Common.getBinaryFeature(input, lines, feature);
		HashMap<Integer, ArrayList<Integer>> token2rec = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 1; i <= lines; i++) {
			for (int j = 0; j < (int)Math.ceil(feature[i].length*prefix) || (j < 3 && j < feature[i].length); j++) {
				if (token2rec.containsKey(feature[i][j])) token2rec.get(feature[i][j]).add(i);
				else {
					ArrayList<Integer> value = new ArrayList<Integer>();
					value.add(i);
					token2rec.put(feature[i][j], value);
				}
			}
			if (i%10000 == 0) System.out.println(new Date().toString() + " : " + i + " lines indexed");
		}
		int maxBlockSize = 0;
		int totalBlockSize = 0;
		int blockCount = 0;
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer i : token2rec.keySet()) {
			ArrayList<Integer> recs = token2rec.get(i);
			if (recs.size() == 1) continue;
			if (recs.size() > maxBlockSize) maxBlockSize = recs.size();
			totalBlockSize += recs.size();
			blockCount++;
			boolean first = true;
			for (Integer j : recs) {
				if (first) {
					pw.print(j);
					first = false;
				} else {
					pw.print(" " + j);
				}
			}
			pw.println();
		}
		pw.close();
		long time = new Date().getTime()-startTime;
		pw = IOFactory.getPrintWriter(report);
		pw.println("#individual: " + lines);
		pw.println("blocking parameter: " + prefix);
		pw.println("time: " + time);
		pw.println("#block: " + blockCount);
		pw.println("max block size: " + maxBlockSize);
		pw.println("avg block size: " + (totalBlockSize+0.0)/blockCount);
		pw.close();
		System.out.println("#individual:" + lines);
		System.out.println("blocking parameter: " + prefix);
		System.out.println("time: " + time);
		System.out.println("#block: " + blockCount);
		System.out.println("max block size: " + maxBlockSize);
		System.out.println("avg block size: " + (totalBlockSize+0.0)/blockCount);
	}
	
	/**
	 * dump candidate pairs in blocks to evaluate recall of blocking
	 * @param blockFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void dumpCanPairs(String blockFile, String output) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(blockFile);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String toTest = docNums[i] + " " + docNums[j];
				pw.println(toTest);
			}
		}
		br.close();
		pw.close();
	}

	/**
	 * evaluate recall of blocking
	 * @param blockFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void evaluate(String blockFile, String stdAns, String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		HashSet<String> resSet = new HashSet<String>();
		BufferedReader br = IOFactory.getBufferedReader(blockFile);
		int overlap = 0;
		int maxBlockSize = 0;
		int blockNum = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			if (docNums.length > maxBlockSize) maxBlockSize = docNums.length;
//			System.out.println(docNums.length);
			blockNum++;
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String toTest = docNums[i] + " " + docNums[j];
				if (stdSet.contains(toTest)) {
					overlap++;
					stdSet.remove(toTest); // to avoid duplicate counting
				}
				resSet.add(toTest);
			}
		}
		br.close();
		Common.printResult(overlap, stdAns, resSet.size(), output);
	}
	
	/**
	 * find blocks from translated and sorted ppjoin result, each input line contains a pair of doc#s
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	public static void findBlock(String input, String output) throws Exception {
		IDataSourceReader br = IOFactory.getReader(input);
		HashSet<HashSet<Integer>> blocks = new HashSet<HashSet<Integer>>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			
			// get document numbers
			int ix = Integer.parseInt(parts[0]);
			int iy = Integer.parseInt(parts[1]);

			if (blocks.size() == 0) {
				HashSet<Integer> block = new HashSet<Integer>();
				block.add(ix);
				block.add(iy);
				blocks.add(block);
			} else {
				boolean added = false;
				for (HashSet<Integer> block : blocks) {
					if (block.contains(ix)) {
						block.add(iy);
						added = true;
						break;
					} else if (block.contains(iy)) {
						block.add(ix);
						added = true;
						break;
					}
				}
				if (!added) {
					HashSet<Integer> block = new HashSet<Integer>();
					block.add(ix);
					block.add(iy);
					blocks.add(block);
				}
			}
			count++;
			if (count%100000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		br.close();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (HashSet<Integer> block : blocks) {
			boolean first = true;
			for (Integer i : block) {
				if (first) {
					pw.print(i.intValue());
					first = false;
				} else {
					pw.print(" " + i.intValue());
				}
			}
			pw.println();
		}
		pw.close();
	}
	
	// the end of this source file

//	public static String ppIndex = 
//		"smb://poseidon/team/Semantic Search/BillionTripleData/index/ppIndex";
//	
//	/**
//	 * index contains a set of records
//	 * t is a Jaccard similarity threshold
//	 * output contains all pairs of records (x, y), such that sim(x, y) >= t
//	 */
//	public static void ppjoin(String indexR, double t, 
//			String output) throws Exception {
//		IndexReader ireader = IndexReader.open(indexR);
//		for (int r = 0; r < ireader.maxDoc(); r++) {
//			// record how many words have been overlapped
//			HashMap<Integer, Integer> matched = new HashMap<Integer, Integer>();
//			
//			// get record, calculate necessary prefix length
//			String[] x = ireader.document(r).get("extended").split(" ");
//			int lx = x.length;
//			int p = lx-(int)Math.ceil(t*lx)+1;
//			
//			for (int i = 0; i < p; i++) {
//				String w = x[i];
//				TermPositions tp = ireader.termPositions(new Term("extended", w));
//				while (tp.next()) {
//					int iy = tp.doc();
//					String[] y = ireader.document(iy).get(
//							"extended").split(" ");
//					int ly = y.length;
//					if (ly >= t*lx || lx >= t*ly) continue;
//					int j = tp.nextPosition();
//					int alpha = (int)Math.ceil(t*(lx+ly)/(1+t));
//					int ubound = 1+Math.min(lx-i-1, ly-j-1);
//					int m = matched.get(iy);
//					if (m+ubound >= alpha) matched.put(iy, m+1);
//					else matched.put(iy, 0);
//				}
//			}
//			verify(x, matched, ireader, output);
//		}
//		ireader.close();
//	}
//
//	private static void verify(String[] x, HashMap<Integer, Integer> matched,
//			IndexReader ireader, String output) throws Exception {
//		
//	}
	
//	/**
//	 * translate line#s in the block file to doc#s of individuals
//	 * @param input
//	 * @param keyIndList
//	 * @param output
//	 * @throws Exception
//	 */
//	public static void translateBlock(String input, String keyIndList, String output) throws Exception {
//		int lineNum = Analyzer.countLines(keyIndList);
//		int[] lineList = new int[lineNum+1];
//		BufferedReader br = IOFactory.getBufferedReader(keyIndList);
//		for (int i = 1; i <= lineNum; i++) lineList[i] = Integer.parseInt(br.readLine());
//		br.close();
//		br = IOFactory.getBufferedReader(input);
//		PrintWriter pw = IOFactory.getPrintWriter(output);
//		for (String line = br.readLine(); line != null; line = br.readLine()) {
//			String[] parts = line.split(" ");
//			boolean first = true;
//			for (String s : parts) {
//				if (first) {
//					pw.print(lineList[Integer.parseInt(s)]);
//					first = false;
//				} else {
//					pw.print(" " + lineList[Integer.parseInt(s)]);
//				}
//			}
//			pw.println();
//		}
//		pw.close();
//		br.close();
//	}
	

}
