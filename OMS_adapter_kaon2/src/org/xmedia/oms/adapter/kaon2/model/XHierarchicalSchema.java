package org.xmedia.oms.adapter.kaon2.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.reasoner.SubsumptionHierarchy;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IHierarchicalSchemaNode;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.ISchemaNode;
import org.xmedia.oms.model.impl.NamedConcept;


/**
 * Is a special schema which contains only the is_a property. 
 * @author Administrator
 *
 */
public class XHierarchicalSchema implements IHierarchicalSchema{

	private SubsumptionHierarchy m_delegate;
	private Kaon2Ontology m_onto;

	private static Logger s_log = Logger.getLogger(XHierarchicalSchema.class);
	
	public XHierarchicalSchema(SubsumptionHierarchy hierarchy, Kaon2Ontology onto){
		Emergency.checkPrecondition(hierarchy != null, "hierarchy != null");
		m_delegate = hierarchy;
		m_onto = onto;
	}

	public IHierarchicalSchemaNode getBottomNode() {
		return new XHierarchicalSchemaNode(m_delegate.nothingNode());
	}

	public IHierarchicalSchemaNode getTopNode() {
		return new XHierarchicalSchemaNode(m_delegate.thingNode());
	}

	public ISchemaNode getNode(INamedConcept clazz) {

		return new XHierarchicalSchemaNode(m_delegate.
				getNodeFor(KAON2Manager.factory().owlClass(clazz.getUri())));
	}

	public class XHierarchicalSchemaNode implements IHierarchicalSchemaNode{

		private SubsumptionHierarchy.Node m_delegate;

		public static final String SUBCLASS_PROPERTY = "is_a";

		public XHierarchicalSchemaNode(SubsumptionHierarchy.Node node){
			m_delegate = node;
		}

		public Set<IHierarchicalSchemaNode> getAncestors() {
			Set<SubsumptionHierarchy.Node> delegates  = m_delegate.getAncestorNodes();
			Set<IHierarchicalSchemaNode> nodes = null; 
			if (delegates != null){
				nodes = new HashSet<IHierarchicalSchemaNode>();
				for(SubsumptionHierarchy.Node node : delegates){
					nodes.add(new XHierarchicalSchemaNode(node));
				}
			}
			return nodes;
		}

		public Set<IHierarchicalSchemaNode> getChilds() {
			Set<SubsumptionHierarchy.Node> delegates  = m_delegate.getChildNodes();
			Set<IHierarchicalSchemaNode> nodes = null; 
			if (delegates != null){
				nodes = new HashSet<IHierarchicalSchemaNode>();
				for(SubsumptionHierarchy.Node node : delegates){
					nodes.add(new XHierarchicalSchemaNode(node));
				}
			}
			return nodes;
		}

		public Set<IHierarchicalSchemaNode> getDescendants() {
			Set<SubsumptionHierarchy.Node> delegates  = m_delegate.getDescendantNodes();
			Set<IHierarchicalSchemaNode> nodes = null; 
			if (delegates != null){
				nodes = new HashSet<IHierarchicalSchemaNode>();
				for(SubsumptionHierarchy.Node node : delegates){
					nodes.add(new XHierarchicalSchemaNode(node));
				}
			}
			return nodes;
		}

		public Set<IHierarchicalSchemaNode> getParents() {
			Set<SubsumptionHierarchy.Node> delegates  = m_delegate.getParentNodes();
			Set<IHierarchicalSchemaNode> nodes = null; 
			if (delegates != null){
				nodes = new HashSet<IHierarchicalSchemaNode>();
				for(SubsumptionHierarchy.Node node : delegates){
					nodes.add(new XHierarchicalSchemaNode(node));
				}
			}
			return nodes;
		}

		public Map<String, ISchemaNode> getAllRelatedFromNodes() {
			Set<IHierarchicalSchemaNode> nodes = getAncestors();
			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, (ISchemaNode)node);
				}
			}

			return nodeMap;
		}

		public Map<String, ISchemaNode> getAllRelatedNodes() {
			Set<IHierarchicalSchemaNode> nodes = getAncestors();
			nodes.addAll(getDescendants());

			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, (ISchemaNode)node);
				}
			}

			return nodeMap;
		}

		public Map<String, ISchemaNode> getAllRelatedToNodes() {
			Set<IHierarchicalSchemaNode> nodes = getDescendants();
			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, (ISchemaNode)node);
				}
			}

			return nodeMap;
		}



		public Map<String, ISchemaNode> getRelatedFromNodes() {
			Set<IHierarchicalSchemaNode> nodes = getParents();
			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, (ISchemaNode)node);
				}
			}

			return nodeMap;
		}

		public Map<String, ISchemaNode> getRelatedNodes() {
			Set<IHierarchicalSchemaNode> nodes = getParents();
			nodes.addAll(getChilds());
			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, (ISchemaNode)node);
				}
			}

			return nodeMap;
		}

		public Map<String, ISchemaNode> getRelatedToNodes() {
			Set<IHierarchicalSchemaNode> nodes = getChilds();
			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, (ISchemaNode)node);
				}
			}

			return nodeMap;
		}



		public Set<INamedConcept> getConcepts() {

			Set<OWLClass> clazzs = m_delegate.getOWLClasses();
			Set<INamedConcept> iClazzs = null;

			if (s_log.isDebugEnabled()) s_log.debug("get concept of node: " + this.toString());
			
			if(clazzs != null){
				iClazzs = new HashSet<INamedConcept>();
				for (OWLClass clazz : clazzs){
					INamedConcept concept = new NamedConcept(clazz.getURI(), m_onto);
					concept.setDelegate(clazz);
					iClazzs.add(concept);
					if (s_log.isDebugEnabled()) s_log.debug("concept found: " + ((OWLClass)clazz).getURI());
				}	
			}

			return iClazzs;
		}

	}

}
