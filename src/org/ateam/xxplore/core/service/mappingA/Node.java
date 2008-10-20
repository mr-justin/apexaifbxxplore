package org.ateam.xxplore.core.service.mappingA;

public class Node {
	enum Type {
		concepts,
		relation_subject,
		relation_object,
		attribute,
	}
	
	public String element;
	public Type type;
	
	public Node(String element, Type type) {
		super();
		this.element = element;
		this.type = type;
	}
}
