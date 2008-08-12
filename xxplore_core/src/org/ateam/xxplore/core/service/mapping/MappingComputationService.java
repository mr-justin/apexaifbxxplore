package org.ateam.xxplore.core.service.mapping;

import java.io.File;
import java.io.IOException;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.result.Save;

public class MappingComputationService {
	
	private static final String[] ONTOLOGYFILES = {"D:/BTC/opus_august2007.rdf","D:/BTC/viewAIFB_OWL.owl"};   //ontologies 	
	private static final String EXPLICITFILE = "";				
//	private static final int SCENARIO = Parameter.NOSCENARIO;
	private static final String CUTOFFFILE = "D:/BTC/sampling/mapping/results.txt";
	private static final int MAXITERATIONS = 10;		
	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
	private static final boolean EFFICIENTAGENDA = Parameter.COMPLETE;
	private static final int STRATEGY = Parameter.DECISIONTREE;
	private static final String CLASSIFIERFILE = "config/tree.obj";
	private static final String RULESFILE = "config/rules.obj";	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final double MAXERROR = 0.9; 
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
	private static final String RESULTFILE = "D:/BTC/sampling/mapping/details.txt";	//output
	private static final double CUTOFF = 0.9;  //0.25;0.31;0.35(0.7);0.9(0.95)
	private static final String QUESTIONFILE = "D:/BTC/sampling/mapping/doubt.txt";
	private static final String MANUALMAPPINGSFILE = "D:/BTC/sampling/mapping/MapCSV.txt";	
	
	
	/**
	 * This is just a general testing method.
	 * @param args
	 */
	public static void main(String[] args) {
		application();
	}
	
/*	PARAMETERS FILE FOR THE ONTOLOGY ALIGNMENT PROCESS
	ontology1 = C:/Work/ontology1.owl;		//file name of ontology 1 (mandatory)
	ontology2 = C:/Work/onrology2.owl;		//file name of ontology 2 (mandatory)
	explicitFile = C:/Work/preknown.txt;	//file name of pre-known alignments (highly recommended, if existing)
	scenario = NOSCENARIO;				//description of scenario (highly recommended) *NOSCENARIO*, QUERYREWRITING, ONTOLOGYMERGING, DATAINTEGRATION, REASONING, ONTOLOGYEVOLUTION
	cutoffFile = C:/Work/cutoff.txt;		//file name of cut-off alignments (mandatory)

	maxiterations = 10;				//number of iterations in alignment process 4, *10*, 15, 30		
	strategy = DECISIONTREE;			//used strategy: EQUALLABELS, ONLYLABELS, MANUALWEIGHTED, MANUALSIGMOID, MACHINE, *DECISIONTREE*
	internaltoo = EXTERNAL;				//alignments within one namespace as well? INTERNAL, *EXTERNAL*
	efficientAgenda = EFFICIENT;			//speed the process up, some quality loss: COMPLETE, *EFFICIENT*
	classifierFile = C:/Work/config/tree25a.obj;	//only needed for MACHINE or DECISIONTREE strategy; should contain weka learned classifier or decisiontree
	rulesFile = C:/Work/config/finalRules.obj;	//only needed for MACHINE or DECISIONTREE strategy; should contain weka generated/learned rules 
	semi = FULLAUTOMATIC;				//user interaction during alignment process or not: SEMIAUTOMATIC, *FULLAUTOMATIC*
	maxError = 0.95;					//parameter for semiautomation 0.25, 0.31, 0.7, *0.95*
	numberQuestions = 5;				//how questions are given to the user *5*, 20
	cutoffvalue = 0.95;				//where to set the cut-off 0.25, 0.31, 0.7, *0.95*
	removeDoubles = REMOVEDOUBLES;		//exactly one alignment for each entity allowed (bijectivity) *REMOVEDOUBLES*, ALLOWDOUBLES
	resultFile = C:/Work/result.txt;		//file name of found alignments 
	questionFile = C:/Work/question.txt 		//file name of doubtable alignments, which should be checked by user and re-entered into the system
	manualmappingFile = C:/Work/goldstandard.txt; 	//for an evaluation the goldstandard is necessary
*/
	/**
	 * The alignment process can also be started as a Java application. The
	 * specified parameters are given to the alignment process. After it
	 * has been run we can access the results.
	 */
	public static void application() {
		checkFiles();
		
		Align align = new Align();								//creating the new alignment method
		MyOntology ontologies = new MyOntology(ONTOLOGYFILES);	//assigning the ontologies
		if (ontologies.ok == false) {System.exit(1);}
		ExplicitRelation explicit = new ExplicitRelation(EXPLICITFILE,ontologies);	//assigning pre-known alignments
//		Parameter parameter = new Parameter(Parameter.NOSCENARIO,ONTOLOGYFILES); 
		Parameter parameter = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFILE,RULESFILE,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,ONTOLOGYFILES);	//assigning the parameters
		parameter.manualmappingsFile = MANUALMAPPINGSFILE;
		align.name = "Application";
		align.ontology = ontologies;	
		align.p = parameter;
		align.explicit = explicit;
		align.align();									//process
//		Save.saveVector(align.alignments, RESULTFILE); 	//align.alignments is a vector, with each element containing entity 1, entity 2, relation, and confidence
//		Save.saveCompleteEval(align.ontology,align.resultListLatest,MANUALMAPPINGSFILE,align.p.cutoff,RESULTFILE);	//save mappings
		Save.saveImportantEval(align.ontology,align.resultListLatest,MANUALMAPPINGSFILE,align.p.cutoff,RESULTFILE);	//save mappings
		Save.saveVector(align.cutoff, CUTOFFFILE);
		Save.saveVector(align.questions, QUESTIONFILE);	//align.questions  is an similar vector just presenting the doubtful mappings, these should ideally be checked by the user
		Evaluation evaluation = new Evaluation(MANUALMAPPINGSFILE);
		evaluation.doEvaluation(align.ontology,align.resultListLatest,align.p.cutoff);
		evaluation.printEvaluation();
	}
	
	public static void checkFiles() {
		File results = new File(CUTOFFFILE);
		if(!results.exists()){
			results.getParentFile().mkdirs();
			try {
				results.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File details = new File(RESULTFILE);
		if(!details.exists()){
			details.getParentFile().mkdirs();
			try {
				details.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File doubt = new File(QUESTIONFILE);
		if(!doubt.exists()){
			doubt.getParentFile().mkdirs();
			try {
				doubt.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 

}
