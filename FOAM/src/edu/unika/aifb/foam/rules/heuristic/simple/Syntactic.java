/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.simple;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class Syntactic implements Heuristic {

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
		if ((word1 == null)||(word2 == null)) return 0;
		if ((word1.length()<3)||(word2.length()<3)) return 0;
		int minlength = 1;
		if (word1.length() < word2.length()) {
			minlength = word1.length();
		} else {
			minlength = word2.length();
		}
		double editdistance = editDistance(word1,word2);
		double syntacticsimmeasure = (minlength - editdistance) / minlength;
		if (syntacticsimmeasure < 0) {
			syntacticsimmeasure = 0;
		}
		if (syntacticsimmeasure > 1) {
			syntacticsimmeasure = 1;
		}
		return syntacticsimmeasure;
	}

	private int Minimum(int a, int b, int c) {
		int mi;
		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}

	private int editDistance(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost
		// Step 1
		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];
		// Step 2
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}
		// Step 3
		for (i = 1; i <= n; i++) {
			s_i = s.charAt(i - 1);
			// Step 4
			for (j = 1; j <= m; j++) {
				t_j = t.charAt(j - 1);
				// Step 5
				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}
				// Step 6
				d[i][j] =
					Minimum(
						d[i - 1][j] + 1,
						d[i][j - 1] + 1,
						d[i - 1][j - 1] + cost);
			}
		}
		// Step 7
		return d[n][m];
	}

	public void setPreviousResult(ResultTable resultTable) {
	}


}
