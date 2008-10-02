package org.ateam.xxplore.core.service.q2semantic;

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
			return ele.getResource().getLabel();
		}
	}
	
	
}
