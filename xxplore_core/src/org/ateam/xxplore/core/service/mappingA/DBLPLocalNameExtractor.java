package org.ateam.xxplore.core.service.mappingA;

/**
 * Used to extract the local name from a DBLP URI
 * @author Linyun Fu
 *
 */
public class DBLPLocalNameExtractor implements ILocalNameExtractor {

	@Override
	public String getLocalName(String uri) {
		String tmp = null;
		
		int pos = uri.lastIndexOf("#");
		if(pos == -1) {
			pos = uri.lastIndexOf("/");
			tmp = uri.substring(pos + 1, uri.length()-1);
		} else {
			tmp = uri.substring(pos + 1,uri.length()-1);
		}
		
		if (tmp.endsWith(".html")) tmp = tmp.substring(0, tmp.length()-5);
		int cpos = tmp.indexOf(":");
		if (cpos == -1) return tmp;
		String prefix = tmp.substring(0, cpos);
		int cutpos = tmp.indexOf("_", cpos);
		String suffix = null;
		if (cutpos == -1) suffix = tmp.substring(cpos+1);
		else suffix = tmp.substring(cpos+1, cutpos);
		return suffix+"_"+prefix;

	}

}
