package edu.unika.aifb.foam.nonOntologies;

import java.util.Vector;

import edu.unika.aifb.foam.agenda.Agenda;
import edu.unika.aifb.foam.agenda.AgendaElement;
import edu.unika.aifb.foam.agenda.CompleteInstanceAgenda;
import edu.unika.aifb.foam.combination.Combination;
import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.result.ResultList;
import edu.unika.aifb.foam.result.ResultListImpl;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.util.Semi;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class is the main program to align other structures besides
 * ontologies.
 * 
 * @author Marc Ehrig
 * @date 16/07/05
 */
public class AlignNonOnto extends Thread{

	public AlignNonOnto() {
		UserInterface.print("ONTOLOGY ALIGNMENT\n\n");
	}
	
	/**
	 * The structure containing the ontologies to be aligned.
	 */
	public Structure ontology;				
	/**
	 * A link to the various parameters.
	 */
	public Parameter p;
	/**
	 * Given alignments are entered through this object.
	 */
	public ExplicitRelation explicit;
	/**
	 * An internal representation to access the results.
	 */
	public ResultList resultListLatest;		
	/**
	 * Alignment results are returned in a vector, with each element
	 * containing entity 1, entity 2, relation, and confidence.
	 */
	public Vector alignments;
	/**
	 * Only the values above the cutoff are saved i.e. those which are 
	 * probably alignments.
	 */
	public Vector cutoff;
	/**
	 * The user can be given alignments to validate. They are presented
	 * through the questions object.
	 */
	public Vector questions;
	/**
	 * Ths status is important when running a thread. It allows
	 * to check the current status of the alignment. This makes
	 * it possible to retrieve the new results after each 
	 * iteration.
	 */
	public String status = "idle";
	/**
	 * The name of this thread.
	 */
	public String name = "";
	
	/**
	 * The alignment process can be started directly from command line. 
	 * @param args parameters file
	 */
	public static void main(String[] args) {	
		if (args.length<1) {
			System.out.println("\nUsage: align alignmentParameterFile");
			System.exit(1);
		}
		AlignNonOnto align = new AlignNonOnto();
		align.name = "Commandline";
		Parameter parameter = new Parameter(args[0]);
		align.p = parameter;
		MyOntology ontologies = new MyOntology(parameter.ontologies);
		if (ontologies.ok == false) {System.exit(1);}
		align.ontology = ontologies;
		align.explicit = new ExplicitRelation(parameter.explicitFile,align.ontology);
		align.align();
		Save.saveVector(align.cutoff,parameter.cutoffFile);
//		Save.saveCompleteEval(align.resultListLatest,"",parameter.resultFile);
		if (parameter.resultFile.equals("")==false) {
		Save.saveVector(align.alignments,parameter.resultFile);}
		if (parameter.questionFile.equals("")==false) {
		Save.saveVector(align.questions,parameter.questionFile);}
		if (parameter.manualmappingsFile.equals("")==false) {
			Evaluation evaluation = new Evaluation(parameter.manualmappingsFile);
			evaluation.doEvaluation(align.ontology,align.resultListLatest,parameter.cutoff);
			evaluation.printEvaluation();}
	}		
	
	/**
	 * This is the actual alignment process.
	 *
	 */
	public void align() {									//this starts the actual alignment process
		UserInterface.print("\nAlignment process ("+name+") started");
		status = "started";
		double timestart = System.currentTimeMillis();
		Combination combination = null;	
		Rules rules = null;
		ResultTable lastresult = new ResultTableImpl();
		ResultList resultList = new ResultListImpl(5);
		resultListLatest = resultList;	
		Semi semi = new Semi(p.maxError,p.numberQuestions);		

		for (int i=0 ; i<p.maxiterations; i++) {			//approach uses a defined number of iterations (6. Iteration)
			status = "round"+i;
			UserInterface.print("\n*");
			if (i==0) {
				rules = new PetriRule();
				rules.setPreviousResult(lastresult);		//the strategy for features/similarities is set (1. Feature Selection, 3. Similiarity Calculation, 4. Similarity Aggregation)
				combination = new PetriCombination();		
			}
			Agenda agenda = new CompleteInstanceAgenda();	//creation of agenda i.e. which entities to compare (2. Search Step Selection)
			agenda.create(ontology,p.internaltoo);			
			UserInterface.print(">");
																
			lastresult.copy(resultList,5,0.05);				//last results are input for next iteration
			resultListLatest = resultList;					//old results are saved for changepropagation agenda
			resultList = new ResultListImpl(5);	

			int counter = 0;			
			agenda.iterate();			
			while (agenda.hasNext()) {						//the actual comparison process of entities begins
				AgendaElement element = agenda.next();		//entity pair for comparison is loaded
				Object object1 = element.object1;
				Object object2 = element.object2;
				if ((object1.toString().contains("Client")&&(object2.toString().contains("Customer")))) {
//					System.out.println("Now!");
				}
				counter++;
				if (counter%1000==0) {
					UserInterface.print("|");
					if (counter%100000==0) {UserInterface.print("\n");}
				}		
				combination.reset();
				combination.setObjects(object1,object2);	//all feature/similarities are calculated and finally aggregated
				for (int j=0; j<rules.total(); j++) {
					combination.setValue(j,rules.process(object1,object2,j,ontology));	//	individual feature/similarities are processed
				}
				combination.process();						//	similarities are combined
				if (combination.result()>0.01) {
				resultList.set(object1,object2,combination.result(),combination.getAddInfo());	//	results are added to the similarity list
				resultList.set(object2,object1,combination.result(),combination.getAddInfo());	//	in both directions
				}
			}

			explicit.addExplicitToList(resultList,rules.total());	//explicit mappings are included, they overwrite the calculated values
			if ((p.semi)&&((i==(p.maxiterations/4))||(i==(p.maxiterations*2/4))||(i==(p.maxiterations*3/4)))) {  	//active human feedback is added
				semi.semi(ontology,resultList,explicit);					 
				explicit.addExplicitToList(resultList,rules.total());
				p.cutoff = semi.proposedCutoff;
			}
			if ((i==0)&&(p.maxiterations>1)) {
				explicit.transform(resultList,0.99);		//first results are added as explicit mappings
			}
			alignments = resultList.vectorResult();	
			cutoff = resultList.cutoffResult(p.cutoff);	
			questions = semi.questions(ontology,resultList,explicit);
//			UserInterface.print(Double.toString(resultList.completeSimilarity()));
		}
		
		if (p.removeDoubles) {								//if only one mapping partner per entity is allowed, the others are removed
			resultList.removeDoubles();
		}
		resultListLatest = resultList;
		alignments = resultList.vectorResult();				//results are stored for access
		cutoff = resultList.cutoffResult(p.cutoff);
		questions = semi.questions(ontology,resultList,explicit);	//doubtable results as well
		double timeend = System.currentTimeMillis();
		double time = timeend-timestart;
		UserInterface.print("\nAlignment ("+name+") finished");
		UserInterface.print("\n\nRequired time: "+time+"\n");
		status = "finished";
	}
	
	/**
	 * The alignment process can be run in a seperate thread. run() is the method to 
	 * start this thread.
	 */
	public void run() {		
		align();
	}		
	
}
