package org.team.xxplore.core.service.q2semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.team.xxplore.core.service.mapping.MappingIndexService;


/**
 * This class is the Q2Semantic service API that can be called by the search engine interface
 * @author tpenin
 */

public class Q2SemanticService {

	public static String root;
	public static String summaryObjsRoot;
	public static String schemaObjsRoot;
	public static String keywordIndexRoot;
	public static String mappingIndexRoot;
	public static HashSet<String> keywordIndexSet;
	public static HashMap<String, String> summaryObjSet;
	public static HashMap<String, String> schemaObjSet;
	public static final String ConceptMark = "c", PredicateMark = "p";
	public QueryInterpretationService inter;
	public MappingIndexService mis = new MappingIndexService();

	public Q2SemanticService(){}
	
	public Q2SemanticService(String fn) {
		try {
			this.loadPara(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<String,Collection<SummaryGraphElement>> searchKeyword(LinkedList<String> queryList,double prune) {
		//search for elements
		Map<String,Collection<SummaryGraphElement>> elementsMap = new HashMap<String,Collection<SummaryGraphElement>>();

		for(String qt : queryList) {
			for(String ds : summaryObjSet.keySet()) {
				String keywordIndex = keywordIndexRoot + "/" + ds + "-keywordIndex";
				System.out.println("keywordIndex " + keywordIndex);
				Map<String, Collection<SummaryGraphElement>> hm = 
					new KeywordIndexServiceForBTFromNT(keywordIndex, false).searchKb(qt, prune);
	
				for(String key_str : hm.keySet()) {
					Collection<SummaryGraphElement> coll = elementsMap.get(key_str);
					if(coll == null) {
						elementsMap.put(key_str, hm.get(key_str));
					}
					else {
						coll.addAll(hm.get(key_str));
					}
				}
			}
		}
				
		for(String key : elementsMap.keySet()) {
			Collection<SummaryGraphElement> t = elementsMap.get(key);
			System.out.println("=================================");
			System.out.println(key + " : ");
			for(SummaryGraphElement ele : t) {
				System.out.println(SummaryGraphUtil.getResourceUri(ele) + "\t" + ele.getDatasource());
			}
			System.out.println();
			System.out.println();
			
		}
		return elementsMap;
	}
	
	/**
	 * This method returns an ordered list of QueryGraph objects (most suitable at the head of the list) that 
	 * are possible semantic interpretations of the ordered list of keywords (first input word at the head of the 
	 * list) provided as a parameter. The most relevant graphs only are provided, respecting the maximum number of 
	 * graph condition.
	 * @param keywordList
	 * @param topNbGraphs
	 * @return
	 */
	public LinkedList<Subgraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs, double prune, int distance,
			double edge_score) {
		
		// TODO
		// Note: I will certainly have to find a way to serialize this list of graphs to XML... (tpenin)
		
		QueryInterpretationService.EDGE_SCORE = edge_score;		
		Map<String, Collection<SummaryGraphElement>> elementsMap = searchKeyword(keywordList,prune);
		
		if(elementsMap.size()<keywordList.size()) return null;
		
		LinkedList<Subgraph> graphs = inter.computeQueries(elementsMap, distance, topNbGraphs);
		if(graphs == null) return null;
//		LinkedList<QueryGraph> result = this.getQueryGraphFromTopKResult(graphs);
//		
//		for(int i=0; i<result.size(); i++) {
//			System.out.println("=============== Top "+(i+1)+" QueryGraph ==============");
//			result.get(i).print();
//		}

		return graphs;
	}
	
	public void loadPara(String fn) throws Exception {
		Properties prop = new Properties();
		InputStream is = new FileInputStream(fn);
		prop.load(is);
		root = prop.getProperty("root")+File.separator;
		summaryObjsRoot = root+prop.getProperty("summaryObjsRoot")+File.separator;
		schemaObjsRoot = root+prop.getProperty("schemaObjsRoot")+File.separator;
		keywordIndexRoot = root+prop.getProperty("keywordIndexRoot")+File.separator;
		mappingIndexRoot = root+prop.getProperty("mappingIndexRoot")+File.separator;
		System.out.println("Root:"+root+"\r\nsummaryObjsRoot:"+summaryObjsRoot+"\r\nschemaObjsRoot:"+schemaObjsRoot+"\r\nkeywordIndexRoot:"+keywordIndexRoot+"\r\nmappingIndexRoot:"+mappingIndexRoot);

		//		add keywordindexes
		keywordIndexSet = new HashSet<String>();
		File[] indexes = new File(keywordIndexRoot).listFiles();
		for(File index: indexes)
			keywordIndexSet.add(index.getAbsolutePath());

		//		add graphs
		summaryObjSet = new HashMap<String, String>();
		File[] summaries = new File(summaryObjsRoot).listFiles();
		for(File summary: summaries)
			summaryObjSet.put(summary.getName().substring(0, summary.getName().lastIndexOf('-')), summary.getAbsolutePath());
		schemaObjSet = new HashMap<String, String>();
		File[] schemas = new File(schemaObjsRoot).listFiles();
		for(File schema: schemas)
			schemaObjSet.put(schema.getName().substring(0, schema.getName().lastIndexOf('-')), schema.getAbsolutePath());
		
		inter = new QueryInterpretationService(summaryObjSet.keySet());		
		mis.init4Search(mappingIndexRoot);
		
	}
}
