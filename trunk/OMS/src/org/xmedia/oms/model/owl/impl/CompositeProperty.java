package org.xmedia.oms.model.owl.impl;

import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.impl.Resource;
import org.xmedia.oms.model.owl.api.ICompositeProperty;

public class CompositeProperty extends Resource implements ICompositeProperty{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1123249947719815552L;
	
	public CompositeProperty(){super();};
	
	public CompositeProperty(IOntology onto){
		super(onto);
	}

	public CompositeProperty(String label){
		super(label);
	}
	
	public CompositeProperty(String label, IOntology onto){
		super(label, onto);
	}

}
