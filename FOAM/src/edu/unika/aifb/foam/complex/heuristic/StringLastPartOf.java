package edu.unika.aifb.foam.complex.heuristic;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

public class StringLastPartOf implements Heuristic {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public double get(Object object1, Object object2) {
		String word1;
		String word2;
		try {
			word1 = (String) object1;
			word2 = (String) object2;
		} catch (Exception e) {
			word1 = object1.toString();
			word2 = object2.toString();
		}
		if ((word1 == null) || (word2 == null))
			return 0;
		word1 = word1.toLowerCase();
		word2 = word2.toLowerCase();
		if (word1.length() == word2.length()) return 0;
		int index = word1.lastIndexOf(word2);
		if (index == -1)
			return 0;
		else {
			int word1Length = word1.length();
			int word2Length = word2.length();
			int intCheck = word1Length - index;
			if (intCheck == word2Length) {				
				return 1;
			} else {
				return 0;
			}
		}
	}

	public void setPreviousResult(ResultTable resultTable) {
		// TODO Auto-generated method stub

	}

}
