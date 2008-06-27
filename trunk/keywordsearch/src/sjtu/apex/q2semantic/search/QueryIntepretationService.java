package sjtu.apex.q2semantic.search;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.WeightedPseudograph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.Variable;

import sjtu.apex.q2semantic.graph.Edge;
import sjtu.apex.q2semantic.graph.Node;
import sjtu.apex.q2semantic.index.IndexEnvironment;

public class QueryIntepretationService {

	private static Logger s_log = Logger
			.getLogger(QueryIntepretationService.class);

	private int TOTAL_NUMBER_OF_VERTEX = -1;

	private int TOTAL_NUMBER_OF_EDGE = -1;

	private int K_TOP = 10;

	private WeightedPseudograph<KbVertex, KbEdge> resourceGraph;

	private Set<KbVertex> verticesOfGraphIndex;

	private Set<KbVertex> verticesOfGraphWithCursors;

	private Collection<Collection<OWLPredicate>> queries;

	private Map<String, Collection<KbElement>> resources;

	private Collection<Collection<KbEdge>> subgraphs;

	private PriorityQueue<QueueEntry> expansionQueue;

	private PriorityQueue<Subgraph> subgraphQueue;

	private Set<KbVertex> connectingElements;

	private Set<KbElement> matchingREdges;

	private Map<String, Collection<KbElement>> keywordREdgeMap;

	private double threshold = 50;

	private int width = 0;

	private String obj_path;

	private String freq_path;

	private String baseURI = "";

	private String ns = "";

	private HashMap<String, Integer> catMap;

	private HashMap<String, Integer> relMap;

	public QueryIntepretationService(int flag) {
		if (flag == IndexEnvironment.TAP_FLAG) {
			obj_path = IndexEnvironment.TAP_OBJ_PATH;
			freq_path = IndexEnvironment.TAP_FREQ;
			baseURI = IndexEnvironment.TAP_BASEURI;
			ns = IndexEnvironment.TAP_NS;
		} else if (flag == IndexEnvironment.LUBM_FLAG) {
			obj_path = IndexEnvironment.LUBM_OBJ_PATH;
			freq_path = IndexEnvironment.LUBM_FREQ;
			baseURI = IndexEnvironment.LUBM_BASEURI;
			ns = IndexEnvironment.LUBM_NS;
		} else if (flag == IndexEnvironment.DBLP_FLAG) {
			obj_path = IndexEnvironment.DBLP_OBJ_PATH;
			freq_path = IndexEnvironment.DBLP_FREQ;
		}
		catMap = new HashMap<String, Integer>();
		relMap = new HashMap<String, Integer>();
	}

	public Collection<Collection<OWLPredicate>> computeQueries(
			Map<String, Collection<KbElement>> elements, int width, int depth) {

		if (elements == null) {
			return null;
		}

		if (width > 0) {
			this.width = width;
		}

		computeTotalNumber();
		expansionQueue = new PriorityQueue<QueueEntry>();
		subgraphQueue = new PriorityQueue<Subgraph>();
		connectingElements = new LinkedHashSet<KbVertex>();
		resourceGraph = computeGraphSchemaIndex();

		resources = computeResources(elements);
		if ((resources == null) || (resources.size() == 0)) {
			return null;
		}
		
		computeGraph(resources, resourceGraph);
		subgraphs = computeSubgraphs(resources);
		if ((subgraphs == null) || (subgraphs.size() == 0)) {
			return null;
		}
		queries = computeQueries(subgraphs);
		if ((queries == null) || (queries.size() == 0)) {
			return null;
		}

		return queries;
	}

	public Map<String, Collection<KbElement>> computeResources(
			Map<String, Collection<KbElement>> ress) {
		keywordREdgeMap = new LinkedHashMap<String,Collection<KbElement>>();
		matchingREdges = new LinkedHashSet<KbElement>();
		Map<String,IProperty> keywordAttributeMap = new LinkedHashMap<String, IProperty>();
		Set<KbEdge> existingAEdges = new LinkedHashSet<KbEdge>();
		Set<String> keywords = new LinkedHashSet<String>();
		for (String keyword : ress.keySet()) {
			boolean allEdges = false;
			// System.out.println(keyword);
			Collection<KbElement> collection = ress.get(keyword);
			for (Iterator<KbElement> ite = collection.iterator(); ite.hasNext();) {
				KbElement element = ite.next();
				if(element instanceof KbVertex && element.getType() == KbElement.CVERTEX){
					// System.out.println(((INamedConcept)((KbVertex)element).getResource()).getUri());
					double weight = computeWeight((INamedConcept) ((KbVertex) element)
							.getResource());
					if (weight == Double.POSITIVE_INFINITY) {
						ite.remove();
					}
				} else if ((element instanceof KbEdge)
						&& (element.getType() == KbElement.REDGE)) {
					double weight = computeWeight(((KbEdge) element)
							.getProperty());
					if (weight == Double.POSITIVE_INFINITY) {
						ite.remove();
					} else {
						double weight1 = computeWeight((INamedConcept) ((KbEdge) element)
								.getVertex1().getResource());
						double weight2 = computeWeight((INamedConcept) ((KbEdge) element)
								.getVertex2().getResource());
						if ((weight1 == Double.POSITIVE_INFINITY)
								|| (weight2 == Double.POSITIVE_INFINITY)) {
							ite.remove();
						}
					}
					
				}
				else if(element instanceof KbEdge && element.getType() == KbElement.AEDGE){
					KbEdge edge = (KbEdge)element;
					IProperty dataprop = edge.getProperty();
					KbVertex vertex = edge.getVertex2();
					if(vertex.getType() == KbElement.VVERTEX){
						existingAEdges.add(edge);
					}
					else if(vertex.getType() == KbElement.DUMMY){
						keywordAttributeMap.put(keyword, dataprop);
					} 
				}
			}
			if ((collection == null) || (collection.size() == 0)) {
				keywords.add(keyword);
			}
			for (KbElement element : collection) {
				if ((element instanceof KbEdge)
						&& (element.getType() == KbElement.REDGE)) {
					allEdges = true;
					matchingREdges.add(element);
				} else {
					allEdges = false;
					break;
				}
			}
			if (allEdges) {
				keywordREdgeMap.put(keyword, ress.get(keyword));
				keywords.add(keyword);
			}
		}
		for (String keyword : keywords) {
			ress.remove(keyword);
		}
		for(String attribute : keywordAttributeMap.keySet()){
			IProperty keyattr = keywordAttributeMap.get(attribute); 
			for(KbEdge edge : existingAEdges){
				IProperty attr = edge.getProperty();
				if(keyattr.equals(attr)){
					ress.get(attribute).add(edge.getVertex2());
				}
			}
		}
		return ress;
	}

	public WeightedPseudograph<KbVertex, KbEdge> computeGraphSchemaIndex() {
		// if(resourceGraph != null) return resourceGraph;

		WeightedPseudograph<KbVertex, KbEdge> graph = new WeightedPseudograph<KbVertex, KbEdge>(
				KbEdge.class);

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					obj_path));
			sjtu.apex.q2semantic.graph.Graph g = (sjtu.apex.q2semantic.graph.Graph) ois
					.readObject();
			System.out.println("loading graph successfully!");

			Map<Integer, Node> nodeMap = g.nodes;
			Collection<Node> nodes = nodeMap.values();
			Iterator<Node> nodeIter = nodes.iterator();
			String lastURI = "", lastEdge = "", lastRangeURI = "";
			while (nodeIter.hasNext()) {
				Node node = nodeIter.next();
				if (node.type.equals(sjtu.apex.q2semantic.graph.Graph.Typ)) {
					boolean addVertex = false;
					String uri = baseURI + node.name;
					INamedConcept concept = new NamedConcept(pruneString(uri));
					KbVertex vertex = new KbVertex(concept, KbElement.CVERTEX,
							computeWeight(concept));
					addVertex = graph.addVertex(vertex);
//					if (addVertex) {
//						s_log.debug("Domain Vertex " + vertex + " is added to the graph!");
//					} else {
//						s_log.debug("Domain Vertex " + vertex + " is already in the graph!");
//					}
					
					List<Edge> edges = node.edges;
					for (int i = 0; i < edges.size(); i++) {
						Edge edge = edges.get(i);
//						System.out.println(edge.name + "\t" + edge.type + "\t" + nodeMap.get(Integer.valueOf(edge.to_id)).name);
						if (edge.type.equals(sjtu.apex.q2semantic.graph.Graph.Rel)) {
							if (edge.originDirection) {
								
								Node range = nodeMap.get(Integer.valueOf(edge.to_id));
								String range_uri = baseURI + range.name;
								if (uri.equals(lastURI) && edge.name.equals(lastEdge) && range_uri.equals(lastRangeURI))
									continue;
								lastURI = uri;
								lastEdge = edge.name;
								lastRangeURI = range_uri;
//								System.out.println(edge.name + "\t" + uri + "\t" + range_uri);
								IObjectProperty property = new ObjectProperty(pruneString(ns + edge.name));
								INamedConcept range_concept = new NamedConcept(pruneString(range_uri));
								KbVertex rangevertex = new KbVertex(range_concept,
										KbElement.CVERTEX, computeWeight(range_concept));
								addVertex = graph.addVertex(rangevertex);
//								if (addVertex) {
//									s_log.debug("Range Vertex " + rangevertex
//											+ " is added to the graph!");
//								} else {
//									s_log.debug("Range Vertex " + rangevertex
//											+ " is already in the graph!");
//								}

								addGraphElements(vertex, rangevertex, property, graph);
							}
						}
					}
				}
			}
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		verticesOfGraphIndex = graph.vertexSet();
		return graph;
	}

	public void computeGraph(Map<String, Collection<KbElement>> resources,
			WeightedPseudograph<KbVertex, KbEdge> graph) {
		for (String keyword : resources.keySet()) {
			Collection<KbElement> ress = resources.get(keyword);
			for (KbElement element : ress) {
				if (element.getType() == KbElement.VVERTEX) {
					KbVertex vertex = (KbVertex) element;
					addGraphElement(vertex, graph);
				} else if (element.getType() == KbElement.CVERTEX) {
					KbVertex vertex = (KbVertex) element;
					for (KbVertex ver : verticesOfGraphIndex) {
						if (ver.equals(vertex)) {
							ver.setScore(vertex.getScore());
						}
					}
					addGraphElement(vertex, graph);
				} else if (element.getType() == KbElement.AEDGE) {
					KbEdge edge = (KbEdge) element;
					addGraphElement(edge, graph);
				}
			}
		}

		for (KbEdge edge : graph.edgeSet()) {
			if (!matchingREdges.contains(edge)) {
//				if (edge.getProperty().equals(Property.SUBCLASS_OF)) {
//					graph.setEdgeWeight(edge, 3);
//				} else {
					double weight = edge.getVertex1().getCost()
							+ edge.getVertex2().getCost() + edge.getCost();
					graph.setEdgeWeight(edge, weight);
//				}
			} else {
				double weight = edge.getVertex1().getCost()
						+ edge.getVertex2().getCost();
				graph.setEdgeWeight(edge, 3);
			}
		}

		verticesOfGraphWithCursors = graph.vertexSet();
	}

	public Collection<Collection<KbEdge>> computeSubgraphs(
			Map<String, Collection<KbElement>> ress) {

		for (KbVertex vertex : verticesOfGraphWithCursors) {
			vertex.createCursors(ress.keySet());
		}
		
		for (String keyword : ress.keySet()) {
			
			for (KbElement element : ress.get(keyword)) {
				if (element instanceof KbVertex) {
					KbVertex vertex = (KbVertex) element;
					QueueEntry entry = new QueueEntry(keyword,
							getVertexInGraphWithCursors(vertex));
					expansionQueue.add(entry);
				}
			}
		}
		
		boolean isFinished = false;
		while (!isFinished) {
			QueueEntry expansion = expansionQueue.poll();
			boolean isExhausted = expansion.cursorExpand();
			expansionQueue.add(expansion);
			if (isExhausted == true) {
				expansionQueue.remove(expansion);
			}
			if (expansionQueue.size() == 0) {
				isFinished = true;
			} else if (!(subgraphQueue.size() < K_TOP)) {
				double lowestCost = 0;
				for (QueueEntry entry : expansionQueue) {
					lowestCost += entry.getCost();
				}
				double highestCost = subgraphQueue.peek().getCost();
				if (lowestCost > highestCost) {
					isFinished = true;
				}
			}
		}

		List<Collection<KbEdge>> subgraphs = new LinkedList<Collection<KbEdge>>();
		int size = subgraphQueue.size();
		System.out.println("computeSubgraphs - subgraphs: " + size);
		for (int i = 0; i < size; i++) {
			Subgraph subgraph = subgraphQueue.poll();
			System.out.println(i + ": " + subgraph + "\n");
			Set<KbEdge> edges = subgraph.getPaths();
			if (!subgraphs.contains(edges)) {
				subgraphs.add(0, edges);
			}
		}

		return subgraphs;
	}

	public KbVertex getVertexInGraphWithCursors(KbVertex vertex) {
		for (KbVertex vertexInGraph : verticesOfGraphWithCursors) {
			if (vertex.equals(vertexInGraph)) {
				return vertexInGraph;
			}
		}
		return null;
	}
	
	public String generateSPARQL(Collection<OWLPredicate> query) {
		Iterator<OWLPredicate> q_iter = query.iterator();
		StringBuffer body = new StringBuffer();
		HashSet<String> variables = new HashSet<String>();
		String variable;
		while (q_iter.hasNext()) {
			OWLPredicate owl_pred = q_iter.next();
			if (owl_pred instanceof ConceptMemberPredicate) {
				ConceptMemberPredicate cmp = (ConceptMemberPredicate) owl_pred;
				variable = "?" + ((Variable) cmp.getTerm()).getName();
				if (!variables.contains(variable))
					variables.add(variable);
				body.append("\t" + variable + "\t<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\t<" + ((INamedConcept) cmp.getConcept()).getUri() + ">.\n");
				
			}else if (owl_pred instanceof PropertyMemberPredicate) {
				PropertyMemberPredicate pmp = (PropertyMemberPredicate) owl_pred;
				IResource term1 = ((PropertyMemberPredicate) owl_pred).getFirstTerm();
				String res_term1 = "";
				if (term1 instanceof Variable) {
					res_term1 = "?" + ((Variable) term1).getName();
					if (!variables.contains(res_term1))
						variables.add(res_term1);
				}
				else if (term1 instanceof ILiteral) {
					res_term1 = "\"" + ((ILiteral) term1).getLiteral() + "\"";
//					String temp1 = ((ILiteral) term1).getLiteral();
//					int idx1 = temp1.indexOf("@");
//					if (idx1 != -1) {
//						res_term1 = "\"" + temp1.substring(0, idx1) + "\"" + temp1.substring(idx1);
//					} else {
//						res_term1 = "\"" + ((ILiteral) term1).getLiteral() + "\"@en";
//					}
				}
				IResource term2 = ((PropertyMemberPredicate) owl_pred).getSecondTerm();
				String res_term2 = "";
				if (term2 instanceof Variable) {
					res_term2 = "?" + ((Variable) term2).getName();
					if (!variables.contains(res_term2))
						variables.add(res_term2);
				}
				else if (term2 instanceof ILiteral) {
					res_term2 = "\"" + ((ILiteral) term2).getLiteral() + "\"";
//					String temp2 = ((ILiteral) term2).getLiteral();
//					int idx2 = temp2.indexOf("@");
//					if (idx2 != -1) {
//						res_term2 = "\"" + temp2.substring(0, idx2) + "\"" + temp2.substring(idx2);
//					} else {
//						res_term2 = "\"" + ((ILiteral) term2).getLiteral() + "\"@en";
//					}
				}
				body.append("\t" + res_term1 + "\t<" + pmp.getProperty().getUri() + ">\t" + res_term2 + ".\n");
			}
		}
		StringBuffer head = new StringBuffer();
		head.append("SELECT\t");
		Iterator<String> var_iter = variables.iterator();
		boolean first = true;
		while (var_iter.hasNext()) {
			String var = var_iter.next();
			if (first) {
				head.append(var);
				first = false;
			} else {
				head.append(" " + var);
			}
		}
		variables.clear();
		head.append("\nWHERE {\n");
		return head.toString() + body.toString() + "}";
	}

	private Collection<Collection<OWLPredicate>> computeQueries(
			Collection<Collection<KbEdge>> subgraphs) {
		Collection<Collection<OWLPredicate>> queries = new LinkedHashSet<Collection<OWLPredicate>>();
		for (Collection<KbEdge> subgraph : subgraphs) {
			Collection<OWLPredicate> query = new LinkedHashSet<OWLPredicate>();
			queries.add(query);
			Map<Pair, Variable> labelVar = new LinkedHashMap<Pair, Variable>();
			UniqueIdGenerator.getInstance().resetVarIds();
			for (KbEdge edge : subgraph) {
				if ((edge.getType() == KbElement.AEDGE)
						&& (edge.getVertex2().getType() != KbElement.DUMMY)) {
					IProperty p = edge.getProperty();
					IResource v1 = edge.getVertex1().getResource();
					IResource v2 = edge.getVertex2().getResource();
					IResource t;
					String conceptLabel = ((INamedConcept) v1).getUri();
					String literalLabel = ((ILiteral) v2).getLiteral();
					Pair label = new Pair(conceptLabel, literalLabel);
					Variable var = labelVar.get(label);
					if (var == null) {
						t = getNewVariable();
						labelVar.put(label, (Variable) t);
						query.add(new ConceptMemberPredicate(v1, t));
					} else {
						t = var;
					}

					query.add(new PropertyMemberPredicate(p, t, v2));
				}
			}
			for (KbEdge edge : subgraph) {
				if (edge.getType() == KbElement.REDGE) {
					IProperty p = edge.getProperty();
					IResource v1 = edge.getVertex1().getResource();
					IResource v2 = edge.getVertex2().getResource();

					Set<IResource> t1 = new LinkedHashSet<IResource>();
					String label1 = ((INamedConcept) v1).getUri();
					for (Pair pair : labelVar.keySet()) {
						if (pair.getHead().equals(label1)) {
							t1.add(labelVar.get(pair));
						}
					}
					if (t1.size() == 0) {
						Variable var = getNewVariable();
						t1.add(var);
						labelVar.put(new Pair(label1, null), var);
						query.add(new ConceptMemberPredicate(v1, var));
					}

					Set<IResource> t2 = new LinkedHashSet<IResource>();
					String label2 = "";
					if (v2 instanceof INamedConcept)
						label2 = ((INamedConcept) v2).getUri();
					else if (v2 instanceof ILiteral)
						label2 = ((ILiteral) v2).getLiteral();
					for (Pair pair : labelVar.keySet()) {
						if (pair.getHead().equals(label2)) {
							t2.add(labelVar.get(pair));
						}
					}
					if (t2.size() == 0) {
						Variable var = getNewVariable();
						t2.add(var);
						labelVar.put(new Pair(label2, null), var);
						query.add(new ConceptMemberPredicate(v2, var));
					}

					for (IResource resource1 : t1) {
						for (IResource resource2 : t2) {
							query.add(new PropertyMemberPredicate(p, resource1,
									resource2));
						}
					}

				}
			}
			for (KbEdge edge : subgraph) {
				if ((edge.getType() == KbElement.AEDGE)
						&& (edge.getVertex2().getType() == KbElement.DUMMY)) {
					IProperty p = edge.getProperty();
					IResource v1 = edge.getVertex1().getResource();
					IResource v2 = edge.getVertex2().getResource();

					Set<IResource> t1 = new LinkedHashSet<IResource>();
					String label1 = ((INamedConcept) v1).getUri();
					for (Pair pair : labelVar.keySet()) {
						if (pair.getHead().equals(label1)) {
							t1.add(labelVar.get(pair));
						}
					}
					if (t1.size() == 0) {
						Variable var = getNewVariable();
						t1.add(var);
						labelVar.put(new Pair(label1, null), var);
						query.add(new ConceptMemberPredicate(v1, var));
					}

					for (IResource resource1 : t1) {
						query.add(new PropertyMemberPredicate(p, resource1,
								getNewVariable()));
					}
				}
			}
		}

		return queries;
	}

	private String pruneString(String str) {
		return str.replace("\"", "");
	}

	public void addGraphElements(KbVertex vertex1, KbVertex vertex2,
			IProperty property, WeightedPseudograph<KbVertex, KbEdge> graph) {
		boolean addEdge = false;
		KbEdge edge = null;
		if (vertex2.getType() == KbElement.CVERTEX) {
//			if (property.equals(Property.SUBCLASS_OF)) {
//				edge = new KbEdge(vertex1, vertex2, property, KbElement.REDGE,
//						0);
//			} else {
				edge = new KbEdge(vertex1, vertex2, property, KbElement.REDGE,
						computeWeight(property));
//			}
		} else {
			edge = new KbEdge(vertex1, vertex2, property, KbElement.AEDGE, 1);
		}

		if (!(graph.containsEdge(edge))) {
			addEdge = graph.addEdge(vertex1, vertex2, edge);
//			if (addEdge) {
//				s_log.debug("Edge " + edge + " is added to the graph!");
//			}
		} else {
//			s_log.debug("Edge " + edge + " is already in the graph!");
		}
	}

	private void addGraphElement(KbVertex vertex,
			WeightedPseudograph<KbVertex, KbEdge> graph) {
		boolean addVertex = false;
		addVertex = graph.addVertex(vertex);
//		if (addVertex) {
//			s_log.debug("Vertex " + vertex + " is added to the graph!");
//		} else {
//			s_log.debug("Vertex " + vertex + " is already in the graph!");
//		}

	}

	public void addGraphElement(KbEdge edge,
			WeightedPseudograph<KbVertex, KbEdge> graph) {
		boolean addEdge = false;
		if (!(graph.containsEdge(edge))) {
			KbVertex vertex1 = edge.getVertex1();
			if (!(graph.containsVertex(vertex1))) {
				graph.addVertex(vertex1);
			}
			KbVertex vertex2 = edge.getVertex2();
			if (!(graph.containsVertex(vertex2))) {
				graph.addVertex(vertex2);
			}
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if (addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}
	}

	private Variable getNewVariable() {
		String var = String.valueOf(UniqueIdGenerator.getInstance()
				.getNewVarId());
		return new Variable("x" + var);
	}

	public Set<Variable> getRankedVariables(Collection<OWLPredicate> query) {
		Set<Variable> vars = new LinkedHashSet<Variable>();
		if ((query == null) || (query.size() == 0)) {
			return null;
		}

		// TODO perform the ranking
		for (OWLPredicate p : query) {
			IResource var1 = null;
			IResource var2 = null;

			if (p instanceof PropertyMemberPredicate) {
				var1 = ((PropertyMemberPredicate) p).getFirstTerm();
				var2 = ((PropertyMemberPredicate) p).getSecondTerm();
			} else if (p instanceof ConceptMemberPredicate) {
				var1 = ((ConceptMemberPredicate) p).getConcept();
				var2 = ((ConceptMemberPredicate) p).getTerm();
			}
			if (var1 instanceof Variable) {
				vars.add((Variable) var1);
			}
			if (var2 instanceof Variable) {
				vars.add((Variable) var2);
			}
		}

		return vars;
	}

	public boolean isSubConcept(Object subconcept, Object superconcept) {
		if ((subconcept instanceof INamedConcept)
				&& (superconcept instanceof INamedConcept)) {
			INamedConcept sup = (INamedConcept) superconcept;
			INamedConcept sub = (INamedConcept) subconcept;
			if (sup.getSubconcepts() != null) {
				if (sup.getSubconcepts().contains(sub)) {
					return true;
				} else {
					for (Object obj : sup.getSubconcepts()) {
						if (isSubConcept(sub, obj)) {
							return true;
						}
					}
				}
			} else {
				return false;
			}
		}
		return false;
	}

	private void computeTotalNumber() {
		if ((TOTAL_NUMBER_OF_VERTEX != -1) && (TOTAL_NUMBER_OF_EDGE != -1)) {
			return;
		}

		String line = null;
		int cat_num = 0;
		int rel_num = 0;
		int cat_total = 0;
		int rel_total = 0;
		String[] parts;
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(freq_path));
			while ((line = reader.readLine()) != null) {
				parts = line.split("\t");
				if (parts[0].equals("c")) {
					if (parts[1].equals("total: ")) {
						cat_total = Integer.parseInt(parts[2]);
					} else {
						cat_num++;
						catMap.put(parts[1], Integer.parseInt(parts[2]));
					}
				} else if (parts[0].equals("r")) {
					if (parts[1].equals("total: ")) {
						rel_total = Integer.parseInt(parts[2]);
					} else {
						rel_num++;
						relMap.put(parts[1], Integer.parseInt(parts[2]));
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TOTAL_NUMBER_OF_VERTEX = cat_num + cat_total;

		TOTAL_NUMBER_OF_EDGE = rel_num + rel_total;
	}

	private double computeEdgeWeight(double num, double totalNum) {
		if (num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2 - Math.log(1 + num / totalNum) / Math.log(2);
	}

	private double computeVertexWeight(double num, double totalNum) {
		if (num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2 - Math.log(1 + num / totalNum) / Math.log(2);
	}

	private double computeWeight(INamedConcept concept) {
		String uri = concept.getUri();
		uri = uri.substring(baseURI.length());
		int numIndividual = catMap.get(uri).intValue();
		double weight = computeVertexWeight(numIndividual,
				TOTAL_NUMBER_OF_VERTEX);
		if (weight < 0)
		System.out.println(uri + "\t" + numIndividual + "\t" + TOTAL_NUMBER_OF_VERTEX + "\t" + weight);
		return weight;
	}

	private double computeWeight(IProperty property) {
		Property prop = (Property) property;
		String uri = prop.getUri();
		uri = uri.substring(ns.length());
		int numProMem = relMap.get(uri).intValue();// property.getNumberOfPropertyMember();
		double weight =  computeEdgeWeight(numProMem, TOTAL_NUMBER_OF_EDGE);
		if (weight < 0)
		System.out.println(uri + "\t" + numProMem + "\t" + TOTAL_NUMBER_OF_EDGE + "\t" + weight);
		return weight;
	}

	public int mul(int a[], int n) {
		return n > 0 ? ((a[n - 1] + 1) * mul(a, --n)) : 1;
	}

	class QueueEntry implements Comparable {

		private ClosestFirstIterator<KbVertex, KbEdge> iter;

		private String keyword;

		private KbVertex matchingVertex;

		private float score;

		private double cost = 0;

		public QueueEntry(String keyword, KbVertex startVertex) {
			this.matchingVertex = startVertex;
			this.score = startVertex.getScore();
			this.keyword = keyword;

			if (width > 0) {
				// iter = new ClosestFirstIterator<KbVertex,
				// KbEdge>(resourceGraph, matchingVertex, width);
				iter = new ClosestFirstIterator<KbVertex, KbEdge>(
						resourceGraph, matchingVertex);
				iter.next();
			} else {
				iter = new ClosestFirstIterator<KbVertex, KbEdge>(
						resourceGraph, matchingVertex);
				iter.next();
			}
		}

		public boolean cursorExpand() {
			if (!iter.hasNext()) {
				return true;
			} else {
				KbVertex endVertex = iter.next();
				Map<String, Queue<Cursor>> cursors = getVertexInGraphWithCursors(
						endVertex).getCursors();
				List<KbEdge> path = createEdgeList(resourceGraph, iter,
						endVertex);
				double pathCost = iter.getShortestPathLength(endVertex);
				cost = pathCost / score;
				// System.out.println("(" + matchingVertex + ")" + " to " + "("
				// + endVertex + ")");
				// System.out.println("cost: " + cost);
				if (cost > threshold) {
					return true;
				}
				cursors.get(keyword)
						.add(new Cursor(matchingVertex, cost, path));

				boolean isConnectingVertex = false;
				double subgraphCost = 0;
				List<List<Cursor>> allCursors = new ArrayList<List<Cursor>>();
				for (Queue<Cursor> queue : cursors.values()) {
					if (queue.size() != 0) {
						isConnectingVertex = true;
						Cursor cursor = queue.peek();
						double cursorCost = cursor.getCost();
						if (cursorCost > threshold) {
							break;
						}
						subgraphCost += cursorCost;

						List<Cursor> css = new ArrayList<Cursor>();
						css.addAll(queue);
						allCursors.add(css);
					} else {
						isConnectingVertex = false;
						break;
					}
				}
				if ((isConnectingVertex == true) && (subgraphCost <= threshold)) {
					connectingElements.add(endVertex);
					Cursor[][] targetCursors = computeTargetCursors(allCursors);
					out: for (int i = 0; i < targetCursors.length; i++) {
						Cursor[] curs = targetCursors[i];
						double cost = 0;
						Set<KbEdge> edges = new LinkedHashSet<KbEdge>();
						for (Cursor cursor : curs) {
							cost += cursor.getCost();
							if (cost > threshold) {
								continue out;
							}
							edges.addAll(cursor.getPath());
						}

						boolean isSubgraph = false;
						if ((keywordREdgeMap != null)
								&& (keywordREdgeMap.size() != 0)) {
							for (Collection<KbElement> collection : keywordREdgeMap
									.values()) {
								isSubgraph = !Collections.disjoint(edges,
										collection);
								if (!isSubgraph) {
									break;
								}
							}
						} else {
							isSubgraph = true;
						}
						if (isSubgraph) {
							Subgraph subgraph = new Subgraph(endVertex, edges,
									cost);
							if (!subgraphQueue.contains(subgraph)) {
								if (subgraphQueue.size() < K_TOP) {
									subgraphQueue.add(subgraph);
								} else {
									double highestCost = subgraphQueue.peek()
											.getCost();
									if (subgraph.getCost() < highestCost) {
										subgraphQueue.poll();
										subgraphQueue.add(subgraph);
									}
								}
							}
							else {
								for(Subgraph sub : subgraphQueue){
									if(sub.equals(subgraph)){
										if(sub.getCost() > subgraph.getCost())
											sub.setCost(subgraph.getCost());
									}
								}
							}
						}
						
					}
				}
			}
			return false;
		}

		public List<KbEdge> createEdgeList(Graph<KbVertex, KbEdge> graph,
				ClosestFirstIterator<KbVertex, KbEdge> iter, KbVertex endVertex) {

			List<KbEdge> edgeList = new ArrayList<KbEdge>();

			while (true) {
				KbEdge edge = iter.getSpanningTreeEdge(endVertex);

				if (edge == null) {
					break;
				}
				edgeList.add(edge);
				endVertex = Graphs.getOppositeVertex(graph, edge, endVertex);
			}
			Collections.reverse(edgeList);
			return edgeList;
		}

		private Cursor[][] computeTargetCursors(List<List<Cursor>> cursors) {
			int size = cursors.size();
			int[] guard = new int[size];
			int i = 0;
			for (List<Cursor> list : cursors) {
				guard[i++] = list.size() - 1;
			}
			int entrySize = mul(guard, size);
			Cursor[][] entries = new Cursor[entrySize][size];

			int[] index = new int[size];
			for (int p : index) {
				p = 0;
			}
			guard[size - 1]++;
			i = 0;
			do {
				for (int m = 0; m < size; m++) {
					entries[i][m] = cursors.get(m).get(index[m]);
				}
				i++;
				index[0]++;
				for (int j = 0; j < size; j++) {
					if (index[j] > guard[j]) {
						index[j] = 0;
						index[j + 1]++;
					}
				}
			} while (index[size - 1] < guard[size - 1]);

			return entries;
		}

		public double getCost() {
			return cost;
		}

		public int compareTo(Object o) {
			QueueEntry other = (QueueEntry) o;
			if (cost > other.cost) {
				return 1;
			}
			if (cost < other.cost) {
				return -1;
			}
			return 0;
		}

	}

	class Cursor implements Comparable {

		private KbVertex matchingVertex;

		private double cost;

		List<KbEdge> edges;

		public Cursor() {
		}

		public Cursor(KbVertex matchingVertex, double cost, List<KbEdge> edges) {
			this.matchingVertex = matchingVertex;
			this.cost = cost;
			this.edges = edges;
		}

		public double getCost() {
			return cost;
		}

		public List<KbEdge> getPath() {
			return edges;
		}

		public KbVertex getMatchingVertex() {
			return matchingVertex;
		}

		public int compareTo(Object o) {
			Cursor other = (Cursor) o;
			if (cost > other.cost) {
				return 1;
			}
			if (cost < other.cost) {
				return -1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Cursor)) {
				return false;
			}
			Cursor other = (Cursor) o;
			if (cost != other.getCost()) {
				return false;
			}
			if (!(matchingVertex.equals(other.getMatchingVertex()))) {
				return false;
			}
			if (!(edges.equals(other.getPath()))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int code = 0;
			code += 7 * matchingVertex.hashCode() + 13 * edges.hashCode();
			return code;
		}

		@Override
		public String toString() {
			return "cost: " + cost + "\n" + "matchingVertex: " + matchingVertex
					+ "\n" + "Path: " + edges + "\n";
		}

	}

	class Subgraph implements Comparable {

		private KbVertex connectingVertex;

		private Set<KbEdge> paths;

		double cost;

		public Subgraph(KbVertex connectingVertex, Set<KbEdge> paths,
				double cost) {
			this.connectingVertex = connectingVertex;
			this.cost = cost;
			this.paths = paths;
		}

		public Set<KbEdge> getPaths() {
			return paths;
		}

		public KbVertex getConnectingVertex() {
			return connectingVertex;
		}
		
		public void setCost(double cost){
			this.cost = cost;
		}

		public double getCost() {
			return cost;
		}

		public int compareTo(Object o) {
			Subgraph other = (Subgraph) o;
			if (this.cost > other.cost) {
				return -1;
			}
			if (this.cost < other.cost) {
				return 1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Subgraph)) {
				return false;
			}

			Subgraph other = (Subgraph) o;
			if (!(paths.equals(other.getPaths()))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return 13 * paths.hashCode();
		}

		@Override
		public String toString() {
			return "cost: " + cost + "\n" + "Connecting vertex: "
					+ connectingVertex + "\n" + "Paths: " + paths + "\n";
		}
	}

}
