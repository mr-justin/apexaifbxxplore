package basic;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Intersection/Union class mapping with identical-local-name class pairs removed
 * @author fulinyun
 *
 */
public class IURemoveCalculator {

	public void calculate(String rawClassMap, String sameNameMap, String output) throws Exception {
		HashSet<String> sameName = loadMap(sameNameMap);
		BufferedReader br = new BufferedReader(new FileReader(rawClassMap));
		HashMap<String, Integer> classSize1 = new HashMap<String, Integer>();
		HashMap<String, Integer> classSize2 = new HashMap<String, Integer>();
		HashMap<String, Integer> intersectionSize = new HashMap<String, Integer>();
		int total = 0;
		int lineCount = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			int intersection = Integer.parseInt(part[2]);
			if (!sameName.contains(part[0]) && !sameName.contains(part[1])) 
				intersectionSize.put(part[0] + "\t" + part[1], intersection);
			add2map(classSize1, part[0], intersection);
			add2map(classSize2, part[1], intersection);
			total += intersection;
			lineCount++;
		}
		br.close();
		
		int average = total/lineCount;
		for (Iterator<String> is = intersectionSize.keySet().iterator(); is.hasNext(); ) {
			String key = is.next();
			if (intersectionSize.get(key) < average) is.remove();
		}
		TreeMap<Double, ArrayList<String>> tm = new TreeMap<Double, ArrayList<String>>(new Comparator<Double>() {
			public int compare(Double a, Double b) {
				if (a < b) return 1;
				if (a > b) return -1;
				return 0;
			}
		});
		for (String key : intersectionSize.keySet()) {
			String[] part = key.split("\t");
			int intersection = intersectionSize.get(key);
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
		
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (Double d : tm.keySet()) for (String s : tm.get(d)) pw.printf("%s\t%.6f\n", s, d);
		pw.close();

	}

	private HashSet<String> loadMap(String sameNameMap) throws Exception {
		if (sameNameMap == null) return new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(sameNameMap));
		HashSet<String> ret = new HashSet<String>();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] part = line.split("\t");
			ret.add(part[0]);
			ret.add(part[1]);
		}
		br.close();
		return ret;
	}

	private void add2map(HashMap<String, Integer> classSize, String string,
			int intersection) {
		if (classSize.keySet().contains(string)) classSize.put(string, classSize.get(string)+intersection);
		else classSize.put(string, intersection);

	}

}
