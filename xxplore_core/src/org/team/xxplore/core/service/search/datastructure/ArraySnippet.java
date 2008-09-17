package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

import org.dom4j.Document;

/**
 * This class contains the elements to implement the "array snippet" fonctionality
 * @author tpenin
 */
public class ArraySnippet implements XMLSerializable  {
	
	// The result item this ArraySnippet object is attached to
	private ResultItem resultItem;
	// The list of relation-attribute couples attached to this result item
	private LinkedList<Couple<Relation, Attribute>> relation_attribute;
	// The list of attribute-value couples attached to this result item. Note that the value can be a litteral or not
	private LinkedList<Couple<Attribute, Facet>> attribute_value;
	// The list of the classes attached to this result item
	private LinkedList<Concept> classeList;
	
	/**
	 * Default constructor
	 */
	public ArraySnippet() {
		this.resultItem = null;
		this.relation_attribute = new LinkedList<Couple<Relation, Attribute>>();
		this.attribute_value = new LinkedList<Couple<Attribute, Facet>>();
		this.classeList = new LinkedList<Concept>();
	}

	/**
	 * Constructor
	 * @param resultItem The result item this ArraySnippet object is attached to
	 * @param relation_attribute The list of relation-attribute couples attached to this result item
	 * @param attribute_value The list of attribute-value couples attached to this result item. Note 
	 * that the value can be a litteral or not
	 * @param classeList The list of the classes attached to this result item
	 */
	public ArraySnippet(ResultItem resultItem, LinkedList<Couple<Relation, Attribute>> relation_attribute, LinkedList<Couple<Attribute, Facet>> attribute_value, LinkedList<Concept> classeList) {
		this.resultItem = resultItem;
		this.relation_attribute = relation_attribute;
		this.attribute_value = attribute_value;
		this.classeList = classeList;
	}
	
	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the attribute_value
	 */
	public LinkedList<Couple<Attribute, Facet>> getAttribute_value() {
		return this.attribute_value;
	}

	/**
	 * @param attribute_value the attribute_value to set
	 */
	public void setAttribute_value(LinkedList<Couple<Attribute, Facet>> attribute_value) {
		this.attribute_value = attribute_value;
	}

	/**
	 * @return the classeList
	 */
	public LinkedList<Concept> getClasseList() {
		return this.classeList;
	}

	/**
	 * @param classeList the classeList to set
	 */
	public void setClasseList(LinkedList<Concept> classeList) {
		this.classeList = classeList;
	}

	/**
	 * @return the relation_attribute
	 */
	public LinkedList<Couple<Relation, Attribute>> getRelation_attribute() {
		return this.relation_attribute;
	}

	/**
	 * @param relation_attribute the relation_attribute to set
	 */
	public void setRelation_attribute(
			LinkedList<Couple<Relation, Attribute>> relation_attribute) {
		this.relation_attribute = relation_attribute;
	}

	/**
	 * @return the resultItem
	 */
	public ResultItem getResultItem() {
		return this.resultItem;
	}

	/**
	 * @param resultItem the resultItem to set
	 */
	public void setResultItem(ResultItem resultItem) {
		this.resultItem = resultItem;
	}
}
