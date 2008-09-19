package org.team.xxplore.core.service.search.datastructure;

public class SourceFacet extends Facet {
	/**
	 * Default constructor
	 */
	public SourceFacet() {
		super();
	}
	
	public SourceFacet(String label, String uri, Source source) {
		super(label, uri, source);
	}
}
