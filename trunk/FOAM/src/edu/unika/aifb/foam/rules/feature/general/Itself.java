/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.general;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class Itself implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		return object;
	}

}
