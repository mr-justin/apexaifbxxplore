package org.aifb.xxplore.model;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.views.propertyview.InstanceTreeNodePropertySource;
import org.aifb.xxplore.views.propertyview.OutlineTreeNodePropertySource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.views.properties.IPropertySource;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.query.ResourceTuple;

public class ElementContentProvider implements ITreeContentProvider{


	public static final String INDIVIDUALS = "Individuals";
	public static final String PROPERTIES = "Property";
	public static final String OBJECTPROPERTY = "Object Property";
	public static final String DATAPROPERTY = "Data Property";
	public static final String PROPERTYMEMBERS = "Property Members";
	public static final String DATAPROPERTYMEMBERS = "Data Property Members";
	public static final String OBJECTPROPERTYMEMBERS = "Object Property Members";
	public static final String TYPE = "Type of Individual";

	public void dispose() {}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	
	public Object[] getElements(Object inputElement) {		
		assert inputElement instanceof IStructuredSelection;
		IStructuredSelection selection = (IStructuredSelection) inputElement;
		
		if (selection.toArray()[0] instanceof ResourceTuple){
			InstanceTreeNode[] nodes = createInstanceTreeNodes(null, selection.toArray());		
			return  nodes;
		}
		//create tree node with the first element of the selection, which is the resource activated in the tree viewer
		OutlineTreeNode[] nodes = createOutlineTreeNodes(null, new Object[]{selection.toArray()[0]});		
		return  nodes;
	}
	
	public Object[] getChildren(Object parentElement) {	
		if (parentElement instanceof OutlineTreeNode) {
			return ((OutlineTreeNode)parentElement).getChildren();
		} else if (parentElement instanceof InstanceTreeNode) {
			return ((InstanceTreeNode)parentElement).getChildren();
		} else {
			return null;
		}
	}

	public Object getParent(Object element) {
		if (element instanceof OutlineTreeNode) {
			return ((OutlineTreeNode)element).getParent();
		} else if (element instanceof InstanceTreeNode) {
			return ((InstanceTreeNode)element).getParent();
		} else {
			return null;
		}
	}

	public boolean hasChildren(Object element) {
		if (element instanceof OutlineTreeNode) {
			return ((OutlineTreeNode)element).hasChildren();
		} else if (element instanceof InstanceTreeNode) {
			return ((InstanceTreeNode)element).hasChildren();
		} else {
			return false;
		}
	}

	public boolean isSubConcept(Object subconcept, Object superconcept){
		if ((subconcept instanceof INamedConcept) && (superconcept instanceof INamedConcept)){
			INamedConcept sup = (INamedConcept)superconcept;
			INamedConcept sub = (INamedConcept)subconcept;
			if(sup.getSubconcepts() !=  null){
				if (sup.getSubconcepts().contains(sub)) {
					return true;
				} else {
					for (Object obj : sup.getSubconcepts()) {
						if(isSubConcept(sub,obj)) {
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
	
	public OutlineTreeNode[] createOutlineTreeNodes(OutlineTreeNode parent , Object[] elements){		
		if((elements == null) || (elements.length == 0)) {
			return null;
		}
		OutlineTreeNode[] nodes = new OutlineTreeNode[elements.length];
		int i = -1;
		for (Object obj : elements){
			i++;
			nodes[i] = new OutlineTreeNode(parent,obj);									
		}
		return nodes;
	}	
	
	public InstanceTreeNode[] createInstanceTreeNodes(InstanceTreeNode parent , Object[] elements){
		if((elements == null) || (elements.length == 0)) {
			return null;
		}
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

	public class OutlineTreeNode implements IAdaptable{
		
		OutlineTreeNode m_parent;
		Object m_value;
		boolean m_childrencalc= false;
		boolean m_haschildren= false;
		OutlineTreeNode[] m_children;

		public OutlineTreeNode(OutlineTreeNode parent, Object value) {			
			m_parent=parent;
			m_value= value;
		}

		public Object getParent(){
			return m_parent;
		}

		public Object getValue(){
			return m_value;
		}

		public boolean hasChildren(){
			if (!m_childrencalc){
				m_childrencalc=true;
				m_children= (OutlineTreeNode[]) getChildrenImplementation(this);
				if (m_children==null) {
					m_haschildren= false;
				}else {
					if (m_children.length!=0) {
						m_haschildren= true;
					} else {
						m_children=null;
					}
				}
			}
			return m_haschildren;
		}	
		
		public Object[] getChildren() {
			if (hasChildren()) {
				return m_children;
			}
			return m_children;
		}
		
		private Object[] getChildrenImplementation(Object parentElement) {		
			Object[] children = null;
			OutlineTreeNode parent = (OutlineTreeNode) parentElement;		
			if (parent.getValue() instanceof INamedConcept){
				System.out.println("Val "+ ((INamedConcept)parent.getValue()).getLabel());
				children = createOutlineTreeNodes(parent,new Object[] {
						new Container(PROPERTIES, parent.getValue()),
						new Container(INDIVIDUALS, parent.getValue())
				});			
				return children;
			}
			
			if (parent.getValue() instanceof Container){
				Container container = (Container)parent.getValue();
				if (container.getValue() instanceof INamedConcept) {
					if (INDIVIDUALS.equals(container.getPredicate())) {				
						Set<IIndividual> indivs = ((INamedConcept) container.getValue()).getMemberIndividuals();
						if((indivs != null) && (indivs.size() > 0)) {
							children = createOutlineTreeNodes(parent, indivs.toArray());
						}
					}
	
					if (PROPERTIES.equals(container.getPredicate())) {
						Set<IProperty> props = ((INamedConcept) container.getValue()).getPropertiesFrom();
						Set<IProperty> objectProps = new HashSet<IProperty>();
						Set<IProperty> dataProps = new HashSet<IProperty>();
						Object[] children1 = null;
						Object[] children2 = null;
						if((props != null) && (props.size() > 0)){	
							for (IProperty element : props){
								if(element instanceof IObjectProperty) {
									objectProps.add(element);
								}
							}
							children2 = createOutlineTreeNodes(parent, objectProps.toArray());
							
							for (IProperty element : props){
								if(element instanceof IDataProperty) {
									dataProps.add(element);
								}
							}	
							children1 = createOutlineTreeNodes(parent, dataProps.toArray());
							
							int length1 = 0;
							int length2 = 0;
							if(children1 != null) {
								length1 = children1.length;
							}
							if(children2 != null) {
								length2 = children2.length;
							} 
							children = new OutlineTreeNode[length1 + length2]; 
							if(length1 > 0) {
								System.arraycopy(children1, 0, children, 0, length1);
							}
							if(length2 > 0) {
								System.arraycopy(children2, 0, children, length1, length2);
							}
						}
					}
				}
				return children;
			}
			return null;
		}

		public Object getAdapter(Class adapter) {
			if (adapter == IPropertySource.class){
				if((this.getValue() instanceof INamedIndividual) ||
						(this.getValue() instanceof INamedConcept) ||
						(this.getValue() instanceof IProperty)){
					return new OutlineTreeNodePropertySource(this);
				}
				else{
					return null;
				}				
			}
			else{
				return null;
			}
		}		
	}

	public class InstanceTreeNode implements IAdaptable{

		private InstanceTreeNode m_parent;
		/** 
		 * Either a Container object or an IIndividual
		 */
		private Object m_value;
		private InstanceTreeNode[] m_children;
		private boolean m_children_found;
		

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
			
//			children not set yet ... do it now
			if(m_children == null){
				setChildren();
			}
			if(m_children_found){
				return m_children.length > 0 ? true : false;
			}
			else{
				return false;
			}
		}

		public Object[] getChildren() {
			
			if(m_children == null){
				setChildren();
			}
			return m_children;
		}
		
		private void setChildren() {	
						
			if (m_value instanceof IIndividual) {
				m_children = createInstanceTreeNodes(this, new Object[] {
					new Container(TYPE,m_value),	
					new Container(PROPERTYMEMBERS,m_value)
				});
			}
			else if (m_value instanceof Container){
				
				Container container = (Container)m_value;
					
				if(container.getPredicate().equals(PROPERTYMEMBERS)){

					Set<IPropertyMember> prop_members = ((INamedIndividual)container.getValue()).getPropertyFromValues();
					Set<IPropertyMember> objectprop_members = new HashSet<IPropertyMember>();
					Set<IPropertyMember> dataprop_members = new HashSet<IPropertyMember>();
					
					if ((prop_members != null) && (prop_members.size() > 0)) {

						for (IPropertyMember prop_member : prop_members) {
								
							if(prop_member.getProperty() instanceof IObjectProperty){
								objectprop_members.add(prop_member);
							}
							else if(prop_member.getProperty() instanceof IDataProperty){
								dataprop_members.add(prop_member);
							}	
						}
					}
					
					InstanceTreeNode[] objectprop_members_children = createInstanceTreeNodes(this, objectprop_members.toArray());
					InstanceTreeNode[] dataprop_members_children = createInstanceTreeNodes(this, dataprop_members.toArray());
					
					m_children = new InstanceTreeNode[objectprop_members_children.length + dataprop_members_children.length]; 
					
//					copy stuff...
					System.arraycopy(objectprop_members_children, 0, m_children, 0, objectprop_members_children.length);
					System.arraycopy(dataprop_members_children, 0, m_children, objectprop_members_children.length, dataprop_members_children.length);
					
				}
				else if(container.getPredicate().equals(TYPE)){
					
					Object[] cons = ((INamedIndividual)container.getValue()).getTypes().toArray();
					for (int j = cons.length - 1; j > 0; j--) {
						if (isSubConcept(cons[j],cons[j-1])) {
							Object o = cons[j];
							cons[j] = cons[j-1];
							cons[j-1] = o;
						}
					}

					m_children = createInstanceTreeNodes(this, new Object[]{cons[0]});			
				}
			}
			
//			still ... no children found -> set flag
			if(m_children == null){
				m_children_found = false;
			}
			else{
				m_children_found = true;
			}
		}
		
		public Object getAdapter(Class adapter) {
			if (adapter == IPropertySource.class){
				if((this.getValue() instanceof INamedIndividual) ||
						(this.getValue() instanceof INamedConcept)){
					return new InstanceTreeNodePropertySource(this);
				}
				else{
					return null;
				}
			}
			else{
				return null;
			}
		}	
	}
	
	public class Container {

		private String m_predicate;
		private Object m_value;

		public Container(String predicate, Object value) {
			m_predicate = predicate;
			m_value = value;
		}

		public String getPredicate() {
			return m_predicate;
		}

		public Object getValue(){
			return m_value;
		}
	}

}
