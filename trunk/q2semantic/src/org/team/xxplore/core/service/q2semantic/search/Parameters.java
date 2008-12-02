package org.team.xxplore.core.service.q2semantic.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Parameters {
	
	public static Parameters single = null;
	public static String configFilePath = null;
	
	public double EDGE_SCORE = 0.5;
	public double penalty = 1.0;
	
	public static void setConfigFilePath(String fn) {
		configFilePath = fn;
	}
	
	public static Parameters getParameters() {
		if(single == null) {
			single = new Parameters(configFilePath);
		}
		return single;
	}
	
	public String root;
	public String summaryObjsRoot;
	public String keywordIndexRoot;
	public String mappingIndexRoot;
	public int maxKeywordSearchResult = 5;
	
	public HashSet<String> keywordIndexSet;
	public HashMap<String, String> summaryObjSet;
	
	public Set<String> getDataSourceSet() {
		return summaryObjSet.keySet();
	}
	
	private Parameters(String fn) {
		loadPara(fn);
	}
	
	private void loadPara(String fn) {
		try {
			Properties prop = new Properties();
			InputStream is = new FileInputStream(fn);
			prop.load(is);
			root = prop.getProperty("root")+File.separator;
			summaryObjsRoot = root+prop.getProperty("summaryObjsRoot")+File.separator;
			keywordIndexRoot = root+prop.getProperty("keywordIndexRoot")+File.separator;
			mappingIndexRoot = root+prop.getProperty("mappingIndexRoot")+File.separator;
			System.out.println("Root:"+root+"\r\nsummaryObjsRoot:"+summaryObjsRoot+"\r\nkeywordIndexRoot:"+keywordIndexRoot+"\r\nmappingIndexRoot:"+mappingIndexRoot);

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
			is.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
