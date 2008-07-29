package org.aifb.xxplore.core.service.keywordtrans;

public interface KbElement {
	
	public static final int CVERTEX = 0;
	
	public static final int VVERTEX = 1;
	
	public static final int AEDGE = 2;
	
	public static final int REDGE = 3;
	
	public static final int DUMMY = 4;
	
	public int getType();
}
