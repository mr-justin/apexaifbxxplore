/*
 * Created on 22.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import org.semanticweb.kaon2.api.Entity;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author meh
 *
 */
public class FeatureSpecialImpl implements Feature{

	private static final long serialVersionUID = 1L;
	public Object[] object = new Object[3];
	
	public Object get(Object object, Structure structure) {
		return null;
	}

	public void set(Entity entityT, String type) {
		object[0] = entityT;
		try {
		object[1] = entityT.getURI();
		} catch (Exception e) {}
		object[2] = type;		//C Concept, D DataProperty, O ObjectProperty, I Instance
	}	
	
	public void serialize() {
		object[0] = null;		
	}
	
	public void deserialize(Structure structure) {
		try {
			if (object[2].equals("C")) {
				OWLClass concept = KAON2Manager.factory().owlClass((String) object[1]);
				object[0] = concept;
			}
			if (object[2].equals("D")) {
				DataProperty dataproperty = KAON2Manager.factory().dataProperty((String) object[1]);
				object[0] = dataproperty;
			}
			if (object[2].equals("O")) {
				ObjectProperty objectproperty = KAON2Manager.factory().objectProperty((String) object[1]);
				object[0] = objectproperty;
			}
			if (object[2].equals("I")) {
				Individual instance = KAON2Manager.factory().individual((String) object[1]);
				object[0] = instance;
			}
		} catch (Exception e) {}
	}
	
}
