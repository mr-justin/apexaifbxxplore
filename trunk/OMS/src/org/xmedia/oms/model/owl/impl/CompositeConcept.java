package org.xmedia.oms.model.owl.impl;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.impl.Concept;
import org.xmedia.oms.model.owl.api.ICompositeConcept;

public class CompositeConcept extends Concept implements ICompositeConcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5055476562883287655L;

	private int m_type; 
	
	private Set<IConcept> m_concepts;
	
	public CompositeConcept(int type) {
		super();
		Emergency.checkPrecondition(type == UNION || type == INTERSECTION, "type == UNION || type == INTERSECTION");		
		m_type = type;
		m_concepts = new HashSet<IConcept>();
	}

	public CompositeConcept(String label, int type) {
		super(label);
		Emergency.checkPrecondition(type == UNION || type == INTERSECTION, "type == UNION || type == INTERSECTION");		
		m_type = type;
		m_concepts = new HashSet<IConcept>();
	}
	
	public CompositeConcept(String label, IOntology onto, int type) {
		super(label, onto);
		Emergency.checkPrecondition(type == UNION || type == INTERSECTION, "type == UNION || type == INTERSECTION");		
		m_type = type;
		m_concepts = new HashSet<IConcept>();
	}
	
	public void addConcept(IConcept concept) {
		m_concepts.add(concept);
	}

	public Set<IConcept> getConcepts() {
		return m_concepts;
	}
	
	public int getType(){
		return m_type;
	}

}
