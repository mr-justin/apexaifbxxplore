package main;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import basic.IOFactory;

public class KeywordSelector {
	
	public static void main(String[] args) throws Exception {
		selectKeywordsByAns(Clusterer.workFolder+"clusterTh=1000sn=60.txt", 
				Indexer.indexFolder+"sameAsID.txt", Blocker.workFolder+"keyIndBasicFeatureIndex", 
				Clusterer.workFolder+"selectedKeywords.txt");
	}
	
	/**
	 * select keywords that cause blocks with at least minPair pairs of sameAs entities
	 * @param indexFolder
	 * @param stdAns
	 * @param minPair
	 * @throws Exception
	 */
	public static void selectKeyword(String indexFolder, String stdAns, 
			int minPair, String output) throws Exception {
		IndexReader ireader = IndexReader.open(indexFolder);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		TermEnum te = ireader.terms();
		int blockCount = 0;
		while (te.next()) {
			String keyword = te.term().text();
			HashSet<Integer> block = Blocker.getKeywordHits(keyword, indexFolder);
			HashSet<String> stdSet = Common.getFilteredStringSet(stdAns, block);
			if (stdSet.size() > minPair) pw.println(keyword + "\t" + stdSet.size());
			blockCount++;
			if (blockCount%1000 == 0) System.out.println(blockCount);
		}
		ireader.close();
		pw.close();
	}

	public static void selectKeywordsByAns(String clusterResult, String stdAns, String indexFolder, 
			String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		HashSet<Integer> indSet = new HashSet<Integer>();
		BufferedReader br = IOFactory.getBufferedReader(clusterResult);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String toTest = docNums[i] + " " + docNums[j];
				if (stdSet.contains(toTest)) {
					indSet.add(docNums[i]);
					indSet.add(docNums[j]);
				}
			}
		}
		br.close();
		System.out.println(indSet.size());

		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		HashSet<String> keywordSet = new HashSet<String>();
		int count = 0;
		for (Integer ind : indSet) {
			String features = ireader.document(ind).get("basic");
			String[] featuresV = features.split(" ");
			for (String keyword : featuresV) keywordSet.add(keyword);
			count++;
			if (count % 1000 == 0) System.out.println(count);
		}
		ireader.close();
		System.out.println(keywordSet.size());
		
		PrintWriter pw = IOFactory.getPrintWriter(output);
		count = 0;
		for (String keyword : keywordSet) {
			HashSet<Integer> block = Blocker.getKeywordHits(keyword, indexFolder);
			HashSet<String> stdSet1 = Common.getFilteredStringSet(stdAns, block);
			pw.println(keyword + "\t" + stdSet1.size() + "\t" + block.size());
			count++;
			if (count % 1000 == 0) System.out.println(count);
		}
		pw.close();
		
	}

}
