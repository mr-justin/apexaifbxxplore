package org.xmedia.oms.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IOntology;

/**
 * A Manager for Reasoners 
 */
public class ReasonerPool implements IReasonerPool{

	private List<Object> m_reasoners;

	//set default pool size
	private int m_maxpool = 10;		

	private List<Boolean> m_available;

	private IOntology m_ontology;

	private boolean m_isConfigured = false;

	public ReasonerPool(IOntology ontology) {		
		m_ontology = ontology;
	}

	private int getCurrentSize(){
		if (m_reasoners!=null) return m_reasoners.size()-1;
		else return -1;
	}

	public synchronized Object getAvailableReasoner(){
		Emergency.checkPrecondition(m_isConfigured, "Reasoner pool must be configured first!");
		if (getCurrentSize()>-1){	    		
			int index =-1;
			if ((index = m_available.indexOf(true))>-1){
				m_available.set(index, false);
				return m_reasoners.get(index);

			}else {

				return createReasoner();
			}
		}else {
			m_reasoners = new LinkedList<Object>();
			m_available = new LinkedList<Boolean>();

			return createReasoner();

		}	    	
	}

	private Object createReasoner(){
		if (getCurrentSize()<m_maxpool){
			Object reasoner = m_ontology.createReasoner();
			m_reasoners.add(reasoner);
			m_available.add(true);
			return reasoner;
		}else {
			throw new DatasourceException("Already too many Reasoners");
		}
	}

	public synchronized void freeReasoner(Object reasoner){
		int i = m_reasoners.indexOf(reasoner);
		if (i > -1) m_available.set(i, true);

	}


	/**
	 * TODO check if really required --> garbage collector does this job
	 * when ontolgo is closed anyway 
	 *
	 */
	public void dispose(){
		if (m_reasoners != null){
			m_reasoners.clear();
			m_reasoners = null;
		}
	}

	public void configure(Map properties) {
		if(properties != null){
			if((Integer)properties.get(KbEnvironment.REASONER_POOL_SIZE) != null)
				m_maxpool = ((Integer)properties.get(KbEnvironment.REASONER_POOL_SIZE)).intValue();
		}
		m_isConfigured = true;

	}
}