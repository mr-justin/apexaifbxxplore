package edu.unika.aifb.foam.input;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.Entity;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.owl.elements.AnnotationProperty;

import edu.unika.aifb.foam.util.UserInterface;

/**
 * This is the object we would like to compare. Ontologies
 * are directly loaded into it. Basically RDFS and OWL ontologies
 * are loaded into the KAON2-system ({@link http://kaon2.semanticweb.org}). 
 * 
 * @author Marc Ehrig
 */
public class MyOntology implements Structure{

	private static final String[] ONTOLOGYFILES = {"D:/zl/eclipse/workspace_google/FOAM/ontologies/animalsA.owl","D:/zl/eclipse/workspace_google/FOAM/ontologies/animalsB.owl"};
//	private static final String[] ONTOLOGYFILES = {"C:/Work/Petri/PNOntology.owl","C:/Work/Petri/travelBooking.owl","C:/Work/Petri/reservation.owl"};
//	private static final String[] ONTOLOGYFILES = {"C:/Work/BT/LibraryData/protons.rdf","C:/Work/BT/LibraryData/protont.rdf"};		
//	private static final String[] ONTOLOGYFILES = {"C:/Work/BT/LibraryData/protons.rdf","C:/Work/BT/LibraryData/protont.rdf","C:/Work/BT/LibraryData/ABI_topics/topics.owl"}; 	//ontologies		
//	private static final String[] ONTOLOGYFILES = {"C:/Work/DissData/first03/russia1.owl","C:/Work/DissData/first03/russia2.owl","C:/Work/DissData/first03/russiaA.owl","C:/Work/DissData/first03/russiaB.owl","C:/Work/DissData/first03/russiaC.owl","C:/Work/DissData/first03/russiaD.owl","C:/Work/DissData/first03/tourismA.owl","C:/Work/DissData/first03/tourismB.owl"}; 	//ontologies	
//	private static final String[] ONTOLOGYFILES = {"C:/Work/DissData/second04/sportSoccer.owl"}; 	//ontologies	
//	private static final String[] ONTOLOGYFILES = {"C:/Work/DissData/i3con04/animalsA.owl","C:/Work/DissData/i3con04/animalsB.owl","C:/Work/DissData/i3con04/csA.owl","C:/Work/DissData/i3con04/csB.owl","C:/Work/DissData/i3con04/hotelA.owl","C:/Work/DissData/i3con04/hotelB.owl","C:/Work/DissData/i3con04/networkA.owl","C:/Work/DissData/i3con04/networkB.owl","C:/Work/DissData/i3con04/people+petsA.owl","C:/Work/DissData/i3con04/people+petsB.owl","C:/Work/DissData/i3con04/people+pets-noninstanceA.owl","C:/Work/DissData/i3con04/people+pets-noninstanceB.owl"}; 	//ontologies	
//	private static final String[] ONTOLOGYFILES = {"C:/Work/DissData/second04/swap.owl"};
/*	private static final String[] OAEINUMBERS = {"101","102","103","104","201","202","203","204","205",
			"206","207","208","209","210","221","222","223","224",
			"225","228","230","231","232","233","236","237","238",
			"239","240","241","246","247","248","249","250","251",
			"252","253","254","257","258","259","260","261","262",
			"265","266","301","302","303","304"};*/
/*	private static final String[] OAEINUMBERS = {"101","103","104","201","202","203","204","205",
			"206","207","208","209","210","221","222","223","224",
			"225","228","230","231","232","236","237","238",
			"239","240","241","246","247","248","249","250","251",
			"252","253","254","257","258","259","260","261","262",
			"265","266","301","302","303","304"};*/	
	private String type;	
	public Ontology ontology;
	public AnnotationProperty owlxlabel;
	public AnnotationProperty owlxcomment;
	public boolean ok = true;

	public MyOntology() {
	}
	
	public MyOntology(String[] fileNames) {
		String[] physicalURIs = new String[fileNames.length];
		for (int i = 0; i< fileNames.length; i++) {
			if (fileNames[i].startsWith("http://")==true) {
				physicalURIs[i] = fileNames[i];
			} else {
				physicalURIs[i] = "file:"+fileNames[i];
			}
		}
		try {
//			Namespaces namespaces = new Namespaces();
//			namespaces.registerStandardPrefixes();
	        String[] logicalURIs=new String[physicalURIs.length];
			DefaultOntologyResolver resolver = new DefaultOntologyResolver();
			for (int i = 0; i<physicalURIs.length; i++) {
	            logicalURIs[i]=resolver.registerOntology(physicalURIs[i]);
//	            logicalURIs[i]= KAON2Manager.getOntologyURI(physicalURIs[i],null);
//				resolver.registerReplacement(logicalURIs[i],physicalURIs[i]);
			}
			Ontology ontologies[] = new Ontology[physicalURIs.length];
			OntologyManager ontologyManager = KAON2Manager.newOntologyManager();
			ontologyManager.setOntologyResolver(resolver);
			Map parameters = Collections.emptyMap();
			for (int i = 0; i<physicalURIs.length; i++) {
				ontologies[i] = ontologyManager.openOntology(logicalURIs[i], parameters);
			}
			ontology = ontologies[0];
			for (int i = 1; i<physicalURIs.length; i++) {
				ontology.addToImports(ontologies[i]);
			}
			type = "KAON2 Ontology";
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
			ok = false;
		}
		owlxlabel = KAON2Manager.factory().annotationProperty("http://www.w3.org/2000/01/rdf-schema#label");
		owlxcomment = KAON2Manager.factory().annotationProperty("http://www.w3.org/2000/01/rdf-schema#comment");
	}		
	
	public MyOntology(InputStream[] ontologyStreams) {
	}
	
	public String type() {
		return type;
	}
	
	public static void main (String args[]) {
		try{
		MyOntology myontology = new MyOntology(ONTOLOGYFILES);
		Iterator iter = myontology.ontology.createEntityRequest().getAll().iterator();
		int counter = 0;
		while (iter.hasNext()) {
			Entity entity = (Entity) iter.next();
			System.out.println(entity.getURI());
			counter++;
		}
		System.out.println(counter);
		System.out.println("end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
