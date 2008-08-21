/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules.feature;

import java.io.Serializable;

import edu.unika.aifb.foam.input.Structure;

/**
 * Access to an individual ontology feature such as
 * label, subclasses, or domain specifica, etc.
 * 
 * @author Marc Ehrig
 */
public interface Feature extends Serializable{
	
	public Object get(Object object, Structure structure);
	
}
