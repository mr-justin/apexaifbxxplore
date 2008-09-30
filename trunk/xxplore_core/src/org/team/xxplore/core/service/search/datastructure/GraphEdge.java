package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents the edge of a decorated graph.
 * @author tpenin
 */
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
	public GraphEdge() {}

	/**
	 * @param from
	 * @param to
	 * @param decoration
	 */
	public GraphEdge(Facet from, Facet to, Facet decoration) {
		this.fromElement = from;
		this.toElement = to;
		this.decorationElement = decoration;
	}

	/**
	 * @return the decoration
	 */
	public Facet getDecoration() {
		return decorationElement;
	}

	/**
	 * @param decoration the decoration to set
	 */
	public void setDecoration(Facet decoration) {
		this.decorationElement = decoration;
	}

	/**
	 * @return the from
	 */
	public Facet getFrom() {
		return fromElement;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(Facet from) {
		this.fromElement = from;
	}

	/**
	 * @return the to
	 */
	public Facet getTo() {
		return toElement;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(Facet to) {
		this.toElement = to;
	}
}
