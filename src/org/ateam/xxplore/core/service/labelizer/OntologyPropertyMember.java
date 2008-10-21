package org.ateam.xxplore.core.service.labelizer;

import org.xmedia.oms.model.api.INamedConcept;

public class OntologyPropertyMember {

	private INamedConcept _source;
	private INamedConcept _target;
	private int _numInstances;
	
	public OntologyPropertyMember(INamedConcept source, INamedConcept target) {
		_source = source;
		_target = target;
		_numInstances = 0;
	}
	
	public INamedConcept getSource() {
		return _source;
	}
	
	public INamedConcept getTarget() {
		return _target;
	}
	
	public int getNumInstances() {
		return _numInstances;
	}
	
	public void increaseNumInstances() {
		_numInstances ++;
	}
	
	public boolean equals(Object obj) {
		OntologyPropertyMember propertyMember = (OntologyPropertyMember) obj;
		if (getSource().equals(propertyMember.getSource()) && getTarget().equals(propertyMember.getTarget()))
			return true;
		return false;
	}
	
	public String toString() {
		return new String(getSource() + " --> " + getTarget());
	}
}
