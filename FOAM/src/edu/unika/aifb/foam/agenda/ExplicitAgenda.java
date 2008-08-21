/*
 * Created on 31.10.2004
 *
 */
package edu.unika.aifb.foam.agenda;

//import java.io.FileInputStream;
//import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

//import com.Ostermiller.util.CSVParser;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.util.CSVParse;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * Loads a list of entities, e.g. from a former alignment process,
 * another system, or user defined. These are then inserted for
 * comparison.
 * 
 * @author Marc Ehrig
 */
public class ExplicitAgenda extends AgendaImpl {

	public void load(String fileName, Structure structure) {
		list = new HashSet();
		if (fileName!="") {
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
					AgendaElement element = new AgendaElement();
					element.object1 = object1;
					element.object2 = object2;
					element.action = "comp";
					list.add(element);
				}
			}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());		
		}
		}
	}

	public void create(Structure structure, boolean internaltoo) {
	}

}
