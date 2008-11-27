package org.apexlab.service.session.semplore;

import org.apexlab.service.session.datastructure.Relation;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;

public class RelationOperation implements FacetOperation {

	private Relation relation;
	
	public RelationOperation(Relation r) {
		relation = r;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		try {
			int target = graph.getTargetVariable();
			String ds = graph.getDataSource(target);
			graph.add(SchemaFactoryImpl.getInstance().createCompoundCategory(1));	// AND
			graph.add(SchemaFactoryImpl.getInstance().createRelation((relation.getURI())), 
					target, graph.numOfNodes()-1);
			graph.setTargetVariable(graph.numOfNodes()-1);
			graph.setDataSource(graph.numOfNodes()-1, ds);
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
					createRelation((relation.getURI())),
					graph.getTargetVariable());
			graph.setTargetVariable(newTarget);
			return graph;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}

}
