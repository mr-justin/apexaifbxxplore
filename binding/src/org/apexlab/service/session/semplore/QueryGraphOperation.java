package org.apexlab.service.session.semplore;

import com.ibm.semplore.btc.Graph;

public class QueryGraphOperation implements Operation {

	private Graph previous;
	private Graph current;
	
	public QueryGraphOperation(Graph pre, Graph cur) {
		previous = pre;
		current = cur;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		return current;
	}

	@Override
	public Graph undo(Graph graph) {
		return previous;
	}

}
