package org.team.xxplore.core.service.mapping;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


public class LocalNameMapper {
	
	public static final String rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	public static final String rdfProperty = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>";
	
	public static final String owlObjectProperty = "<http://www.w3.org/2002/07/owl#ObjectProperty>";
	public static final String owlClass = "<http://www.w3.org/2002/07/owl#Class>";
	public static final String owlAnnotationProperty = "<http://www.w3.org/2002/07/owl#AnnotationProperty>";
	public static final String owlDatatypeProperty = "<http://www.w3.org/2002/07/owl#DatatypeProperty>";
	public static final String owlRestriction = "<http://www.w3.org/2002/07/owl#Restriction>";
	
	public static final String owlPrefix = "<http://www.w3.org/2002/07/owl#";
	public static final String rdfPrefix = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
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
	 * Summarize all the classes appeared in the dataset
	 * @param input is the name of the file that indicates instance-class belonging relationships
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void summarizeClass(String input, String output) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		HashMap<String, Integer> summary = new HashMap<String, Integer>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			if (summary.keySet().contains(part[2])) summary.put(part[2], summary.get(part[2])+1);
			else summary.put(part[2], 1);
		}
		br.close();
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (String s : summary.keySet()) pw.println(s + "\t" + summary.get(s).intValue());
		pw.close();
	}

	/**
	 * Extract information about which class each instance belongs to from a dataset
	 * @param reader is the data source reader
	 * @param output is the name of the output file
	 * Each line of this file has the following format:
	 * <Instance URI> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Class URI>
	 * @throws Exception
	 */
	public static void extractInstance(IDataSourceReader reader, String output) throws Exception {
		reader.init();
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			lineCount++;
			if (lineCount % 1000000 == 0)
				System.out.println(new Date().toString() + " " + lineCount);
			String[] part = line.split(" ");
			if (part[1].equals(rdfType)) {
				pw.println(line);
				pw.flush();
			}
		}
		reader.close();
		pw.close();

	}
	
	/**
	 * Extract instance about which class each instance or class or property belongs to from a dataset that is an .nt file
	 * @param reader is the BufferedReader initialized with the input .nt file
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public static void extractInstance(BufferedReader reader, String output) throws Exception {
		reader.readLine();
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			lineCount++;
			if (lineCount % 1000000 == 0) System.out.println(new Date().toString() + " " + lineCount);
			String[] part = line.split(" ");
			if (part[1].equals(rdfType)) {
				pw.println(line);
				pw.flush();
			}
		}
		reader.close();
		pw.close();
	}
	
	private static void findAllSchemaElements(String input) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		HashSet<String> elementNames = new HashSet<String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			if (part[2].startsWith(owlPrefix) || part[2].startsWith(rdfPrefix)) elementNames.add(part[2]);
		}
		br.close();
		for (String s : elementNames) System.out.println(s);
	}
	
	/**
	 * Extract classes and properties from the element file
	 * @param input is the name of the element file generated with one of the extractInstance() methods
	 * @param output is the name of the output file
	 * @param remain is the remaining lines of the element file after extraction
	 * @throws Exception
	 */
	public static void extractSchemaElements(String input, String output, String remain) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		PrintWriter outputWriter = new PrintWriter(new FileWriter(output));
		PrintWriter remainWriter = new PrintWriter(new FileWriter(remain));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			if (part[2].equals(owlClass) || part[2].equals(owlAnnotationProperty) || part[2].equals(owlDatatypeProperty)
					|| part[2].equals(owlObjectProperty) || part[2].equals(owlRestriction)) outputWriter.println(line);
			else remainWriter.println(line);
		}
		br.close();
		outputWriter.close();
		remainWriter.close();
	}
	
	/**
	 * Mapping instance between two data sources according to local names only
	 * @param source1
	 * @param extractor1 is the local name extractor for source1
	 * @param source2
	 * @param extractor2 is the local name extractor for source2
	 * @param output is the output file name
	 * Each line of this file has the following format:
	 * LocalName \t <Instance URI 1> \t <Class URI 1> \t <Instance URI 2> \t <Class URI 2>
	 * @throws Exception
	 */
	public static void map(String source1, ILocalNameExtractor extractor1, String source2, 
			ILocalNameExtractor extractor2, String output) throws Exception {
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadMap(source2, extractor2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 1000000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localn = extractor1.getLocalName(part[0]);
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();

	}
	
	private static HashMap<String, String> loadMap(String source, ILocalNameExtractor extractor, 
			HashMap<String, String> localNameMap) throws Exception {
		System.out.println(new Date().toString() + " begin loading " + source);
		BufferedReader br = new BufferedReader(new FileReader(source));
		HashMap<String, String> ret = new HashMap<String, String>();
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			String localName = extractor.getLocalName(part[0]);
			ret.put(localName, part[2]);
			localNameMap.put(localName, part[0]);
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
		}
		System.out.println(new Date().toString() + " finish loading, " + lineCount + " lines loaded");
		return ret;

	}
	
	/**
	 * Sample usage of the methods
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		System.out.println(new Date().toString() + " begin extracting instance from freebase");
//		extractInstance(new DirZipReader("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\freebase-new\\"), "d:\\freebaseElements.txt");
//		
//		System.out.println(new Date().toString() + " begin extracting instance from geonames");
//		try {
//			extractInstance(new WarcReader("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\geonames.warc"), "d:\\geonamesElements.txt");
//		} catch (Exception e) {
//			
//		}
//		
//		System.out.println(new Date().toString() + " begin extracting instance from uscensus");
//		try {
//			extractInstance(new TarGzReader("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\uscensus.nt.tar.gz"), "d:\\uscensusElements.txt");
//		} catch (Exception e) {
//			
//		}
//		
//		System.out.println(new Date().toString() + " begin extracting instance from dblp");
//		extractInstance(new GzReader("\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\swetodblp_noblank.gz"), "d:\\dblpElements.txt");
//		
//		summarizeClass("d:\\freebaseElements.txt", "d:\\freebaseSummary.txt");
//		System.out.println("finish summarizing freebase");
//		
//		summarizeClass("d:\\geonamesElements.txt", "d:\\geonamesSummary.txt");
//		System.out.println("finish summarizing geonames");
//		
//		summarizeClass("d:\\uscensusElements.txt", "d:\\uscensusSummary.txt");
//		System.out.println("finish summarizing uscensus");
//		
//		summarizeClass("d:\\dblpElements.txt", "d:\\dblpSummary.txt");
//		System.out.println("finish summarizing dblp");
//		
//		summarizeClass("d:\\dbpediaElements.txt", "d:\\dbpediaSummary.txt");
//		System.out.println("finish summarizing dbpedia");
//		
//		System.out.println(new Date().toString() + " begin mapping freebase and dbpedia");
//		map("d:\\freebaseElements.txt", new FreebaseLocalNameExtractor(), "d:\\dbpediaElements.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dbpediaFreebaseInstanceMap.txt");
//
//		System.out.println(new Date().toString() + " begin mapping uscensus and dbpedia");
//		map("d:\\uscensusElements.txt", new USCensusLocalNameExtractor(), "d:\\dbpediaElements.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dbpediaUscensusInstanceMap.txt");
//		
//		System.out.println(new Date().toString() + " begin mapping dblp and dbpedia");
//		map("d:\\dblpElements.txt", new DBLPLocalNameExtractor(), "d:\\dbpediaElements.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dbpediaDblpInstanceMap.txt");
//
//		System.out.println(new Date().toString() + " begin mapping freebase and dblp");
//		map("d:\\freebaseElements.txt", new FreebaseLocalNameExtractor(), "d:\\dblpElements.txt", 
//				new DBLPLocalNameExtractor(), "d:\\dblpFreebaseInstanceMap.txt");
//
//		System.out.println(new Date().toString() + " begin mapping dblp and uscensus");
//		map("d:\\dblpElements.txt",	new DBLPLocalNameExtractor(), "d:\\uscensusElements.txt", 
//				new USCensusLocalNameExtractor(), "d:\\dblpUscensusInstanceMap.txt");
//		
//		System.out.println(new Date().toString() + " begin mapping freebase and uscensus");
//		map("d:\\freebaseElements.txt", new FreebaseLocalNameExtractor(), "d:\\uscensusElements.txt", 
//				new USCensusLocalNameExtractor(), "d:\\freebaseUscensusInstanceMap.txt");
//		
//		classMap("d:\\dbpediaFreebaseInstanceMap.txt", "d:\\dbpediaFreebaseClassMap.txt");
//		System.out.println("finish mapping freebase and dbpedia classes");
//
//		classMap("d:\\dbpediaUscensusInstanceMap.txt", "d:\\dbpediaUscensusClassMap.txt");
//		System.out.println("finish mapping uscensus and dbpedia classes");
//
//		classMap("d:\\dbpediaDblpInstanceMap.txt", "d:\\dbpediaDblpClassMap.txt");
//		System.out.println("finish mapping dblp and dbpedia classes");
//
//		classMap("d:\\dblpFreebaseInstanceMap.txt", "d:\\dblpFreebaseClassMap.txt");
//		System.out.println("finish mapping freebase and dblp classes");
//
//		classMap("d:\\dblpUscensusInstanceMap.txt", "d:\\dblpUscensusClassMap.txt");
//		System.out.println("finish mapping dblp and uscensus classes");
//		
//		classMap("d:\\freebaseUscensusInstanceMap.txt", "d:\\freebaseUscensusClassMap.txt");
//		System.out.println("finish mapping freebase and uscensus classes");
//
//		new IURemoveCalculator().calculate("d:\\dbpediaFreebaseClassMap.txt", 
//				"\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\mapping\\dbpedia_freebase\\dbpedia_freebase_concept", 
//				"d:\\dbpediaFreebaseClassMapIUr.txt");
//
//		new IURemoveCalculator().calculate("d:\\dbpediaUscensusClassMap.txt", 
//				"\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\mapping\\dbpedia_uscensus\\dbpedia_uscensus_concept", 
//				"d:\\dbpediaUscensusClassMapIUr.txt");
//
//		new IURemoveCalculator().calculate("d:\\dbpediaDblpClassMap.txt", 
//				"\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\mapping\\dbpedia_dblp\\dbpedia_dblp_concept", 
//				"d:\\dbpediaDblpClassMapIUr.txt");
//
//		new IURemoveCalculator().calculate("d:\\dblpFreebaseClassMap.txt", 
//				"\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\mapping\\freebase_dblp\\freebase_dblp_concept", 
//				"d:\\dblpFreebaseClassMapIUr.txt");
//
//		new IURemoveCalculator().calculate("d:\\dblpUscensusClassMap.txt", 
//				"\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\mapping\\dblp_uscensus\\dblp_uscensus_concept", 
//				"d:\\uscensusDblpClassMapIUr.txt");
//		
//		new IURemoveCalculator().calculate("d:\\freebaseUscensusClassMap.txt", 
//				"\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\mapping\\freebase_uscensus\\freebase_uscensus_concept", 
//				"d:\\uscensusFreebaseClassMapIUr.txt");
		
//		System.out.println(new Date().toString() + " begin extracting instance from semanticweb.org.nt");
//		extractInstance(new BufferedReader(new FileReader("E:\\semanticweb.org.nt")), "d:\\semanticwebElements.txt");

//		findAllSchemaElements("d:\\semanticwebElements.txt");
//		extractSchemaElements("d:\\semanticwebElements.txt", "d:\\semanticwebSchema.txt", "d:\\semanticwebInstance.txt");
//		extractSchemaElements("d:\\dbpediaElements.txt", "d:\\dbpediaSchema.txt", "d:\\dbpediaInstance.txt");
//		extractSchemaElements("d:\\freebaseElements.txt", "d:\\freebaseSchema.txt", "d:\\freebaseInstance.txt");
//		extractSchemaElements("d:\\geonamesElements.txt", "d:\\geonamesSchema.txt", "d:\\geonamesInstance.txt");
//		extractSchemaElements("d:\\uscensusElements.txt", "d:\\uscensusSchema.txt", "d:\\uscensusInstance.txt");
//		extractSchemaElements("d:\\dblpElements.txt", "d:\\dblpSchema.txt", "d:\\dblpInstance.txt");
//		extractSchemaElements("d:\\semanticwebElements.txt", "d:\\semanticwebSchema.txt", "d:\\semanticwebInstance.txt");

//		summarizeClass("d:\\semanticwebInstance.txt", "d:\\semanticwebSummary.txt");
//		System.out.println(new Date().toString() + " begin mapping semanticweb and dbpedia");
//		map("d:\\dbpediaElements.txt", new DBpediaLocalNameExtractor(), "d:\\semanticwebInstance.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dbpediaSemanticwebInstanceMap.txt");

//		extractInstance(new BufferedReader(new FileReader("E:\\swrc_AIFB_Portal.nt")), "d:\\aifbPortalElements.txt");
//		extractSchemaElements("d:\\aifbPortalElements.txt", "d:\\aifbPortalSchema.txt", "d:\\aifbPortalInstance.txt");
//		summarizeClass("d:\\aifbPortalInstance.txt", "d:\\aifbPortalSummary.txt");
//		System.out.println(new Date().toString() + " begin mapping aifbPortal and dbpedia");
//		map("d:\\dbpediaElements.txt", new DBpediaLocalNameExtractor(), "d:\\aifbPortalInstance.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dbpediaAifbPortalInstanceMap.txt");

//		System.out.println(new Date().toString() + " begin mapping semanticweb and aifbPortal");
//		map("d:\\aifbPortalInstance.txt", new DBpediaLocalNameExtractor(), "d:\\semanticwebInstance.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\AifbPortalSemanticwebInstanceMap.txt");

//		classMap("d:\\dbpediaSemanticwebInstanceMap.txt", "d:\\dbpediaSemanticWebClassMap.txt");

//		map("d:\\dblpElements.txt", new DBLPLocalNameExtractor(), "d:\\semanticwebInstance.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dblpSemanticwebInstanceMap.txt");
//
//		map("d:\\dblpElements.txt", new DBLPLocalNameExtractor(), "d:\\aifbPortalInstance.txt", 
//				new DBpediaLocalNameExtractor(), "d:\\dblpAifbPortalInstanceMap.txt");

		classMap("d:\\dblpSemanticwebInstanceMap.txt", "d:\\dblpSemanticwebClassMap.txt");
	}
	
}

