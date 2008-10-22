/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.util.HashMap;
import java.util.Iterator;

import com.ibm.semplore.btc.QueryConverter4Semplore;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.KeywordCategory;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.CatRelGraphImpl;
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

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.QueryConverter4Semplore#convertQuery(com.ibm.semplore.btc.SubGraph)
	 */
	@Override
	public XFacetedQuery convertQuery(SubGraph graph) {
		// Convert KeywordCategory into AttributeKeywordCategory
		// Assuming target node is not KeywordCategory!
		HashMap<Integer, GeneralCategory> map = new HashMap<Integer, GeneralCategory>();
		for (int i=0; i<graph.numOfNodes(); i++)
			map.put(i, graph.getNode(i));
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
					newcat.addComponentCategory(schemaFactory.createAttributeKeywordCategory(e.getRelation().getURI(), to.getKeyword()));
					map.put(fromn, newcat);
				}
			}
		}

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
