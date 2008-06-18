package org.xmedia.oms.adapter.kaon2.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.ITransaction;
import org.xmedia.oms.persistence.KbEnvironment;


public class Kaon2Transaction implements ITransaction {

	// changes to be applied to the ontology
	private List<OntologyChangeEvent> m_changes;
	
	//the kaon ontology delegate;
	private Ontology m_onto; 
		
	public void configure(Map props) {
		m_onto = ((Kaon2Ontology)props.get(KbEnvironment.ONTOLOGY)).getDelegate();
	}
	
	public void begin() throws DatasourceException {
		m_changes = new ArrayList<OntologyChangeEvent>();
	}
	
	public void commit() throws DatasourceException {
		Emergency.checkPrecondition(m_changes != null, "m_changes != null");
		
		if (m_changes.size() > 0){ 
			try {
				m_onto.applyChanges(m_changes);
			} catch (KAON2Exception e) {
				
				throw new DatasourceException(e);
			}
		}
	}

	public boolean isActive() throws DatasourceException {
		if (m_changes != null){
			
			return true;
		}
		else return false;
	}

	public void rollback() throws DatasourceException {
		// TODO Auto-generated method stub

	}
	
	public void addChanges(OntologyChangeEvent change){
		m_changes.add(change);
	}

}
