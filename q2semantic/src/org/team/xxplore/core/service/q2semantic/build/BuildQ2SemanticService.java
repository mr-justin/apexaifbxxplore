package org.team.xxplore.core.service.q2semantic.build;

import org.team.xxplore.core.service.q2semantic.LineSortFile;

/**
 * Service of building summary and schema graph
 * 
 * @author kaifengxu
 * 
 */
public class BuildQ2SemanticService {
	
	/**
	 * main entry
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.err
					.println("java BuildQ2SemanticService configFilePath(String) removeBlankNode(boolean) sortNTFile(boolean) scoring(boolean)");
			return;
		}
		long start = System.currentTimeMillis();

		Parameters.setConfigFilePath(args[0]);
		Parameters para = Parameters.getParameters();

		if (args[1].equals("true")) {
			new RemoveBlankNode(para.source, "_:node").removeBlankNode();
		}
		if (args[2].equals("true")) {
			new LineSortFile(para.source).sortFile();
		}

		SplitSummaryGraphIndexServiceForBTFromNT splitGraphBuilder = new SplitSummaryGraphIndexServiceForBTFromNT();
		splitGraphBuilder.buildGraphs(para.indexRoot, Boolean.parseBoolean(args[3]));// db index location
		KeywordIndexBuilder keywordBuilder = new KeywordIndexBuilder();
		keywordBuilder.indexKeywords(para.keywordIndex, para.datasource);

		long end = System.currentTimeMillis();
		System.out.println("Time customing: " + (end - start) + " ms");
	}
}
