package org.ateam.xxplore.core.service.mappingA;


public interface IClassSimilarityCalculator {

	/**
	 * Calculate similarity between classes based on their sizes and mapped instances
	 * @param rawClassMap is the name of the file that records the mapped classes and the number of mapped instances belonging to them
	 * @param classSummary1 is the name of the class summary file for data source 1
	 * @param classSummary2 is the name of the class summary file for data source 2
	 * @param output is the name of the output file
	 * @throws Exception
	 */
	public void calculate(String rawClassMap, String output) throws Exception;
}
