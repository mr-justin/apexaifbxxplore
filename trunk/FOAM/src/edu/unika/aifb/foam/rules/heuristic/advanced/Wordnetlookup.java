/*
 * The method is disabled.
 * Created on 17.12.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.advanced;

import java.io.FileInputStream;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;
import edu.unika.aifb.foam.util.UserInterface;

/*import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;*/

/**
 * @author meh
 *
 */
public class Wordnetlookup implements Heuristic {

	private static final long serialVersionUID = 1L;
//	private static final String WORDNET_PROPERTIES_FILE = "C:/Documents/Implementation/FOAM/config/file_properties_local.xml";
	private static Syntactic syntactic = new Syntactic();
	
	public Wordnetlookup() {
/*		try {											//currently disabled, instead only the syntactic comparison is done
		JWNL.initialize(new FileInputStream(WORDNET_PROPERTIES_FILE));	//just uncomment to use; you need WordNet installed for this
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}*/
	}
	
	public double get(Object object1, Object object2) {
		return syntactic.get(object1,object2);
	}
	
/*	public double get(Object object1, Object object2) {
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
		if ((word1.indexOf(" ")!=-1)||(word2.indexOf(" ")!=-1)||(word1.indexOf(".")!=-1)||(word2.indexOf(".")!=-1)
				||(word1.indexOf(",")!=-1)||(word2.indexOf(",")!=-1)||(word1.indexOf("_")!=-1)||(word2.indexOf("_")!=-1)) {
			double result = 0.0;
			return result;
		}
		Synset[][] first_senses = new Synset[5][0];
		Synset[][] second_senses = new Synset[5][0];
		try {
		IndexWord first_word = Dictionary.getInstance().lookupIndexWord(POS.NOUN, word1);
		IndexWord second_word = Dictionary.getInstance().lookupIndexWord(POS.NOUN, word2);	
		first_senses[1] = first_word.getSenses();
		second_senses[1] = second_word.getSenses();
		} catch (Exception e ) {}
		try {
		IndexWord first_word = Dictionary.getInstance().lookupIndexWord(POS.VERB, word1);
		IndexWord second_word = Dictionary.getInstance().lookupIndexWord(POS.VERB, word2);	
		first_senses[2] = first_word.getSenses();
		second_senses[2] = second_word.getSenses();
		} catch (Exception e ) {}
		try {
		IndexWord first_word = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, word1);
		IndexWord second_word = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, word2);	
		first_senses[3] = first_word.getSenses();
		second_senses[3] = second_word.getSenses();
		} catch (Exception e ) {}
		try {
		IndexWord first_word = Dictionary.getInstance().lookupIndexWord(POS.ADVERB, word1);
		IndexWord second_word = Dictionary.getInstance().lookupIndexWord(POS.ADVERB, word2);	
		first_senses[4] = first_word.getSenses();
		second_senses[4] = second_word.getSenses();
		} catch (Exception e ) {}
		int first_length = first_senses[1].length+first_senses[2].length+first_senses[3].length+first_senses[4].length;
		int second_length = second_senses[1].length+second_senses[2].length+second_senses[3].length+second_senses[4].length;
		first_senses[0] = new Synset[first_length];
		second_senses[0] = new Synset[second_length];	
		int counter1 = 0;
		int counter2 = 0;
		for (int j = 1; j<=4; j++) {
			for (int i = 0; i<first_senses[j].length; i++) {
				first_senses[0][counter1] = first_senses[j][i];
				counter1++;
			}
			for (int i = 0; i<second_senses[j].length; i++) {
				second_senses[0][counter2] = second_senses[j][i];
				counter2++;
			}
		}		
		double first_count = first_senses[0].length;
		double second_count = second_senses[0].length;
		double result = 0;
		int index = 0;
		for (int i = 0; i < first_senses[0].length; i++) {
			Synset first_sense = first_senses[0][i];
			for (int k = 0; k < second_senses[0].length; k++) {
				Synset second_sense = second_senses[0][k];
				if (first_sense.equals(second_sense)) {
					index++;
				}
			}
		}
		if (first_count >= second_count)	result = index / first_count;
		if (first_count < second_count)	result = index / second_count;		
		if ((first_count==0)&&(second_count==0)) result = 0;
		return result;
	}*/
	
	public static void main(String[] args){
		Wordnetlookup wordnet = new Wordnetlookup();
		String string1 = "R_contact";
		String string2 = "R_booking";
		double result = wordnet.get(string1, string2);
		System.out.println(string1+", "+string2+" : "+result);
	}

	public void setPreviousResult(ResultTable resultTable) {
	}
}
