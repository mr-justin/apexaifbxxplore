/*
 * Created on 26.11.2003
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.*;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultList;

/**
 * This Agenda triggers comparisons between entities which have been 
 * identified as similar or one of the most similar in previous 
 * iterations. For this it requires a ResultList as input.
 * 
 * @author Marc Ehrig
 */

public class AllMatchesAgenda extends AgendaImpl {

	private ResultList resultList;
	private static final int MAX = 5;

	public void create(Structure structure, boolean internaltooT) {
		internaltoo = internaltooT;
		list = new HashSet();
		Iterator iter = resultList.objectList().iterator();
		while (iter.hasNext()) {
			Object entity = iter.next();
			if ((entity instanceof OWLClass)||(entity instanceof DataProperty)||(entity instanceof ObjectProperty)||(entity instanceof Individual)) {
				HashSet set1 = new HashSet();
				HashSet set2 = new HashSet();					
				Object object1 = entity;
				set1.add(object1);
				Object object2 = resultList.getObject(object1, 0);
				int i = 1;
				while ((object2!=null)&&(i<=MAX)&&(i<=resultList.maxRanks()))
				{
					set2.add(object2);				
					object2 = resultList.getObject(object1,i);
					i++;
//					System.out.print(i);
				}
//				System.out.println();
				add(set1,set2);				
			}				
		}
	}

	public void parameter(Object object) {
		resultList = (ResultList) object;
	}
}
