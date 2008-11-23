package org.team.xxplore.core.service.mapping;

public interface ILocalNameExtractor {

	/**
	 * Get the local name from a URI
	 * @param uri is the URI
	 * @return the local name extracted from the URI
	 */
	public String getLocalName(String uri);
}
