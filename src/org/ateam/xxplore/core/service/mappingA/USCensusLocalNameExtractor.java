package org.ateam.xxplore.core.service.mappingA;

/**
 * Used to extract the local name from a USCensus URI
 * @author Linyun Fu
 *
 */
public class USCensusLocalNameExtractor implements ILocalNameExtractor {

	@Override
	public String getLocalName(String uri) {
		int pos = uri.lastIndexOf("/");
		String suffix = uri.substring(pos+1, uri.length()-1);
		String[] token = suffix.split("_");
		String ret = Character.toUpperCase(token[0].charAt(0))+token[0].substring(1);
		for (int i = 1; i < token.length; i++) {
			ret += "_";
			if (token[i].length() > 0) ret += Character.toUpperCase(token[i].charAt(0))+token[i].substring(1);
		}
		return ret;

	}

}
