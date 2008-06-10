package org.xmedia.oms.query;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Individual;

public class ConceptMemberPredicate extends OWLPredicate {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2568493860845748847L;

	private INamedConcept m_concept;
	
	private IResource m_term; 
	
	public ConceptMemberPredicate(IResource concept, IResource term){
	
		super();
		
		Emergency.checkPrecondition(concept instanceof INamedConcept ||
				concept instanceof IDatatype, "term instanceof INamedConcept || term instanceof IDatatype");	
		Emergency.checkPrecondition(term instanceof Individual ||
				term instanceof Variable, "resource instanceof NamedIndividual || resource instanceof Variable");	
		m_concept = (INamedConcept)concept;
		m_term = term;
	}
	
	public IResource getConcept(){
		return m_concept;
	}
	
	public IResource getTerm(){
		return m_term;
	}
	
	public void setConcept(IResource concept){
		Emergency.checkPrecondition(concept instanceof INamedConcept ||
				concept instanceof IDatatype, "term instanceof INamedConcept || term instanceof IDatatype");	

		m_concept = (INamedConcept)concept;
	}
	
	public void setTerm(IResource term){
		Emergency.checkPrecondition(term instanceof Individual ||
				term instanceof Variable, "resource instanceof NamedIndividual || resource instanceof Variable");	
		m_term = term;
	}
	
	public String toString(){
		return m_concept.getUri() + "(" + m_term.getLabel() + ")";
	}
}
