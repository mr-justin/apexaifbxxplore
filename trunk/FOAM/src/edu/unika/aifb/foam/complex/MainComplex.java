package edu.unika.aifb.foam.complex;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Save;

/**
 * This class is the main interface for a complex ontology alignment.
 *  
 * @author Marc Ehrig
 * @version 1.0
 * @date 02/07/05
 * 
 */
public class MainComplex {

	/**
	 * This is just a general testing method.
	 * @param args
	 */
	public static void main(String[] args) {
		MyOntology ontologies = new MyOntology(ONTOLOGYFILES);	//load ontologies
		if (ontologies.ok == false) {System.exit(1);}
		
		Align align = new Align();								//calculate similarity between entities
		ExplicitRelation explicit = new ExplicitRelation("",ontologies);	
		Parameter parameter = new Parameter(1,Parameter.ONLYLABELS,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.FULLAUTOMATIC,0.7,5,Parameter.REMOVEDOUBLES,0.7,ONTOLOGYFILES);	
		align.name = "Identity Alignment based on Labels";
		align.ontology = ontologies;	
		align.p = parameter;
		align.explicit = explicit;
		align.align();					
		
		ComplexAlign complex = new ComplexAlign();				//calculate complex alignments
		complex.name = "Complex Alignment (Pengyun)";
		complex.ontology = ontologies;
		complex.resultListLatestSimilar = align.resultListLatest;
		complex.align();
		
//		Save.saveVector(complex.alignments, RESULTFILE); 		//align.alignments is a vector, with each element containing entity 1, entity 2, relation, and confidence
		Save.saveCompleteEval(ontologies,complex.resultListLatestSub,"",0.1,RESULTFILE);
	}
	private static final String[] ONTOLOGYFILES = {"C:/Work/DissData/i3con04/animalsA.owl","C:/Work/DissData/i3con04/animalsB.owl",}; 	//ontologies
	private static final String RESULTFILE = "C:/Work/Plus/Complex/resultcheck.txt";	//output
		
}
