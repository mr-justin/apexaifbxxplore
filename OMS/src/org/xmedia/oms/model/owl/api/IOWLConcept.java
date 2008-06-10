package org.xmedia.oms.model.owl.api;

import java.util.Set;

import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.INamedConcept;

public interface IOWLConcept extends INamedConcept, IOWLEntity{

	public Set<IConcept> getDisjointConcepts();
	
	public Set<IConcept> getEquivalentConcepts();

}
