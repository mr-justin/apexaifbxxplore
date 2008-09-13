package com.ibm.semplore.btc.impl;

import java.util.ArrayList;

import com.ibm.semplore.btc.DecomposedGraph;
import com.ibm.semplore.btc.QueryPlanner;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.btc.Visit;

public class QueryPlannerImpl implements QueryPlanner {
	DecomposedGraph degraph;
	
	@Override
	public void setDecomposedGraph(DecomposedGraph degraph) {
		this.degraph = degraph;
	}

	@Override
	public void startTraverse(Visit pre, Visit post) {
		degraph.startTraverse(pre, post);
	}

}
