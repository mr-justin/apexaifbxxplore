package org.aifb.xxplore.core.service.labelizer;

//import net.didion.jwnl.data.POS;

public class LabelizerEnvironment {

	
	public static final String CONCEPT = "Concept";
	public static final String OBJECT_PROPERTY = "ObjectProperty";
	public static final String DATA_PROPERTY = "DataProperty";
	public static final String INDIVIDUAL = "Individual";
	public static final String LITERAL = "Literal";
	
	public static final String LABEL = "Label";
	public static final String SUBLABEL = "Sublabel";
	public static final String ALT_LABEL = "AltLabel";
	
	public static final String URI = "URI";
	public static final String INDEXING_DP = "IndexingDP";
	public static final String LABEL_FULL_STRING = "LabelFullString";
	public static final String LABEL_STRING = "LabelString";
	
	public static final String C_NODE = "CNode";
	public static final String K_NODE = "KNode";
	public static final String R_EDGE = "REdge";
	public static final String A_EDGE = "AEdge";
	
	public static final String WordNet_Properties_File = "c:/MasterThesis/file_properties.xml";

	public static int countNumWords(String keyword) {
		return keyword.split("[ ]+").length;
	}
	
	public static String replaceUnderscoresInString(String keyword) {
		return keyword.replace("_", " ");
	}
	
	public static String pruneString(String str) {
		return str.replace("\"", "").toLowerCase();
	}
	
	/**
	 * Apply the label full-string heuristics that determine if the data property it belongs to is still
	 * a possible labeling data property for the concepts of the ontology 
	 * @param labelFullString the label full-string to be tested by the heuristics
	 * @return true if the label full-string passed the tests of all heuristics
	 * 		   false if the label full-string has failed on of the tests
	 */
	public static boolean applyHeuristics(String labelFullString) {

//		/* 1st heuristic: Not an URI (URL or e-mail address) */
//		String uriPattern = "^(([^:/?#]+):)((//([^/?#]*))([^?#]*)(\\?([^#]*))?)?(#(.*))?";
//		Pattern p = Pattern.compile(uriPattern);
//		Matcher m = p.matcher(labelFullString);
//		if (m.matches())
//			return false;
//
//		/* 2nd heuristic: Not a pure number or combination of numbers */
//		String combNumberPattern = "([\\W]*[\\d]+[\\W]*)+";
//		p = Pattern.compile(combNumberPattern);
//		m = p.matcher(labelFullString);
//		if (m.matches())
//			return false;

		/* 3rd heuristic: Doesn't have long length */
		if (labelFullString.length() > 255) {
			return false;
		}
		return true;
	}
}
