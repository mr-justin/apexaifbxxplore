package org.team.xxplore.core.service.q2semantic.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.q2semantic.SummaryGraphEdge;
import org.team.xxplore.core.service.q2semantic.SummaryGraphElement;
import org.team.xxplore.core.service.q2semantic.SummaryGraphUtil;

/**
 * This is the augment part of Graph4TopK which will be created very query. 
 * @author jqchen
 *
 */
public class AugmentPart {
	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> augmentPart;
	public HashMap<String, SummaryGraphElement> element_hm;
	public HashMap<String, Set<SummaryGraphElement>> no_num_element_hm;
	
	/**
	 * The property uri be added a number after splitted. element_hm contains uri + num -> element
	 * no_num_element_hm contains uri -> set < element >. This method is used to get the no_num_element_hm
	 */
	public void getNoNum() {
		this.no_num_element_hm = new HashMap<String, Set<SummaryGraphElement>>();
		for(String key : element_hm.keySet()) {
			String uri = SummaryGraphUtil.removeNum(key);
			System.out.println("begin");
			System.out.println(uri);
			SummaryGraphElement ele = element_hm.get(key);
			Set<SummaryGraphElement> ele_set = no_num_element_hm.get(uri);
			if(ele_set == null) {
				ele_set = new HashSet<SummaryGraphElement>();
				no_num_element_hm.put(uri, ele_set);
			}
			ele_set.add(ele);
		}
	}
	
	public AugmentPart() {
		augmentPart = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
		element_hm = new HashMap<String, SummaryGraphElement>();
	}
}
