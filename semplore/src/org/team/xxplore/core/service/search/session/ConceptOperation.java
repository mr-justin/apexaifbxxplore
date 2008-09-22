package org.team.xxplore.core.service.search.session;

import org.team.xxplore.core.service.search.datastructure.Concept;

import com.ibm.semplore.btc.Graph;

public class ConceptOperation implements FacetOperation {

	private Concept concept;
	
	public ConceptOperation(Concept c) {
		concept = c;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		
	}

	@Override
	public Graph undo(Graph graph) {
		// TODO Auto-generated method stub
		return null;
	}

}
