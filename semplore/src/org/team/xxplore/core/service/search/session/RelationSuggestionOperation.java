package org.team.xxplore.core.service.search.session;

import org.team.xxplore.core.service.search.datastructure.RelationSuggestion;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

public class RelationSuggestionOperation implements SuggestionOperation {

	private RelationSuggestion relationSuggestion;
	private String originalSource;
	
	public RelationSuggestionOperation(RelationSuggestion rs) {
		relationSuggestion = rs;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		try {
			int target = graph.getTargetVariable();
			originalSource = graph.getDataSource(target);
			graph.add(SchemaFactoryImpl.getInstance().createCompoundCategory(1));	// AND
			graph.add(SchemaFactoryImpl.getInstance().createRelation(Md5_BloomFilter_64bit.URItoID(relationSuggestion.getURI())), 
					target, graph.numOfNodes()-1);
			graph.setTargetVariable(graph.numOfNodes()-1);
			graph.setDataSource(graph.numOfNodes()-1, relationSuggestion.getSource().getName());
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}

	@Override
	public Graph undo(Graph graph) {
		try {
			int newTarget = graph.removeRelation(SchemaFactoryImpl.getInstance().
					createRelation(Md5_BloomFilter_64bit.URItoID(relationSuggestion.getURI())),
					graph.getTargetVariable());
			graph.setTargetVariable(newTarget);
			graph.setDataSource(newTarget, originalSource);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
		
	}

}
