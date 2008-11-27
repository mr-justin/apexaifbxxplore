package org.apexlab.service.session.datastructure;

public class GraphEdge {
	
	// Origin of the edge
	public Facet fromElement;
	// Destination
	public Facet toElement;
	// Decoration
	public Facet decorationElement;
	
	/**
	 * Default constructor
	 */
	public GraphEdge() {
		this.fromElement = null;
		this.toElement = null;
		this.decorationElement = null;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param dec
	 */
	public GraphEdge(Facet from, Facet to, Facet dec) {
		fromElement = from;
		toElement = to;
		decorationElement = dec;
	}
	
	public Facet getFromElement() {
		return fromElement;
	}
	
	public void setFromElement(Facet fromElement) {
		this.fromElement = fromElement;
	}

	public Facet getToElement() {
		return toElement;
	}
	
	public void setToElement(Facet toElement) {
		this.toElement = toElement;
	}

	public Facet getDecorationElement() {
		return decorationElement;
	}

	public void setDecorationElement(Facet decorationElement) {
		this.decorationElement = decorationElement;
	}

}
