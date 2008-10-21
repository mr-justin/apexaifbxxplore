package org.ateam.xxplore.core.service.q2semantic;

import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.Property;

public class SummaryGraphUtil {
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
		return line.replaceFirst("\\u0028.*\\u0029", "");
	}
	
	public static String removeGtOrLs(String line) {
		if(line == null || line.length() == 0) return line;
		
		int begin = line.charAt(0) == '<' ? 1 : 0;
		int end = line.charAt(line.length() - 1) == '>' ? line.length() - 1 : line.length();
		return line.substring(begin,end);
	}
	
	public static String getLocalName(String uri) {
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
