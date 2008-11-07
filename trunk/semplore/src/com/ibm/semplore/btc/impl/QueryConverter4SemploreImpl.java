/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.util.HashMap;
import java.util.Iterator;

import com.ibm.semplore.btc.QueryConverter4Semplore;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.KeywordCategory;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.impl.SearchFactoryImpl;

/**
 * @author xrsun
 *
 */
public class QueryConverter4SemploreImpl implements QueryConverter4Semplore {
	SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	/* Task 1: Convert KeywordCategory into AttributeKeywordCategory
	 *         Assuming target node is not KeywordCategory!
	 * Task 2: Relax constraints
	 * @see com.ibm.semplore.btc.QueryConverter4Semplore#convertQuery(com.ibm.semplore.btc.SubGraph)
	 */
	@Override
	public XFacetedQuery convertQuery(SubGraph graph, boolean relax) {
		HashMap<Integer, GeneralCategory> map = new HashMap<Integer, GeneralCategory>();

		// prepare the map storing intermediate nodes
		for (int i=0; i<graph.numOfNodes(); i++) {
			GeneralCategory n = graph.getNode(i);
			if (relax && n instanceof Category && !((Category)n).isUniversal())
				map.put(i, schemaFactory.createUniversalCategory());
			else
				map.put(i, n);
		}
		
		// convert node where necessary
		for (int i=0; i<graph.numOfNodes(); i++) {
			Iterator<Edge> itr = graph.getEdges(i);
			while (itr.hasNext()) {
				Edge e = itr.next();
				int fromn = e.getFromNode();
				int ton = e.getToNode();
				if (Math.min(fromn, ton)<i) continue;
				if (graph.getNode(ton) instanceof KeywordCategory) {
					GeneralCategory from = map.get(fromn);
					KeywordCategory to = (KeywordCategory) map.get(ton);
					CompoundCategory newcat = schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND);
					newcat.addComponentCategory(from);
					if (relax)
						newcat.addComponentCategory(schemaFactory.createKeywordCategory(to.getKeyword()));
					else
						newcat.addComponentCategory(schemaFactory.createAttributeKeywordCategory(e.getRelation().getURI(), to.getKeyword()));
					map.put(fromn, newcat);
				}
			}
		}

		// copy the new nodes and original relations into a new CatRelGraph
		CatRelGraph g = schemaFactory.createCatRelGraph();
		for (int i=0; i<graph.numOfNodes(); i++) g.add(map.get(i));
		for (int i=0; i<graph.numOfNodes(); i++) {
			Iterator<Edge> itr = graph.getEdges(i);
			while (itr.hasNext()) {
				Edge e = itr.next();
				if (!(graph.getNode(e.getToNode()) instanceof KeywordCategory))
					g.add(e.getRelation(), e.getFromNode(), e.getToNode());
			}
		}
		
		XFacetedQuery query = searchFactory.createXFacetedQuery();
		query.setQueryConstraint(g);
		query.setSearchTarget(graph.getTargetVariable());
		query.setResultSpec(searchFactory.createXFacetedResultSpec());
		return query;
	}
}
