package org.team.xxplore.core.service.q2semantic.search;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.mapping.Mapping;
import org.team.xxplore.core.service.mapping.MappingIndexSearcher;
import org.team.xxplore.core.service.q2semantic.SummaryGraphEdge;
import org.team.xxplore.core.service.q2semantic.SummaryGraphElement;
import org.team.xxplore.core.service.q2semantic.SummaryGraphUtil;

/**
 * This is a factory to create Graph4TopK
 * @author jqchen
 *
 */
public class Graph4TopKFactory {
	private Parameters param;
	
	public HashMap<String,SummaryPart> summaryGraph_HM;
	public HashMap<String,Pseudograph<SummaryGraphElement, SummaryGraphEdge>> summaryobj_HM = 
		new HashMap<String, Pseudograph<SummaryGraphElement,SummaryGraphEdge>>();
	public Pseudograph<SummaryGraphElement,SummaryGraphEdge> mappingGraph;
	public ArrayList<Mapping> mappings;
	
	
	/**
	 * TopK will modified some data structures of SummaryPart. This method is used to remove the modification.
	 */
	public void refreshFactory() {
		for(SummaryPart sg : summaryGraph_HM.values()) { 
			for(SummaryGraphElement ele : sg.summaryGraph.vertexSet()) {
				ele.setCursors(null);
				ele.m_exploredCursorCombinations = null;
				ele.m_newCursorCombinations = null;			
			}
		}
	}
	
	/**
	 * return the summary graph.
	 * @param key
	 * @return
	 */
	public Pseudograph<SummaryGraphElement,SummaryGraphEdge> getObj(String key) {
		return summaryobj_HM.get(key);
	}
	
	public Graph4TopKFactory(MappingIndexSearcher index) {
		param = Parameters.getParameters();
		summaryGraph_HM = new HashMap<String, SummaryPart>();
		this.getSummaryGraphs();
		mappingGraph = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
		this.getMapping(index);
	}
	
	/**
	 * read the summary graphs.
	 * @param keys
	 */
	private void getSummaryGraphs() {
		System.out.println("Loading summary graph ... ...");
		
		for(String ds : param.getDataSourceSet()) {
			String dsDFileName = param.summaryObjSet.get(ds);
			
			
			try {
				System.out.println("Loading " + dsDFileName);
				ObjectInputStream obj_input = new ObjectInputStream(new FileInputStream(dsDFileName));
				Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph_obj = 
					(Pseudograph<SummaryGraphElement, SummaryGraphEdge>)obj_input.readObject();
				summaryobj_HM.put(ds, graph_obj);
				summaryGraph_HM.put(ds, new SummaryPart(graph_obj));
				
				for(SummaryGraphElement ele : graph_obj.vertexSet()) {
					ele.setDatasource(ds);
					summaryGraph_HM.get(ds).element_hm.put(SummaryGraphUtil.getResourceUri(ele), ele);
				}
				summaryGraph_HM.get(ds).getNoNum();
				
				// scoring according to ICDE09 paper
				for(SummaryGraphElement ele : graph_obj.vertexSet()) {
					if(ele.getMatchingScore() != 0) {
						if(ele.getEF() == 0)
							ele.setTotalCost(1.0 / ele.getMatchingScore());
						else
							ele.setTotalCost((1.0 - ele.getEF()) / ele.getMatchingScore());
					}
					else if(ele.getEF() != 0) {
						ele.setTotalCost(1.0 - ele.getEF());
					}
					else {
						ele.setTotalCost(param.DEFAULT_SCORE);
					}
					ele.setTotalCost(ele.getTotalCost() + param.EDGE_SCORE);
				}
				
				obj_input.close();
				System.out.println("OK!");
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * create the mapping structure for the Graph4TopK.
	 * @param index
	 * @param keys
	 */
	private void getMapping(MappingIndexSearcher index) {
		System.out.println("Load mapping info ... ...");
		mappings = new ArrayList<Mapping>();
		
		for (String ds : param.getDataSourceSet()) {
			mappings.addAll(index.searchMappingsForDS(ds,MappingIndexSearcher.SEARCH_TARGET_AND_SOURCE_DS));
		}
		System.out.println("OK!");
		
		System.out.println("Create mapping graph ... ...");
		for(Mapping mapping : mappings) {
			String source = mapping.getSource();
			String s_ds = mapping.getSourceDsURI();
			String target = mapping.getTarget();
			String t_ds = mapping.getTargetDsURI();
			
			
			SummaryPart sg1 = this.summaryGraph_HM.get(s_ds);
			SummaryPart sg2 = this.summaryGraph_HM.get(t_ds);
			if(sg1 != null && sg2 != null) {
				SummaryGraphElement s = sg1.element_hm.get(source);
				SummaryGraphElement t = sg2.element_hm.get(target);
				if(s != null && t != null) {
					SummaryGraphEdge edge = new SummaryGraphEdge(s, t, SummaryGraphEdge.MAPPING_EDGE);
					mappingGraph.addVertex(s);
					mappingGraph.addVertex(t);
					mappingGraph.addEdge(s, t, edge);
				}
			}
			
		}
		System.out.println("OK!");
		System.out.println("maping vertex size " + mappingGraph.vertexSet().size());
		
	}
	
	/**
	 * create a Graph4TopK. The summary graph will shared by all Graph4TopK
	 * @param keywords
	 * @return
	 */
	public Graph4TopK createGraphAdapter(Map<String, Collection<SummaryGraphElement>> keywords) {
		Graph4TopK t = new Graph4TopK(this.summaryGraph_HM,this.mappingGraph);
		t.getAugmentPart(keywords);
		return t;
	}
	
}