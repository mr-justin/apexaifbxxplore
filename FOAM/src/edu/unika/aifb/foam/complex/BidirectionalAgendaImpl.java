package edu.unika.aifb.foam.complex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

import edu.unika.aifb.foam.agenda.Agenda;
import edu.unika.aifb.foam.agenda.AgendaElement;
import edu.unika.aifb.foam.input.Structure;

public class BidirectionalAgendaImpl implements Agenda {

	public HashSet list = new HashSet();

	public Iterator iter;

	public Set set = new HashSet();

	public boolean internaltoo = false;

	private static final int MAX = 500000;

	public boolean hasNext() {
		return iter.hasNext();
	}

	public int size() {
		return list.size();
	}

	public void create(Structure structure1, boolean internaltoo) {
	}

	public void add(Agenda agenda) {
		Iterator iter = agenda.collection().iterator();
		while (iter.hasNext()) {
			AgendaElement element = (AgendaElement) iter.next();
			Integer code = new Integer(element.object1.hashCode()
					+ element.object2.hashCode());
			if (set.contains(code) == false) {
				list.add(element);
				set.add(code);
			}
		}
	}

	public Collection collection() {
		return list;
	}

	public void parameter(Object object) {
	}

	public AgendaElement next() {
		return (AgendaElement) iter.next();
	}

	public void iterate() {
		iter = list.iterator();
	}

	public void add(Set set1, Set set2) {
		int counter = 0;
		Iterator iter1 = set1.iterator();
		while (iter1.hasNext() && (counter < MAX)) {
			Object object1 = iter1.next();
			Iterator iter2 = set2.iterator();
			while (iter2.hasNext() && (counter < MAX)) {
				Object object2 = iter2.next();
				String substring1 = "";
				String substring2 = "";
				String string1 = "";
				String string2 = "";
				try {
					//removes comparisons within the same namespace
					OWLEntity entity1 = (OWLEntity) object1; 
					OWLEntity entity2 = (OWLEntity) object2;
					string1 = entity1.getURI().toString();
					int index1 = string1.indexOf('#');
					substring1 = string1.substring(0, index1);
					string2 = entity2.getURI().toString();
					int index2 = string2.indexOf('#');
					substring2 = string2.substring(0, index2);
				}
				catch (Exception e) {
					// UserInterface.print(e.getMessage());
				}
				/*
				 * if (string1.endsWith("Aktivurlaub")) { System.out.print("!"); }
				 */
				if ((object1.equals(object2) == false)
						&& (internaltoo || ((substring1.equals(substring2) == false) ))) {
					AgendaElement element = new AgendaElement();
					element.object1 = object1;
					element.object2 = object2;
					element.action = "comp";
					// System.out.println(object1.toString()+"
					// "+object2.toString()+" "+list.size());
					list.add(element);
					counter++;
					// System.out.println(" "+list.size());
					
					//bidirectional
					element.object1 = object2;
					element.object2 = object1;
					element.action = "comp";
					list.add(element);
					counter++;

				}
			}
		}
	}

}
