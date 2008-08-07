/**
 * 
 */
package com.ibm.semplore.imports.impl;

/**
 * @author lql
 *
 */
public class Util4NT {
	public static final String RELATION = "3r:";
	public static final String CATEGORY = "2c:";
	public static final String ATTRIBUTE = "4a:";
	public static final String INSTANCE = "1i:";
		
	public static final String TYPE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

	public static String checkTripleType(String[] triple) {
		if (triple==null || triple.length<3 || triple[0].charAt(0)!='<' || triple[1].charAt(0)!='<')
			return null;
		else if (triple[1].equals(TYPE))
			return Util4NT.CATEGORY;
		else if (triple[2].charAt(0)=='<')
			return Util4NT.RELATION;
		else
			return Util4NT.ATTRIBUTE;
	}
	
}
