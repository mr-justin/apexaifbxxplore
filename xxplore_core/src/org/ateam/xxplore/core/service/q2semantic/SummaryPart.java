package org.ateam.xxplore.core.service.q2semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;

public class SummaryPart {
	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph;
	public HashMap<String, SummaryGraphElement> element_hm;
	public HashMap<String, Set<SummaryGraphElement>> no_num_element_hm;
	
	public void getNoNum() {
		this.no_num_element_hm = new HashMap<String, Set<SummaryGraphElement>>();
		for(String key : element_hm.keySet()) {
			String uri = SummaryGraphUtil.removeNum(key);
			SummaryGraphElement ele = element_hm.get(key);
			Set<SummaryGraphElement> ele_set = no_num_element_hm.get(uri);
			if(ele_set == null) {
				ele_set = new HashSet<SummaryGraphElement>();
				no_num_element_hm.put(uri, ele_set);
			}
			ele_set.add(ele);
		}
	}
	
	public SummaryPart(
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph) {
		super();
		this.summaryGraph = summaryGraph;
		this.element_hm = new HashMap<String, SummaryGraphElement>();
	}
}