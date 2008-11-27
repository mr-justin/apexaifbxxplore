package org.apexlab.service.session.datastructure;

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

	/**
	 * @return the element1
	 */
	public Facet getElement1() {
		return element1;
	}

	/**
	 * @param element1 the element1 to set
	 */
	public void setElement1(Facet element1) {
		this.element1 = element1;
	}

	/**
	 * @return the element2
	 */
	public Facet getElement2() {
		return element2;
	}

	/**
	 * @param element2 the element2 to set
	 */
	public void setElement2(Facet element2) {
		this.element2 = element2;
	}
}
