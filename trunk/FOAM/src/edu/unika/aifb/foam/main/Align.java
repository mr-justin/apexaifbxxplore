package edu.unika.aifb.foam.main;

import java.util.Vector;

import edu.unika.aifb.foam.agenda.Agenda;
import edu.unika.aifb.foam.agenda.AgendaElement;
import edu.unika.aifb.foam.agenda.AgendaImpl;
import edu.unika.aifb.foam.agenda.AllMatchesAgenda;
import edu.unika.aifb.foam.agenda.ChangePropagationAgenda;
import edu.unika.aifb.foam.agenda.ClosestLabelAgenda;
import edu.unika.aifb.foam.agenda.CompleteAgenda;
import edu.unika.aifb.foam.agenda.EmptyAgenda;
import edu.unika.aifb.foam.agenda.RandomAgenda;
import edu.unika.aifb.foam.combination.Averaging;
import edu.unika.aifb.foam.combination.BestValue;
import edu.unika.aifb.foam.combination.Combination;
import edu.unika.aifb.foam.combination.DecisionTree;
import edu.unika.aifb.foam.combination.ManualWeightsLinear;
import edu.unika.aifb.foam.combination.ManualWeightsSigmoid;
import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.machine.ObjectIO;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.result.ResultList;
import edu.unika.aifb.foam.result.ResultListImpl;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.result.ResultTableImpl2;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.rules.EqualLabelRule;
import edu.unika.aifb.foam.rules.LabelRule;
import edu.unika.aifb.foam.rules.ManualRuleSimple;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.util.Semi;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class is the main program to align ontologies.
 * For the proper use please refer to the class Main.
 * The algorithms are based on the work of Ehrig et al. as presented at
 * ESWS04, ISWC04, WWW05, and ISWC05. The main alignment method is commented along the 
 * source code. 
 * The system is currently parameterizable in many ways. For most 
 * applications only very simple parameters are actually needed. 
 * The package requires KAON2 and for some special parameter 
 * cases jwnl.jar, googleapi.jar, and weka.jar.
 * This work is ongoing research and will be constantly enhanced.
 * 
 * @author Marc Ehrig
 * @version 3.1
 * @date 15/12/05
 */
public class Align extends Thread{

	public Align() {
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
		Align align = new Align();
		align.name = "Commandline";
		Parameter parameter = new Parameter(args[0]);
		align.p = parameter;
		MyOntology ontologies = new MyOntology(parameter.ontologies);
		if (ontologies.ok == false) {System.exit(1);}
		align.ontology = ontologies;
		align.explicit = new ExplicitRelation(parameter.explicitFile,align.ontology);
		align.align();
		Save.saveVector(align.cutoff,parameter.cutoffFile);
		if (parameter.resultFile.equals("")==false) {
//		Save.saveVector(align.alignments,parameter.resultFile);
//		Save.saveCompleteEval(align.resultListLatest,"",parameter.resultFile);
		Save.saveImportantEval(align.ontology,align.resultListLatest,parameter.manualmappingsFile,align.p.cutoff,parameter.resultFile);	}
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
//		ResultTable lastresult = new ResultTableImpl();
		ResultTable lastresult = new ResultTableImpl2();
		ResultList resultList = new ResultListImpl(5);
		resultListLatest = resultList;	
		Semi semi = new Semi(p.maxError,p.numberQuestions);		

		for (int i=0 ; i<p.maxiterations; i++) {			//approach uses a defined number of iterations (6. Iteration)
			status = "round"+i;
			UserInterface.print("\n*");

			if (i==0) {										//the first round is different as it provides the basis						
				rules = new LabelRule();					
				combination = new Averaging(rules.total(),rules.total(),rules.total());	
				if (p.strategy==Parameter.EQUALLABELS) {
					rules = new EqualLabelRule();					
					combination = new Averaging(rules.total(),rules.total(),rules.total());				
				} else {
					rules = new LabelRule();					
					combination = new BestValue(rules.total(),rules.total());				
				}
			}			
			if (i==1) {
				switch (p.strategy) {						//the strategy for features/similarities is set (1. Feature Selection, 3. Similiarity Calculation, 4. Similarity Aggregation)
				case Parameter.EQUALLABELS:  						//only labels are compared (equality)
					rules = new EqualLabelRule();					
					combination = new Averaging(rules.total(),rules.total(),rules.total());				
					break;					
				case Parameter.ONLYLABELS:  						//only labels are compared (similarity)
					rules = new LabelRule();					
					combination = new Averaging(rules.total(),rules.total(),rules.total());				
					break;					
				case Parameter.MANUALWEIGHTED: 						//the manual strategy features specific rules and a combination with weights thereof
					rules = new ManualRuleSimple();								
					combination = new ManualWeightsLinear(rules.total());		
					break;
				case Parameter.MANUALSIGMOID: 						//the manual strategy features specific rules and a combination with sigmoid weights thereof
					rules = new ManualRuleSimple();								
					combination = new ManualWeightsSigmoid(rules.total());		
					break;
				case Parameter.MACHINE:								//learnt rules and classifier are loaded
					rules = (Rules) ObjectIO.load(p.rulesFile);		
					combination = (Combination) ObjectIO.load(p.classifierFile);
					break;
				case Parameter.DECISIONTREE:
					rules = (Rules) ObjectIO.load(p.rulesFile);		//learnt rules and decision tree are loaded
					combination = new DecisionTree(p.classifierFile,rules,ontology);		
					break;
				}
				rules.setPreviousResult(lastresult);
			}
			
			Agenda agenda = new EmptyAgenda();				//creation of agenda i.e. which entities to compare (2. Search Step Selection)
			if (p.efficientAgenda==Parameter.COMPLETE) {
				agenda = new AgendaImpl();
				Agenda agenda1 = new CompleteAgenda();		//	no efficiency means complete comparison, all entities with all entities
				agenda1.create(ontology,p.internaltoo);		//	most times it doesn't make sense to look for mappings within one namespace
				agenda.add(agenda1);
			} else {										// if operations are supposed to efficient the agenda is optimized				
			if (i == 0) {										//	first round compares only similar labelled entities
				agenda = new AgendaImpl();
				Agenda agenda1 = new ClosestLabelAgenda();			
				agenda1.create(ontology,p.internaltoo);
				agenda.add(agenda1);
			}
			if ((i > 0) && (i <= 5)) {							//	entities whose neighbors were updated are recalculated
				agenda = new AgendaImpl();
				Agenda agenda1 = new AllMatchesAgenda();
				agenda1.parameter(resultList);
				agenda1.create(ontology,p.internaltoo);
				Agenda agenda2 = new ChangePropagationAgenda();		
				ResultList[] parameter1 = {resultListLatest,resultList};
				agenda2.parameter(parameter1);
				agenda2.create(ontology,p.internaltoo);
				agenda.add(agenda1);
				agenda.add(agenda2);	
			}
			if (i > 5) {										//	a random component is added (5% of all pairs)
				agenda = new AgendaImpl();				
				Agenda agenda1 = new AllMatchesAgenda();
				agenda1.parameter(resultList);
				agenda1.create(ontology,p.internaltoo);
				Agenda agenda2 = new ChangePropagationAgenda();		
				ResultList[] parameter1 = {resultListLatest,resultList};
				agenda2.parameter(parameter1);
				agenda2.create(ontology,p.internaltoo);
				Agenda agenda3 = new RandomAgenda(); 		
				agenda3.parameter(new Double(0.05));
				agenda3.create(ontology,p.internaltoo);
				agenda.add(agenda1);
				agenda.add(agenda2);
				agenda.add(agenda3);
			}
			if ((i == (p.maxiterations - 1))&&(i!=0)) {			//	matches are recalculated for clean-up
				agenda = new AgendaImpl();							
				Agenda agenda1 = new AllMatchesAgenda();
				agenda1.parameter(resultList);
				agenda1.create(ontology,p.internaltoo);
				agenda.add(agenda1);
			}
			}
			UserInterface.print(">");

			if (i == 2) {lastresult = new ResultTableImpl();}				//last results are input for next iteration
			lastresult.copy(resultList,5,0.01);				
			if (i == 2) {rules.setPreviousResult(lastresult);}			//if a new resultList is used this has to be propagated to the rules
			resultListLatest = resultList;					//old results are saved for changepropagation agenda
			resultList = new ResultListImpl(5);	

			int counter = 0;			
			agenda.iterate();			
			while (agenda.hasNext()) {						//the actual comparison process of entities begins
				AgendaElement element = agenda.next();		//entity pair for comparison is loaded
				Object object1 = element.object1;
				Object object2 = element.object2;
				counter++;
				if (counter%1000==0) {
					UserInterface.print("|");
					if (counter%100000==0) {UserInterface.print("\n");}
				}		
				combination.reset();
				combination.setObjects(object1,object2);	//all feature/similarities are calculated and finally aggregated
				if ((p.strategy!=Parameter.DECISIONTREE)||(i==0)) {	//the decision tree strategy only calculates those on the path
					for (int j=0; j<rules.total(); j++) {
						combination.setValue(j,rules.process(object1,object2,j,ontology));	//	individual feature/similarities are processed
					}
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
