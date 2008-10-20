package org.ateam.xxplore.core.service;

import org.ateam.xxplore.core.model.interaction.ApplicationInteraction;
import org.ateam.xxplore.core.model.interaction.IInteractionDao;


public class DefaultInteractionTrackingService implements
		IInteractionTrackingService {

	ApplicationInteraction m_interaction;
	
	
	IInteractionDao m_delegate;
	
	
	public DefaultInteractionTrackingService(IInteractionDao interactionDao){
		m_delegate = interactionDao;
	}
	
	public void storeInteraction(){
		try {
			m_delegate.insert(m_interaction);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub

	}

	public void disposeService() {
		// TODO Auto-generated method stub

	}

	public void init(Object... params) {
		// TODO Auto-generated method stub

	}
	
	 

}
