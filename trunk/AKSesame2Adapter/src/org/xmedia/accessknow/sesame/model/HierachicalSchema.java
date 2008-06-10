package org.xmedia.accessknow.sesame.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.xmedia.accessknow.sesame.persistence.converter.AK2Ses;
import org.xmedia.accessknow.sesame.persistence.converter.Ses2AK;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IHierarchicalSchemaNode;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.ISchemaNode;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IDaoManager;

public class HierachicalSchema implements IHierarchicalSchema {

	private SesameOntology m_onto;
	private ValueFactory m_factory;

	public HierachicalSchema(SesameOntology onto){
		
		Emergency.checkPrecondition(onto != null, "onto != null");
		m_onto = onto;
		m_factory = m_onto.getRepository().getValueFactory();
		
	}	
	
	public IHierarchicalSchemaNode getTopNode() {
		
		return new HierarchicalSchemaNode(m_factory.createURI("http://www.w3.org/2002/07/owl#Thing"));
		
	}

	public ISchemaNode getNode(INamedConcept clazz) {

		if(clazz.equals(NamedConcept.TOP)){
			return getTopNode();
		}
		else{
			
			Resource result = m_onto.findResourceByURI(clazz.getUri());

			if(result != null) {
				return new HierarchicalSchemaNode(result);
			} else {
				return new HierarchicalSchemaNode(Status.NO_CONCEPT_FOUND);
			}
		}
	}

	
	private class Status{
		
		protected static final int CONCEPT_FOUND = 0;
		protected static final int NO_CONCEPT_FOUND = 1;
		
	}
		
	public class HierarchicalSchemaNode implements IHierarchicalSchemaNode{
		
		public static final String SUBCLASS_PROPERTY = "is_a";
		
		private int m_status;
		private URI m_uri;
		private IDaoManager m_doaManager;
		
		
		public HierarchicalSchemaNode(Object res){
			
			if(res instanceof URI){				
				m_uri = (URI)res;
				m_status = Status.CONCEPT_FOUND;
				m_doaManager = PersistenceUtil.getDaoManager();
			}
			else if(res instanceof Integer){
				m_status = (Integer)res;
			}
		}

		public Set<IHierarchicalSchemaNode> getAncestors() {
			
			HashSet<IHierarchicalSchemaNode> ancestors = new HashSet<IHierarchicalSchemaNode>();
			
			if(this.m_status == Status.CONCEPT_FOUND){
				
				ArrayList<IHierarchicalSchemaNode> tmp = new ArrayList<IHierarchicalSchemaNode>();
				ArrayList<IHierarchicalSchemaNode> direct_parents = new ArrayList<IHierarchicalSchemaNode>();

				tmp.addAll(this.getParents());
				ancestors.addAll(tmp);

				while(!tmp.isEmpty()){

					direct_parents.clear();
					direct_parents.addAll(tmp.get(0).getParents());

					tmp.addAll(direct_parents);
					ancestors.addAll(direct_parents);

					tmp.remove(0);

				}
			}
			
			return ancestors;		
		}

		public Set<IHierarchicalSchemaNode> getChilds() {

			HashSet<IHierarchicalSchemaNode> children = new HashSet<IHierarchicalSchemaNode>();

			if(this.m_status == Status.CONCEPT_FOUND){
				
				try{
					IConceptDao conceptDao = m_doaManager.getConceptDao();

					if(this.equals(getTopNode())) {	

						List<? extends IBusinessObject> concepts = conceptDao.findAll();//

						for(Object object : concepts){

							if(object instanceof INamedConcept){

								INamedConcept this_concept = (INamedConcept)object;
								Set<IConcept> superConcepts = conceptDao.findSuperconcepts(this_concept);

								if(superConcepts.isEmpty()){
									try {
										children.add(new HierarchicalSchemaNode(AK2Ses.getResource(this_concept, m_factory)));
									} 
									catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					} 
					else {

						Set<IConcept> concepts = conceptDao.findSubconcepts(Ses2AK.getNamedConcept(m_uri, m_onto));		
						Iterator<IConcept> iter = concepts.iterator();

						while(iter.hasNext()){

							try {
								IConcept this_concept = iter.next();
								children.add(new HierarchicalSchemaNode(AK2Ses.getResource(this_concept, m_factory)));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}						
				}
				catch(DaoUnavailableException e){
					e.printStackTrace();
				}
			}
			
			return children;
		}

		public Set<IHierarchicalSchemaNode> getDescendants() {

			HashSet<IHierarchicalSchemaNode> descendants = new HashSet<IHierarchicalSchemaNode>();

			if(this.m_status == Status.CONCEPT_FOUND){
				
				ArrayList<IHierarchicalSchemaNode> tmp = new ArrayList<IHierarchicalSchemaNode>();
				ArrayList<IHierarchicalSchemaNode> direct_children = new ArrayList<IHierarchicalSchemaNode>();

				tmp.addAll(this.getChilds());
				descendants.addAll(tmp);

				while(!tmp.isEmpty()){

					direct_children.clear();
					direct_children.addAll(tmp.get(0).getChilds());

					tmp.addAll(direct_children);
					descendants.addAll(direct_children);

					tmp.remove(0);

				}
			}
			
			return descendants;
		}
		
		public Set<IHierarchicalSchemaNode> getParents() {

			HashSet<IHierarchicalSchemaNode> parents = new HashSet<IHierarchicalSchemaNode>();

			if(this.m_status == Status.CONCEPT_FOUND){
				
				try{

					IConceptDao conceptDao = m_doaManager.getConceptDao();

					if(!this.equals(getTopNode())) {	

						Set<IConcept> super_concepts = conceptDao.findSuperconcepts(Ses2AK.getNamedConcept(m_uri, m_onto));

						for(IConcept super_concept : super_concepts){

							try {
								parents.add(new HierarchicalSchemaNode(AK2Ses.getResource(super_concept, m_factory)));
							} 
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						if(parents.isEmpty()){
							parents.add(getTopNode());
						}
					} 				
				}
				catch(DaoUnavailableException e){
					e.printStackTrace();
				}
			}
			
			return parents;
		}

		public Map<String, ISchemaNode> getAllRelatedFromNodes() {
			
			Set<IHierarchicalSchemaNode> nodes = getAncestors();
			Map<String, ISchemaNode> nodeMap = null;
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, node);
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
					nodeMap.put(SUBCLASS_PROPERTY, node);
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
					nodeMap.put(SUBCLASS_PROPERTY, node);
				}
			}

			return nodeMap;
		}

		public Set<INamedConcept> getConcepts() {
		
			Set<INamedConcept> concepts = new HashSet<INamedConcept>();
			
			if(this.m_status == Status.CONCEPT_FOUND){				
				concepts.add(Ses2AK.getNamedConcept(m_uri, m_onto));
			}
			
			return concepts;
		}

		public Map<String, ISchemaNode> getRelatedFromNodes() {
			
			Set<IHierarchicalSchemaNode> nodes = getParents();
			Map<String, ISchemaNode> nodeMap = null;
			
			if (nodes != null){

				nodeMap = new HashMap<String, ISchemaNode>();
				for(IHierarchicalSchemaNode node : nodes) {
					nodeMap.put(SUBCLASS_PROPERTY, node);
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
					nodeMap.put(SUBCLASS_PROPERTY, node);
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
					nodeMap.put(SUBCLASS_PROPERTY, node);
				}
			}

			return nodeMap;
		}
		
		@Override
		public String toString(){
			return m_uri.stringValue();
		}
		
		@Override
		public boolean equals(Object object){
			
			if(object instanceof HierarchicalSchemaNode){
				
				String uri1 = ((HierarchicalSchemaNode)object).m_uri.toString();
				String uri2 = this.m_uri.toString();
				
				return uri1.equals(uri2);
				
			}
			else {
				return false;
			}
		}
	}
}
