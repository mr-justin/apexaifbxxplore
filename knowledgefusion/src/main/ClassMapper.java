package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import basic.IOFactory;

/**
 * provide input files for class clusterer
 * @author fulinyun
 *
 */
public class ClassMapper {

	public static String workFolder = "e:\\user\\fulinyun\\classCluster\\";
	
	public static void main(String[] args) throws Exception {
//		collectInstancePre(Indexer.indexFolder+"classIndTemp.txt"); // done
//		collectInstanceFromClassInfo(Indexer.indexFolder+"classIndTemp.txt", Indexer.indexFolder+"classInd.txt"); // done
//		collectInstance(Indexer.indexFolder+"nonNullClass.txt", 
//				Indexer.indexFolder+"nonNullClassInd.txt"); // to run
//		findIndWith2Classes(Indexer.indexFolder+"classInd.txt"); // done
//		indClusterFeatureExtraction(Blocker.workFolder+"prefix0.2cluster2&1.1.txt", 
//				Indexer.indexFolder+"classInd.txt", Indexer.indexFolder+"clusterClass.txt"); // running
		findClassOutsideDBpedia(workFolder+"cluster.txt", workFolder+"nonDBpediaClass.txt");
	}
	
	public static void findClassOutsideDBpedia(String clusterFile, String output) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(clusterFile));
		IndexReader ireader = IndexReader.open(Indexer.basicFeatureIndex);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			if (parts[1].equals("-1")) {
				String uri = ireader.document(Integer.parseInt(parts[0])).get("URI");
				if (!uri.contains(KeyIndDealer.domainDBpedia)) pw.println(parts[0] + " " + uri);
			}
		}
		pw.close();
		br.close();
		ireader.close();
	}
	
	public static void collectInstance(String classList, String output) throws Exception {
		IndexReader ireader = IndexReader.open(Indexer.refIndex);
		TreeMap<Integer, HashSet<Integer>> ret = new TreeMap<Integer, HashSet<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(classList));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int i = Integer.parseInt(line);
			Document doc = ireader.document(i);
			String[] instances = doc.getValues(Common.dbpediaSubject+"from");
			String[] subclasses = doc.getValues(Common.dbpediaSubclass+"from");
			String[] instances1 = doc.getValues(Common.rdfType+"from");
			String[] instances2 = doc.getValues(Common.owlClass+"from");
			String[] all = new String[instances.length+subclasses.length+
			                          instances1.length+instances2.length];
			if (all.length != 0) {
				if (ret.containsKey(i)) {
					for (String uri : all) {
						try {
							TermDocs td = ireader.termDocs(new Term("URI", uri));
							td.next();
							ret.get(i).add(td.doc());
						} catch (Exception e) {
							//System.out.print("*");
						}
					}
				} else {
					HashSet<Integer> docNumSet = new HashSet<Integer>();
					for (String uri : all) {
						try {
							TermDocs td = ireader.termDocs(new Term("URI", uri));
							td.next();
							docNumSet.add(td.doc());
						} catch (Exception e) {
							//System.out.print("*");
						}
					}
					ret.put(i, docNumSet);
				}
			}
			if ((i+1)%10000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
		}
		br.close();
		System.out.println(new Date().toString() + " : read all!");
		boolean cont = true;
		int maxLap = 1000;
		int lap = 0;
		while (cont && lap < maxLap) {
			cont = false;
			for (Integer key : ret.keySet()) {
				HashSet<Integer> containList = ret.get(key);
				for (Integer i : containList) if (ret.containsKey(i)) {
					if (containList.addAll(ret.get(i)))	{
						cont = true;
						break;
					}
				}
			}
			lap++;
			System.out.println(new Date().toString() + " : " + lap + " laps finished");
		}
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer key : ret.keySet()) {
			pw.print(key.intValue());
			for (Integer value : ret.get(key)) if (!ret.containsKey(value)) pw.print(" " + value);
			pw.println();
		}
		pw.close();
	}

	public static void collectInstancePre(String output) throws Exception {
		IndexReader ireader = IndexReader.open(Indexer.refIndex);
		TreeMap<Integer, HashSet<Integer>> ret = new TreeMap<Integer, HashSet<Integer>>();
		for (int i = 0; i < ireader.maxDoc(); i++) {
			Document doc = ireader.document(i);
			String[] instances = doc.getValues(Common.dbpediaSubject+"from");
			String[] subclasses = doc.getValues(Common.dbpediaSubclass+"from");
			String[] instances1 = doc.getValues(Common.rdfType+"from");
			String[] instances2 = doc.getValues(Common.owlClass+"from");
			String[] all = new String[instances.length+subclasses.length+
			                          instances1.length+instances2.length];
			int allcount = 0;
			for (String s : instances) {
				all[allcount] = s;
				allcount++;
			}
			for (String s : subclasses) {
				all[allcount] = s;
				allcount++;
			}
			for (String s : instances1) {
				all[allcount] = s;
				allcount++;
			}
			for (String s : instances2) {
				all[allcount] = s;
				allcount++;
			}
			if (all.length != 0) {
				if (ret.containsKey(i)) {
					for (String uri : all) {
						try {
							TermDocs td = ireader.termDocs(new Term("URI", uri));
							td.next();
							ret.get(i).add(td.doc());
						} catch (Exception e) {
							System.out.print("*");
						}
					}
				} else {
					HashSet<Integer> docNumSet = new HashSet<Integer>();
					for (String uri : all) {
						try {
							TermDocs td = ireader.termDocs(new Term("URI", uri));
							td.next();
							docNumSet.add(td.doc());
						} catch (Exception e) {
							System.out.print("*");
						}
					}
					ret.put(i, docNumSet);
				}
			}
			if ((i+1)%100000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
		}
		System.out.println(new Date().toString() + " : read all!");
		System.out.println(ret.size() + " classes in all");
		// write preprocess result
		writeClassInd(ret, output);
		
	}

	/**
	 * 
	 * @param classInfo record class information, one class per line, class ID followed by 
	 * its individuals and subclasses, be aware of cycles!
	 * @param output
	 * @throws Exception
	 */
	public static void collectInstanceFromClassInfo(String classInfo, 
			String output) throws Exception {
		
		TreeMap<Integer, HashSet<Integer>> ret = getClassInfo(classInfo);
		
		// recursive retrieval & result writing
		PrintWriter pw = IOFactory.getPrintWriter(output);
		int count = 0;
		for (Integer key: ret.keySet()) {
			HashSet<Integer> cycleLog = new HashSet<Integer>();
			cycleLog.add(key);
			HashSet<Integer> indSet = getAllInd(ret, key, cycleLog);
			pw.print(key.intValue());
			for (Integer i : indSet) pw.print(" " + i.intValue());
			pw.println();
			count++;
			if (count%10000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		pw.close();
	}
	
	private static TreeMap<Integer, HashSet<Integer>> getClassInfo(
			String classInfo) throws Exception {
		TreeMap<Integer, HashSet<Integer>> ret = new TreeMap<Integer, HashSet<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(classInfo));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int key = Integer.parseInt(parts[0]);
			HashSet<Integer> value = new HashSet<Integer>();
			for (int i = 1; i < parts.length; i++) value.add(Integer.parseInt(parts[i]));
			ret.put(key, value);
		}
		br.close();
		return ret;
	}

	/**
	 * get all instances of class key, based on its individuals and subclasses and subclasses of subclasses...
	 * @param classInfo
	 * @param key
	 * @param cycleLog 
	 * @return
	 * @throws Exception
	 */
	private static HashSet<Integer> getAllInd(
			TreeMap<Integer, HashSet<Integer>> classInfo, int key, HashSet<Integer> cycleLog) throws Exception {
		HashSet<Integer> containList = classInfo.get(key);
		HashSet<Integer> ret = new HashSet<Integer>();
		for (Integer i : containList) {
			if (cycleLog.contains(i)) continue;
			else if (classInfo.containsKey(i)) {
				cycleLog.add(i);
				HashSet<Integer> part = getAllInd(classInfo, i, cycleLog); // recursion
				ret.addAll(part);
				cycleLog.remove(i);
			} else ret.add(i);
		}
		return ret;
	}

	private static void writeClassInd(TreeMap<Integer, HashSet<Integer>> ret,
			String output) throws Exception {
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (Integer key : ret.keySet()) {
			pw.print(key.intValue());
			for (Integer value : ret.get(key)) if (!ret.containsKey(value)) pw.print(" " + value);
			pw.println();
		}
		pw.close();
	}

	/**
	 * extract class features, which are sets of individual clusters
	 * @param indCluster individual clusters, one per line, numbered from 0
	 * @param classInd class owned individuals, one class per line, first class doc# then a list of 
	 * individual doc#s
	 * @param output class features, one per line, first class doc# then a list of cluster#s (classCluster)
	 */
	public static void classFeatureExtraction(String indCluster, String classInd, 
			String output) throws Exception {
		ArrayList<HashSet<Integer>> indclusters = new ArrayList<HashSet<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(indCluster));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			HashSet<Integer> cluster = new HashSet<Integer>();
			for (String s : parts) cluster.add(Integer.parseInt(s));
			indclusters.add(cluster);
		}
		br.close();
		br = new BufferedReader(new FileReader(classInd));
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			pw.print(parts[0]); // class doc#
			for (int n = 0; n < indclusters.size(); n++) {
				HashSet<Integer> cluster = indclusters.get(n);
				int count = 0;
				for (int i = 1; i < parts.length; i++) if (cluster.contains(Integer.parseInt(parts[i]))) 
					count++;
				if (count != 0) pw.print(" " + n + " " + (count+0.0)/(parts.length-1));
			}
			pw.println();
		}
		br.close();
		pw.close();
	}
	
	/**
	 * find whether classes share individuals; due to the existence of subclasses, they do!
	 * @param classInd
	 * @throws Exception
	 */
	public static void findIndWith2Classes(String classInd) throws Exception {
		HashSet<Integer> indSet = new HashSet<Integer>();
		BufferedReader br = new BufferedReader(new FileReader(classInd));
		int count = 0;
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			for (int i = 1; i < parts.length; i++) {
				int indID = Integer.parseInt(parts[i]);
				if (indSet.contains(indID)) count++;
				indSet.add(indID);
			}
			lineCount++;
			if (lineCount%10000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
		}
		System.out.println(count);
	}
	
	/**
	 * extract individual cluster features, which are sets of classes they belong to, along with cluster-class 
	 * association coefficients
	 * @param indCluster individual clusters, one per line, numbered from 0
	 * @param classInd class owned individuals, one class per line, first class doc# then a list of 
	 * individual doc#s
	 * @param output class features, one per line, first class doc# then a list of cluster#s (classCluster)
	 */
	public static void indClusterFeatureExtraction(String indCluster, String classInd, 
			String output) throws Exception {
		HashMap<Integer, ArrayList<Integer>> indClass = new HashMap<Integer, ArrayList<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(classInd));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			int classID = Integer.parseInt(parts[0]);
			for (int i = 1; i < parts.length; i++) {
				int indID = Integer.parseInt(parts[i]);
				if (indClass.containsKey(indID)) indClass.get(indID).add(classID);
				else {
					ArrayList<Integer> classList = new ArrayList<Integer>();
					classList.add(classID);
					indClass.put(indID, classList);
				}
			}
		}
		br.close();
		System.out.println(new Date().toString() + " : ind-class information obtained");
		
		br = new BufferedReader(new FileReader(indCluster));
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(" ");
			HashMap<Integer, Integer> classOwnCount = new HashMap<Integer, Integer>();
			for (String s : parts) {
				int indID = Integer.parseInt(s);
				if (indClass.containsKey(indID)) {
					ArrayList<Integer> classList = indClass.get(indID);
					for (Integer c : classList) {
						if (classOwnCount.containsKey(c)) classOwnCount.put(c, classOwnCount.get(c)+1);
						else classOwnCount.put(c, 1);
					}
				}
			}
			boolean first = true;
			for (Integer c : classOwnCount.keySet()) {
				float coefficient = (float)(classOwnCount.get(c)+0.0)/parts.length;
				if (first) {
					pw.print(c + " " + coefficient);
					first = false;
				} else {
					pw.print(" " + c + " " + coefficient);
				}
			}
			pw.println();
		}
		br.close();
		pw.close();
	}
	
}
