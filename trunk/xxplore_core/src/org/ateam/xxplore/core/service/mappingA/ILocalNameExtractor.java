package org.ateam.xxplore.core.service.mappingA;

public interface ILocalNameExtractor {

	/**
	 * Get the local name from a URI
	 * @param uri is the URI
	 * @return the local name extracted from the URI
	 */
	public String getLocalName(String uri);
}
