package org.aifb.xxplore.shared.util;

import java.util.EventObject;

public class ModelChangeEvent extends EventObject{

	private static final long serialVersionUID = 1L;

	public static final int SUBJECT_CHANGE = 0; 
	
	public static final int PREDICATE_CHANGE = 1; 
	
	public static final int OBJECT_CHANGE = 2; 
	
	public static final int QUERY_CHANGE = 3; 

	private int m_type = -1; 
	
	public ModelChangeEvent(Object source, int type) {
		super(source);
		m_type = type;

	}

	public int getType(){
		return m_type;
	}


}
