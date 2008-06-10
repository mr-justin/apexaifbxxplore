package org.xmedia.oms.model.owl.api;

import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IProperty;

public interface IHasPropConcept extends IConcept {
	
	public IProperty getProperty();
	
	public IConcept getConcept();
	
	public static int SOME_OF = 0;
	
	public static int ALL_OF = 1;
		
	public int getType();
	
}
