package edu.unika.aifb.foam.main;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class is the main interface to use the Align algorithm.
 * The algorithms are based on the work of Ehrig et al. as presented at
 * ESWS04, ISWC04, WWW05, and ISWC05. 
 * The proper use is explained with each accessing method.
 *  
 * @author Marc Ehrig
 * @version 3.0
 * @date 22/07/05
 * 
 */
public class Main {

	/**
	 * This is just a general testing method.
	 * @param args
	 */
	public static void main(String[] args) {
//		commandLine(args);
		application();
//		thread();
	}
	
	/**
	 * The alignment process can be started through the command line. All
	 * input ontologies, parameters, and the corresponding file to store
	 * the output have to be specified in the parameters file. 
	 * @param args name of parameters file
	 */
	public static void commandLine(String[] args) {
		args = new String[1];
		args[0] = "C:/Documents/Implementation/rules2/alignmentParameters.txt";
		Align.main(args);				//calling the main method
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
	manualmappingFile = C:/Work/goldstandard.txt; 	//for an evaluation the goldstandard is necessary*/
	/**
	 * The alignment process can also be started as a Java application. The
	 * specified parameters are given to the alignment process. After it
	 * has been run we can access the results.
	 */
	public static void application() {
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

	private static final String[] ONTOLOGYFILES = {"C:/Work/ontology1.owl","C:/Work/ontology2.owl"};   //ontologies 	
//	private static final String[] ONTOLOGYFILES = {"C:/FOAM/ontologies/animalsA.owl","C:/FOAM/ontologies/animalsB.owl"};   //ontologies 	
//	private static final String[] ONTOLOGYFILES = {"C:/FOAM/ontologies/people+petsA.owl","C:/FOAM/ontologies/people+petsB.owl"};   //ontologies 	
//	private static final String[] ONTOLOGYFILES = {"C:/FOAM/ontologies/russia1.owl","C:/FOAM/ontologies/russia2.owl"}; 	//ontologies
//	private static final String[] ONTOLOGYFILES = {"C:/FOAM/ontologies/onto101.owl","C:/FOAM/ontologies/onto303.owl"}; 	//ontologies	
	private static final String EXPLICITFILE = "";				
//	private static final int SCENARIO = Parameter.NOSCENARIO;
	private static final String CUTOFFFILE = "C:/FOAM/results.txt";
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
	private static final String RESULTFILE = "C:/FOAM/details.txt";	//output
	private static final double CUTOFF = 0.5;  //0.25;0.31;0.35(0.7);0.9(0.95)
	private static final String QUESTIONFILE = "C:/FOAM/doubt.txt";
//	private static final String MANUALMAPPINGSFILE = "C:/FOAM/ontologies/animalsABMapCSV.txt";		
	private static final String MANUALMAPPINGSFILE = "C:/FOAM/ontologies/people+petsABMapCSV.txt";		
//	private static final String MANUALMAPPINGSFILE = "C:/FOAM/ontologies/russia12Map.txt";
//	private static final String MANUALMAPPINGSFILE = "C:/FOAM/ontologies/onto303MapCSV.txt";	
	
	/**
	 * The alignment process can be started in a thread. Other applications 
	 * can continue running. Intermediate results can be already presented
	 * and used.
	 */
	public static void thread() {
		Align align = new Align();		//creating the new alignment thread
		Main mainClass = new Main();
		TestThread check = mainClass.new TestThread(align);	//another thread is created to check the alignment thread
		check.start();					//	and started	
		align.ontology = new MyOntology(ONTOLOGYFILES);	//assigning the ontologies
		align.name = "Thread1";
		align.p = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFILE,RULESFILE,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,ONTOLOGYFILES);
		align.explicit = new ExplicitRelation(EXPLICITFILE,align.ontology);	//assigning pre-known alignments
		align.start();					//start the alignment thread	
	}
	
	/**
	 * Testthread to check the running alignment thread.
	 * @author Marc Ehrig
	 */
	public class TestThread extends Thread {
		private Align alignTh;
		public TestThread(Align alignT) {
			alignTh = alignT;
		}
		public void run() {
			while (alignTh.status!="finished") {
				System.out.println();						//prints the current status
				System.out.println(alignTh.status);			//the status changes from idle, started, round1, round2,... to finished
				System.out.println();
				try {
					sleep(3000);							//continues to sleep
				} catch (InterruptedException e) {
					UserInterface.print(e.getMessage());
				}			
			}
			System.out.println(alignTh.status);
			Save.saveCompleteEval(alignTh.ontology,alignTh.resultListLatest,MANUALMAPPINGSFILE,alignTh.p.cutoff,RESULTFILE);	//finally the results are saved
		}
	}
	
}
