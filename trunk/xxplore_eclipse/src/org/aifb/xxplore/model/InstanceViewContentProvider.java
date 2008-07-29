package org.aifb.xxplore.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.query.ResourceTuple;

public class InstanceViewContentProvider implements ITreeContentProvider{

	public static final String INDIVIDUALS = "Individuals";
	public static final String PROPERTIES = "Relations";
	public static final String PROPERTYMEMBERS = "Property Members";
	private static final String RELATED = "Related Properties";
	private static final String DATAPROPERTYMEMBERS = "Data Property Members";
	private static final String OBJECTPROPERTYMEMBERS = "Object Property Members";

	public void dispose() {}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	public Object[] getElements(Object inputElement) {		
		assert inputElement instanceof IStructuredSelection;
		IStructuredSelection selection = (IStructuredSelection) inputElement;
		InstanceTreeNode[] nodes = createTreeNodes(null, selection.toArray());		
		return  nodes;
	}

	
	public Object[] getChildren(Object parentElement) {		
		return  ((InstanceTreeNode)parentElement).getChildren();
	}
	
	public Object[] getChildrenImplementation(Object parentElement) {		
		Object[] children = null;
		InstanceTreeNode parent = (InstanceTreeNode) parentElement;		
//		if (parent.getValue() instanceof INamedConcept){
//			System.out.println("Val "+ ((INamedConcept)parent.getValue()).getLabel());
//			children = createTreeNodes(parent,new Object[] {
//				new Container(PROPERTIES, parent.getValue()),
//				new Container(INDIVIDUALS, parent.getValue())
//			});			
//			return children;
//		}
//		if (parent.getValue() instanceof IProperty){
//			System.out.println("Val "+ ((IProperty)parent.getValue()).getLabel());
//			children = createTreeNodes(parent,new Object[] {
//				new Container(PROPERTYMEMBERS, parent.getValue()),
//				new Container(RELATED, parent.getValue())
//			});			
//			return children;
//		}
		if (parent.getValue() instanceof IIndividual) {
			children = createTreeNodes(parent, new Object[] {
				new Container(OBJECTPROPERTYMEMBERS, parent.getValue()),
				new Container(DATAPROPERTYMEMBERS, parent.getValue())
			});
			return children;
		}
		if (parent.getValue() instanceof Container){
			Container container = (Container)parent.getValue();
//			if (container.getValue() instanceof INamedConcept) {
//				if (INDIVIDUALS.equals(container.getPredicate())) {				
//					Set<IIndividual> indis = ((INamedConcept) container.getValue())
//							.getMemberIndividuals();
//					if(indis != null && indis.size() > 0) children = createTreeNodes(parent, indis.toArray());
//				}
//				if (PROPERTIES.equals(container.getPredicate())) {
//					Set<IProperty> types = ((INamedConcept) container.getValue())
//							.getProperties();
//					if(types != null && types.size() > 0) children = createTreeNodes(parent, types.toArray());
//				}
//			}
//			if (container.getValue() instanceof IProperty) {
//				if (PROPERTYMEMBERS.equals(container.getPredicate()) ){
//					Set<IPropertyMember> indis = ((IProperty)container.getValue()).getMemberIndividuals();
//					if(indis != null && indis.size() > 0) children = createTreeNodes(parent, indis.toArray());
//				}
//				if (RELATED.equals(container.getPredicate()) ){					
//					Set<? extends IResource> relclass = ((IProperty)container.getValue()).getDomainsAndRanges();
//					if(relclass != null && relclass.size() > 0) children = createTreeNodes(parent, relclass.toArray());
//				}
//			}
			if (container.getValue() instanceof INamedIndividual) {
				Set<IPropertyMember> props = ((INamedIndividual)container.getValue()).getObjectPropertyFromValues();
				Set<IPropertyMember> itemProps = new HashSet<IPropertyMember>();
				if (props != null && props.size() > 0) {
					if (OBJECTPROPERTYMEMBERS.equals(container.getPredicate())) {
						for (IPropertyMember prop : props) {
							if (prop.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER)
								itemProps.add(prop);
						}
					}
					if (DATAPROPERTYMEMBERS.equals(container.getPredicate())) {
						for (IPropertyMember prop : props) {
							if (prop.getType() == PropertyMember.DATA_PROPERTY_MEMBER)
								itemProps.add(prop);
						}
					}
					children = createTreeNodes(parent, itemProps.toArray());
				}
//				props.addAll(((INamedIndividual)container.getValue()).getPropertyFromValues());
			}
			return children;
		}
		return null;
	}

	public Object getParent(Object element) {
		return ((InstanceTreeNode)element).getParent();
	}

	public boolean hasChildren(Object element) {
		return ((InstanceTreeNode)element).hasChildren();
	}
	
	public InstanceTreeNode[] createTreeNodes(InstanceTreeNode parent , Object[] elements){
		if(elements == null || elements.length == 0) return null;
		InstanceTreeNode[] nodes = null;
		if (elements[0] instanceof ResourceTuple) {
			ResourceTuple rt = (ResourceTuple)elements[0];
			nodes = new InstanceTreeNode[rt.getArity()];
			for (int i = 0; i < rt.getArity(); i++) {
				nodes[i] = new InstanceTreeNode(parent, rt.getElementAt(i));
			}
		}
		else {
			nodes = new InstanceTreeNode[elements.length];
			int i = -1;
			for (Object obj : elements){
				i++;
				nodes[i] = new InstanceTreeNode(parent,obj);									
			}
		}
		return nodes;
	}

	public class InstanceTreeNode {

		InstanceTreeNode m_parent;
		Object m_value;
		boolean m_childrencalc = false;
		boolean m_haschildren = false;
		InstanceTreeNode[] m_children;

		public InstanceTreeNode(InstanceTreeNode parent, Object value) {
			m_parent = parent;
			m_value = value;
		}

		public Object getParent() {
			return m_parent;
		}

		public Object getValue() {
			return m_value;
		}

		public boolean hasChildren() {
			if (!m_childrencalc) {
				m_childrencalc = true;
				m_children = (InstanceTreeNode[]) getChildrenImplementation(this);
				if (m_children == null) {
					m_haschildren = false;
				} else {
					if (m_children.length != 0)
						m_haschildren = true;
					else
						m_children = null;
				}
			}
			return m_haschildren;
		}

		public Object[] getChildren() {
			if (hasChildren())
				return m_children;
			return m_children;
		}

	}

	public class Container {

		String m_predicate;
		Object m_contained;

		public Container(String predicate, Object value) {
			m_predicate = predicate;
			m_contained = value;
		}

		public String getPredicate() {
			return m_predicate;
		}

		public Object getValue() {
			return m_contained;
		}
	}
}
