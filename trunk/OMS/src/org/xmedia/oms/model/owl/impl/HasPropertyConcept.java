package org.xmedia.oms.model.owl.impl;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.Concept;
import org.xmedia.oms.model.owl.api.IHasPropConcept;

public class HasPropertyConcept extends Concept implements IHasPropConcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8943234448056801200L;

	private int m_type; 
	
	private IConcept m_concept;
	
	private IProperty m_property;
	
	public HasPropertyConcept(int type){
		super();
		Emergency.checkPrecondition(type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF, 
				"type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF");
		m_type = type;
	}
	
	public HasPropertyConcept(IOntology onto, int type){
		super(onto);
		Emergency.checkPrecondition(type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF, 
			"type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF");
		m_type = type;
	}

	public HasPropertyConcept(String label, int type){
		super(label);
		Emergency.checkPrecondition(type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF, 
			"type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF");
		m_type = type;
	}
	
	public HasPropertyConcept(String label, IOntology onto, int type){
		super(label, onto);
		Emergency.checkPrecondition(type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF, 
			"type == IHasPropConcept.ALL_OF || type == IHasPropConcept.SOME_OF");
		m_type = type;
	}
	
	public IConcept getConcept() {
		
		return m_concept;
	}

	public void setConcept(IConcept concept){
		m_concept = concept;
		
	}
	public IProperty getProperty() {
		return m_property;
	}
	
	public void setProperty(IProperty property){
		m_property = property;
	}

	public int getType() {
		
		return m_type;
	}
}
