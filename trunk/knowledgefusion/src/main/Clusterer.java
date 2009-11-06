package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.lucene.index.IndexReader;

import basic.IDataSourceReader;
import basic.IOFactory;

public class Clusterer {

	public static String workFolder = "/usr/fulinyun/clusterer/";
	public static String classFolder = "/usr/fulinyun/classCluster/";
	
	public static void main(String[] args) throws Exception {
		
//		NN me = new NN(0, 0);
//		NN a = new NN(1, 50);
//		NN b = new NN(2, 75);
//		NN c = new NN(3, 101);
//		NN d = new NN(4, 102);
//		NNListAndNG listAndNg = new NNListAndNG(new NN[]{me, a, b, c, d}, 2);
//		
//		System.out.println(calcNg(new NN[]{me, a, b, c, d}));
//		cluster(Blocker.workFolder+"prefix0.2blockTranslated.txt", 
//				Blocker.workFolder+"prefix0.2cluster2&1.1.txt", 2, 1.1f); // done
//		evaluate(Blocker.workFolder+"prefix0.2cluster2&1.1.txt", Indexer.indexFolder+"sameAsID.txt"); // done
//		getClusterWrong(Blocker.workFolder+"prefix0.2cluster2&2.txt", Indexer.indexFolder+"sameAsID.txt", 
//				Blocker.workFolder+"cluster2&2tolabel.txt");
//		cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt",
//				Blocker.workFolder+"prefix0.2&3cluster2&2.txt", 2, 2); // done
//		evaluate(Blocker.workFolder+"prefix0.2&3cluster2&2.txt", Indexer.indexFolder+"sameAsID.txt"); // to run
//		cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt",
//				Blocker.workFolder+"prefix0.2&3cluster2&1.5.txt", 2, 1.5f);
//		evaluate(Blocker.workFolder+"prefix0.2&3cluster2&1.5.txt", Indexer.indexFolder+"sameAsID.txt", 
//				Blocker.workFolder+"prefix0.2&3cluster2&1.5eval.txt"); // done 
//		evaluate(Blocker.workFolder+"prefix0.2&3cluster2&2.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"prefix0.2&3cluster2&2eval.txt"); // done
//		getClusterWrong(workFolder+"prefix0.2&3cluster2&2.txt", Indexer.indexFolder+"sameAsID.txt",
//				workFolder+"prefix0.2&3cluster2&2tolabel.txt");
//		cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", 
//				workFolder+"prefix0.2&3cluster2&1.1.txt", 2, 1.1f);
//		evaluate(workFolder+"prefix0.2&3cluster2&1.1.txt", Indexer.indexFolder+"sameAsID.txt",
//				workFolder+"prefix0.2&3cluster2&1.1eval.txt");
		// for drawing P-R curve for CS&SN cluster
//		for (int i = 3; i < 6; i++) {
//			float th = 2.5f+i*0.5f;
//			String clusterResult = workFolder+"prefix0.2&3cluster2&"+th;
//			cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", 
//					clusterResult+".txt", 2, th);
//			evaluate(clusterResult+".txt", Indexer.indexFolder+"sameAsID.txt",
//					clusterResult+"eval.txt");
//		} // done
		
		// to show the cluster complexity, to show the impact of blocking
//		generateTestFiles();
//		for (int i = 100; i < 2000; i += 100) {
//			long startTimeMs = new Date().getTime();
//			cluster(workFolder+"test"+i+".txt", workFolder+"test"+i+"result.txt", 2, 1.5f);
//			long timeMs = new Date().getTime()-startTimeMs;
//			System.out.println(i + " : " + timeMs);
//			PrintWriter pw = IOFactory.getPrintWriter(workFolder+"clusterTime.txt", true);
//			pw.println(i + " : " + timeMs);
//			pw.close();
//		} // done

		// for P-R curve for jaccard constraint cluster
//		for (int i = 0; i < 9; i++) {
//			float jth = 0.1f+i*0.1f;
//			String clusterResult = workFolder+"j"+jth+".txt";
//			jaccardCluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", clusterResult, jth);
//		} // done
		
		// sort -n jX.X.txt | uniq > jX.Xsorted.txt // done
		
//		evaluateWithDomain(workFolder+"prefix0.2&3cluster2&1.1.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"cluster2&1.1domainEval.txt"); // running
//		for (int i = 20; i < 55; i += 5) {
//			evaluateWithDomain(workFolder+"prefix0.2&3cluster2&"+i/10+"."+i%10+".txt", 
//					Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"cluster2&"+i/10+"."+i%10+"domainEval.txt"); 
//		} // done
//		getClusterWrongWithDomain(workFolder+"prefix0.2&3cluster2&1.1.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"cluster2&1.1domain2label.txt");
//		getCrossDomainClassMatching(classFolder+"prefix0.2&3classCluster2&1.5.txt", 
//				classFolder+"prefix0.2&3classCluster2&1.5domain.txt");
//		getCrossDomainClassMatching(classFolder+"prefix0.2cluster2&1.1class_Th0.05.txt", 
//				classFolder+"prefix0.2cluster2&1.1class_th0.05domain.txt");
//		getClusterDomainDistribution(workFolder+"prefix0.2cluster2&1.1.txt", 
//				workFolder+"cluster2&1.1domainDistributionOld.txt");
//		for (int i = 31; i <= 39; i++) {
//			jaccardCluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"j0."+i+".txt", i/100.0f);
//		} // done
		// sort -n jX.X.txt | uniq > jX.Xsorted.txt // done
//		PrintWriter pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
//		pw.println(new Date().toString());
//		pw.close();
//		for (int i = 31; i < 40; i++) {
//			evaluateWithDomain(workFolder+"j0."+i+"sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"clusterPR/j0."+i+"domainEval.txt");
//		}
		
//		for (int i = 41; i <= 49; i++) {
//			jaccardCluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"j0."+i+".txt", i/100.0f);
//		} // done
		// sort -n jX.X.txt | uniq > jX.Xsorted.txt
//		pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
//		pw.println(new Date().toString());
//		pw.close();
//		for (int i = 41; i < 50; i++) {
//			evaluateWithDomain(workFolder+"j0."+i+"sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"clusterPR/j0."+i+"domainEval.txt");
//		}
		
//		for (int i = 391; i <= 399; i++) {
//			jaccardCluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"j0."+i+".txt", i/1000.0f);
//		} // running
		// sort -n j0.xxx.txt | uniq > j0.xxxsorted.txt // todo
//		PrintWriter pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
//		pw.println(new Date().toString() + " j=0.391-0.399");
//		pw.close();
//		for (int i = 391; i < 399; i++) {
//			evaluateWithDomain(workFolder+"j0."+i+"sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"clusterPR/j0."+i+"domainEval.txt");
//		} // done
//		
//		PrintWriter pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
//		pw.println(new Date().toString() + " theta=2.41-2.49");
//		pw.close();
//		for (int i = 241; i <= 249; i++) {
//			cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"cluster"+i/100+"."+i%100+".txt", 2, i/100.0f);
//			evaluateWithDomain(workFolder+"cluster"+i/100+"."+i%100+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"clusterPR/cluster"+i/100+"."+i%100+"domainEval.txt");
//		} // running
//		jaccardCluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"j0.4.txt", 0.4f); // running
		// sort -n j0.4.txt | uniq > j0.4sorted.txt // done
//		PrintWriter pw = IOFactory.getPrintWriter(Indexer.indexFolder+"pr.txt", true);
//		pw.println(new Date().toString() + " j=0.4");
//		pw.close();
//		evaluateWithDomain(workFolder+"j0.4sorted.txt", Indexer.indexFolder+"sameAsID.txt", 
//			workFolder+"clusterPR/j0.4domainEval.txt"); // done
//		getClusterDomainDistribution(Indexer.indexFolder+"sameAsID.txt", 
//				Indexer.indexFolder+"sameAsIDdomainDistribution.txt"); // done
//		cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"cluster2.5.txt", 2, 2.5f);
//		evaluateWithDomain(workFolder+"cluster2.5.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"clusterPR/cluster2.5domainEval.txt");
//		cluster(Blocker.workFolder+"nonNullIndBlocks0.2&100.txt", workFolder+"cluster0.2&100&2.5.txt", 
//				2, 2.5f, 4);
//		evaluateWithDomain(workFolder+"cluster0.2&100&2.5.txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"clusterPR/cluster0.2&100&2.5domainEval.txt"); // running
//		cluster(Blocker.workFolder+"prefix0.2&3blockTranslated.txt", workFolder+"clusterTemp.txt", 2, 1.1f, 100);
//		pickThreshold("/usr/fulinyun/ng.txt", "/usr/fulinyun/ngFiltered.txt");	// all avg ng = 1.0
//		int i = 60;
//			float th = i/10.0f;
//			cluster(Blocker.workFolder+"keyIndBasicFeatureP=50.txt", workFolder+"clusterBasic"+i+".txt", 
//					2, th, 100);
//		 // done
//		for (i = 15; i <= 60; i += 5) {
//			evaluateWithDomain(workFolder+"clusterBasic"+i+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"clusterBasic"+i+"eval.txt");
//		} // done
		// pr.txt -> prClusterBasic.txt // done
//		for (i = 15; i <= 60; i += 5) {
//			th = i/10.0f;
//			cluster(Blocker.workFolder+"keyIndExtendedFeatureP=25.txt", workFolder+"clusterExtended"+i+".txt", 
//					2, th, 100);
//		} // done
//		for (int i = 15; i <= 60; i += 5) {
//			evaluateWithDomain(workFolder+"clusterExtended"+i+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"clusterExtended"+i+"eval.txt");
//		} // done
		// pr.txt -> prClusterExtended.txt // done
//		int i = 105;
//		float th = i/100.0f;
//		cluster(Blocker.workFolder+"keyIndExtendedFeatureP=25.txt", workFolder+"clusterExtended"+i+".txt", 
//				2, th, 100, new ISimCal() {
//			public float distance(String[][] features, int i, int j) {
//				return jaccard(features, i, j);
//			}
//		});
//		evaluateWithDomain(workFolder+"clusterExtended"+i+".txt", Indexer.indexFolder+"sameAsID.txt", 
//				workFolder+"clusterExtended"+i+"eval.txt");
//		for (int i = 15; i <= 60; i += 5) {
//			float th = i/10.0f;
//			cluster(Blocker.workFolder+"keyIndBasicFeatureP=50.txt", workFolder+"clusterBasicCosine"+i+".txt", 
//					2, th, 100, new ISimCal() {
//				public float distance(String[][] features, int i, int j) {
//					return cosine(features, i, j);
//				}
//			});
//		} // done
//		for (int i = 15; i <= 60; i += 5) {
//			evaluateWithDomain(workFolder+"clusterBasicCosine"+i+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"clusterBasicCosine"+i+"eval.txt");
//		} // running
//		// pr.txt -> prClusterBasicCosine.txt // done
		
//		for (int i = 15; i <= 60; i += 5) {
//			float th = i/10.0f;
//			cluster(Blocker.workFolder+"keyIndExtendedFeatureP=25.txt", workFolder+"clusterExtendedCosine"+i+".txt", 
//					2, th, 100, new ISimCal() {
//				public float distance(String[][] features, int i, int j) {
//					return cosine(features, i, j);
//				}
//			});
//		} // done
//		for (int i = 15; i <= 60; i += 5) {
//			evaluateWithDomain(workFolder+"clusterExtendedCosine"+i+".txt", Indexer.indexFolder+"sameAsID.txt", 
//					workFolder+"clusterExtendedCosine"+i+"eval.txt");
//		} // done
//		// pr.txt -> prClusterExtendedCosine.txt // done

		for (int i = 15; i <= 60; i += 5) {
			float th = i/10.0f;
			cluster(Blocker.workFolder+"keyIndBasicFeatureOpedP=45.txt", workFolder+"clusterOped"+i+".txt", 
					2, th, 100, new ISimCal() {
				public float distance(String[][] features, int i, int j) {
					return jaccard(features, i, j);
				}
			});
		} // done
		for (int i = 15; i <= 60; i += 5) {
			evaluateWithDomain(workFolder+"clusterOped"+i+".txt", Indexer.indexFolder+"sameAsID.txt", 
					workFolder+"clusterOped"+i+"eval.txt");
		} // done
		// pr.txt -> prClusterExtendedCosine.txt // done

	}
	
	public static void getClusterDomainDistribution(String clusterFile,
			String output) throws Exception {
		HashMap<Integer, String> indURI = new HashMap<Integer, String>();
		HashMap<String, Integer> domainDistribution = new HashMap<String, Integer>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		int clusterNum = 0;
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			String[] uris = getURIs(ireader, docNums, indURI);
			clusterNum++;
			if (clusterNum % 10000 == 0) System.out.println(new Date().toString() + " : " + clusterNum);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String key = getDomain(uris[i])+"->"+getDomain(uris[j]);
				if (domainDistribution.containsKey(key)) domainDistribution.put(key, domainDistribution.get(key)+1);
				else domainDistribution.put(key, 1);
			}
		}
		br.close();
		ireader.close();
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String key : domainDistribution.keySet()) 
			pw.println(key + " " + domainDistribution.get(key));
		pw.close();
	}

	private static String getDomain(String uri) {
		String start = "<http://";
		int end = uri.indexOf("/", start.length());
		return uri.substring(start.length(), end);
	}

	/**
	 * 
	 * @param clusterFile
	 * @param output
	 * @throws Exception
	 */
	public static void getCrossDomainClassMatching(String clusterFile, String output) throws Exception {
		HashMap<Integer, String> classURI = new HashMap<Integer, String>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		int clusterNum = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			String[] uris = getURIs(ireader, docNums, classURI);
			clusterNum++;
			if (clusterNum%10000 == 0) System.out.println(new Date().toString() + " : " + clusterNum);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				if (uris[i].contains(KeyIndDealer.domainDBpedia) && uris[j].contains(KeyIndDealer.domainDBpedia)) continue;
				if (uris[i].contains(KeyIndDealer.domainGeonames) && uris[j].contains(KeyIndDealer.domainGeonames)) continue;
				if (uris[i].contains(KeyIndDealer.domainDblp) && uris[j].contains(KeyIndDealer.domainDblp)) continue;
				pw.println(docNums[i] + " " + docNums[j]);
			}
		}
		br.close();
		pw.close();
		ireader.close();
	}
	
	/**
	 * evaluate individual clusters of size 2 in clusterFile in terms of precision and recall w.r.t. stdAns, 
	 * consider only 
	 * individual pairs from dbpedia<->geonames and dblp<->dblp
	 * @param clusterFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void evaluateSize2withDomain(String clusterFile, String stdAns, String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		HashSet<String> resSet = new HashSet<String>();
		HashMap<Integer, String> indURI = new HashMap<Integer, String>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		int overlap = 0;
		int maxClusterSize = 0;
		int clusterNum = 0;
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			String[] uris = getURIs(ireader, docNums, indURI);
			if (docNums.length > maxClusterSize) maxClusterSize = docNums.length;
//			System.out.println(docNums.length);
			clusterNum++;
			if (clusterNum%10000 == 0) System.out.println(new Date().toString() + " : " + clusterNum);
			if (docNums.length == 2)
				if (uris[0].contains(KeyIndDealer.domainDBpedia) && uris[1].contains(KeyIndDealer.domainGeonames) 
					|| uris[0].contains(KeyIndDealer.domainGeonames) && uris[1].contains(KeyIndDealer.domainDBpedia)
					|| uris[0].contains(KeyIndDealer.domainDblp) && uris[1].contains(KeyIndDealer.domainDblp)) {
				String toTest = docNums[1] + " " + docNums[0];
				if (stdSet.contains(toTest)) {
					overlap++;
					stdSet.remove(toTest); // to avoid duplicate counting
				}
				resSet.add(toTest);
			}
		}
		br.close();
		Common.printResult(overlap, stdAns, resSet.size(), output);
		System.out.println("max cluster size: " + maxClusterSize + " ; #cluster: " + clusterNum);
		PrintWriter pw = IOFactory.getPrintWriter(output, true);
		pw.println("max cluster size: " + maxClusterSize + " ; #cluster: " + clusterNum);
		pw.close();
		ireader.close();
	}
	
	/**
	 * get false negative pairs w.r.t. stdAns, consider only 
	 * individual pairs from dbpedia<->geonames and dblp<->dblp
	 * @param clusterFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void getClusterWrongWithDomain(String clusterFile, String stdAns, String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		HashMap<Integer, String> indURI = new HashMap<Integer, String>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int clusterNum = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			String[] uris = getURIs(ireader, docNums, indURI);
			clusterNum++;
			if (clusterNum % 10000 == 0) System.out.println(new Date().toString() + " : " + clusterNum);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) 
				if (uris[i].contains(KeyIndDealer.domainDBpedia) && uris[j].contains(KeyIndDealer.domainGeonames) 
					|| uris[i].contains(KeyIndDealer.domainGeonames) && uris[j].contains(KeyIndDealer.domainDBpedia)
					|| uris[i].contains(KeyIndDealer.domainDblp) && uris[j].contains(KeyIndDealer.domainDblp)) {
				String toTest = docNums[i] + " " + docNums[j];
				if (!stdSet.contains(toTest)) pw.println(toTest);
			}
		}
		br.close();
		pw.close();
		ireader.close();
	}
	
	/**
	 * evaluate individual clusters in clusterFile in terms of precision and recall w.r.t. stdAns, consider only 
	 * individual pairs from dbpedia<->geonames and dblp<->dblp
	 * @param clusterFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void evaluateWithDomain(String clusterFile, String stdAns, String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		HashSet<String> resSet = new HashSet<String>();
		HashMap<Integer, String> indURI = new HashMap<Integer, String>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		int overlap = 0;
		int maxClusterSize = 0;
		int clusterNum = 0;
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			String[] uris = getURIs(ireader, docNums, indURI);
			if (docNums.length > maxClusterSize) maxClusterSize = docNums.length;
//			System.out.println(docNums.length);
			clusterNum++;
			if (clusterNum%100000 == 0) System.out.println(new Date().toString() + " : " + clusterNum);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) 
				if (uris[i].contains(KeyIndDealer.domainDBpedia) && uris[j].contains(KeyIndDealer.domainGeonames) 
					|| uris[i].contains(KeyIndDealer.domainGeonames) && uris[j].contains(KeyIndDealer.domainDBpedia)
					|| uris[i].contains(KeyIndDealer.domainDblp) && uris[j].contains(KeyIndDealer.domainDblp)
					|| uris[i].contains(KeyIndDealer.domainDBpedia) && uris[j].contains(KeyIndDealer.domainDblp)
					|| uris[j].contains(KeyIndDealer.domainDblp) && uris[i].contains(KeyIndDealer.domainDBpedia)) {
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
		System.out.println("max cluster size: " + maxClusterSize + " ; #cluster: " + clusterNum);
		PrintWriter pw = IOFactory.getPrintWriter(output, true);
		pw.println("max cluster size: " + maxClusterSize + " ; #cluster: " + clusterNum);
		pw.close();
	}
	
	private static String[] getURIs(IndexReader ireader, int[] docNums, 
			HashMap<Integer, String> indURI) throws Exception {
		String[] ret = new String[docNums.length];
		for (int i = 0; i < docNums.length; i++) {
			if (indURI.containsKey(docNums[i])) ret[i] = indURI.get(docNums[i]);
			else {
				ret[i] = ireader.document(docNums[i]).get("URI");
				indURI.put(docNums[i], ret[i]);
			}
		}
		return ret;
	}

	public static void generateTestFiles() throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(Indexer.indexFolder+"keyInd.txt");
		int[] docNums = new int[1900];
		for (int i = 0; i < 1900; i++) docNums[i] = Integer.parseInt(br.readLine());
		br.close();
		for (int i = 100; i < 2000; i += 100) {
			PrintWriter pw = IOFactory.getPrintWriter(workFolder+"test"+i+".txt");
			pw.print(docNums[0]);
			for (int j = 0; j < i; j++) pw.print(" " + docNums[j]);
			pw.println();
			pw.close();
		}
	}
	
	/**
	 * cluster by jaccard constraint
	 * @param blockFile
	 * @param output
	 * @param jth
	 * @throws Exception
	 */
	public static void jaccardCluster(String blockFile, String output, float jth) throws Exception {
		IDataSourceReader br = IOFactory.getReader(blockFile);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] records = line.split(" ");
			int[] docNums = new int[records.length];
			for (int i = 0; i < records.length; i++) docNums[i] = Integer.parseInt(records[i]);
			jaccardCluster(docNums, output, jth);
			count++;
			if (count%10000 == 0) System.out.println(new Date().toString() + " : " + count + " blocks");
		}
		br.close();
		System.out.println(new Date().toString() + " : clustering finished");
	}
	
	private static void jaccardCluster(int[] docNums, String output, float jth) throws Exception {
		String[][] basicFeatures = new String[docNums.length][];
		getBasicFeatures(docNums, basicFeatures);
		PrintWriter pw = IOFactory.getPrintWriter(output, true);
		for (int i = 0; i < basicFeatures.length; i++) for (int j = 0; j < i; j++) {
			if (1-jaccard(basicFeatures, i, j) > jth) pw.println(docNums[i] + " " + docNums[j]);
		}
		pw.close();
	}

	/**
	 * extract pairs found by clusterer but are not contained in stdAns and write the result to output
	 * output need manual labeling
	 * @param clusterFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void getClusterWrong(String clusterFile, String stdAns, String output) throws Exception {
		TreeSet<String> resSet = new TreeSet<String>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) {
				String toTest = docNums[i] + " " + docNums[j];
				resSet.add(toTest);
			}
		}
		br.close();
		System.out.println(new Date().toString() + " : all clusters read");
		
		br = IOFactory.getBufferedReader(stdAns);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			resSet.remove(line);
		}
		br.close();
		
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String s : resSet) pw.println(s);
		pw.close();
	}
	
	/**
	 * evaluate individual clusters in clusterFile in terms of precision and recall w.r.t. stdAns
	 * @param clusterFile
	 * @param stdAns
	 * @throws Exception
	 */
	public static void evaluate(String clusterFile, String stdAns, String output) throws Exception {
		HashSet<String> stdSet = Common.getStringSet(stdAns);
		HashSet<String> resSet = new HashSet<String>();
		BufferedReader br = IOFactory.getBufferedReader(clusterFile);
		int overlap = 0;
		int maxClusterSize = 0;
		int clusterNum = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int[] docNums = Common.getNumsInLineSorted(line);
			if (docNums.length > maxClusterSize) maxClusterSize = docNums.length;
//			System.out.println(docNums.length);
			clusterNum++;
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
		System.out.println("max cluster size: " + maxClusterSize + " ; #cluster: " + clusterNum);
		PrintWriter pw = IOFactory.getPrintWriter(output, true);
		pw.println("max cluster size: " + maxClusterSize + " ; #cluster: " + clusterNum);
		pw.close();
	}
	
	/**
	 * implement the CS & SN criteria
	 * @param input a set of doc# blocks, one per line 
	 * @param output a set of doc# clusters, one per line
	 * @param tsn threshold for SN criteria
	 * @param maxClusterSize
	 * @throws Exception
	 */
	public static void cluster(String input, String output, int ngRadius, float tsn, int maxClusterSize, 
			ISimCal simCal) throws Exception {
		new File(output).delete();
		IDataSourceReader br = IOFactory.getReader(input);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] records = line.split(" ");
//			if (records.length > 10) System.out.println(records.length);
			int[] docNums = new int[records.length];
			for (int i = 0; i < records.length; i++) docNums[i] = Integer.parseInt(records[i]);
			cluster(docNums, output, ngRadius, tsn, maxClusterSize, simCal);
			count++;
			if (count%100000 == 0) System.out.println(new Date().toString() + " : " + count + " blocks");
		}
		br.close();
		System.out.println(new Date().toString() + " : clustering finished");
	}

	/**
	 * clustering within a block
	 * @param docNums map from internal index (0-n) to doc# of index
	 * @param output
	 */
	private static void cluster(int[] docNums, String output, int ngRadius, float tsn, int maxClusterSize, 
			ISimCal simCal) throws Exception {
		String[][] basicFeatures = new String[docNums.length][];
		getBasicFeatures(docNums, basicFeatures);
		NN[][] nnList = new NN[docNums.length][docNums.length];
		for (int i = 0; i < docNums.length; i++) for (int j = i+1; j < docNums.length; j++) {
			nnList[i][j] = new NN(j, simCal.distance(basicFeatures, i, j));
		}
		for (int i = 0; i < docNums.length; i++) for (int j = 0; j < i; j++) { 
			nnList[i][j] = new NN(j, nnList[j][i].distance);
		}
		for (int i = 0; i < docNums.length; i++) {
			nnList[i][i] = new NN(i, 0);
		}
		for (int i = 0; i < docNums.length; i++) Arrays.sort(nnList[i], new Comparator<NN>() {
			public int compare(NN a, NN b) {
				if (a.distance > b.distance) return 1;
				if (a.distance == b.distance) return 0;
				return -1;
			}
		});
		for (int i = 0; i < docNums.length; i++) for (int j = 0; j < docNums.length; j++) 
			if (nnList[i][j].distance < 0) {
				System.out.println(i + " : " + basicFeatures[i]);
				System.out.println(nnList[i][j].neighbor + " : " + basicFeatures[nnList[i][j].neighbor]);
				System.exit(0);
			}
		NNListAndNG[] records = new NNListAndNG[docNums.length];
		for (int i = 0; i < docNums.length; i++) { 
			records[i] = new NNListAndNG(nnList[i], calcNg(nnList[i], ngRadius));
		}
		boolean[] clustered = new boolean[docNums.length];
		for (int i = 0; i < docNums.length; i++) if (!clustered[i]) 
			cluster(docNums, records, i, tsn, output, clustered, maxClusterSize);
	}

	private static void getBasicFeatures(int[] docNums, String[][] basicFeatures) throws Exception {
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		for (int i = 0; i < docNums.length; i++) basicFeatures[i] = 
			Common.sortUnique(ireader.document(docNums[i]).get("basic"), 0);
		ireader.close();
	}

	/**
	 * jaccard distance
	 * @param basicFeatures
	 * @param i
	 * @param j
	 * @return
	 * @throws Exception
	 */
	private static float jaccard(String[][] features, int i, int j) {
		String[] r1 = features[i];
		String[] r2 = features[j];
		HashSet<String> tokenSet = new HashSet<String>();
		for (String s : r1) tokenSet.add(s);
		for (String s : r2) tokenSet.add(s);
		int intersection = r1.length+r2.length-tokenSet.size();
		float ret = (float)(tokenSet.size()-intersection+0.0)/tokenSet.size();
		return ret;
	}

	private static float cosine(String[][] features, int i, int j) {
		String[] r1 = features[i];
		String[] r2 = features[j];
		HashSet<String> tokenSet = new HashSet<String>();
		for (String s : r1) tokenSet.add(s);
		for (String s : r2) tokenSet.add(s);
		int intersection = r1.length+r2.length-tokenSet.size();
		return intersection/(float)Math.sqrt(r1.length)/(float)Math.sqrt(r2.length);
	}
	
	private static float calcNg(NN[] nn, int radius) {
		float nnDistance = nn[1].distance;
		int i;
		for (i = 2; i < nn.length; i++) if (nn[i].distance > radius*nnDistance) break;
		return i-1;
	}

	/**
	 * try to cluster i and its nearest neighbors
	 * @param nnList
	 * @param i
	 * @param tsn
	 * @param output
	 * @param clustered
	 */
	private static void cluster(int[] docNums, NNListAndNG[] records, int i, float tsn, String output,
			boolean[] clustered, int maxClusterSize) throws Exception {
		NNListAndNG theOne = records[i];
		HashSet<Integer> currentSet = new HashSet<Integer>();
		for (int j = 0; j < theOne.nnList.length && j < maxClusterSize; j++) currentSet.add(theOne.nnList[j].neighbor);
		for (int j = currentSet.size()-1; j >= 1; j--) {
			float avgNgv = avgNg(records, currentSet);
			if (isCompactSet(records, currentSet) && 
					avgNgv < tsn) {
//				PrintWriter ng = IOFactory.getPrintWriter("/usr/fulinyun/ng.txt", true);
//				ng.println(avgNgv);
//				ng.close();
				PrintWriter pw = IOFactory.getPrintWriter(output, true);
				boolean first = true;
				for (Integer ind : currentSet) {
					if (first) {
						pw.print(docNums[ind]);
						first = false;
					} else pw.print(" " + docNums[ind]);
				}
				pw.println();
				pw.close();
				for (Integer ind : currentSet) clustered[ind] = true;
				break;
			}
			currentSet.remove(theOne.nnList[j].neighbor);
		}
	}

	private static boolean isCompactSet(NNListAndNG[] records,
			HashSet<Integer> currentSet) {
		for (Integer ind : currentSet) {
			NN[] nn = records[ind].nnList;
			for (int i = 0; i < currentSet.size(); i++) if (!currentSet.contains(nn[i].neighbor)) return false;
		}
		return true;
	}

	/**
	 * the average neighborhood growth of the first j+1 elements in nnList
	 * @param records
	 * @param nnList
	 * @param j
	 * @return
	 */
	private static float avgNg(NNListAndNG[] records, HashSet<Integer> currentSet) {
		float ret = 0;
		for (Integer ind : currentSet) ret += records[ind].ng;
		ret /= currentSet.size();
		return ret;
	}

	/**
	 * is this two nnList the same for the first j+1 elements?
	 * @param nnList
	 * @param nnList2
	 * @param j
	 * @return
	 */
	private static boolean equalSet(NN[] nnList1, NN[] nnList2, int j) {
		HashSet<Integer> nnSet1 = new HashSet<Integer>();
		for (int i = 0; i <= j; i++) nnSet1.add(nnList1[i].neighbor);
		for (int i = 0; i <= j; i++) if (!nnSet1.contains(nnList2[i].neighbor)) return false;
		return true;
	}
	
	public static void pickThreshold(String input, String output) throws Exception {
		BufferedReader br = IOFactory.getBufferedReader(input);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (!line.equals("1.0")) {
				System.out.println(line);
				pw.println(line);
			}
		}
		pw.close();
		br.close();
	}
	
//	/**
//	 * based on the input blocking results and basic feature index, calculate clusters
//	 * @param input is the output of the ssjoin program
//	 * @param output one basic feature per line, format: doc# \t basicFeature 
//	 * @throws Exception
//	 */
//	public static void dumpBasicFeature(String input, String output) throws Exception {
//		IndexReader ireader = IndexReader.open(Indexer.lap3index);
//		IDataSourceReader br = IOFactory.getReader(input);
//		HashSet<Integer> docNumSet = new HashSet<Integer>();
//		for (String line = br.readLine(); line != null; line = br.readLine()) {
//			String[] parts = line.split(" ");
//			
//			// get document numbers
//			int ix = Integer.parseInt(parts[0])-1;
//			int iy = Integer.parseInt(parts[1])-1;
//			docNumSet.add(ix);
//			docNumSet.add(iy);
//		}
//		br.close();
//		PrintWriter pw = IOFactory.getPrintWriter(output);
//		for (Integer i : docNumSet) {
//			// get and write doc# & basic features
//			pw.println(i.intValue() + "\t" + ireader.document(i).get("basic"));
//		}
//		pw.close();
//	}
	
}

class NN {
	public int neighbor;
	public float distance;
	public NN(int neighbor, float distance) {
		this.neighbor = neighbor;
		this.distance = distance;
	}
}

class NNListAndNG {
	public NN[] nnList;
	public float ng;
	public NNListAndNG(NN[] nnList, float ng) {
		this.nnList = nnList;
		this.ng = ng;
	}
}

interface ISimCal {
	public float distance(String[][] features, int i, int j);
}
