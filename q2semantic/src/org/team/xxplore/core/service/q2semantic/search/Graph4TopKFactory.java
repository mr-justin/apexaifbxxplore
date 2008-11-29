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
	private HashMap<MappingCell, Set<MappingCell>> mapping_HM;
	public Set<String> conceptMappings;
	private ArrayList<Mapping> mappings;
	public Pseudograph<SummaryGraphElement,SummaryGraphEdge> mappingGraph;
	
	/**
	 * return the mapping arrayList
	 * @return
	 */
	public ArrayList<Mapping> getMappings() {
		return mappings;
	}
	
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
		mapping_HM = new HashMap<MappingCell, Set<MappingCell>>();
		this.getSummaryGraphs(param.getDataSourceSet());
		this.getMapping(index, param.getDataSourceSet());
	}
	
	private Collection<SummaryGraphEdge> getNeighbor(SummaryGraphElement ele) {
		Collection<SummaryGraphEdge> edges = new ArrayList<SummaryGraphEdge>();
		
		SummaryPart summaryGraph = summaryGraph_HM.get(ele.getDatasource());
		if(summaryGraph != null && summaryGraph.summaryGraph.vertexSet().contains(ele)) {
			edges.addAll(summaryGraph.summaryGraph.edgesOf(ele));
		}
		return edges;
	}
	
	private Collection<SummaryGraphElement> getElement(SummaryGraphElement ele,String type) {
		Collection<SummaryGraphElement> ele_set = new HashSet<SummaryGraphElement>();
		
		Collection<SummaryGraphEdge> edges = this.getNeighbor(ele);
		
		for(SummaryGraphEdge edge : edges) {
			if(edge.equals(type)) {
				SummaryGraphElement other = edge.getSource().equals(ele) ? edge.getTarget() : edge.getSource();
				ele_set.add(other);
			}
		}
		
		return ele_set;
	}
	
	/**
	 * read the summary graphs.
	 * @param keys
	 */
	private void getSummaryGraphs(Set<String> keys) {
		System.out.println("Loading summary graph ... ...");
		
		for(String ds : keys) {
			String dsDFileName = param.summaryObjSet.get(ds);
			try {
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
	private void getMapping(MappingIndexSearcher index,Set<String> keys) {
		mappings = new ArrayList<Mapping>();
		conceptMappings = new HashSet<String>();
		mappingGraph = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
		
		for (String ds : keys) {
			mappings.addAll(index.searchMappingsForDS(ds,
					MappingIndexSearcher.SEARCH_TARGET_AND_SOURCE_DS));
		}
		for(Mapping mapping : mappings) {
			MappingCell mc1 = new MappingCell(SummaryGraphUtil.removeGtOrLs(mapping.getSource()),mapping.getSourceDsURI());
			MappingCell mc2 = new MappingCell(SummaryGraphUtil.removeGtOrLs(mapping.getTarget()),mapping.getTargetDsURI());
			conceptMappings.add(mc1.datasource + mc1.uri + mc2.datasource + mc2.uri);
			
			Set<MappingCell> mc1_set = mapping_HM.get(mc1);
			if(mc1_set == null) {
				mc1_set = new HashSet<MappingCell>();
				mapping_HM.put(mc1, mc1_set);
			}
			mc1_set.add(mc2);
			
			Set<MappingCell> mc2_set = mapping_HM.get(mc2);
			if(mc2_set == null) {
				mc2_set = new HashSet<MappingCell>();
				mapping_HM.put(mc2, mc2_set);
			}
			mc2_set.add(mc1);
		}
		
		for(Mapping mapping : mappings) {
			MappingCell mc1 = new MappingCell(SummaryGraphUtil.removeGtOrLs(mapping.getSource()),mapping.getSourceDsURI());
			MappingCell mc2 = new MappingCell(SummaryGraphUtil.removeGtOrLs(mapping.getTarget()),mapping.getTargetDsURI());
			
			Set<MappingCell> mc1_set = mapping_HM.get(mc1);
			Set<MappingCell> mc2_set = mapping_HM.get(mc2);
			
			if(mc1_set != null && mc2_set != null) {
				for(MappingCell mc11 : mc1_set) {
					if(this.summaryGraph_HM.get(mc11.datasource) == null) continue;
					SummaryGraphElement s = this.summaryGraph_HM.get(mc11.datasource).element_hm.get(mc11.uri);
					label1:
					for(MappingCell mc22 : mc2_set) {
						if(this.summaryGraph_HM.get(mc22.datasource) == null) continue;
						SummaryGraphElement t = this.summaryGraph_HM.get(mc22.datasource).element_hm.get(mc22.uri);
						
						if(s != null && t != null) {
							label2:
							if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION)
							{
								boolean flag = false;
								label3:
								for(SummaryGraphElement sElem: this.getElement(s,SummaryGraphEdge.DOMAIN_EDGE))
									for(SummaryGraphElement tElem: getElement(t,SummaryGraphEdge.DOMAIN_EDGE))
										if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
											{flag = true; break label3;}
								if(flag)
								for(SummaryGraphElement sElem: getElement(s,SummaryGraphEdge.RANGE_EDGE))
									for(SummaryGraphElement tElem: getElement(t,SummaryGraphEdge.RANGE_EDGE))
										if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
											break label2;
								continue label1;
							}
							
							
							SummaryGraphEdge edge = new SummaryGraphEdge(s, t, SummaryGraphEdge.MAPPING_EDGE);
							mappingGraph.addVertex(s);
							mappingGraph.addVertex(t);
							mappingGraph.addEdge(s, t, edge);
						}
					}
				}
			}					
		}
		
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