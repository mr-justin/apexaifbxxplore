package org.xmedia.oms.model.owl.api;

import java.util.Set;

import org.xmedia.oms.model.api.IConcept;

public interface ICompositeConcept extends IConcept {

	public void addConcept(IConcept concept);
	
	public Set<IConcept> getConcepts();
	
	public static int UNION = 0;
	
	public static int INTERSECTION = 1;
	
	public int getType();
	
}
