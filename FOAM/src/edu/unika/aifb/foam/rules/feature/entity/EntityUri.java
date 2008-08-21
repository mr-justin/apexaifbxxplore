/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.entity;

import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class EntityUri implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {		
			OWLEntity entity = (OWLEntity) object;
			String uri = entity.getURI();
			return uri;
		} catch (Exception e) {
			return null;
		}
	}

}