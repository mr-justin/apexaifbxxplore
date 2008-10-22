package org.ateam.xxplore.core.service.q2semantic;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ateam.xxplore.core.service.mapping.Mapping;
import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.jgrapht.graph.Pseudograph;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.impl.DataProperty;


class SummaryGraph {
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
	
	public SummaryGraph(
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> summaryGraph) {
		super();
		this.summaryGraph = summaryGraph;
		this.element_hm = new HashMap<String, SummaryGraphElement>();
	}
}

class AugmentPart {
	public Pseudograph<SummaryGraphElement, SummaryGraphEdge> augmentPart;
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
	
	public AugmentPart() {
		augmentPart = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
		element_hm = new HashMap<String, SummaryGraphElement>();
	}
}

class MappingCell {
	public String uri;
	public String datasource;
	public MappingCell(String uri, String datasource) {
		super();
		this.uri = uri;
		this.datasource = datasource;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((datasource == null) ? 0 : datasource.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingCell other = (MappingCell) obj;
		if (datasource == null) {
			if (other.datasource != null)
				return false;
		} else if (!datasource.equals(other.datasource))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}

class GraphAdapterFactory {
	public HashMap<String,SummaryGraph> summaryGraph_HM;
	private HashMap<MappingCell, Set<MappingCell>> mapping_HM;
	public Set<String> conceptMappings;
	private ArrayList<Mapping> mappings;
	public Pseudograph<SummaryGraphElement,SummaryGraphEdge> mappingGraph;
	
	public ArrayList<Mapping> getMappings() {
		return mappings;
	}
	
	public GraphAdapterFactory(Set<String> keys, MappingIndexService index) {
		summaryGraph_HM = new HashMap<String, SummaryGraph>();
		mapping_HM = new HashMap<MappingCell, Set<MappingCell>>();
		this.getSummaryGraphs(keys);
		this.getMapping(index, keys);
	}
	
	public Collection<SummaryGraphEdge> getNeighbor(SummaryGraphElement ele) {
		Collection<SummaryGraphEdge> edges = new ArrayList<SummaryGraphEdge>();
		
		SummaryGraph summaryGraph = summaryGraph_HM.get(ele.getDatasource());
		if(summaryGraph != null && summaryGraph.summaryGraph.vertexSet().contains(ele)) {
			edges.addAll(summaryGraph.summaryGraph.edgesOf(ele));
		}
		return edges;
	}
	
	public Collection<SummaryGraphElement> getElement(SummaryGraphElement ele,String type) {
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
	
	public void getSummaryGraphs(Set<String> keys) {
		for(String ds : keys) {
			String dsDFileName = SearchQ2SemanticService.summaryObjSet.get(ds);
			try {
				ObjectInputStream obj_input = new ObjectInputStream(new FileInputStream(dsDFileName));
				Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph_obj = 
					(Pseudograph<SummaryGraphElement, SummaryGraphEdge>)obj_input.readObject();
				summaryGraph_HM.put(ds, new SummaryGraph(graph_obj));
				
				for(SummaryGraphElement ele : graph_obj.vertexSet()) {
					ele.setDatasource(ds);
					summaryGraph_HM.get(ds).element_hm.put(SummaryGraphUtil.getResourceUri(ele), ele);
				}
				summaryGraph_HM.get(ds).getNoNum();
				
				for(SummaryGraphElement ele : graph_obj.vertexSet()) {
					if(ele.getMatchingScore() != 0) {
						ele.setTotalScore(1.0 / ele.getMatchingScore());
					}
					else if(ele.getEF() != 0) {
						ele.setTotalScore(ele.getEF());
					}
					else {
						ele.setTotalScore(QueryInterpretationService.EDGE_SCORE);
					}
					ele.setTotalScore(ele.getTotalScore() + QueryInterpretationService.EDGE_SCORE);
				}
				
				obj_input.close();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void getMapping(MappingIndexService index,Set<String> keys) {
		mappings = new ArrayList<Mapping>();
		conceptMappings = new HashSet<String>();
		mappingGraph = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
		
		for (String ds : keys) {
			mappings.addAll(index.searchMappingsForDS(ds,
					MappingIndexService.SEARCH_TARGET_AND_SOURCE_DS));
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
					SummaryGraphElement s = this.summaryGraph_HM.get(mc11.datasource).element_hm.get(mc11.uri);
					label1:
					for(MappingCell mc22 : mc2_set) {
						
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
								//System.out.println("gua le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
								continue label1;
							}
							
							if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION) 
								System.out.println("nb le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
							
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
		
//		for(SummaryGraphElement s: ele1_set)
//			label1:
//			for(SummaryGraphElement t : ele2_set)
//			{
//				label2:
//				if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION)
//				{
//					boolean flag = false;
//					label3:
//					for(SummaryGraphElement sElem: this.getElement(s,SummaryGraphEdge.DOMAIN_EDGE))
//						for(SummaryGraphElement tElem: getElement(t,SummaryGraphEdge.DOMAIN_EDGE))
//							if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
//								{flag = true; break label3;}
//					if(flag)
//					for(SummaryGraphElement sElem: getElement(s,SummaryGraphEdge.RANGE_EDGE))
//						for(SummaryGraphElement tElem: getElement(t,SummaryGraphEdge.RANGE_EDGE))
//							if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
//								break label2;
//					//System.out.println("gua le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
//					continue label1;
//				}
//				
//				if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION) 
//					System.out.println("nb le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
//
////				System.out.println("chenjunquan is ok!");
////				System.out.println("mapping " + SummaryGraphUtil.getResourceUri(s) + "\t" +
////						SummaryGraphUtil.getResourceUri(t));
//				
//				SummaryGraphEdge edge = new SummaryGraphEdge(s, t, SummaryGraphEdge.MAPPING_EDGE);
//				edges.add(edge);
//			}
	}
	
	public GraphAdapter createGraphAdapter(Map<String, Collection<SummaryGraphElement>> keywords) {
		GraphAdapter t = new GraphAdapter(this.summaryGraph_HM,this.mapping_HM,this.mappingGraph);
		t.getAugmentPart(keywords);
		return t;
	}
	
}

public class GraphAdapter {
	private HashMap<String,SummaryGraph> summaryGraph_HM;	
	private HashMap<MappingCell, Set<MappingCell>> mapping_HM;
	private HashMap<String, AugmentPart> augmentPart_HM;
	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> mappingGraph;
	private Map<String, Integer> m_datasources = new HashMap<String, Integer>();
	private Set<String> conceptMappings;
	
	
	private void updateDsCoverage(String ds) {
		Integer cover = m_datasources.get(ds);
		if (cover != null) {
			cover = new Integer(cover.intValue() + 1);
		} else {
			m_datasources.put(ds, new Integer(1));
		}
	}
	
	private int getDsCoverage(SummaryGraphElement e) {
		String ds = e.getDatasource();
		return m_datasources.get(ds).intValue();
	}
	
	public GraphAdapter(HashMap<String, SummaryGraph> summaryGraph_HM,
			HashMap<MappingCell, Set<MappingCell>> mapping_HM,
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> mappingGraph) {
		super();
		this.summaryGraph_HM = summaryGraph_HM;
		this.mapping_HM = mapping_HM;
		this.mappingGraph = mappingGraph;
		augmentPart_HM = new HashMap<String, AugmentPart>();
	}

	public void getAugmentPart(Map<String, Collection<SummaryGraphElement>> keywords) {
		Collection<SummaryGraphElement> start_element = new ArrayList<SummaryGraphElement>();
		for(Collection<SummaryGraphElement> t : keywords.values()) {
			for(SummaryGraphElement e : t) {
				this.updateDsCoverage(e.getDatasource());
				start_element.add(e);
			}
		}
		int count = 0;
		for(String key : keywords.keySet()) {
			Collection<SummaryGraphElement> coll = keywords.get(key);

			
			for(SummaryGraphElement ele : coll) {
				
				AugmentPart augmentPart = augmentPart_HM.get(ele.getDatasource());
				if(augmentPart == null) {
					augmentPart = new AugmentPart();
					augmentPart_HM.put(ele.getDatasource(), augmentPart);
				}
				
				if(ele instanceof SummaryGraphValueElement) {
					SummaryGraphValueElement valueElement = (SummaryGraphValueElement)ele;
					augmentPart_HM.get(ele.getDatasource()).element_hm.put(
							SummaryGraphUtil.getResourceUri(ele), ele);
					
					Map<IDataProperty, Collection<INamedConcept>> neighbors = 
						valueElement.getNeighbors();
					for(IDataProperty prop : neighbors.keySet()) {
						SummaryGraphElement pvertex = new SummaryGraphElement(
								new DataProperty(prop.getUri()+"("+(count++)+")"), SummaryGraphElement.ATTRIBUTE);
						augmentPart_HM.get(ele.getDatasource()).element_hm.put(
								SummaryGraphUtil.getResourceUri(pvertex), pvertex);
						
						for(INamedConcept con : neighbors.get(prop)) {
							SummaryGraphElement cvertex = summaryGraph_HM.get(
									ele.getDatasource()).element_hm.get(con.getUri());
							
							if(cvertex == null) {
								System.err.println("cvertex is null!");
								System.exit(1);
							}
							
							SummaryGraphEdge domain = new SummaryGraphEdge(
									cvertex, pvertex,
									SummaryGraphEdge.DOMAIN_EDGE);
							SummaryGraphEdge range = new SummaryGraphEdge(
									pvertex, ele,
									SummaryGraphEdge.RANGE_EDGE);
							
							cvertex.setDatasource(ele.getDatasource());
							pvertex.setDatasource(ele.getDatasource());
							
							augmentPart.augmentPart.addVertex(cvertex);
							augmentPart.augmentPart.addVertex(pvertex);
							augmentPart.augmentPart.addVertex(ele);
							
							augmentPart.augmentPart.addEdge(domain.getSource(), domain.getTarget(), domain);
							augmentPart.augmentPart.addEdge(range.getSource(), range.getTarget(), range);
						}
					}
				}

				this.updateScore(ele, start_element);
			}
		}
		
		for(AugmentPart ap : augmentPart_HM.values()) {
			ap.getNoNum();
			for(SummaryGraphElement ele : ap.augmentPart.vertexSet()) {
				if(ele.getMatchingScore() != 0) {
					ele.setTotalScore(1.0 / ele.getMatchingScore());
				}
				else if(ele.getEF() != 0) {
					ele.setTotalScore(ele.getEF());
				}
				else {
					ele.setTotalScore(QueryInterpretationService.DEFAULT_SCORE);
				}
				ele.setTotalScore(ele.getTotalScore() + QueryInterpretationService.EDGE_SCORE);
			}
		}
	}
	
	private void updateScore(
			SummaryGraphElement e, Collection<SummaryGraphElement> keywords) {
		double score = -1;
		for (SummaryGraphElement k : keywords) {
			if (k.equals(e))
				score = k.getMatchingScore();
		}

		// == chenjunquan ==
		e.setMatchingScore(score);
		e.applyCoverage(getDsCoverage(e));
	}
	
	
	public Collection<SummaryGraphElement> getElement(SummaryGraphElement ele,String type) {
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
	
	public Collection<SummaryGraphEdge> getNeighbor(SummaryGraphElement ele) {
		Collection<SummaryGraphEdge> edges = new ArrayList<SummaryGraphEdge>();
		
		SummaryGraph summaryGraph = summaryGraph_HM.get(ele.getDatasource());
		if(summaryGraph != null && summaryGraph.summaryGraph.vertexSet().contains(ele)) {
			edges.addAll(summaryGraph.summaryGraph.edgesOf(ele));
		}
		
		AugmentPart augmentPart = augmentPart_HM.get(ele.getDatasource());
		if(augmentPart != null && augmentPart.augmentPart.vertexSet().contains(ele)) {
			edges.addAll(augmentPart.augmentPart.edgesOf(ele));
		}
		return edges;
	}
	

	public Collection<SummaryGraphEdge> neighborVertex(
			SummaryGraphElement ele) {
		Collection<SummaryGraphEdge> edges = this.getNeighbor(ele);
		
		if(mappingGraph.vertexSet().contains(ele)) {
			edges.addAll(mappingGraph.edgesOf(ele));
		}
		
//		String uri = SummaryGraphUtil.getResourceUri(ele);
//		uri = SummaryGraphUtil.removeNum(uri);
//		
//		
//		MappingCell mc = new MappingCell(uri,ele.getDatasource());
//		if(mc.datasource.equals("dbpedia")) return edges;
//		
//		Set<MappingCell> mc2_set = mapping_HM.get(mc);
//		
//		if(mc2_set != null) {
//			for(MappingCell mc2 : mc2_set) {
//				if(mc2.datasource.equals("dbpedia"));
//				Set<SummaryGraphElement> ele1_set = null;
//				if(this.summaryGraph_HM.get(mc.datasource) != null) {
//					ele1_set = this.summaryGraph_HM.get(mc.datasource).no_num_element_hm.get(mc.uri);
//				}
//				if(ele1_set == null) {
//					if(this.augmentPart_HM.get(mc.datasource) == null) continue;
//					ele1_set = this.augmentPart_HM.get(mc.datasource).no_num_element_hm.get(mc.uri);
//				}
//				
//				Set<SummaryGraphElement> ele2_set = null;
//				if(this.summaryGraph_HM.get(mc2.datasource) != null) {
//					ele2_set = this.summaryGraph_HM.get(mc2.datasource).no_num_element_hm.get(mc2.uri);
//				}
//				if(ele2_set == null) {
//					if(this.augmentPart_HM.get(mc2.datasource) == null) continue;
//					ele2_set = this.augmentPart_HM.get(mc2.datasource).no_num_element_hm.get(mc2.uri);
//				}
//				
//				if(ele1_set != null && ele2_set != null) {
//				for(SummaryGraphElement s: ele1_set)
//					label1:
//					for(SummaryGraphElement t : ele2_set)
//					{
//						label2:
//						if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION)
//						{
//							boolean flag = false;
//							label3:
//							for(SummaryGraphElement sElem: this.getElement(s,SummaryGraphEdge.DOMAIN_EDGE))
//								for(SummaryGraphElement tElem: getElement(t,SummaryGraphEdge.DOMAIN_EDGE))
//									if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
//										{flag = true; break label3;}
//							if(flag)
//							for(SummaryGraphElement sElem: getElement(s,SummaryGraphEdge.RANGE_EDGE))
//								for(SummaryGraphElement tElem: getElement(t,SummaryGraphEdge.RANGE_EDGE))
//									if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
//										break label2;
//							//System.out.println("gua le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
//							continue label1;
//						}
//						
//						if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION) 
//							System.out.println("nb le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
//
////						System.out.println("chenjunquan is ok!");
////						System.out.println("mapping " + SummaryGraphUtil.getResourceUri(s) + "\t" +
////								SummaryGraphUtil.getResourceUri(t));
//						
//						SummaryGraphEdge edge = new SummaryGraphEdge(s, t, SummaryGraphEdge.MAPPING_EDGE);
//						edges.add(edge);
//					}
//				}
//			}
//		}
		
		return edges;
	}
	
}