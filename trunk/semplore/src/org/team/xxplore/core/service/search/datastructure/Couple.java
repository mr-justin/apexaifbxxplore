package org.team.xxplore.core.service.search.datastructure;

/**
 * Simple class representing a couple of Facet objects
 * @author tpenin
 */
public class Couple {

	public Facet element1;
	public Facet element2;
	
	public Couple() {
		this.element1 = null;
		this.element2 = null;
	}

	/**
	 * @param element1
	 * @param element2
	 */
	public Couple(Facet element1, Facet element2) {
		this.element1 = element1;
		this.element2 = element2;
	}
}
