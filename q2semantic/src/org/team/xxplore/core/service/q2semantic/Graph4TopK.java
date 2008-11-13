package org.team.xxplore.core.service.q2semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.impl.DataProperty;

/**
 * This is the search graph for TopK. There are two main members: summaryGraph_HM,augmentPart_HM.
 * summargGraph_HM is the summary graphs. It will not be changed all the time. augmentPart_HM is changed with
 * the keywords.
 * @author jqchen
 *
 */
public class Graph4TopK {
	private HashMap<String,SummaryPart> summaryGraph_HM;	
	private HashMap<MappingCell, Set<MappingCell>> mapping_HM;
	private HashMap<String, AugmentPart> augmentPart_HM;
	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> mappingGraph;
	private Map<String, Integer> m_datasources = new HashMap<String, Integer>();
	private Set<String> conceptMappings;
	
	/**
	 * Find the element of the graph with the uri of element.
	 * @param ds
	 * @param uri
	 * @return
	 */
	public Set<SummaryGraphElement> getElementFromUri(String ds,String uri) {
		Set<SummaryGraphElement> ret_set = new HashSet<SummaryGraphElement>();
		if(summaryGraph_HM.get(ds) != null) {
			Set<SummaryGraphElement> tmp = summaryGraph_HM.get(ds).no_num_element_hm.get(uri);
			if(tmp != null) {
				ret_set.addAll(tmp);
			}
		}
		if(augmentPart_HM.get(ds) != null) {
			Set<SummaryGraphElement> tmp = augmentPart_HM.get(ds).no_num_element_hm.get(uri);
			if(tmp != null) {
				ret_set.addAll(tmp);
			}
		}
		return ret_set;
	}
	
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
	
	public Graph4TopK(HashMap<String, SummaryPart> summaryGraph_HM,
			HashMap<MappingCell, Set<MappingCell>> mapping_HM,
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> mappingGraph) {
		super();
		this.summaryGraph_HM = summaryGraph_HM;
		this.mapping_HM = mapping_HM;
		this.mappingGraph = mappingGraph;
		augmentPart_HM = new HashMap<String, AugmentPart>();
	}

	/**
	 * Create the augment part according to the keywords.
	 * @param keywords
	 */
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
					
					Map<DataProperty, Collection<INamedConcept>> neighbors = 
						valueElement.getNeighbors();
					for(DataProperty prop : neighbors.keySet()) {
						SummaryGraphElement pvertex = new SummaryGraphElement(
								new DataProperty(prop.getUri()+"("+(count++)+")"), SummaryGraphElement.ATTRIBUTE);
						
						augmentPart_HM.get(ele.getDatasource()).element_hm.put(
								SummaryGraphUtil.getResourceUri(pvertex), pvertex);
						
						for(INamedConcept con : neighbors.get(prop)) {
							SummaryGraphElement cvertex = summaryGraph_HM.get(
									ele.getDatasource()).element_hm.get(con.getUri());
							
							if(cvertex == null) {
								continue;
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
				
				if (ele instanceof SummaryGraphAttributeElement){
					ele.type = SummaryGraphElement.ATTRIBUTE;
					Collection<INamedConcept> cons = ((SummaryGraphAttributeElement)ele).getNeighborConcepts();	
					Iterator<INamedConcept> conIter = cons.iterator();
					String ds = ele.getDatasource();
					augmentPart_HM.get(ele.getDatasource()).element_hm.put(
							SummaryGraphUtil.getResourceUri(ele), ele);
					while (conIter.hasNext()){
						INamedConcept con = conIter.next();
						SummaryGraphElement cvertex = summaryGraph_HM.get(
								ele.getDatasource()).element_hm.get(con.getUri());
						
						if(cvertex == null) {
							continue;
						}
						System.out.println(con.getUri());
						
						//SummaryGraphElement cvertex = new SummaryGraphElement(con,SummaryGraphElement.CONCEPT);
						SummaryGraphEdge domain = new SummaryGraphEdge(cvertex, (SummaryGraphAttributeElement)ele, SummaryGraphEdge.DOMAIN_EDGE);
						cvertex.setDatasource(ds);
						augmentPart.augmentPart.addVertex(cvertex);
						augmentPart.augmentPart.addVertex(ele);
						augmentPart.augmentPart.addEdge(cvertex, (SummaryGraphAttributeElement)ele, domain);
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
	
	/**
	 * Get the neighbor of one element without I-edge.
	 * @param ele
	 * @return
	 */
	private Collection<SummaryGraphEdge> getNeighbor(SummaryGraphElement ele) {
		Collection<SummaryGraphEdge> edges = new ArrayList<SummaryGraphEdge>();
		
		SummaryPart summaryGraph = summaryGraph_HM.get(ele.getDatasource());
		if(summaryGraph != null && summaryGraph.summaryGraph.vertexSet().contains(ele)) {
			edges.addAll(summaryGraph.summaryGraph.edgesOf(ele));
		}
		
		AugmentPart augmentPart = augmentPart_HM.get(ele.getDatasource());
		if(augmentPart != null && augmentPart.augmentPart.vertexSet().contains(ele)) {
			edges.addAll(augmentPart.augmentPart.edgesOf(ele));
		}
		return edges;
	}
	

	/**
	 * Find the neighbor edges of one element.
	 * @param ele
	 * @return
	 */
	public Collection<SummaryGraphEdge> neighborEdges(
			SummaryGraphElement ele) {
		Collection<SummaryGraphEdge> edges = this.getNeighbor(ele);
		
		if(mappingGraph.vertexSet().contains(ele)) {
			edges.addAll(mappingGraph.edgesOf(ele));
		}
		return edges;
	}
	
}