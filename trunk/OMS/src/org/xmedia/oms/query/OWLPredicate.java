package org.xmedia.oms.query;

import org.xmedia.oms.model.impl.Resource;

public abstract class OWLPredicate extends Resource{
			
	public OWLPredicate(){
		super();
	}
	
	private int m_arity;
	
	public int getArity(){
		return m_arity;
	}
}
