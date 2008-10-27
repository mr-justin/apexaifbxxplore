package org.team.xxplore.core.service.search.session;

import org.team.xxplore.core.service.search.datastructure.ConceptSuggestion;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;

public class ConceptSuggestionOperation implements SuggestionOperation {

	private ConceptSuggestion conceptSuggestion;
	private Edge iedge;
	
	public ConceptSuggestionOperation(ConceptSuggestion cs) {
		conceptSuggestion = cs;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		try {
			int originalTarget = graph.getTargetVariable();
			String originalSource = graph.getDataSource(originalTarget);
			CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(1);	//AND
			Category c = SchemaFactoryImpl.getInstance().createCategory((conceptSuggestion.getURI()));
			cc.addComponentCategory(c);
			graph.add(cc);
			graph.setDataSource(graph.numOfNodes()-1, conceptSuggestion.getSource().getName());
			iedge = new Edge(originalTarget, graph.numOfNodes()-1, null);
			graph.addIEdges(iedge);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}

	@Override
	public Graph undo(Graph graph) {
		try {
			int newTarget = graph.removeIEdge(iedge, graph.getTargetVariable());
			graph.setTargetVariable(newTarget);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
		
	}

}
