package org.apexlab.service.session.semplore;

import com.ibm.semplore.btc.Graph;

public interface Operation {

	public Graph applyTo(Graph graph);
		
	public Graph undo(Graph graph);
	
}
