package org.team.xxplore.core.service.search.session;

import org.team.xxplore.core.service.search.datastructure.RelationSuggestion;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;

public class RelationSuggestionOperation implements SuggestionOperation {

	private RelationSuggestion relationSuggestion;
	private Edge iedge;
	
	public RelationSuggestionOperation(RelationSuggestion rs) {
		relationSuggestion = rs;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		try {
			int target = graph.getTargetVariable();
			Category c = SchemaFactoryImpl.getInstance().createUniversalCategory();
			graph.add(c);
			graph.setDataSource(graph.numOfNodes()-1, relationSuggestion.getSource().getName());
			CompoundCategory cc = SchemaFactoryImpl.getInstance().createCompoundCategory(1);	//AND
			Category c1 = SchemaFactoryImpl.getInstance().createUniversalCategory();
			cc.addComponentCategory(c1);
			graph.add(cc);
			graph.setDataSource(graph.numOfNodes()-1, relationSuggestion.getSource().getName());
			iedge = new Edge(target, graph.numOfNodes()-2, null);
			graph.addIEdges(iedge);
			graph.add(SchemaFactoryImpl.getInstance().createRelation((relationSuggestion.getURI())), 
					graph.numOfNodes()-2, graph.numOfNodes()-1);
			graph.setTargetVariable(graph.numOfNodes()-1);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}

	@Override
	public Graph undo(Graph graph) {
		try {
			int nodeIdx = graph.removeRelation(SchemaFactoryImpl.getInstance().
					createRelation((relationSuggestion.getURI())),
					graph.getTargetVariable());
			int newTarget = graph.removeIEdge(iedge, nodeIdx);
			graph.setTargetVariable(newTarget);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
		
	}

}
