package org.team.xxplore.core.service.q2semantic;

import org.team.xxplore.core.service.impl.DataProperty;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.Property;

/**
 * This class offer some misc function to operation the summary graph.
 * @author jqchen
 *
 */
public class SummaryGraphUtil {
	public static String[] stopWords = {"Property-3A","Category-3A"};
	
	/**
	 * Get the uri of a summary graph element.
	 * @param ele
	 * @return
	 */
	public static String getResourceUri(SummaryGraphElement ele) {
		if(ele.getType() == SummaryGraphElement.CONCEPT) {
			return  ((NamedConcept)ele.getResource()).getUri();
		}
		else if(ele.getType() == SummaryGraphElement.ATTRIBUTE || ele.getType() == SummaryGraphElement.RELATION){
			return ((Property)ele.getResource()).getUri();
		}
		else {
			if(ele.getResource() instanceof DataProperty) {
				return ((Property)ele.getResource()).getUri();
			}
			return ele.getResource().getLabel();
		}
	}
	
	public static String removeNum(String line) {
		String res = line.replaceFirst("\\u0028.*\\u0029", "");

		return res;
	}
	
	/**
	 * remove the first < or > from a string.
	 * @param line
	 * @return
	 */
	public static String removeGtOrLs(String line) {
		if(line == null || line.length() == 0) return line;
		
		int begin = line.charAt(0) == '<' ? 1 : 0;
		int end = line.charAt(line.length() - 1) == '>' ? line.length() - 1 : line.length();
		return line.substring(begin,end);
	}
	
	/**
	 * Get the local name of the uri.
	 * @param uri
	 * @return
	 */
	public static String getLocalName(String uri) {
		for(String stopWord: stopWords)
			uri = uri.replaceAll(stopWord, "");
		if( uri.lastIndexOf("#") != -1 ) {
			return uri.substring(uri.lastIndexOf("#") + 1);
		}
		else if(uri.lastIndexOf("/") != -1) {
			return uri.substring(uri.lastIndexOf("/") + 1);
		}
		else {
			return uri;
		}
	}
}
