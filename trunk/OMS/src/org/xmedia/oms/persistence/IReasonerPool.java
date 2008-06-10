package org.xmedia.oms.persistence;

import java.util.Map;

public interface IReasonerPool {
	
	public void configure(Map properties);
	
	public Object getAvailableReasoner();
	
	public void freeReasoner(Object reasoner);
	
	public void dispose();

}
