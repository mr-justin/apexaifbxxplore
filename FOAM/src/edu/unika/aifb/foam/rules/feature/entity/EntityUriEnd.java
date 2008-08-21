/*
 * Created on 04.01.2005
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
public class EntityUriEnd implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {			
			OWLEntity entity = (OWLEntity) object;
			String uri = entity.getURI();
			int pos = uri.indexOf("#");
			if (pos!=-1) {
				String sub = uri.substring(pos+1,uri.length());
				return sub;
			} 
			return null;
		} catch (Exception e) {
			return null;
		}
	}

}