package org.xmedia.accessknow.sesame.model;

import java.util.Set;

import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;

public class Property extends org.xmedia.oms.model.impl.Property {

	private static final long serialVersionUID = 8479874496626970063L;
	
	protected Property(String uri, IOntology ontology) {
		super(uri, ontology);
	}

	/**
	 * @deprecated
	 */
//	@Override
//	public String getLabel() {
//		return toString();
//	}

	@Override
	public String toString() {
		return getUri();
	}

	/**
	 * @deprecated
	 */
	public Set<? extends IResource> getDomainsAndRanges() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated
	 */
	public Set<? extends IResource> getRanges() {
		return null;
	}

}
