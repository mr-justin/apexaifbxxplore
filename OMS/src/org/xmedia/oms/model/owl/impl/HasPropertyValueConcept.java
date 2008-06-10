package org.xmedia.oms.model.owl.impl;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.Concept;
import org.xmedia.oms.model.owl.api.IHasPropValueConcept;

public class HasPropertyValueConcept extends Concept implements IHasPropValueConcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8943234448056801200L;
	
	private IProperty m_property;
	
	private IIndividual m_individual;
	
//	public HasPropertyValueConcept(){
//		super();
//	}
//	
//	public HasPropertyValueConcept(IOntology onto){
//		super(onto);
//	}
//
//	public HasPropertyValueConcept(String label){
//		super(label);
//	}
//	
//	public HasPropertyValueConcept(String label, IOntology onto){
//		super(label, onto);
//	}
	
	public IIndividual getIndividual() {
		return m_individual;
	}
	
	public void setIndividual(IIndividual ind){
		m_individual = ind;
	}

	public IProperty getProperty() {
		
		return m_property;
	}
	
	public void setProperty(IProperty property) {
		m_property = property;
	}

}
