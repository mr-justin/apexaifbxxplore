package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import org.apache.lucene.index.IndexReader;

import basic.IDataSourceReader;
import basic.IOFactory;

public class CosClusterer {
	
	public static String workFolder = "/usr/fulinyun/clusterer/";
	public static String classFolder = "/usr/fulinyun/classCluster/";
	
	public static void main(String args[]) throws Exception {
		cluster(Blocker.workFolder+"nonNullIndBlocks0.2&100.txt", workFolder+"cluster0.2&100&2.5.txt", 
				2, 2.5f, 4);
		Clusterer.evaluateWithDomain(workFolder+"cluster0.2&100&2.5.txt", Indexer.indexFolder+"sameAsID.txt", 
				workFolder+"clusterPR/cluster0.2&100&2.5domainEval.txt");
	}
	
	public static void cluster(String input, String output, int ngRadius, float tsn, int maxClusterSize) throws Exception {
		new File(output).delete();
		IDataSourceReader br = IOFactory.getReader(input);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] records = line.split(" ");
//			if (records.length > 10) System.out.println(records.length);
			int[] docNums = new int[records.length];
			for (int i = 0; i < records.length; i++) docNums[i] = Integer.parseInt(records[i]);
			cluster(docNums, output, ngRadius, tsn, maxClusterSize);
			count++;
			if (count%100000 == 0) System.out.println(new Date().toString() + " : " + count + " blocks");
		}
		br.close();
		System.out.println(new Date().toString() + " : clustering finished");
	}
	
	private static void cluster(int[] docNums, String output, int ngRadius, float tsn, int maxClusterSize) throws Exception {
		String[][] basicFeatures = new String[docNums.length][];
		getBasicFeatures(docNums, basicFeatures);
		NN[][] nnList = new NN[docNums.length][docNums.length];
		for (int i = 0; i < docNums.length; i++) for (int j = i+1; j < docNums.length; j++) {
			nnList[i][j] = new NN(j, cosine(basicFeatures, i, j));
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
	
	private static void cluster(int[] docNums, NNListAndNG[] records, int i, float tsn, String output,
			boolean[] clustered, int maxClusterSize) throws Exception {
		NNListAndNG theOne = records[i];
		HashSet<Integer> currentSet = new HashSet<Integer>();
		for (int j = 0; j < theOne.nnList.length && j < maxClusterSize; j++) currentSet.add(theOne.nnList[j].neighbor);
		for (int j = currentSet.size()-1; j >= 1; j--) {
			if (isCompactSet(records, currentSet) && 
					avgNg(records, currentSet) < tsn) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
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
	
	private static void getBasicFeatures(int[] docNums, String[][] basicFeatures) throws Exception {
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		for (int i = 0; i < docNums.length; i++) basicFeatures[i] = 
			Common.sortUnique(ireader.document(docNums[i]).get("basic"), 0);
		ireader.close();
	}
	
	private static float calcNg(NN[] nn, int radius) {
		float nnDistance = nn[1].distance;
		int i;
		for (i = 2; i < nn.length; i++) if (nn[i].distance > radius*nnDistance) break;
		return i-1;
	}
	
	private static boolean isCompactSet(NNListAndNG[] records,
			HashSet<Integer> currentSet) {
		for (Integer ind : currentSet) {
			NN[] nn = records[ind].nnList;
			for (int i = 0; i < currentSet.size(); i++) if (!currentSet.contains(nn[i].neighbor)) return false;
		}
		return true;
	}
	
	private static float avgNg(NNListAndNG[] records, HashSet<Integer> currentSet) {
		float ret = 0;
		for (Integer ind : currentSet) ret += records[ind].ng;
		ret /= currentSet.size();
		return ret;
	}
	
	private static float cosine(String[][] basicFeatures, int i, int j) throws Exception {
		String[] r1 = basicFeatures[i];
		String[] r2 = basicFeatures[j];
		HashSet<String> tokenSet = new HashSet<String>();
		for (String s : r1) tokenSet.add(s);
		for (String s : r2) tokenSet.add(s);
		int intersection = r1.length + r2.length - tokenSet.size();
		float ret = 1 - (float)(intersection+0.0)/(r1.length * r2.length);
		return ret;
	}
	
	static class NN {
		public int neighbor;
		public float distance;
		public NN(int neighbor, float distance) {
			this.neighbor = neighbor;
			this.distance = distance;
		}
	}

	static class NNListAndNG {
		public NN[] nnList;
		public float ng;
		public NNListAndNG(NN[] nnList, float ng) {
			this.nnList = nnList;
			this.ng = ng;
		}
	}
	
}
