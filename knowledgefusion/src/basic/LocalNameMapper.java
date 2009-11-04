package basic;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	
	public static int maxLineNum = 4110000;

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

	public static void map4aifbPortal(String source1, ILocalNameExtractor extractor1, String source2, String output) throws Exception {
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> classMap = loadMap4aifbPortal(source2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 1000000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split(" ");
			String localname = extractor1.getLocalName(part[0]);
			if (localNameMap.keySet().contains(localname)) 
				pw.println(localname + "\t" + localNameMap.get(localname) + "\t" + classMap.get(localname) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();

	}

	private static HashMap<String, String> loadMap4aifbPortal(String source, HashMap<String, String> localNameMap) throws Exception {
		System.out.println(new Date().toString() + " begin loading " + source);
		BufferedReader br = new BufferedReader(new FileReader(source));
		HashMap<String, String> ret = new HashMap<String, String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			if (part[1].equals("null")) continue;
			String localName = part[1].replaceAll(" ", "_");
			ret.put(localName, part[2]);
			localNameMap.put(localName, part[0]);
		}
		System.out.println(new Date().toString() + " finish loading");
		return ret;
		
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
	
	public static void map4aifbPortal(String source1, String source2, ILocalNameExtractor extractor2, String output) throws Exception {
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> dbpediaMap = loadMap(source2, extractor2, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(source1));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			if (part[1].equals("null")) continue;
			String localn = part[1].replaceAll(" ", "_");
			if (localNameMap.keySet().contains(localn)) 
				pw.println(localn + "\t" + localNameMap.get(localn) + "\t" + dbpediaMap.get(localn) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();

	}
	
	public static void geonamesAifbPortalMap(String geonames, String aifbPortal, String output) throws Exception {
		HashMap<String, String> localNameMap = new HashMap<String, String>();
		HashMap<String, String> classMap = loadMap4aifbPortal(aifbPortal, localNameMap);
		BufferedReader br = new BufferedReader(new FileReader(geonames));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count % 1000000 == 0) System.out.println(new Date().toString() + " : " + count);
			String[] part = line.split("\t");
			if (part[1].equals("null")) continue;
			String localname = part[1].replaceAll(" ", "_");
			if (localNameMap.keySet().contains(localname)) 
				pw.println(localname + "\t" + localNameMap.get(localname) + "\t" + classMap.get(localname) + "\t" + part[0] + "\t" + part[2]);
		}
		br.close();
		pw.close();
		
	}
	
	public static void addClassToInstanceMapping(String instance1, String instance2, 
			String instanceMapping, String output) throws Exception {
		HashMap<String, ArrayList<String>> classMap1 = loadClassMap(instance1);
		System.out.println("instance-class 1 info loaded");
		HashMap<String, ArrayList<String>> instanceMap = loadInstanceMap(instanceMapping);	//column 2 -> column 0
		System.out.println("instance mapping info loaded");
		BufferedReader br = new BufferedReader(new FileReader(instance2));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			if (instanceMap.containsKey(part[0])) {
				ArrayList<String> mappedInst = instanceMap.get(part[0]);
				for (String s : mappedInst)
					pw.println("localname\t"  + s + "\t" + classMap1.get(s) + "\t" + part[0] + "\t" + part[2]);
			}
			lineCount++;
			if (lineCount % 1000000 == 0) System.out.println(lineCount);
		}
		pw.close();
		br.close();
	}
	
	private static HashMap<String, ArrayList<String>> loadInstanceMap(String instanceMapping) throws Exception {
		HashMap<String, ArrayList<String>> ret = new HashMap<String, ArrayList<String>>();
		BufferedReader br = new BufferedReader(new FileReader(instanceMapping));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			if (ret.containsKey(part[2])) {
				ret.get(part[2]).add(part[0]);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(part[0]);
				ret.put(part[2], list);
			}
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(lineCount);
		}
		br.close();
		return ret;
	}
	
	private static HashMap<String, ArrayList<String>> loadClassMap(String dbpediaInstance) throws Exception {
		HashMap<String, ArrayList<String>> ret = new HashMap<String, ArrayList<String>>();
		BufferedReader br = new BufferedReader(new FileReader(dbpediaInstance));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			if (ret.containsKey(part[0])) {
				ret.get(part[0]).add(part[2]);
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(part[2]);
				ret.put(part[0], list);
			}
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(lineCount);
		}
		br.close();
		return ret;
	}

	public static void checkFreebaseInstance(String freebaseInstance) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(freebaseInstance));
		HashSet<String> instanceSet = new HashSet<String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String instance = line.split(" ")[0];
			if (instanceSet.contains(instance)) System.out.println("dup in freebase instance!!");
			instanceSet.add(instance);
		}
		br.close();
	}
	
	public static void getInstanceSet(String input, String output, String remain) throws Exception {
		HashSet<String> instanceSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(input));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			instanceSet.add(part[0]);
			lineCount++;
			if (lineCount%500000 == 0) System.out.println(lineCount);
			if (lineCount == maxLineNum) break;
		}
		if (lineCount == maxLineNum) {
			PrintWriter pw = new PrintWriter(new FileWriter(remain));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] part = line.split(" ");
				if (!instanceSet.contains(part[0])) pw.println(part[0]);
				lineCount++;
				if (lineCount%500000 == 0) System.out.println(lineCount);
			}
			pw.close();
		}
		br.close();
		writeSet(instanceSet, output);
	}

	public static void getClassSet(String input, String output, String remain) throws Exception {
		HashSet<String> classSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(input));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			classSet.add(part[2]);
			lineCount++;
			if (lineCount == maxLineNum) break;
		}
		if (lineCount == maxLineNum) {
			PrintWriter pw = new PrintWriter(new FileWriter(remain));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] part = line.split(" ");
				if (!classSet.contains(part[2])) pw.println(part[2]);
			}
			pw.close();
		}
		br.close();
		writeSet(classSet, output);
	}
	
	private static void writeSet(HashSet<String> instanceSet, String output) throws Exception {
		PrintWriter pw = new PrintWriter(new FileWriter(output, true));
		for (String s : instanceSet) pw.println(s);
		pw.close();
	}

	public static void addRemainToSet(String set, String remain) throws Exception {
		HashSet<String> set1 = loadSet(set);
		BufferedReader br = new BufferedReader(new FileReader(remain));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (!set1.contains(line)) set1.add(line);
		}
		writeSet(set1, set);
	}
	
	private static HashSet<String> loadSet(String set) throws Exception {
		HashSet<String> ret = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(set));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			ret.add(line);
		}
		return ret;
	}

	public static void filter(String dbpediaInstance, String dbpediaSelected, String freebaseInstance, String freebaseSelected, 
			String instanceMapping) throws Exception {
		HashSet<String> dbpediaInstanceSet = getSet(instanceMapping, 0);
		HashSet<String> freebaseInstanceSet = getSet(instanceMapping, 2);
		filter(dbpediaInstance, dbpediaInstanceSet, dbpediaSelected);
		filter(freebaseInstance, freebaseInstanceSet, freebaseSelected);
	}
	
	private static void filter(String input, HashSet<String> filterSet, String output) throws Exception {
		System.out.println("begin filtering");
		BufferedReader br = new BufferedReader(new FileReader(input));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (filterSet.contains(line.split(" ")[0])) pw.println(line);
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(lineCount);
		}
		pw.close();
		br.close();
	}

	private static HashSet<String> getSet(String input, int col) throws Exception {
		HashSet<String> ret = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(input));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			ret.add(part[col]);
		}
		br.close();
		return ret;
	}

	private static void testMem(String input) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		HashSet<String> set = new HashSet<String>();
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			set.add(line.split(" ")[0]);
			lineCount++;
			if (lineCount%10000 == 0) System.out.println(lineCount);
		}
	}
	
	public static void toID4instance(String instance, String instanceSet, String classSet, String output) throws Exception {
		HashMap<String, Integer> instanceIdMap = loadIdMap(instanceSet);
		System.out.println("instance set loaded");
		HashMap<String, Integer> classIdMap = loadIdMap(classSet);
		System.out.println("class set loaded");
		int lineNum = getLineNum(instance);
		System.out.println("#line: " + lineNum);
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		dos.writeInt(lineNum);
		BufferedReader br = new BufferedReader(new FileReader(instance));
		for (int i = 0; i < lineNum; i++) {
			String line = br.readLine();
			String[] part = line.split(" ");
			dos.writeInt(instanceIdMap.get(part[0]));
			dos.writeInt(classIdMap.get(part[2]));
			if ((i+1)%500000 == 0) System.out.println((i+1));
		}
		br.close();
		dos.close();
	}

	public static void toID4mapping(String mapping, String instanceSet1, String instanceSet2, String output) throws Exception {
		HashMap<String, Integer> instanceIdMap1 = loadIdMap(instanceSet1);
		System.out.println("instance set 1 loaded");
		HashMap<String, Integer> instanceIdMap2 = loadIdMap(instanceSet2);
		System.out.println("instance set 2 loaded");
		int lineNum = getLineNum(mapping);
		System.out.println("#line: " + lineNum);
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		dos.writeInt(lineNum);
		BufferedReader br = new BufferedReader(new FileReader(mapping));
		for (int i = 0; i < lineNum; i++) {
			String line = br.readLine();
			String[] part = line.split(" ");
			dos.writeInt(instanceIdMap1.get(part[0]));
			dos.writeInt(instanceIdMap2.get(part[2]));
			if ((i+1)%500000 == 0) System.out.println((i+1));
		}
		br.close();
		dos.close();
	}

	private static int getLineNum(String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			count++;
			if (count%1000000 == 0) System.out.println(count);
		}
		br.close();
		return count;
	}

	private static HashMap<String, Integer> loadIdMap(String set) throws Exception {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(set));
		int id = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			ret.put(line, id);
			id++;
		}
		br.close();
		return ret;
	}

	public static HashSet<String> getColumnSet(String file, int col) throws Exception {
		HashSet<String> ret = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split(" ");
			ret.add(part[col]);
			lineCount++;
			if (lineCount%500000 == 0) System.out.println(lineCount);
		}
		br.close();
		return ret;
	}
	
	public static void freebaseDbpediaInstanceMapBinary(String dbpediaInstance, String freebaseInstance, String instanceMapping, 
			String output) throws Exception {
		HashMap<Integer, ArrayList<Integer>> freebaseInstClass = getInstClassBinary(freebaseInstance);
		System.out.println("freebase loaded");
		HashMap<Integer, ArrayList<Integer>> dbpediaInstClass = getInstClassBinary(dbpediaInstance);
		System.out.println("dbpedia loaded");
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(instanceMapping)));
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		int numLine = dis.readInt();
		for (int i = 0; i < numLine; i++) {
			int dbpediaInst = dis.readInt();
			int freebaseInst = dis.readInt();
			if (dbpediaInstClass.containsKey(dbpediaInst) && freebaseInstClass.containsKey(freebaseInst)) {
				for (Integer j : dbpediaInstClass.get(dbpediaInst)) for (Integer k : freebaseInstClass.get(freebaseInst)) {
					dos.writeInt(dbpediaInst);
					dos.writeInt(j.intValue());
					dos.writeInt(freebaseInst);
					dos.writeInt(k.intValue());
				}
			}
			if ((i+1)%500000 == 0) System.out.println((i+1));
		}
		dos.writeInt(-1);
		dos.close();
		dis.close();
	}
	
	public static void interpret(String input, String dbpediaInstID, String dbpediaClassID, String freebaseInstID, String freebaseClassID, 
			String output) throws Exception {
		String[] dbpediaInst = loadName(dbpediaInstID);
		String[] dbpediaClass = loadName(dbpediaClassID);
		String[] freebaseInst = loadName(freebaseInstID);
		String[] freebaseClass = loadName(freebaseClassID);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int di = dis.readInt();
		while (di != -1) {
			int dc = dis.readInt();
			int fi = dis.readInt();
			int fc = dis.readInt();
			pw.println("localname\t" + dbpediaInst[di] + "\t" + dbpediaClass[dc] + "\t" + freebaseInst[fi] + "\t" + freebaseClass[fc]);
			di = dis.readInt();
		}
		pw.close();
		dis.close();
	}
	
	private static String[] loadName(String set) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(set));
		for (String line = br.readLine(); line != null; line = br.readLine()) list.add(line);
		String[] ret = new String[list.size()];
		list.toArray(ret);
		return ret;
	}

	private static HashMap<Integer, ArrayList<Integer>> getInstClassBinary(String instance) throws Exception {
		HashMap<Integer, ArrayList<Integer>> ret = new HashMap<Integer, ArrayList<Integer>>();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(instance)));
		int lineNum = dis.readInt();
		for (int i = 0; i < lineNum; i++) {
			int key = dis.readInt();
			int v = dis.readInt();
			if (ret.containsKey(key)) ret.get(key).add(v);
			else {
				ArrayList<Integer> value = new ArrayList<Integer>();
				value.add(v);
				ret.put(key, value);
			}
		}
		return ret;
	}

	/**
	 * Sample usage of the methods
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		map4aifbPortal("D:\\freebaseElements.txt", new FreebaseLocalNameExtractor(), "E:\\swrc_instance_name.txt", 
//		"D:\\aifbPortalFreebaseInstanceMap.txt");
//		map4aifbPortal("E:\\swrc_instance_name.txt", "D:\\uscensusElements.txt", new USCensusLocalNameExtractor(), 
//		"D:\\uscensusAifbPortalInstanceMap.txt");
//		map("D:\\semanticwebInstance.txt", new DBpediaLocalNameExtractor(), "D:\\dbpediaElements.txt", new DBpediaLocalNameExtractor(), 
//				"D:\\dbpediaSemanticwebInstanceMap.txt");
//		map("D:\\freebaseInstance.txt", new FreebaseLocalNameExtractor(), "D:\\semanticwebInstance.txt", new DBpediaLocalNameExtractor(), 
//		"D:\\semanticwebFreebaseInstanceMap.txt");
//		map("D:\\semanticwebInstance.txt", new DBpediaLocalNameExtractor(), "D:\\uscensusElements.txt", new USCensusLocalNameExtractor(), 
//		"D:\\btcInstanceMapping\\uscensusSemanticwebInstanceMap.txt");

//		map4aifbPortal("D:\\dbpediaElements.txt", new DBpediaLocalNameExtractor(), "E:\\geonames_instance_name.txt", 
//		"D:\\btcInstanceMapping\\geonamesDbpediaInstanceMap.txt");
//		map4aifbPortal("D:\\dblpElements.txt", new DBLPLocalNameExtractor(), "E:\\geonames_instance_name.txt", 
//		"D:\\btcInstanceMapping\\geonamesDblpInstanceMap.txt");
//		map4aifbPortal("D:\\freebaseElements.txt", new FreebaseLocalNameExtractor(), "E:\\geonames_instance_name.txt", 
//		"D:\\btcInstanceMapping\\geonamesFreebaseInstanceMap.txt");
//		map4aifbPortal("D:\\uscensusElements.txt", new USCensusLocalNameExtractor(), "E:\\geonames_instance_name.txt", 
//		"D:\\btcInstanceMapping\\geonamesUscensusInstanceMap.txt");
//		map4aifbPortal("D:\\semanticwebInstance.txt", new DBpediaLocalNameExtractor(), "E:\\geonames_instance_name.txt", 
//		"D:\\btcInstanceMapping\\geonamesSemanticwebInstanceMap.txt");
//		geonamesAifbPortalMap("E:\\geonames_instance_name.txt", "E:\\swrc_instance_name.txt", 
//				"D:\\btcInstanceMapping\\aifbPortalGeonamesInstanceMap.txt");

		String freebaseDbpediaMapFolder = "\\\\192.168.4.201\\fulingyun\\";
//		addClassToFreebaseDbpediaInstanceMapping(freebaseDbpediaMapFolder + "freebase1.txt", freebaseDbpediaMapFolder + "dbpedia1.txt", 
//				freebaseDbpediaMapFolder + "instancemappingEx.txt", freebaseDbpediaMapFolder + "freebaseDbpediaInstanceMapping5c.txt");
//		checkFreebaseInstance(freebaseDbpediaMapFolder + "dbpedia.txt");
//		toID(freebaseDbpediaMapFolder + "freebase.txt", freebaseDbpediaMapFolder + "freebaseInstanceID.txt", 
//				freebaseDbpediaMapFolder + "freebaseClassID.txt");
//		toID(freebaseDbpediaMapFolder + "dbpedia.txt", freebaseDbpediaMapFolder + "dbpediaInstanceID.txt", 
//				freebaseDbpediaMapFolder + "dbpediaClassID.txt");
//		testMem(freebaseDbpediaMapFolder + "dbpedia.txt");
//		getInstanceSet(freebaseDbpediaMapFolder + "dbpedia1.txt", freebaseDbpediaMapFolder + "dbpediaInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "dbpediaInstanceRemain.txt");
//		System.out.println("got instance set\n");
//		getClassSet(freebaseDbpediaMapFolder + "dbpedia1.txt", freebaseDbpediaMapFolder + "dbpediaClassSet.txt",	
//				freebaseDbpediaMapFolder + "dbpediaClassRemain.txt");
//		filter(freebaseDbpediaMapFolder + "dbpedia.txt", freebaseDbpediaMapFolder + "dbpedia1.txt", 
//				freebaseDbpediaMapFolder + "freebase.txt", freebaseDbpediaMapFolder + "freebase1.txt", 
//				freebaseDbpediaMapFolder + "instancemappingEx.txt");
//		filter(freebaseDbpediaMapFolder + "dbpedia.txt", getSet(freebaseDbpediaMapFolder + "instancemappingEx.txt", 0), 
//				freebaseDbpediaMapFolder + "dbpedia1.txt");
//		filter(freebaseDbpediaMapFolder + "freebase.txt", getSet(freebaseDbpediaMapFolder + "instancemappingEx.txt", 2), 
//				freebaseDbpediaMapFolder + "freebase1.txt");
//		getInstanceSet(freebaseDbpediaMapFolder + "freebase1.txt", freebaseDbpediaMapFolder + "freebaseInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "freebaseInstanceRemain.txt");
//		System.out.println("got instance set\n");
//		getClassSet(freebaseDbpediaMapFolder + "freebase1.txt", freebaseDbpediaMapFolder + "freebaseClassSet.txt",	
//				freebaseDbpediaMapFolder + "freebaseClassRemain.txt");
//		addRemainToSet(freebaseDbpediaMapFolder + "freebaseClassSet.txt", freebaseDbpediaMapFolder + "freebaseClassRemain.txt");
//		toID4instance(freebaseDbpediaMapFolder + "dbpedia1.txt", freebaseDbpediaMapFolder + "dbpediaInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "dbpediaClassSet.txt", freebaseDbpediaMapFolder + "dbpedia");
//		toID4instance(freebaseDbpediaMapFolder + "freebase1.txt", freebaseDbpediaMapFolder + "freebaseInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "freebaseClassSet.txt", freebaseDbpediaMapFolder + "freebase");
//		toID4mapping(freebaseDbpediaMapFolder + "instancemappingEx.txt", freebaseDbpediaMapFolder + "dbpediaInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "freebaseInstanceSet.txt", freebaseDbpediaMapFolder + "instancemapping");
//		writeSet(getColumnSet(freebaseDbpediaMapFolder + "instancemappingEx.txt", 0), freebaseDbpediaMapFolder + "dbpediaInstanceSet.txt");
//		writeSet(getColumnSet(freebaseDbpediaMapFolder + "instancemappingEx.txt", 2), freebaseDbpediaMapFolder + "freebaseInstanceSet.txt");
//		freebaseDbpediaInstanceMapBinary(freebaseDbpediaMapFolder + "dbpedia", freebaseDbpediaMapFolder + "freebase", 
//				freebaseDbpediaMapFolder + "instancemapping", freebaseDbpediaMapFolder + "instanceMapping5c");
//		interpret(freebaseDbpediaMapFolder + "instanceMapping5c", freebaseDbpediaMapFolder + "dbpediaInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "dbpediaClassSet.txt", freebaseDbpediaMapFolder + "freebaseInstanceSet.txt", 
//				freebaseDbpediaMapFolder + "freebaseClassSet.txt", freebaseDbpediaMapFolder + "instanceMapping5c.txt");
//		classMap(freebaseDbpediaMapFolder + "instanceMapping5c.txt", freebaseDbpediaMapFolder + "classMapping.txt");
//		new IURemoveCalculator().calculate(freebaseDbpediaMapFolder + "classMapping.txt", null, freebaseDbpediaMapFolder + "classMappingIU.txt");
		String mapFolder = "\\\\192.168.4.201\\fulingyun\\";
		filter(mapFolder+"swetodblp_noblank.nt", getSet(mapFolder+"links_dblp_en.nt", 2), mapFolder+"dblpInstance1.txt");
//		addClassToInstanceMapping(mapFolder+"articlecategories_en.nt.clean", mapFolder+"dblpInstance1.txt", mapFolder+"links_dblp_en.nt", 
//				mapFolder+"dblpDbpediaInstanceMap5c.txt");
	}
	
}

