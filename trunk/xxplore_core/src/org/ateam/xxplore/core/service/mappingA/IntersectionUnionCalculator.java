package org.ateam.xxplore.core.service.mappingA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class IntersectionUnionCalculator implements IClassSimilarityCalculator {

	@Override
	public void calculate(String rawClassMap, String classSummary1,
			String classSummary2, String output) throws Exception {
		// TODO Auto-generated method stub
		HashMap<String, Integer> classSize1 = loadMap(classSummary1);
		HashMap<String, Integer> classSize2 = loadMap(classSummary2);
		BufferedReader br = new BufferedReader(new FileReader(rawClassMap));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			int intersection = Integer.parseInt(part[2]);
			int size1 = classSize1.get(part[0]);
			int size2 = classSize2.get(part[1]);
			int union = size1+size2-intersection;
			double sim = (intersection+1.0)/(union+1.0);
			pw.printf("%s\t%s\t%0.6f\n", part[0], part[1], sim);
		}
		br.close();
		pw.close();
	}

	private HashMap<String, Integer> loadMap(String classSummary) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(classSummary));
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			ret.put(part[0], Integer.parseInt(part[1]));
		}
		br.close();
		return ret;
	}

}
