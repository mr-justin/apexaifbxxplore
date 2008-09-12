
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.law.warc.filters.Filter;
import it.unimi.dsi.law.warc.filters.Filters;
import it.unimi.dsi.law.warc.io.GZWarcRecord;
import it.unimi.dsi.law.warc.io.WarcFilteredIterator;
import it.unimi.dsi.law.warc.io.WarcRecord;
import it.unimi.dsi.law.warc.util.BURL;
import it.unimi.dsi.law.warc.util.WarcHttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import com.ice.tar.TarInputStream;


public class LocalNameMapper {
	
	public static final String rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	public static final String owlProperty = "<http://www.w3.org/2002/07/owl#ObjectProperty>";
	public static final String owlClass = "<http://www.w3.org/2002/07/owl#Class>";
	public static final String rdfProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>";
	
	/**
	 * Find information about belonging classes of instances from .nt.tar.gz file
	 * @param input is the name of the input .nt.tar.gz file
	 * @param dump is the name of the output file
	 * @throws Exception
	 */
	public static void findSchemaElementsFromTarGZ(String input, String dump) throws Exception {
		System.out.println("Begin processing " + input);
		GZIPInputStream gzinput = new GZIPInputStream(new FileInputStream(input));
		TarInputStream targzinput = new TarInputStream(gzinput);
		PrintWriter pw = new PrintWriter(new FileWriter(dump));
		int lineCount = 0;
		while (targzinput.getNextEntry()!= null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(targzinput));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] part = line.split(" ");
				if (part.length < 4) {
					lineCount++;
					System.out.println(lineCount + " : " + line);
					continue;
				}
				if (part[1].equals(rdfType)) {
					pw.println(line);
					pw.flush();
				}
				lineCount++;
				if (lineCount%1000 == 0) System.out.println(lineCount);
			}
		}
		pw.close();
	}

	/**
	 * Find instance mapping between DBpedia and DBLP datasets based on local name only
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void mapDBpediaDBLP(String output) throws Exception {
		String source1 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\dblpElements.txt";
		String source2 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\yago_typeOf.nt";
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadDBpediaMap(source2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 100000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = getLocalNameFromDblp(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
	}
	
	/**
	 * Create two hash maps mapping local names to original URIs and to classes they belong to
	 * @param source2 is the name of the file that records which class each DBpedia instance belongs to
	 * @param localNameMap is the map used to be filled with mapping from local names to original URIs
	 * @return a hash map that stores mapping between local names to class it belongs to 
	 * @throws Exception
	 */
	private static HashMap<String, String> loadDBpediaMap(String source2,
			HashMap<String, String> localNameMap) throws Exception {
		System.out.println(new Date().toString() + " begin loading yago");
		BufferedReader br = new BufferedReader(new FileReader(source2));
		HashMap<String, String> ret = new HashMap<String, String>();
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			String localName = getLocalNameFromDblp(part[0]);
			ret.put(localName, part[2]);
			localNameMap.put(localName, part[0]);
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
		}
		return ret;
	}

	/**
	 * Get normalized name from DBLP URIs
	 * @param name is the URI from DBLP
	 * @return its normalized form
	 * @throws Exception
	 */
	public static String getLocalNameFromDblp(String name) throws Exception {
		String tmp = null;
		
		int pos = name.lastIndexOf("#");
		if(pos == -1) {
			pos = name.lastIndexOf("/");
			tmp = name.substring(pos + 1,name.length()-1);
		}
		else {
			tmp = name.substring(pos + 1,name.length()-1);
		}
		
		if (tmp.endsWith(".html")) tmp = tmp.substring(0, tmp.length()-5);
		int cpos = tmp.indexOf(":");
		if (cpos == -1) return tmp;
		String prefix = tmp.substring(0, cpos);
		int cutpos = tmp.indexOf("_", cpos);
		String suffix = null;
		if (cutpos == -1) suffix = tmp.substring(cpos+1);
		else suffix = tmp.substring(cpos+1, cutpos);
		return suffix+"_"+prefix;
	}
	
	/**
	 * Get a hash map indicating which class each DBLP instance belongs to
	 * @param dblpFile is the file that stores which class each DBLP instance belongs to
	 * @return a hash map mapping each DBLP instance to the class it belongs to
	 * @throws Exception
	 */
	public static HashMap<String, String> loadDBLPMap(String dblpFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(dblpFile));
		HashMap<String, String> ret = new HashMap<String, String>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			count ++;
			System.out.println(count);
			ret.put(part[0], part[2]);
		}
		return ret;
	}
	
	/**
	 * Summarize numbers of mapped instances under all pairs of classes
	 * @param instanceMap is the name of the instance mapping file
	 * @param output is the name of the output class mapping file
	 * @throws Exception
	 */
	public static void classMap(String instanceMap, String output) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(instanceMap));
		HashMap<String, Integer> classSum = new HashMap<String, Integer>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			String key = part[2]+"\t"+part[4];
			if (classSum.containsKey(key)) classSum.put(key, classSum.get(key)+1);
			else classSum.put(key, 1);
		}
		br.close();

		TreeMap<Integer, ArrayList<String>> sortMap = new TreeMap<Integer, ArrayList<String>>(new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				if (a > b) return -1;
				if (a < b) return 1;
				return 0;
			}
		});
		for (String s : classSum.keySet()) {
			Integer v = classSum.get(s);
			if (sortMap.containsKey(v)) sortMap.get(v).add(s);
			else {
				ArrayList<String> newList = new ArrayList<String>();
				newList.add(s);
				sortMap.put(v, newList);
			}
		}
		
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (Integer i : sortMap.keySet()) for (String s : sortMap.get(i)) {
			pw.println(s + "\t" + i.intValue());
		}
		pw.close();
	}
	
	/**
	 * Extract information about which class each instance belongs to from the Freebase dataset
	 * @param dir is the name of the Freebase dataset directory
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void extractInstanceClassFromFreebase(String dir, String output) throws Exception {
		File dirf = new File(dir);
		File[] files = dirf.listFiles();
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		for (File f : files) {
			ZipInputStream zipinput = new ZipInputStream(new FileInputStream(f));
			BufferedReader br = new BufferedReader(new InputStreamReader(zipinput));
			while (zipinput.getNextEntry() != null) {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					lineCount++;
					if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " " + lineCount);
					String[] part = line.split(" ");
					if (part[1].equals(rdfType)) {
						pw.println(line);
						pw.flush();
					}
				}
			}
			br.close();
		}
		pw.close();
	}
	
	/**
	 * Extract information about which class each instance belongs to from the Geonames dataset
	 * @param source is the file name of the Geonames dataset
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void extractInstanceClassFromGeonames(String source, String output) throws Exception {
		System.out.println(new Date().toString() + " Begin processing " + source);
		final FastBufferedInputStream in = new FastBufferedInputStream(new FileInputStream(source));
		GZWarcRecord record = new GZWarcRecord();
		Filter<WarcRecord> filter = Filters.adaptFilterBURL2WarcRecord(new Filter<BURL>() {
			public boolean accept(BURL x) {
				return true;
			}
			public String toExternalForm() {
				return "true";
			}
		});
		WarcFilteredIterator it = new WarcFilteredIterator(in, record, filter);
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		WarcHttpResponse response = new WarcHttpResponse();
		while (it.hasNext()) {
			WarcRecord nextRecord = it.next();
			try {
				response.fromWarcRecord(nextRecord);
				BufferedReader br = new BufferedReader(new InputStreamReader(response.contentAsStream()));
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					String[] part = line.split(" ");
					if (part[1].equals(rdfType)) {
						pw.println(line);
						pw.flush();
					}
					lineCount++;
					if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		pw.close();
		System.out.println(new Date().toString() + " Finish processing " + source);
		System.out.println("Number of lines: " + lineCount);
	}
	
	/**
	 * Extract information about which class each instance belongs to from the USCensus dataset
	 * @param input is the file name of the USCensus dataset
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void extractInstanceClassFromUscensus(String input, String output) throws Exception {
		System.out.println(new Date().toString() + " Begin processing " + input);
		TarInputStream tis = new TarInputStream(new GZIPInputStream(new FileInputStream(input)));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		while (tis.getNextEntry() != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(tis));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] part = line.split(" ");
				if (part[1].equals(rdfType)) {
					pw.println(line);
					pw.flush();
				}
				lineCount++;
				if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
			}
		}
		tis.close();
		pw.close();
		System.out.println(new Date().toString() + " Finish processing " + input);
		System.out.println("Number of lines: " + lineCount);
	}
	
	/**
	 * Summarize all the classes appeared in the dataset
	 * @param input is the name of the file that indicates instance-class belonging relationships
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void summarizeClass(String input, String output) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		HashSet<String> summarySet = new HashSet<String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			summarySet.add(part[2]);
		}
		br.close();
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (String s : summarySet) pw.println(s);
		pw.close();
	}

	/** For the DBpedia dataset, the subject, predicate and object of a statement is separated by tab, not space
	 * This method is for DBpedia specially
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	public static void summarizeClass4DBpedia(String input, String output) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		HashSet<String> summarySet = new HashSet<String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			summarySet.add(part[2]);
		}
		br.close();
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (String s : summarySet) pw.println(s);
		pw.close();
	}

	/**
	 * Get the normalized local name of a URI in the Freebase dataset
	 * @param uri is a URI in the Freebase dataset
	 * @return its normalized local name
	 * @throws Exception
	 */
	public static String getLocalNameFromFreebase(String uri) throws Exception {
		String prefix = "<http://www.freebase.com/resource/";
		int pos = uri.indexOf("/", prefix.length());
		return uri.substring(prefix.length(), pos);
	}
	
	/**
	 * Perform instance mapping between DBpedia and Freebase datasets
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void mapDBpediaFreebase(String output) throws Exception {
		String source1 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\freebaseElements.txt";
		String source2 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\yago_typeOf.nt";
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadDBpediaMap(source2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 100000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = getLocalNameFromFreebase(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
	}

	/**
	 * Get the normalized local name of a URI in the USCensus dataset
	 * @param uri is a URI in the USCensus dataset
	 * @return its normalized local name
	 * @throws Exception
	 */
	public static String getLocalNameFromUscensus(String uri) throws Exception {
		int pos = uri.lastIndexOf("/");
		String suffix = uri.substring(pos+1, uri.length()-1);
		String[] token = suffix.split("_");
		String ret = Character.toUpperCase(token[0].charAt(0))+token[0].substring(1);
		for (int i = 1; i < token.length; i++) {
			ret += "_";
			if (token[i].length() > 0) ret += Character.toUpperCase(token[i].charAt(0))+token[i].substring(1);
		}
		return ret;
	}
	
	/**
	 * Perform instance mapping between DBpedia and USCensus datasets
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void mapDBpediaUscensus(String output) throws Exception {
		String source1 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\uscensusElements.txt";
		String source2 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\yago_typeOf.nt";
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadDBpediaMap(source2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 100000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = getLocalNameFromUscensus(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
	}

	/**
	 * Perform instance mapping between DBLP and Freebase datasets
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void mapDblpFreebase(String output) throws Exception {
		String source1 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\dblpElements.txt";
		String source2 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\freebaseElements.txt";
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadDblpMap(source1, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source2));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 100000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = getLocalNameFromFreebase(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
	}

	/**
	 * Load mappings from normalized local name to URI, and to class it belongs to from DBLP instance-class belonging relationship file
	 * @param source is the name of the file indicating which class each instance belongs to
	 * @param localNameMap is a hash map used to be filled with mappings from normalized names to original URIs
	 * @return a hash map that stores mappings from normalized names to classes they belong to
	 * @throws Exception
	 */
	private static HashMap<String, String> loadDblpMap(String source,
			HashMap<String, String> localNameMap) throws Exception {
		System.out.println(new Date().toString() + " begin loading dblp");
		BufferedReader br = new BufferedReader(new FileReader(source));
		HashMap<String, String> ret = new HashMap<String, String>();
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			String localName = getLocalNameFromDblp(part[0]);
			ret.put(localName, part[2]);
			localNameMap.put(localName, part[0]);
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
		}
		return ret;
	}

	/** 
	 * Perform instance mapping between DBLP and USCensus datasets
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void mapDblpUscensus(String output) throws Exception {
		String source1 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\dblpElements.txt";
		String source2 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\uscensusElements.txt";
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadUscensusMap(source2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 100000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = getLocalNameFromDblp(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
	}

	/**
	 * Load mappings from normalized local name to URI, and to class it belongs to from USCensus instance-class belonging relationship file
	 * @param source2 is the name of the file indicating which class each instance belongs to
	 * @param localNameMap is a hash map used to be filled with mappings from normalized names to original URIs
	 * @return a hash map that stores mappings from normalized names to classes they belong to
	 * @throws Exception
	 */
	private static HashMap<String, String> loadUscensusMap(String source2,
			HashMap<String, String> localNameMap) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(new Date().toString() + " begin loading uscensus");
		BufferedReader br = new BufferedReader(new FileReader(source2));
		HashMap<String, String> ret = new HashMap<String, String>();
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			String localName = getLocalNameFromUscensus(part[0]);
			ret.put(localName, part[2]);
			localNameMap.put(localName, part[0]);
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
		}
		return ret;
	}

	/** 
	 * Perform instance mapping between Freebase and USCensus datasets
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void mapFreebaseUscensus(String output) throws Exception {
		String source1 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\freebaseElements.txt";
		String source2 = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\uscensusElements.txt";
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadUscensusMap(source2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 100000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = getLocalNameFromFreebase(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
	}

	/**
	 * Sample usage of the methods
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		extractInstanceClassFromFreebase("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\freebase-new\\", "d:\\freebaseElements.txt");
		extractInstanceClassFromGeonames("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\geonames.warc", "d:\\geonamesElements.txt");
		extractInstanceClassFromUscensus("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\uscensus.nt.tar.gz", "d:\\uscensusElements.txt");
		summarizeClass("d:\\freebaseElements.txt", "d:\\freebaseSummary.txt");
		summarizeClass("d:\\geonamesElements.txt", "d:\\geonamesSummary.txt");
		summarizeClass("d:\\uscensusElements.txt", "d:\\uscensusSummary.txt");
		summarizeClass("\\\\poseidon\\team\\semantic search\\BillionTripleData\\mapping\\dblp\\dblpElements.txt", "d:\\dblpSummary.txt");
		summarizeClass4DBpedia("\\\\poseidon\\team\\semantic search\\BillionTripleData\\yago_typeOf.nt", "d:\\dbpediaSummary.txt");
		mapDBpediaFreebase("d:\\dbpediaFreebaseInstanceMap.txt");
		mapDBpediaUscensus("d:\\dbpediaUscensusInstanceMap.txt");
		mapDBpediaDBLP("d:\\dbpediaDblpInstanceMap.txt");
		mapDblpFreebase("d:\\dblpFreebaseInstanceMap.txt");
		mapDblpUscensus("d:\\dblpUscensusInstanceMap.txt");
		mapFreebaseUscensus("d:\\freebaseUscensusInstanceMap.txt");
		classMap("d:\\dbpediaFreebaseInstanceMap.txt", "d:\\dbpediaFreebaseClassMap.txt");
		classMap("d:\\dbpediaUscensusInstanceMap.txt", "d:\\dbpediaUscensusClassMap.txt");
		classMap("d:\\dbpediaDblpInstanceMap.txt", "d:\\dbpediaDblpClassMap.txt");
		classMap("d:\\dblpFreebaseInstanceMap.txt", "d:\\dblpFreebaseClassMap.txt");
		classMap("d:\\dblpUscensusInstanceMap.txt", "d:\\dblpUscensusClassMap.txt");
		classMap("d:\\freebaseUscensusInstanceMap.txt", "d:\\freebaseUscensusClassMap.txt");
	}
	
}

