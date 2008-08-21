package edu.unika.aifb.foam.main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import com.ontoprise.ontostudio.ontomap.simplemapping.ISimpleMapping;
import com.ontoprise.ontostudio.ontomap.simplemapping.ISimpleMappingImpl;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This is the standard class to discover mappings within the SEKT
 * Mediation workpackage (WP4). It will be part of the general 
 * mediation framework. 
 * 
 * @author Marc Ehrig
 * 
 */
public class MappingDiscoverySEKTImpl implements MappingDiscoverySEKT{
	
	private static String classifierFile = "C:/Documents/Implementation/rules2/config/tree25a.obj";
	private static String rulesFile = "C:/Documents/Implementation/rules2/config/finalRules.obj";	
	
	/**
	 * The simple constructor.
	 *
	 */
	public MappingDiscoverySEKTImpl() {
		UserInterface.useOutput = false;						//turn-off the output
	}
	
	/**
	 * Initializes the files required by the alignment algorithm. 
	 * @param classifier the file containing the classification information
	 * @param rules the file containing the similarity rules which have to be processed
	 */
	public void initialize(String classifier, String rules) {
		classifierFile = classifier;
		rulesFile = rules;
	}
	
	/**
	 * Determines the mappings based on an alignment algorithm.
	 * @param ontology1 the (temp) file location of the source ontology
	 * @param ontology2 the (temp) file location of the target ontology
	 * @param mappingObjectsInput a set of already known mappings (ISimpleMapping) 
	 * @return the set of the result mappings (ISimpleMapping), including the confidence value 
	 */
	public Set results(String ontology1, String ontology2, Set mappingObjectsInput) {
		Align align = new Align();								//initializing the alignment method
		Set result = new HashSet();			
		String ontologyFiles[] = new String[2];					//assigning the ontologies
		ontologyFiles[0] = ontology1;
		String sourceOntology = "";
		ontologyFiles[1] = ontology2;
		String targetOntology = "";
		MyOntology ontologies = new MyOntology(ontologyFiles);	
		if (ontologies.ok == true) {
		ExplicitRelation explicit = new ExplicitRelation();		//assigning pre-known alignments
		try{
		Set concepts = ontologies.ontology.createEntityRequest(OWLClass.class).getAll();
		Set properties = ontologies.ontology.createEntityRequest(ObjectProperty.class).getAll();
		Set properties2 = ontologies.ontology.createEntityRequest(DataProperty.class).getAll();
		properties.addAll(properties2);
		Set instances = ontologies.ontology.createEntityRequest(Individual.class).getAll();			
		Iterator iter = mappingObjectsInput.iterator();
		while (iter.hasNext()) {
			ISimpleMapping mappingObject = (ISimpleMapping) iter.next();
			if (sourceOntology.equals("")) {						//assign source and target ontology
				sourceOntology = mappingObject.getSourceOntology();
				targetOntology = mappingObject.getTargetOntology();
			}
			String uri1 = mappingObject.getSource();
			String uri2 = mappingObject.getTarget();
			double value = mappingObject.getProbability();
			Object object1 = null; Object object2 = null;			//determine the objects corresponding to the URIs
			boolean found1 = false; boolean found2 = false;
			for (int j = 0; ((j<3)&&((found1==false)||(found2==false))); j++) {
				Iterator iterIn = null;
				switch (j) {
					case 0: iterIn = concepts.iterator(); break;
					case 1: iterIn = properties.iterator(); break;
					case 2: iterIn = instances.iterator(); break;
				} 
				while (iterIn.hasNext()) {
					OWLEntity next = (OWLEntity) iterIn.next();
					if (next.getURI().equals(uri1)) {object1 = next; found1 = true;}
					if (next.getURI().equals(uri2)) {object2 = next; found2 = true;}
				}
				if (found1&&found2) {explicit.addExplicit(object1,object2,value);}
			}
		} 
		} catch (Exception e) {
			UserInterface.errorPrint(e.getMessage());
		}
		Parameter parameter = new Parameter(Parameter.NOSCENARIO,ontologyFiles);	//assigning the parameters
		parameter.classifierFile = classifierFile;
		parameter.rulesFile = rulesFile;
		parameter.semi = Parameter.FULLAUTOMATIC;
//		Parameter parameter = new Parameter(30,Parameter.DECISIONTREE,INTERNALTOO,Parameter.COMPLETE,CLASSIFIERFILE,RULESFILE,Parameter.FULLAUTOMATIC,0.95,NUMBERQUESTIONS,REMOVEDOUBLES,0.95,ONTOLOGYFILES);			
		align.name = "MappingDiscoveryApplication";
		align.ontology = ontologies;	
		align.p = parameter;
		align.explicit = explicit;
		align.align();											//process
		Iterator iter = align.alignments.iterator();			//save results
		while (iter.hasNext()) {
			String[] objects = (String[]) iter.next();
			ISimpleMapping mappingObject = new ISimpleMappingImpl();
			if (objects[0].contains(sourceOntology) && objects[1].contains(targetOntology)) {	//only the ones which correspond to the correct source and target ontology
				mappingObject.setSource(objects[0]);
				mappingObject.setSourceOntology(sourceOntology);
				mappingObject.setTarget(objects[1]);
				mappingObject.setTargetOntology(targetOntology);
				mappingObject.setProbability(Double.parseDouble(objects[2]));
				System.out.println(objects[0]+" "+objects[1]+" "+objects[2]);
				result.add(mappingObject);
			}
		}
		}
		return result;
	}
	
	/**
	 * Accesses the logged messages during processing. 
	 * @return normal and error messages created during the process
	 */
	public String messages() {
		String message = UserInterface.outputBuffer+"\n"+UserInterface.errorBuffer;
		UserInterface.clear();
		return message;
	}
	
/*	public static void main (String args[]) {
		MappingDiscoverySEKTImpl mapping = new MappingDiscoverySEKTImpl();
		mapping.initialize("C:/Discovery/tree30.obj","C:/Discovery/finalRules.obj");
		Set results = mapping.results("C:/FOAM/onto/module1tmpfile.owl","C:/FOAM/onto/module2tmpfile.owl",new HashSet());
		Iterator iter = results.iterator();
		while (iter.hasNext()) {
			ISimpleMapping mappingObj = (ISimpleMapping) iter.next();
//			System.out.println(mappingObj.getSource()+" "+mappingObj.getTarget()+" "+mappingObj.getProbability());
		}
		System.out.println();
	}*/
	
}
