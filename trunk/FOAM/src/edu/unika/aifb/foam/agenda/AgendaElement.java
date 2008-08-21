/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.Comparator;

/**
 * An AgendaElement is the individual element containing information
 * about what to compare i.e. the two entities and the kind of comparison
 * which is required.
 * 
 * @author Marc Ehrig
 */
public class AgendaElement {

	public Object object1;
	public Object object2;
	public Object action;
	
	/**
	 * @author Marc Ehrig
	 */
	public class MyComparator implements Comparator {
		public boolean equals(Object o1) {
			AgendaElement second = (AgendaElement) o1;
			boolean equal = (object1.equals(second.object1)&&object2.equals(second.object2)&&action.equals(second.action));
			return equal;
		}
		public int compare(Object o1, Object o2) {
			AgendaElement first = (AgendaElement) o1;
			AgendaElement second = (AgendaElement) o2;
			if (first.object1.equals(second.object1)&&first.object2.equals(second.object2)&&first.action.equals(second.action)) {
				return 0;
			}
			return 1;
		}		
	}

}
