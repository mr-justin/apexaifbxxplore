package org.xmedia.oms.query;

import org.xmedia.oms.model.impl.Resource;



public class Variable extends Resource{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7430407598387797696L;
	private String m_name;
	
	
	public Variable(String name){
		super(name);
		m_name = name;
	}
	
	public String getName(){
		return m_name;
	}
	
	public String getLabel(){
		return m_name;
	}
}
