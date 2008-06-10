package org.xmedia.oms.query;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Individual;

public class PropertyMemberPredicate extends OWLPredicate {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5268660094068633690L;

	private IProperty m_property;
	
	private IResource m_term1;
	
	private IResource m_term2;
	
	public PropertyMemberPredicate(IProperty prop, IResource term1, IResource term2){
		super();
		Emergency.checkPrecondition(term1 instanceof Individual || term1 instanceof ILiteral ||
				term1 instanceof Variable, "term1 instanceof NamedIndividual || term1 instanceof ILiteral || term1 instanceof Variable");	
		Emergency.checkPrecondition(term2 instanceof Individual || term2 instanceof ILiteral ||
				term2 instanceof Variable, "term2 instanceof NamedIndividual || term2 instanceof ILiteral || term2 instanceof Variable");	
		
		m_property = prop;
		m_term1 = term1;
		m_term2 = term2;
	}
	
	public IProperty getProperty(){
		return m_property;
	}
	
	public IResource getFirstTerm(){
		return m_term1;
	}
	
	public IResource getSecondTerm(){
		return m_term2;
	}

	public void setFirstTerm(IResource term){
		Emergency.checkPrecondition(term instanceof Individual || term instanceof ILiteral ||
				term instanceof Variable, "term instanceof NamedIndividual || term instanceof ILiteral || term instanceof Variable");	
		m_term1 = term;
	}
	
	public void setSecondTerm(IResource term){
		Emergency.checkPrecondition(term instanceof Individual || term instanceof ILiteral ||
				term instanceof Variable, "term instanceof NamedIndividual || term instanceof ILiteral || term instanceof Variable");	
		m_term2 = term;
	}
	
	public String toString(){
		return getProperty().toString() + "(" + getFirstTerm().toString() + "," + getSecondTerm().toString() + ")";
	}
}
