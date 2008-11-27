package org.apexlab.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.jgrapht.graph.Pseudograph;

/**
 * Service of building summary and schema graph
 * @author kaifengxu
 *
 */
public class BuildQ2SemanticService {

	/* ROOT PATH */
	public static String root;
	public static String datasource;
	public static String indexRoot;
	public static String source;
	public static String summaryObj, schemaObj;
	public static String summaryRDF, schemaRDF;
	public static String keywordIndex, synIndex;
	
	/* CONSTANT */
	public static String[] rdfsEdge = new String[]{
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 		// 0
			"http://www.w3.org/2000/01/rdf-schema#subClassOf", 		// 1	
			"http://www.w3.org/2000/01/rdf-schema#domain",			// 2
			"http://www.w3.org/2000/01/rdf-schema#range",			// 3
			"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",	// 4
			"http://www.w3.org/2000/01/rdf-schema#label",			// 5
			"http://www.w3.org/2000/01/rdf-schema#comment",			// 6
			"http://www.w3.org/2002/07/owl#ObjectProperty",			// 7
			"http://www.w3.org/2002/07/owl#Class", 					// 8
			
			"http://www.w3.org/2002/07/owl#DatatypeProperty",		// 9
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property",	// 10
			
			"http://www.freebase.org/type/object/type",				// 11
			"http://www.freebase.org/type/type",					// 12
			"http://www.freebase.org/freebase/type_profile",		// 13
			"http://www.freebase.org/type/domian",					// 14	
			"http://www.freebase.org/freebase/domain_profile", 		// 15
			"http;//www.freebase.org/type/property", 				// 16	
			"http://www.freebase.org/common/topic",					// 17
			"http://www.freebase.org/type/namespace",				// 18
			"http://www.freebase.org/type/object",					// 19
			"http://www.freebase.org/freebase"						// 20
			
//			"http://www.w3.org/2004/02/skos/core#Concept"
			};
	private static String[] containerEdge = new String[]{
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#List",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#first",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil", };
	public static HashSet<String> rdfsEdgeSet, conEdgeSet;
	
	/* INSTANCE��STATICS FROM SXR */
	public static int[] instance = new int[]{   464843, 1644086,    7517743,  19238235,   14051039,   82702188};
	public static String[] ds = new String[]{"wordnet",  "dblp", "freebase", "dbpedia", "geonames", "uscensus"};
	public static HashMap<String, Integer> instNumMap;
	
	/**
	 * initialization
	 */
	static
	{
		rdfsEdgeSet = new HashSet<String>();
		for(String str: rdfsEdge)
			rdfsEdgeSet.add(str);
		
		conEdgeSet = new HashSet<String>();
		for(String str: containerEdge)
			conEdgeSet.add(str);
		
		instNumMap = new HashMap<String, Integer>();
		for(int i=0; i<ds.length; i++)
			instNumMap.put(ds[i], instance[i]);
	}
	
	/**
	 * remove blanknode
	 * @param fn
	 * @throws Exception
	 */
	public static void removeBlankNode(String fn) throws Exception
	{
		/* Output File Name */
		String blankNode = "_:node";
		String blankNodeFile = fn.substring(0, fn.lastIndexOf('.'))+".blanknode";
		String noBlankNodeFile = fn.substring(0, fn.lastIndexOf('.'))+"_noblanknode.nt";
		
		HashMap<String, String> blankNodeMap = new HashMap<String, String>();
		HashSet<String> bnMeansCollection = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(fn));
		PrintWriter pw1 = new PrintWriter(new FileWriter(blankNodeFile));
		PrintWriter pw2 = new PrintWriter(new FileWriter(noBlankNodeFile));
		
		/* First Scan Record BlankNode */
		String line;
		while((line = br.readLine())!=null)
		{
			String[] parts = line.replaceAll("<", "").replaceAll(">", "").split(" ");
			if(parts[0].startsWith(blankNode) && !parts[2].startsWith(blankNode))
			{
				if(bnMeansCollection.contains(parts[0]))
					pw1.println(line);
				else if(parts[1].equals(rdfsEdge[0]) && conEdgeSet.contains(parts[2]))
					bnMeansCollection.add(parts[0]);
			}
			else if(!parts[0].startsWith(blankNode) && parts[2].startsWith(blankNode))
				blankNodeMap.put(parts[2], "<"+parts[0]+"> <"+parts[1]+">");
			else if(!parts[0].startsWith(blankNode) && !parts[2].startsWith(blankNode))
				pw2.println(line);
		}
		br.close();
		pw1.close();
		
		/* Second Scan Connect BlankNode */
		br = new BufferedReader(new FileReader(blankNodeFile));
		while((line = br.readLine())!=null)
		{
			String[] parts = line.split(" ");
			if(blankNodeMap.containsKey(parts[0]))
				pw2.println(blankNodeMap.get(parts[0])+" "+parts[2]+" .");
		}
		pw2.close();
		br.close();

		/* Delete Temp File */
		System.gc();
		if(!new File(fn).delete())
			new File(fn).deleteOnExit();
		if(!new File(blankNodeFile).delete())
			new File(blankNodeFile).deleteOnExit();
		new File(noBlankNodeFile).renameTo(new File(fn));//cover the original nt file
	}

	/**
	 * load config file
	 * @param fn
	 * @throws Exception
	 */
	public static void getConfiguation(String fn) throws Exception
	{
		Properties prop = new Properties();
		InputStream is = new FileInputStream(fn);
		prop.load(is);
		/* Input File Para */
		root = prop.getProperty("root")+File.separator;
		datasource = prop.getProperty("domain");
		indexRoot = root+datasource;
		source = prop.getProperty("source");
		/* Output File Para */
		summaryObj = root+prop.getProperty("summaryObjsRoot")+File.separator+datasource+"-summary.obj";
		schemaObj = root+prop.getProperty("schemaObjsRoot")+File.separator+datasource+"-schema.obj";
		summaryRDF = root+prop.getProperty("summaryObjsRoot")+"-rdf"+File.separator+datasource+"-summary.rdf";
		schemaRDF = root+prop.getProperty("schemaObjsRoot")+"-rdf"+File.separator+datasource+"-schema.rdf";
		keywordIndex = root+datasource+"-keywordIndex";

		System.out.println("Root:"+root+"\r\nDataSource:"+datasource+"\r\nIndexRoot:"+indexRoot+"\r\nFileSource:"+source);
	}
	
	/**
	 * main entry
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception 
	{
		if(args.length!=4)
		{
			System.err.println("java BuildQ2SemanticService configFilePath(String) removeBlankNode(boolean) sortNTFile(boolean) isBigNT(boolean)");
			return;
		}
		long start = System.currentTimeMillis();
		
		/* load configFile */
		getConfiguation(args[0]);
		
		/* remove blank node */
		if(args[1].equals("true"))
			removeBlankNode(source);
		
		/* sort the nt file */
		if(args[2].equals("true"))
			new LineSortFile(source).sortFile();
		
		/* build summary and schema graphs */
		SummaryGraphIndexServiceForBTFromNT graphBuilder = new SummaryGraphIndexServiceForBTFromNT();
		graphBuilder.buildGraphs(indexRoot);//db index location
		
		/* build splitted-summary graphs */
		SplitSummaryGraphIndexServiceForBTFromNT splitGraphBuilder = new SplitSummaryGraphIndexServiceForBTFromNT();
		splitGraphBuilder.buildGraphs(indexRoot);//db index location
		
		/* build keywordindex */
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = graphBuilder.readGraphIndexFromFile(schemaObj);
		KeywordIndexServiceForBTFromNT keywordBuilder = new KeywordIndexServiceForBTFromNT(keywordIndex, true);
		keywordBuilder.indexKeywords(source, datasource, graph, Boolean.valueOf(args[3]));
		
		long end = System.currentTimeMillis();
		System.out.println("Time customing: "+(end-start)+" ms");
	}
}
