package org.apexlab.service.session.semplore;

import org.apexlab.service.session.datastructure.ConceptSuggestion;

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
			if (!graph.getDataSource(graph.getTargetVariable()).equals(conceptSuggestion.getSource().getName())) {
				int originalTarget = graph.getTargetVariable();
				CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(1);	//AND
				Category c = SchemaFactoryImpl.getInstance().createCategory((conceptSuggestion.getURI()));
				cc.addComponentCategory(c);
				graph.add(cc);
				graph.setDataSource(graph.numOfNodes()-1, conceptSuggestion.getSource().getName());
				iedge = new Edge(originalTarget, graph.numOfNodes()-1, null);
				graph.addIEdges(iedge);
				graph.setTargetVariable(graph.numOfNodes()-1);
			}
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
