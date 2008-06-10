package org.xmedia.oms.model.impl;

import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IOntology;

public class Concept extends Resource implements IConcept {
	
	/**
	 * 	
	 */
	private static final long serialVersionUID = -1241867652890268430L;
	
	public Concept(){super();};

	public Concept(IOntology onto){
		super(onto);
	}

	public Concept(String label){
		super(label);
	}
	
	public Concept(String label, IOntology onto){
		super(label, onto);
	}

}
