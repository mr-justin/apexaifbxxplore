package org.team.xxplore.core.service.search.datastructure;

import org.dom4j.Document;

/**
 * Simple class representing a generic couple of objects
 * @author tpenin
 * @param <T1> Type of the first object
 * @param <T2> Type of the second object
 */
public class Couple<T1, T2> implements XMLSerializable {

	public T1 element1;
	public T2 element2;
	
	public Couple() {
		this.element1 = null;
		this.element2 = null;
	}

	/**
	 * @param element1
	 * @param element2
	 */
	public Couple(T1 element1, T2 element2) {
		this.element1 = element1;
		this.element2 = element2;
	}
	
	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
	}
}
