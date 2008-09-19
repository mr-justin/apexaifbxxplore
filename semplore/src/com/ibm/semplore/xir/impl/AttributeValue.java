/**
 * 
 */
package com.ibm.semplore.xir.impl;

/**
 * @author linna
 *
 */
public class AttributeValue {

	protected String attribute;
	protected String value;
	
	public AttributeValue(String attr, String val) {
		this.attribute = attr;
		this.value = val;
	}
	
	public String getAttribute() {
		return attribute;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getCombination() {
		return attribute+"###"+value;
	}
}
