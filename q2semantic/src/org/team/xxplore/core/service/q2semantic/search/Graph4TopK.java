package org.team.xxplore.core.service.q2semantic.search;

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
import org.team.xxplore.core.service.q2semantic.SummaryGraphAttributeElement;
import org.team.xxplore.core.service.q2semantic.SummaryGraphEdge;
import org.team.xxplore.core.service.q2semantic.SummaryGraphElement;
import org.team.xxplore.core.service.q2semantic.SummaryGraphUtil;
import org.team.xxplore.core.service.q2semantic.SummaryGraphValueElement;

/**
 * This is the search graph for TopK. There are two main members: summaryGraph_HM,augmentPart_HM.
 * summargGraph_HM is the summary graphs. It will not be changed all the time. augmentPart_HM is changed with
 * the keywords.
 * @author jqchen
 *
 */
public class Graph4TopK {
	private HashMap<String,SummaryPart> summaryGraph_HM;	
	private HashMap<String,AugmentPart> augmentPart_HM;
	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> mappingGraph;
	
	private Parameters param;
	
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
	
	public Graph4TopK(HashMap<String, SummaryPart> summaryGraph_HM,
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> mappingGraph) {
		super();
		this.summaryGraph_HM = summaryGraph_HM;
		this.mappingGraph = mappingGraph;
		augmentPart_HM = new HashMap<String, AugmentPart>();
		this.param = Parameters.getParameters();
	}

	/**
	 * Create the augment part according to the keywords.
	 * @param keywords
	 */
	public void getAugmentPart(Map<String, Collection<SummaryGraphElement>> keywords) {
		Collection<SummaryGraphElement> start_element = new ArrayList<SummaryGraphElement>();
		for(Collection<SummaryGraphElement> t : keywords.values()) {
			for(SummaryGraphElement e : t) {
				start_element.add(e);
			}
		}
		int count = 1000;
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
					ele.setType(SummaryGraphElement.ATTRIBUTE);
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
			
//			// scoring according to WWW09 paper
//			for(SummaryGraphElement ele : ap.augmentPart.vertexSet()) {
//				if(ele.getMatchingScore() != 0) {
//					if(ele.getEF() == 0)
//						ele.setTotalCost(1.0 / ele.getMatchingScore());
//					else
//						ele.setTotalCost(1.0 / (ele.getMatchingScore()*ele.getEF()));
//				}
//				else if(ele.getEF() != 0) {
//					ele.setTotalCost(1.0 / ele.getEF());
//				}
//				else {
//					ele.setTotalCost(QueryInterpretationService.EDGE_SCORE);
//				}
//				ele.setTotalCost(ele.getTotalCost() + QueryInterpretationService.EDGE_SCORE);
//			}
			
			// scoring according to ICDE09 paper
			for(SummaryGraphElement ele : ap.augmentPart.vertexSet()) {
				if(ele.getMatchingScore() != 0) {
					if(ele.getEF() == 0)
						ele.setTotalCost(1.0 / ele.getMatchingScore());
					else
						ele.setTotalCost((1.0 - ele.getEF()) / ele.getMatchingScore());
				}
				else if(ele.getEF() != 0) {
					ele.setTotalCost(1.0 - ele.getEF());
				}
//				ele.setTotalCost(ele.getTotalCost() + param.EDGE_SCORE);
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
	}
	
	/**
	 * Get the neighbor of one element without I-edge.
	 * @param ele
	 * @return
	 */
	private Collection<SummaryGraphEdge> getNeighbor(SummaryGraphElement ele,String edgeType) {
		Collection<SummaryGraphEdge> edges = new ArrayList<SummaryGraphEdge>();
		
		SummaryPart summaryGraph = summaryGraph_HM.get(ele.getDatasource());
		if(summaryGraph != null && summaryGraph.summaryGraph.vertexSet().contains(ele)) {
			for(SummaryGraphEdge edge : summaryGraph.summaryGraph.edgesOf(ele)) {
				if(edgeType != null) {
					if(edge.getEdgeLabel().equals(edgeType)) {
						edges.add(edge);
					}
				}
				else {
					edges.add(edge);
				}
			}
		}
		
		AugmentPart augmentPart = augmentPart_HM.get(ele.getDatasource());
		if(augmentPart != null && augmentPart.augmentPart.vertexSet().contains(ele)) {
			for(SummaryGraphEdge edge : augmentPart.augmentPart.edgesOf(ele)) {
				if(edgeType != null) {
					if(edge.getEdgeLabel().equals(edgeType)) {
						edges.add(edge);
					}
				}
				else {
					edges.add(edge);
				}
			}
		}
		
		return edges;
	}
	

	/**
	 * Find the neighbor edges of one element.
	 * @param ele
	 * @return
	 */
	public Collection<SummaryGraphEdge> neighborEdges(
			SummaryGraphElement ele,String edgeType) {
		Collection<SummaryGraphEdge> edges = this.getNeighbor(ele,edgeType);
		
		if(mappingGraph.vertexSet().contains(ele)) {
			edges.addAll(mappingGraph.edgesOf(ele));
		}
		return edges;
	}
	
}