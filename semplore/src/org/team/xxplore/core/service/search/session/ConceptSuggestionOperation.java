package org.team.xxplore.core.service.search.session;

import org.team.xxplore.core.service.search.datastructure.ConceptSuggestion;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

public class ConceptSuggestionOperation implements SuggestionOperation {

	private ConceptSuggestion conceptSuggestion;
	private String originalSource;
	
	public ConceptSuggestionOperation(ConceptSuggestion cs) {
		conceptSuggestion = cs;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		try {
			int target = graph.getTargetVariable();
			originalSource = graph.getDataSource(target);
			Category c = SchemaFactoryImpl.getInstance().createCategory(Md5_BloomFilter_64bit.URItoID(conceptSuggestion.getURI()));
			CompoundCategory cc = (CompoundCategory)graph.getNode(target);
			cc.addComponentCategory(c);
			graph.setDataSource(target, conceptSuggestion.getSource().getName());
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}

	@Override
	public Graph undo(Graph graph) {
		try {
			int target = graph.getTargetVariable();
			Category c = SchemaFactoryImpl.getInstance().createCategory(Md5_BloomFilter_64bit.URItoID(conceptSuggestion.getURI()));
			CompoundCategory cc = (CompoundCategory)graph.getNode(target);
			cc.removeComponentCategory(c);
			graph.setDataSource(target, originalSource);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
		
	}

}
