package com.ibm.semplore.imports.impl.data.load;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import com.ibm.semplore.util.Md5_BloomFilter_64bit;
import com.ibm.semplore.util.TestUnicode;


/**
 * @author lql
 * 
 */
public class Split1ntTo3nt_btc {
	public static String catFile;
	public static String relInsFile;
	public static String relFile;
	public static String hashdataFile;
	public static int triple_count;
	public static long time1;

	private static ArrayList<Pair> sort(Hashtable<String, Pair> count_cats) {
		ArrayList<Pair> list = new ArrayList( count_cats.values());
		Collections.sort( list );
		return list;
	}

	public static String checkFileType(String filename) {
		String[] allowed = new String[] { ".nt", ".warc", ".nt.tar.gz", ".nt.gz", ".nt.zip" };
		for (int i = 0; i < allowed.length; i++) {
			if (filename.contains(allowed[i]) && allowed[i].equals(filename.substring(filename.length()
					- allowed[i].length(), filename.length())))
				return allowed[i];
		}
		return null;
	}

	public static void processTripleLine(String line) throws NoSuchAlgorithmException {
		// check nt gramma
//		System.out.println(line);
		String[] triple = line.replaceAll("\t", " ").split(" ");
		int i = 3;
		while (triple.length > i) {
			if (triple[i].equals(".")) // TODO sxr:what if attribute contains a " . " in the middle? 
				break;
			triple[2] = triple[2] + " " + triple[i];
			i++;
		}
		if (triple.length < 3)
			return;
		line = triple[0] + "\t" + triple[1] + "\t" + triple[2] + "\t.";
		triple = line.split("\t");
		// check nt gramma

		String tripletype = Util4NT.checkTripleType(triple);
		if (tripletype == null)
			return;

		long[] hash = new long[3];
		for (int j=0; j<3; j++) {
			if (tripletype == Util4NT.ATTRIBUTE && j==2) triple[j] = TestUnicode.parse(triple[j]);
			hash[j] = Md5_BloomFilter_64bit.URItoID(triple[j]);
			//this URI appears first time
			outh.println(String.format("%d\tURI\t%d\t%s", hash[j], Md5_BloomFilter_64bit.HASH_URI, triple[j]));
			if (j==0) {	//assume all subjects are at first instances
				outh.println(String.format("%d\tTYPE\t%d\t%d", hash[j], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_INSTANCE));
			}
		}
		if (tripletype == Util4NT.CATEGORY) {// category
			catInsSize++;
			outh.println(String.format("%d\t%s\t%d\t%d", hash[0], tripletype, hash[1], hash[2]));
			outh.println(String.format("%d\tTYPE\t%d\t%d", hash[1], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_INSTANCE));
			outh.println(String.format("%d\tTYPE\t%d\t%d", hash[2], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_CATEGORY));
			out1.println(hash[2]+"\t.");
//			out1.println(line);
			if (!eg_cats.containsKey(triple[2])) {
				count_cats.put(triple[2], new Pair(triple[2], 1));
				eg_cats.put(triple[2], triple[0]);
			} else {
				Pair tmp = count_cats.get(triple[2]);
				tmp.v ++;
				count_cats.put(triple[2], tmp);
			}
		} else if (tripletype == Util4NT.RELATION) {// relation
			relInsSize++;
			outh.println(String.format("%d\t%s\t%d\t%d", hash[0], tripletype, hash[1], hash[2]));
			outh.println(String.format("%d\t%s\t%d\t%d", hash[2], Util4NT.INVRELATION, hash[1], hash[0]));
			outh.println(String.format("%d\tTYPE\t%d\t%d", hash[2], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_INSTANCE));
			outh.println(String.format("%d\tTYPE\t%d\t%d", hash[1], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_RELATION));
			out2.println(String.format("%d\t%d\t%d", hash[0], hash[1], hash[2]));
			out3.println(hash[1]+"\t.");
//			out2.println(line);
			if (!eg_rels.containsKey(triple[1])) {
				count_rels.put(triple[1], new Pair(triple[1], 1));
				eg_rels.put(triple[1], triple[2]);
			} else {
				Pair tmp = count_rels.get(triple[1]);
				tmp.v ++;
				count_rels.put(triple[1], tmp);
			}
		} else if (tripletype == Util4NT.ATTRIBUTE) {// attribute
			attrInsSize++;
			outh.println(String.format("%d\t%s\t%s\t%s", hash[0], tripletype, hash[1], hash[2]));
			outh.println(String.format("%d\tTYPE\t%d\t%d", hash[1], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_RELATION));
			outh.println(String.format("%d\tTYPE\t%d\t%d", hash[2], Md5_BloomFilter_64bit.HASH_TYPE, Md5_BloomFilter_64bit.HASH_TYPE_ATTRIBUTE));
			out2.println(String.format("%d\t%d\t%d", hash[0], hash[1], hash[2]));
			out3.println(hash[1]+"\t.");
//			out3.println(line);
			if (!eg_attrs.containsKey(triple[1])) {
				count_attrs.put(triple[1], new Pair(triple[1], 1));
				eg_attrs.put(triple[1], triple[2]);
			} else {
				Pair tmp = count_attrs.get(triple[1]);
				tmp.v ++;
				count_attrs.put(triple[1], tmp);
			}
		}
//		System.out.println("triple: "+triple[0]+"\t"+triple[1]+"\t"+triple[2]);
		triple_count++;
		if (triple_count%1000000==0) {
			System.out.println("count: "+triple_count+" time: "+(System.currentTimeMillis()-time1)+" ms");
		}
	}

	static int instanceSize = 0;
	static int relInsSize = 0;
	static int catInsSize = 0;
	static int attrInsSize = 0;
//	static HashSet<String> cats = new HashSet<String>();
//	static HashSet<String> rels = new HashSet<String>();
//	static HashSet<String> attrs = new HashSet<String>();
	static Hashtable<String, String> eg_rels = new Hashtable<String, String>();
	static Hashtable<String, String> eg_cats = new Hashtable<String, String>();
	static Hashtable<String, String> eg_attrs = new Hashtable<String, String>();
	static Hashtable<String, Pair> count_rels = new Hashtable<String, Pair>();
	static Hashtable<String, Pair> count_cats = new Hashtable<String, Pair>();
	static Hashtable<String, Pair> count_attrs = new Hashtable<String, Pair>();
	static PrintStream out1;
	static PrintStream out2;
	static PrintStream out3;
	static PrintStream outh;

	public static void main(String nt_dir, String catFile,
			String relInsFile, String relFile, String hashdataFile) throws Exception {
		Split1ntTo3nt_btc.catFile = catFile;
		Split1ntTo3nt_btc.relInsFile = relInsFile;
		Split1ntTo3nt_btc.relFile = relFile;
		Split1ntTo3nt_btc.hashdataFile = hashdataFile;

		/** **************set specific Util4NT properties*************** */
		// for dbpedia
		// Util4NT.setTYPE("<http://www.w3.org/2004/02/skos/core#subject>");
		// Util4NT.setNameSpace("<http://dbpedia.org/resource/Category:",
		// "<http://dbpedia.org/property/", "<http://dbpedia.org/resource/",
		// "");
		// for tap
		// Util4NT.setTYPE("rel_typeOf");
		// Util4NT.setNameSpace("cat_", "rel_", "ins_", "");
		/** **************set specific Util4NT properties*************** */

		long time_b = System.currentTimeMillis();
		time1 = time_b;
		try {
			System.out.println("input dir: " + nt_dir);
			File dir = new File(nt_dir);
			out1 = new PrintStream(new BufferedOutputStream(new FileOutputStream(catFile)));
			out2 = new PrintStream(new BufferedOutputStream(new FileOutputStream(relInsFile)));
			out3 = new PrintStream(new BufferedOutputStream(new FileOutputStream(relFile)));
			outh = new PrintStream(new BufferedOutputStream(new FileOutputStream(hashdataFile)));
			try {
				File[] files = dir.listFiles();
				for (int j = 0; j < files.length; j++) {
					File fin = files[j];
					String filetype= checkFileType(fin.getName());
					if (filetype==null)
						continue;
					System.out.println("processing file: "+fin);
					if (filetype.equals(".warc"))
						(new WarcReader(Split1ntTo3nt_btc.class)).main(new String[]{fin.getPath()});
					else if (filetype.equals(".nt.tar.gz"))
						(new TarGzReader(Split1ntTo3nt_btc.class)).main(new String[]{fin.getPath()});
					else if (filetype.equals(".nt.gz"))
						(new GZReader(Split1ntTo3nt_btc.class)).main(new String[]{fin.getPath()});
					else if (filetype.equals(".nt.zip"))
						(new ZipReader(Split1ntTo3nt_btc.class)).main(new String[]{fin.getPath()});
					else if (filetype.equals(".nt"))
						(new NTReader(Split1ntTo3nt_btc.class)).main(new String[]{fin.getPath()});
					System.out.println("input file: " + fin + "triple_count: " + triple_count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

				out1.close();
				out2.close();
				out3.close();
				outh.close();

				ArrayList<Pair> sorted_cats = sort(count_cats);
				ArrayList<Pair> sorted_rels = sort(count_rels);
				ArrayList<Pair> sorted_attrs = sort(count_attrs);

				String fout = dir + File.separator + "stats";
				System.out.println("output file: " + catFile);
				System.out.println("output file: " + relInsFile);
				System.out.println("output file: " + relFile);
				System.out.println("output file: " + fout);
				PrintStream out = new PrintStream(fout);
				out.println("instance size: " + instanceSize);
				out.println("category size: " + sorted_cats.size());
				out.println("category instance size: " + catInsSize);
				out.println("relation size: " + sorted_rels.size());
				out.println("relation instance size: " + relInsSize);
				out.println("attribute size: " + sorted_attrs.size());
				out.println("attribute instance size: " + attrInsSize);
				out.println("=====category=====");
				for (Pair p:sorted_cats) {
					out.println(p.k + " [" + p.v + "] ("
							+ eg_cats.get(p.k) + ")");
				}
				out.println("=====category=====");
				out.println("=====relation=====");
				for (Pair p:sorted_rels) {
					out.println(p.k + " [" + p.v + "] ("
							+ eg_rels.get(p.k) + ")");
				}
				out.println("=====relation=====");
				out.println("=====attribute=====");
				for (Pair p:sorted_attrs) {
					out.println(p.k + " [" + p.v + "] ("
							+ eg_attrs.get(p.k) + ")");
				}
				out.println("=====attribute=====");
				out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long time_e = System.currentTimeMillis();
		System.out.println("time: " + (time_e - time_b) + " ms");
	}
}

class Pair implements Comparable {
	public String k;
	public Integer v;
	public Pair(String k, Integer v) {
		this.k = k; this.v = v;
	}
	public int compareTo(Object o) {
		return ((Pair)o).v - v;
	}
}
