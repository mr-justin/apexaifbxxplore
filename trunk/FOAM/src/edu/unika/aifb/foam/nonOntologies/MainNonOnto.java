package edu.unika.aifb.foam.nonOntologies;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class is the main interface to use the Align algorithm if
 * used with other structures than ontologies. In specific,
 * we have tested the alignment of Petri Nets.
 *  
 * @author Marc Ehrig
 * @version 2.0
 * @date 20/02/06
 * 
 */
public class MainNonOnto {

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
		AlignNonOnto.main(args);				//calling the main method
	}
	/**
	 * The alignment process can also be started as a Java application. The
	 * specified parameters are given to the alignment process. After it
	 * has been run we can access the results.
	 */
	public static void application() {
		AlignNonOnto align = new AlignNonOnto();						//creating the new alignment method
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
		Save.saveCompleteEval(align.ontology,align.resultListLatest,MANUALMAPPINGSFILE,align.p.cutoff,RESULTFILE);	//save mappings
//		Save.saveVector(align.alignments, RESULTFILE); 	//align.alignments is a vector, with each element containing entity 1, entity 2, relation, and confidence
		Save.saveVector(align.cutoff, CUTOFFFILE);
		Save.saveVector(align.questions, QUESTIONFILE);	//align.questions  is an similar vector just presenting the doubtful mappings, these should ideally be checked by the user
		Evaluation evaluation = new Evaluation(MANUALMAPPINGSFILE);
		evaluation.doEvaluation(align.ontology,align.resultListLatest,align.p.cutoff);
		evaluation.printEvaluation();
	}

//	private static final String[] ONTOLOGYFILES = {"C:/Work/Petri/PNOntologie.owl","C:/Work/Petri/apparat.owl","C:/Work/Petri/machine.owl"}; 	//ontologies	
//	private static final String[] ONTOLOGYFILES = {"C:/Work/Petri/PNOntologie.owl","C:/Work/Petri/order.owl","C:/Work/Petri/delivery.owl"}; 	//ontologies	
	public static String[] ONTOLOGYFILES = {"C:/Work/Petri/PNOntology.owl","C:/Work/Petri/reservation.owl","C:/Work/Petri/travelBooking.owl"}; 	//ontologies	
	private static final String EXPLICITFILE = "";				
	private static final int SCENARIO = Parameter.NOSCENARIO;
	private static final String CUTOFFFILE = "C:/Work/testcut.txt";
	private static final int MAXITERATIONS = 3;		
	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
	private static final boolean EFFICIENTAGENDA = Parameter.EFFICIENT;
	private static final int STRATEGY = 0;
	private static final String CLASSIFIERFILE = "";
	private static final String RULESFILE = "";	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final double MAXERROR = 0.1; 
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
	//private static final boolean REMOVEDOUBLES = Parameter.ALLOWDOUBLES;
	private static final String RESULTFILE = "C:/Work/testRes.txt";	//output
	private static final double CUTOFF = 0.1;  
	private static final String QUESTIONFILE = "C:/Work/testQues.txt";
	private static final String MANUALMAPPINGSFILE = "";		
	
	/**
	 * The alignment process can be started in a thread. Other applications 
	 * can continue running. Intermediate results can be already presented
	 * and used.
	 */
	public static void thread() {
		AlignNonOnto align = new AlignNonOnto();		//creating the new alignment thread
		MainNonOnto mainClass = new MainNonOnto();
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
		private AlignNonOnto alignTh;
		public TestThread(AlignNonOnto alignT) {
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
