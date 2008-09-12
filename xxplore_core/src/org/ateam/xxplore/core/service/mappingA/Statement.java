package org.ateam.xxplore.core.service.mappingA;

/**
 * 
 * @author jqchen
 * descript a triple
 * 
 */
public class Statement {
	public String subject;
	public String predicate;
	public String object;
	
	public Statement(String subject, String predicate, String object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
}
