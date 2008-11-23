package org.team.xxplore.core.service.mapping;

/**
 * Used to extract the local name from a DBpedia URI
 * @author Linyun Fu
 *
 */
public class DBpediaLocalNameExtractor implements ILocalNameExtractor {

	@Override
	public String getLocalName(String uri) {
		String ret = null;
		
		int pos = uri.lastIndexOf("#");
		if(pos == -1) {
			pos = uri.lastIndexOf("/");
			ret = uri.substring(pos + 1, uri.length()-1);
		} else {
			ret = uri.substring(pos + 1, uri.length()-1);
		}
		return ret;
	}

}
