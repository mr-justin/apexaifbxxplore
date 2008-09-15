package org.ateam.xxplore.core.service.mappingA;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

public class IntersectionUnionCalculator implements IClassSimilarityCalculator {

	@Override
	public void calculate(String rawClassMap, String classSummary1,
			String classSummary2, String output) throws Exception {
		// TODO Auto-generated method stub
		HashMap<String, Integer> classSize1 = loadMap(classSummary1);
		HashMap<String, Integer> classSize2 = loadMap(classSummary2);
		BufferedReader br = new BufferedReader(new FileReader(rawClassMap));
		TreeMap<Double, ArrayList<String>> tm = new TreeMap<Double, ArrayList<String>>(new Comparator<Double>() {
			public int compare(Double a, Double b) {
				if (a < b) return 1;
				if (a > b) return -1;
				return 0;
			}
		});
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			int intersection = Integer.parseInt(part[2]);
			int size1 = classSize1.get(part[0]);
			int size2 = classSize2.get(part[1]);
			int union = size1+size2-intersection;
			double sim = (intersection+1.0)/(union+1.0);
			if (tm.keySet().contains(sim)) tm.get(sim).add(part[0] + "\t" + part[1]);
			else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(part[0] + "\t" + part[1]);
				tm.put(sim, list);
			}
		}
		br.close();
		
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (Double d : tm.keySet()) for (String s : tm.get(d)) pw.printf("%s\t%.6f\n", s, d);
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
