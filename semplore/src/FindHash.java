import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import com.ibm.semplore.imports.impl.data.load.NTReader;
import com.ibm.semplore.imports.impl.data.load.TarGzReader;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.imports.impl.data.load.WarcReader;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;


/**
 * @author lql
 * 
 */
public class FindHash {
	static Md5_BloomFilter_64bit md5;
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
		String[] allowed = new String[] { ".nt", ".warc", ".nt.tar.gz" };
		for (int i = 0; i < allowed.length; i++) {
			if (filename.contains(allowed[i]) && allowed[i].equals(filename.substring(filename.length()
					- allowed[i].length(), filename.length())))
				return allowed[i];
		}
		return null;
	}

	public static void processTripleLine(String line) {
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
			if (tripletype == Util4NT.ATTRIBUTE && j==2) break;
			boolean dup = md5.set(triple[j]);
			hash[j] = md5.getLongID_set();
//			if (!dup) {
				if (hash[j] == -1507415458100823075l)
					//	hash[j] == -1193840609352242298l)
					//	hash[j] == -485247751636416734l)
//						hash[j] == -9218031709951924220l ||
//						hash[j] == -9217978834297518856l ||
//						hash[j] == -9213759900972097213l ||
//						hash[j] == -9213560444989569268l ||
//						hash[j] == -9211684749309885691l ||
//						hash[j] == -9211081341593796185l ||
//						hash[j] == -9210887599882462596l)
					System.out.println(j+" "+line);
// 			}
		}
//		System.out.println("triple: "+triple[0]+"\t"+triple[1]+"\t"+triple[2]);
		triple_count++;
		if (triple_count%1000000==0) {
			System.out.println("count: "+triple_count+" time: "+(System.currentTimeMillis()-time1)+" ms");
		}
	}


	public static void main(String[] args) throws Exception {
		System.out.println("Initializing Dictionary");
		md5 = new Md5_BloomFilter_64bit();
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
		String nt_dir = args[0];
		long time_b = System.currentTimeMillis();
		time1 = time_b;
		try {
			System.out.println("input dir: " + nt_dir);
			File dir = new File(nt_dir);
			try {
				File[] files = dir.listFiles();
				for (int j = 0; j < files.length; j++) {
					File fin = files[j];
					String filetype= checkFileType(fin.getName());
					if (filetype==null)
						continue;
					System.out.println("processing file: "+fin);
					if (filetype.equals(".warc"))
						(new WarcReader(FindHash.class)).main(new String[]{fin.getPath()});
					if (filetype.equals(".nt.tar.gz"))
						(new TarGzReader(FindHash.class)).main(new String[]{fin.getPath()});
					if (filetype.equals(".nt"))
						(new NTReader(FindHash.class)).main(new String[]{fin.getPath()});
					System.out.println("input file: " + fin + "triple_count: " + triple_count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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
