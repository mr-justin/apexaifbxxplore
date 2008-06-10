package org.xmedia.oms.query;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.exception.EmergencyException;
import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.model.api.IResource;

public class ResourceTuple implements ITuple {

	private int m_arity; 

	private Object[] m_res; 

	private String[] m_labels; 
	
	private ComplexProvenance m_prov;

	/**
	 * It is assume that the ordering of labels correspond to the ordering of elements in the array so that the 
	 * elmeene
	 * @param arity
	 * @param elements
	 * @param labels
	 */
	public ResourceTuple(int arity, Object[] elements, String[] labels){
		for(Object e : elements){
			if (!(e instanceof IResource)) throw new EmergencyException("e must be instanceof IResource");
		}
		
		//TODO
		//Emergency.checkPrecondition(arity == elements.length && labels.length == elements.length, 
		//"arity == elements.length && labels.length == elements.length");

		m_arity = arity;
		m_res = elements;
		m_labels = labels;
	}
	
	public ResourceTuple(int arity, Object[] elements, String[] labels, ComplexProvenance prov) {
		this(arity, elements, labels);
		m_prov = prov;
	}
	
	public int getArity() {

		return m_arity;
	}

	public IResource getElementAt(int position) {

		return (IResource)m_res[position];

	}
	public String getLabelAt(int position) {

		return m_labels[position];
	}

	public String toString(){
		String tuples ="";
		for (int i = 0; i < m_res.length; i++){
			tuples+= m_labels[i] + "=" + m_res[i] + " ";
		}
		
		return tuples;
	}
	
	public ComplexProvenance getProvenance() {
		return m_prov;
	}
	
	public boolean hasProvenance() {
		return m_prov != null;
	}
}
