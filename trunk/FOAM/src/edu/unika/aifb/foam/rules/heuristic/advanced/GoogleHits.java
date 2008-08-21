/**
 * The methods are disabled. 
 */
package edu.unika.aifb.foam.rules.heuristic.advanced;

import java.util.Hashtable;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.util.UserInterface;

public class GoogleHits implements Heuristic {
	
	private static final long serialVersionUID = 1L;
	private final static String GOOGLEKEY = "";
	private Hashtable hash = new Hashtable();

	public double get(Object object1, Object object2) {
		double finalresult;
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
		try {
		Object result1 = hash.get(word1);
		if (result1 == null) {
			result1 = retrieveGoogleHits(word1);
			hash.put(word1,result1);
		}
		Double hits1 = (Double) result1;
		Object result2 = hash.get(word2);
		if (result2 == null) {
			result2 = retrieveGoogleHits(word2);
			hash.put(word2,result2);			
		}
		Double hits2 = (Double) result2;
		Object result12 = hash.get(word1+word2);
		if (result12 == null) {
			result12 = retrieveGoogleHits(word1+" "+word2);
			hash.put(word1+word2,result12);			
		}
		Double hits12 = (Double) result12;
		Object result21 = hash.get(word2+word1);
		if (result21 == null) {
			result21 = retrieveGoogleHits(word2+" "+word1);
			hash.put(word2+word1,result21);			
		}
		Double hits21 = (Double) result21;
		finalresult = (hits12.doubleValue()+hits21.doubleValue())/(hits1.doubleValue()/1000000*hits2.doubleValue()/1000000*100);
		if (finalresult>1.0) {finalresult = 1.0;}
		return finalresult;
		} catch (Exception e) {
			return 0;
		}
	}
	
	private Double retrieveGoogleHits(String word) {
	    GoogleSearch gs = new GoogleSearch();
	    gs.setKey(GOOGLEKEY); 
	    gs.setQueryString( "\""+ word +"\"" );
	    GoogleSearchResult gsr = null;
	    GoogleSearchResult gsr1 = null;	    
	    try {
	        gsr = gs.doSearch();
	        gsr1 = gs.doSearch();
	    } 
	    catch( GoogleSearchFault e){
			UserInterface.print(e.getMessage());
	      return null;
        }
	    int number = gsr.getEstimatedTotalResultsCount()+gsr1.getEstimatedTotalResultsCount();
	    return new Double(0.5*number);
	}
	    
	public static void main(String[] args) {
		GoogleHits google = new GoogleHits();
		double res = google.get("fanta","glass");
		System.out.println(res);
		res = google.get("fanta","honey");
		System.out.println(res);
		res = google.get("honey","glass");
		System.out.println(res);
		res = google.get("fanta","glass");
		System.out.println(res);
	}

	public void setPreviousResult(ResultTable resultTable) {
	}
}
