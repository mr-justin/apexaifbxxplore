package org.aifb.xxplore.shared.util;

import org.aifb.xxplore.shared.vocabulary.XMLSchema;

public class URIHelper {

	
	public static String getNamespace(String uri) {
		int index = uri.indexOf("#");
		
		if (index < 0)
			index = uri.lastIndexOf("/");
		
		if (index < 0)
			return null;
		
		return uri.substring(0, index + 1);
	}
	
	public static String truncateUri(String uri) {
		
		int index = uri.indexOf("#");
		
		if(index < 0){
			index = uri.lastIndexOf("/");
		}
		else{
			String this_namespace = uri.substring(0,uri.indexOf("#")+1);

			if(this_namespace.equals(XMLSchema.NAMESPACE)){
				return "xsd:"+uri.substring(index+1);
			}
		}

		return index >= 0 ? uri.substring(index+1) : uri;
	}
}
