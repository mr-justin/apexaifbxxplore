package org.team.xxplore.core.service.search.session;

import com.ibm.semplore.btc.Graph;

public interface Operation {

	public Graph applyTo(Graph graph);
		
	public Graph undo(Graph graph);
	
}
