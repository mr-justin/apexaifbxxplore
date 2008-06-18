package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IDao {
	
	public void saveOntology();
	
	public INamedIndividual findIndividualByUri(String uri);
	
}
