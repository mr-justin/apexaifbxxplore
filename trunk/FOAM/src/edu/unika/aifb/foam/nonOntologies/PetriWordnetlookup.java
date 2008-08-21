package edu.unika.aifb.foam.nonOntologies;

import java.util.StringTokenizer;
import java.util.Vector;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.advanced.Wordnetlookup;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;
import edu.unika.aifb.foam.rules.heuristic.simple.SyntacticNumber;

public class PetriWordnetlookup implements Heuristic {

	private static final long serialVersionUID = 1L;
	private Wordnetlookup wordnetlookup = new Wordnetlookup();
	private SyntacticNumber syntactic = new SyntacticNumber();
	
	public void setPreviousResult(ResultTable resultTable) {
	}

	public double get(Object object1, Object object2) {
		String word1;
		String word2;
		try {
		word1 = (String) object1;
		word2 = (String) object2;
//		System.out.println(word1+" "+word2);
		} catch (Exception e) {
			word1 = object1.toString();
			word2 = object2.toString();
		}
		if ((word1 == null)||(word2 == null)) return 0;
		if ((word1.length()<3)||(word2.length()<3)) return 0;
		
		Vector word1elementsa = new Vector();				//load tokens
		Vector word1elementsb = new Vector();
		StringTokenizer st = new StringTokenizer(word1,"\t\n\r\f,;_-()[] ");
		while (st.hasMoreTokens())
		{	
			String token = st.nextToken();
			word1elementsa.add(token);
			word1elementsb.add(token);
		}
		int size1a = word1elementsa.size();
		for (int i = 0; i<(size1a-1); i++ ) {
			word1elementsb.add(word1elementsa.elementAt(i)+" "+word1elementsa.elementAt(i+1));
		}
		int size1b = word1elementsb.size();
		Vector word2elementsa = new Vector();
		Vector word2elementsb = new Vector();
		st = new StringTokenizer(word2,"\t\n\r\f,;_-()[] ");
		while (st.hasMoreTokens())
		{	
			String token = st.nextToken();
			word2elementsa.add(token);
			word2elementsb.add(token);
		}
		int size2a = word2elementsa.size();
		for (int i = 0; i<(size2a-1); i++ ) {
			word2elementsb.add(word2elementsa.elementAt(i)+" "+word2elementsa.elementAt(i+1));
		}
		int size2b = word2elementsb.size();
		
		double[][] simil = new double[word1elementsb.size()][word2elementsb.size()];		//calculate similarities
		for (int i = 0; i<size1b; i++) {
			for (int j = 0; j<size2b; j++) {
				double wordnetlook = wordnetlookup.get(word1elementsb.elementAt(i),word2elementsb.elementAt(j));
				double syntact = syntactic.get(word1elementsb.elementAt(i),word2elementsb.elementAt(j));
				if (wordnetlook>syntact) {
					simil[i][j] = wordnetlook;
				} else {
					simil[i][j] = syntact;
				}
			}
		}
		
		boolean moreleft = true;			//take only best similarities
		double max = -0.01;						// and remove the others
		int max1 = -1;
		int max2 = -1;
		int number = 0;
		double aggregate = 0;
		while (moreleft) {
			for (int i = 0; i<size1b; i++) {
				for (int j = 0; j<size2b; j++) {
					if (simil[i][j]>max) {
						max = simil[i][j];
						max1 = i;
						max2 = j;
					}
				}
			}
			double toadd = 0;
			if ((max1>=0)&&(max1<size1a)) {
				number++;
				toadd = toadd+max;
				word1elementsa.remove(word1elementsb.elementAt(max1));
				for (int i = 0; i<size2b; i++) {
					simil[max1][i]=-0.01;
				}
			}
			if (max1>=size1a) {
				number = number+2;
				toadd = toadd+2*max;
				word1elementsa.remove(word1elementsb.elementAt(max1-size1a));
				word1elementsa.remove(word1elementsb.elementAt(max1-size1a+1));
				for (int i = 0; i<size2b; i++) {
					simil[max1-size1a][i]=-0.01;
				}
				for (int i = 0; i<size2b; i++) {
					simil[max1-size1a+1][i]=-0.01;
				}
				for (int i = 0; i<size2b; i++) {
					simil[max1][i]=-0.01;
				}
			}
			if ((max2>=0)&&(max2<size2a)) {
				number++;
				toadd = toadd + max;
				word2elementsa.remove(word2elementsb.elementAt(max2));
				for (int i = 0; i<size1b; i++) {
					simil[i][max2]=-0.01;
				}
			}
			if (max2>=size2a) {
				number = number+2;
				toadd = toadd+2*max;
				word2elementsa.remove(word2elementsb.elementAt(max2-size2a));
				word2elementsa.remove(word2elementsb.elementAt(max2-size2a+1));
				for (int i = 0; i<size1b; i++) {
					simil[i][max2-size2a]=-0.01;
				}
				for (int i = 0; i<size1b; i++) {
					simil[i][max2-size2a+1]=-0.01;
				}
				for (int i = 0; i<size1b; i++) {
					simil[i][max2]=-0.01;
				}
			}
			aggregate = aggregate + toadd;
			max = -0.01;
			max1 = -1;
			max2 = -1;
			if ((word1elementsa.size()>0)&&(word2elementsa.size()>0)) {
				moreleft = true;
			} else {
				moreleft = false;
			}
		}
		number = number + word1elementsa.size()+word2elementsa.size();	//add those which didn't find a partner
		double result = aggregate/number;				//calculate average
		return result;
	}
	
	public static void main(String[] args){
		PetriWordnetlookup petriwordnet = new PetriWordnetlookup();
//		String string1 = "world soccer community";
//		String string2 = "global association football";
//		String string1 = "data-completed_check-availability";
//		String string2 = "C_DT_NR_N";
		String string1 = "R_contact";
		String string2 = "R_booking";
		double result = petriwordnet.get(string1, string2);
		System.out.println(string1+", "+string2+" : "+result);
	}

}
