package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IDao {
	
	public void saveOntology();
	
	public INamedIndividual findIndividualByUri(String uri);
	
}
