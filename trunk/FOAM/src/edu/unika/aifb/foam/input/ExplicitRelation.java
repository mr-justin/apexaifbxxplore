/*
 * Created on 15.08.2003
 *
 */
package edu.unika.aifb.foam.input;

//import java.io.FileInputStream;
//import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

//import com.Ostermiller.util.CSVParser;

import edu.unika.aifb.foam.result.ResultList;
import edu.unika.aifb.foam.util.CSVParse;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * Allows to load a mapping file. Explicit mapping relations 
 * are added to the result list. User predefined mappings can be
 * inserted into the system. This is also the entry point for 
 * user interaction during runtime.
 * 
 * @author Marc Ehrig
 */
public class ExplicitRelation {

	private class Pair {
		private Object object1;
		private Object object2;
		private double value;
	}

	public Vector pairs = new Vector();
	
	public ExplicitRelation() {
	}
	
	public ExplicitRelation(String fileName, Structure structure) {
		if ((fileName.equals(""))==false) {
			try {
				MyOntology ontology = (MyOntology) structure;
				Set concepts = ontology.ontology.createEntityRequest(OWLClass.class).getAll();
				Set properties = ontology.ontology.createEntityRequest(ObjectProperty.class).getAll();
				Set properties2 = ontology.ontology.createEntityRequest(DataProperty.class).getAll();
				properties.addAll(properties2);
				Set instances = ontology.ontology.createEntityRequest(Individual.class).getAll();
	/*			InputStream inputStream = new FileInputStream(fileName);	
				CSVParser csvparser = new CSVParser(inputStream, "", "", "");
				csvparser.changeDelimiter(';');		
				String array[][] = csvparser.getAllValues();*/
				CSVParse csvParse = new CSVParse(fileName);
				String array[][] = csvParse.getAllValues();
				for (int i = 0; i<(array.length); i++){
					String uri1 = array[i][0];
					String uri2 = array[i][1];
					double value = 1.0;
					if (array[i].length>2) {
					String valueString = array[i][2];
					Double valueDouble = new Double(valueString);
					value = valueDouble.doubleValue();
					}
					Object object1 = null;
					Object object2 = null;
					boolean found1 = false;
					boolean found2 = false;
					for (int j = 0; ((j<3)&&((found1==false)||(found2==false))); j++) {
						Iterator iter = null;
						switch (j) {
							case 0: iter = concepts.iterator(); break;
							case 1: iter = properties.iterator(); break;
							case 2: iter = instances.iterator(); break;
						} 
						while (iter.hasNext()) {
							OWLEntity next = (OWLEntity) iter.next();
							if (next.getURI().equals(uri1)) {
								object1 = next;
								found1 = true;
							}
							if (next.getURI().equals(uri2)) {
								object2 = next;
								found2 = true;
							}
						}
					}
					if (found1&&found2) {
						Pair pair = new Pair();
						pair.object1 = object1;
						pair.object2 = object2;
						pair.value = value;
						pairs.add(pair);
					}
				}
			} catch (Exception e) {
				UserInterface.print(e.getMessage());		
			}
			}
	}

	public void addExplicit(Object object1, Object object2, double value) {
		Pair pair = new Pair();
		pair.object1 = object1;
		pair.object2 = object2;
		pair.value = value;
		pairs.add(pair);
	}

	public void addExplicitToList(ResultList list, int rulestotal) {
		double[] addinfo = new double[rulestotal];
		for (int i = 0; i<rulestotal; i++) {
			addinfo[i]=-1.0;
		}
		Iterator iter = pairs.iterator();
		while (iter.hasNext()) {
			Pair pair = (Pair) iter.next();
			list.set(pair.object1, pair.object2, pair.value, addinfo);
			list.set(pair.object2, pair.object1, pair.value, addinfo);
		}
	}

	public void transform(ResultList list, double threshold) {
		Iterator iter = list.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			Object object2 = list.getObject(object1,0);
			if (object2!=null) {
				double value = list.getValue(object1,0);
				if ((object1.hashCode()<object2.hashCode())&&(value>=threshold)) {
					addExplicit(object1,object2,1.0);
				}
			}
		}
	}
	
	public boolean checkFor(Object object1, Object object2) {
		for (int i = 0; i<pairs.size(); i++) {
			Pair pair = (Pair) pairs.elementAt(i);
			if (pair.object1.equals(object1)&&(pair.object2.equals(object2))) {return true;}
		}
		return false;
	}
	
/*	public void print() {
		Iterator iter = pairs.iterator();
		while (iter.hasNext()) {
			Pair pair = (Pair) iter.next();
			Entity entity1 = (Entity) pair.object1;
			Entity entity2 = (Entity) pair.object2;
			try {
				System.out.println(entity1.getURI()+" "+entity2.getURI()+" "+pair.value);
			} catch (KAONException e) {
			UserInterface.print(e.getMessage());
			}
		}
	}*/
}
