/* * Created on 24.06.2004
 *
 */
package edu.unika.aifb.foam.machine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.Classifier;
//import weka.classifiers.functions.MultilayerPerceptron;
//import weka.classifiers.functions.SMO;
//import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

import edu.unika.aifb.foam.agenda.Agenda;
import edu.unika.aifb.foam.agenda.AgendaElement;
import edu.unika.aifb.foam.agenda.AgendaImpl;
import edu.unika.aifb.foam.agenda.CompleteAgenda;
import edu.unika.aifb.foam.agenda.EmptyAgenda;
import edu.unika.aifb.foam.combination.Averaging;
import edu.unika.aifb.foam.combination.Combination;
import edu.unika.aifb.foam.combination.DecisionTree;
import edu.unika.aifb.foam.combination.MachineLearn;
import edu.unika.aifb.foam.combination.ManualWeightsLinear;
import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.ResultList;
import edu.unika.aifb.foam.result.ResultListImpl;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.rules.EmptyRule;
import edu.unika.aifb.foam.rules.ManualRuleSimple;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.util.CSVParse;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class represents the whole APFEL process. Rules are generated
 * from the predefined rules and rules extracted from an inserted set 
 * of features. Further training examples are determined. After the user
 * has validated these, they are augmented with the results of the 
 * previously created rules. Finally a combiner is learned from the rules 
 * and the training examples. The final rules and the combiner are saved
 * and can be loaded in the normal alignment tool. The work is presented 
 * at WM 05 and a poster at WWW 05.
 * 
 * @author Marc Ehrig
 */
public class Train {

	/**
	 * Start the actual training step
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i<2; i++) {
			round = i;
			postvalidationExamples();
			train();
		}
	}
/*	public static void main(String[] args) {
		round = 0;
		postvalidationExamples();
		train();
	}*/
	
	private static int round = 0;
	
	private static final String[] ONTOLOGYFILES = {
			"C:/Work/DissData/first03/russia1.owl","C:/Work/DissData/first03/russia2.owl","C:/Work/DissData/first03/russiaA.owl","C:/Work/DissData/first03/russiaB.owl","C:/Work/DissData/first03/russiaC.owl","C:/Work/DissData/first03/russiaD.owl","C:/Work/DissData/first03/tourismA.owl","C:/Work/DissData/first03/tourismB.owl",
			"C:/Work/DissData/second04/sportEvent.owl","C:/Work/DissData/second04/sportSoccer.owl","C:/Work/DissData/i3con04/animalsA.owl","C:/Work/DissData/i3con04/animalsB.owl","C:/Work/DissData/i3con04/hotelA.owl","C:/Work/DissData/i3con04/hotelB.owl","C:/Work/DissData/i3con04/csA.owl","C:/Work/DissData/i3con04/csB.owl","C:/Work/DissData/i3con04/networkA.owl","C:/Work/DissData/i3con04/networkB.owl","C:/Work/DissData/i3con04/people+petsA.owl","C:/Work/DissData/i3con04/people+petsB.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto301.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto302.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto303.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto304.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto202.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto206.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto248.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto250.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto251.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto252.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto201.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto204.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto249.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto205.owl"};
/*			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto103.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto104.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto201.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto202.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto203.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto204.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto205.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto206.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto207.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto208.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto209.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto210.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto221.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto222.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto223.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto224.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto225.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto228.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto230.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto231.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto232.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto233.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto236.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto237.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto238.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto239.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto240.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto241.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto246.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto247.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto248.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto249.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto250.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto251.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto252.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto253.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto254.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto257.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto258.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto259.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto260.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto261.owl",
			"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto262.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto265.owl","C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto266.owl"};*/								
	private static final String[] VALIDATEDRESULTS = {
			"C:/Work/DissData/first03/russia12Map.txt","C:/Work/DissData/first03/russiaABMap.txt","C:/Work/DissData/first03/russiaCDMap.txt","C:/Work/DissData/first03/tourismABMap.txt",
			"C:/Work/DissData/second04/sportSoccerEventMap.txt","C:/Work/DissData/i3con04/animalsABMapCSV.txt","C:/Work/DissData/i3con04/hotelABMapCSV.txt","C:/Work/DissData/i3con04/csABMapCSV.txt","C:/Work/DissData/i3con04/networkABMapCSV.txt","C:/Work/DissData/i3con04/people+petsABMapCSV.txt",
			"C:/Work/DissData/oaei05/onto301MapCSV.txt","C:/Work/DissData/oaei05/onto302MapCSV.txt","C:/Work/DissData/oaei05/onto303MapCSV.txt","C:/Work/DissData/oaei05/onto304MapCSV.txt",
			"C:/Work/DissData/oaei05/onto202MapCSV.txt","C:/Work/DissData/oaei05/onto206MapCSV.txt","C:/Work/DissData/oaei05/onto248MapCSV.txt","C:/Work/DissData/oaei05/onto250MapCSV.txt","C:/Work/DissData/oaei05/onto251MapCSV.txt","C:/Work/DissData/oaei05/onto252MapCSV.txt",
			"C:/Work/DissData/oaei05/onto201MapCSV.txt","C:/Work/DissData/oaei05/onto204MapCSV.txt","C:/Work/DissData/oaei05/onto249MapCSV.txt","C:/Work/DissData/oaei05/onto205MapCSV.txt"	};
/*			"C:/Work/DissData/oaei05/onto103MapCSV.txt","C:/Work/DissData/oaei05/onto104MapCSV.txt","C:/Work/DissData/oaei05/onto201MapCSV.txt","C:/Work/DissData/oaei05/onto202MapCSV.txt",
			"C:/Work/DissData/oaei05/onto203MapCSV.txt","C:/Work/DissData/oaei05/onto204MapCSV.txt","C:/Work/DissData/oaei05/onto205MapCSV.txt","C:/Work/DissData/oaei05/onto206MapCSV.txt",
			"C:/Work/DissData/oaei05/onto207MapCSV.txt","C:/Work/DissData/oaei05/onto208MapCSV.txt","C:/Work/DissData/oaei05/onto209MapCSV.txt","C:/Work/DissData/oaei05/onto210MapCSV.txt",
			"C:/Work/DissData/oaei05/onto221MapCSV.txt","C:/Work/DissData/oaei05/onto222MapCSV.txt","C:/Work/DissData/oaei05/onto223MapCSV.txt","C:/Work/DissData/oaei05/onto224MapCSV.txt",
			"C:/Work/DissData/oaei05/onto225MapCSV.txt","C:/Work/DissData/oaei05/onto228MapCSV.txt","C:/Work/DissData/oaei05/onto230MapCSV.txt","C:/Work/DissData/oaei05/onto231MapCSV.txt",
			"C:/Work/DissData/oaei05/onto232MapCSV.txt","C:/Work/DissData/oaei05/onto233MapCSV.txt","C:/Work/DissData/oaei05/onto236MapCSV.txt",
			"C:/Work/DissData/oaei05/onto237MapCSV.txt","C:/Work/DissData/oaei05/onto238MapCSV.txt","C:/Work/DissData/oaei05/onto239MapCSV.txt","C:/Work/DissData/oaei05/onto240MapCSV.txt",
			"C:/Work/DissData/oaei05/onto241MapCSV.txt","C:/Work/DissData/oaei05/onto246MapCSV.txt","C:/Work/DissData/oaei05/onto247MapCSV.txt","C:/Work/DissData/oaei05/onto248MapCSV.txt",
			"C:/Work/DissData/oaei05/onto249MapCSV.txt","C:/Work/DissData/oaei05/onto250MapCSV.txt","C:/Work/DissData/oaei05/onto251MapCSV.txt","C:/Work/DissData/oaei05/onto252MapCSV.txt",
			"C:/Work/DissData/oaei05/onto253MapCSV.txt","C:/Work/DissData/oaei05/onto254MapCSV.txt","C:/Work/DissData/oaei05/onto257MapCSV.txt","C:/Work/DissData/oaei05/onto258MapCSV.txt",
			"C:/Work/DissData/oaei05/onto259MapCSV.txt","C:/Work/DissData/oaei05/onto260MapCSV.txt","C:/Work/DissData/oaei05/onto261MapCSV.txt","C:/Work/DissData/oaei05/onto262MapCSV.txt",
			"C:/Work/DissData/oaei05/onto265MapCSV.txt","C:/Work/DissData/oaei05/onto266MapCSV.txt"};*/	
	private static final int MAXITERATIONS = 5;							
	private static final int STRATEGY = Parameter.DECISIONTREE;	
	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
	private static final boolean EFFICIENTAGENDA = Parameter.COMPLETE;
	private static final String CLASSIFIERFOREXAMPLE = "C:/Work/Plus/Train/treeOld.obj";
	private static final String RULESFOREXAMPLE = "C:/Work/Plus/Train/rulesOld.obj";	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final double MAXERROR = 0.95;	
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
	private static final double CUTOFF = 0.8;									
	private static final String GENERATEDRULES = "C:/Work/Plus/Train/rulesNew.obj";
	private static final String[] SINGLERESULTFORTRAINING = {
				"C:/Work/Plus/Train/russia12res.txt","C:/Work/Plus/Train/russiaABres.txt","C:/Work/Plus/Train/russiaCDres.txt","C:/Work/Plus/Train/tourismABres.txt",
				"C:/Work/Plus/Train/sportSoccerEventres.txt","C:/Work/Plus/Train/animalsabres.txt","C:/Work/Plus/Train/hotelABres.txt","C:/Work/Plus/Train/csabres.txt","C:/Work/Plus/Train/networkabres.txt","C:/Work/Plus/Train/people+petsabres.txt",
				"C:/Work/Plus/Train/onto301abres.txt","C:/Work/Plus/Train/onto302abres.txt","C:/Work/Plus/Train/onto303abres.txt","C:/Work/Plus/Train/onto304abres.txt",
				"C:/Work/Plus/Train/onto202abres.txt","C:/Work/Plus/Train/onto206abres.txt",
				"C:/Work/Plus/Train/onto248abres.txt","C:/Work/Plus/Train/onto250abres.txt","C:/Work/Plus/Train/onto251abres.txt","C:/Work/Plus/Train/onto252abres.txt",
				"C:/Work/Plus/Train/onto201abres.txt","C:/Work/Plus/Train/onto204abres.txt","C:/Work/Plus/Train/onto249abres.txt","C:/Work/Plus/Train/onto205abres.txt"};
	/*			"C:/Work/Plus/Train/onto103abres.txt","C:/Work/Plus/Train/onto104abres.txt","C:/Work/Plus/Train/onto201abres.txt","C:/Work/Plus/Train/onto202abres.txt",
				"C:/Work/Plus/Train/onto203abres.txt","C:/Work/Plus/Train/onto204abres.txt","C:/Work/Plus/Train/onto205abres.txt","C:/Work/Plus/Train/onto206abres.txt",
				"C:/Work/Plus/Train/onto207abres.txt","C:/Work/Plus/Train/onto208abres.txt","C:/Work/Plus/Train/onto209abres.txt","C:/Work/Plus/Train/onto210abres.txt",
				"C:/Work/Plus/Train/onto221abres.txt","C:/Work/Plus/Train/onto222abres.txt","C:/Work/Plus/Train/onto223abres.txt","C:/Work/Plus/Train/onto224abres.txt",
				"C:/Work/Plus/Train/onto225abres.txt","C:/Work/Plus/Train/onto228abres.txt","C:/Work/Plus/Train/onto230abres.txt","C:/Work/Plus/Train/onto231abres.txt",
				"C:/Work/Plus/Train/onto232abres.txt","C:/Work/Plus/Train/onto233abres.txt","C:/Work/Plus/Train/onto236abres.txt","C:/Work/Plus/Train/onto237abres.txt",
				"C:/Work/Plus/Train/onto238abres.txt","C:/Work/Plus/Train/onto239abres.txt","C:/Work/Plus/Train/onto240abres.txt","C:/Work/Plus/Train/onto241abres.txt",
				"C:/Work/Plus/Train/onto246abres.txt","C:/Work/Plus/Train/onto247abres.txt","C:/Work/Plus/Train/onto248abres.txt","C:/Work/Plus/Train/onto249abres.txt",
				"C:/Work/Plus/Train/onto250abres.txt","C:/Work/Plus/Train/onto251abres.txt","C:/Work/Plus/Train/onto252abres.txt","C:/Work/Plus/Train/onto253abres.txt",
				"C:/Work/Plus/Train/onto254abres.txt","C:/Work/Plus/Train/onto257abres.txt","C:/Work/Plus/Train/onto258abres.txt","C:/Work/Plus/Train/onto259abres.txt",
				"C:/Work/Plus/Train/onto260abres.txt","C:/Work/Plus/Train/onto261abres.txt","C:/Work/Plus/Train/onto262abres.txt","C:/Work/Plus/Train/onto265abres.txt",
				"C:/Work/Plus/Train/onto266abres.txt"};*/	
	
	/**
	 * The validated examples are now run with all rules. The values are required
	 * for the training.
	 *
	 */
	public static void postvalidationExamples() {
		String classifierfile = new String();
		String rulesfile = new String();
		if (round==0) {
			classifierfile = CLASSIFIERFOREXAMPLE;
			rulesfile = RULESFOREXAMPLE;
		} else {
			classifierfile = DECISIONTREEFILE;
			rulesfile = GENERATEDRULES;
		}
		System.out.println("value calculation for validated examples");
		int numberOfOntologyPairs = ONTOLOGYFILES.length/2;	
		for (int i = 0; i<numberOfOntologyPairs; i++) {

		String[] myOntologyFiles = new String[2];
		myOntologyFiles[0] = ONTOLOGYFILES[2*i];
		myOntologyFiles[1] = ONTOLOGYFILES[2*i+1];
		System.out.print(myOntologyFiles[0]+" ");
		System.out.println(myOntologyFiles[1]);
		Align align = new Align();
		align.ontology = new MyOntology(myOntologyFiles);	
		align.p = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,classifierfile,rulesfile,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,myOntologyFiles);
		ExplicitRelation explicit = new ExplicitRelation(VALIDATEDRESULTS[i],align.ontology);
//		ExplicitRelation explicit = new ExplicitRelation("",align.ontology);
		align.explicit = explicit;	
		align.align();
		ResultList resultList = align.resultListLatest;
		
		Structure ontology = new MyOntology(myOntologyFiles);
		Agenda agenda = new EmptyAgenda();
/*		Rules rules = new ManualRuleSimple();
		Combination combination = new ManualWeightsLinear(23);*/
		Rules prerules1 = (Rules) ObjectIO.load(rulesfile);
		Rules rules = ObjectIO.fromSerializable(prerules1);
		rules.setPreviousResult(new ResultTableImpl());
		Combination combination1 = new DecisionTree(classifierfile,rules,ontology);
		Rules prerules2 = (Rules) ObjectIO.load(GENERATEDRULES);
		Rules rules2 = ObjectIO.fromSerializable(prerules2);
		Combination combination2 = new Averaging(rules2.total(),rules2.total(),rules2.total());
		explicit = new ExplicitRelation(VALIDATEDRESULTS[i],align.ontology);					
		agenda = new AgendaImpl();
		Agenda agenda1 = new CompleteAgenda();		
		agenda1.create(ontology,INTERNALTOO);
		agenda.add(agenda1);
		ResultTable lastResult = new ResultTableImpl();
		lastResult.copy(resultList,5,0.0);	
		rules.setPreviousResult(lastResult);
		rules2.setPreviousResult(lastResult);
		resultList = new ResultListImpl(5);
		int counter = 0;
		agenda.iterate();	
		while (agenda.hasNext()) {
			counter++;
			if (counter%1000==0) {
				System.out.print("|"); 	
				if (counter%100000==0) {
					System.out.println();
				}					
			}						
			AgendaElement element = agenda.next();
			Object object1 = element.object1;
			Object object2 = element.object2;		
			combination1.reset();								//Old classifier
			combination1.setObjects(object1,object2);
			combination1.process();
			combination2.reset();								//New rules, simply averaged
			combination2.setObjects(object1,object2);
			for (int j=0; j<rules2.total(); j++) {
				double value2 = rules2.process(object1,object2,j,ontology);
				combination2.setValue(j,value2);	
			}
			combination2.process();
			if (explicit.checkFor(object1,object2)||explicit.checkFor(object2,object1)) {
				resultList.set(object1,object2,1.0,combination2.getAddInfo());
				resultList.set(object2,object1,1.0,combination2.getAddInfo());
			} else {
				resultList.set(object1,object2,((combination1.result()+combination2.result())/2.0),combination2.getAddInfo());
				resultList.set(object2,object1,((combination1.result()+combination2.result())/2.0),combination2.getAddInfo());
			}
		}
		Save.saveCompleteEval(ontology,resultList,VALIDATEDRESULTS[i],0.9,SINGLERESULTFORTRAINING[i]);
		}
		System.out.println("\nrules applied\n");
	}

	private static final String[] RESULTFORTRAINING = {
			"C:/Work/Plus/Train/russia12res.txt","C:/Work/Plus/Train/russiaABres.txt","C:/Work/Plus/Train/russiaCDres.txt","C:/Work/Plus/Train/tourismABres.txt",
			"C:/Work/Plus/Train/sportSoccerEventres.txt",
			"C:/Work/Plus/Train/animalsabres.txt","C:/Work/Plus/Train/animalsabres.txt","C:/Work/Plus/Train/animalsabres.txt","C:/Work/Plus/Train/animalsabres.txt",
			"C:/Work/Plus/Train/hotelABres.txt","C:/Work/Plus/Train/hotelABres.txt","C:/Work/Plus/Train/hotelABres.txt","C:/Work/Plus/Train/hotelABres.txt",
			"C:/Work/Plus/Train/csabres.txt",
			"C:/Work/Plus/Train/networkabres.txt","C:/Work/Plus/Train/networkabres.txt","C:/Work/Plus/Train/networkabres.txt",
			"C:/Work/Plus/Train/people+petsabres.txt","C:/Work/Plus/Train/people+petsabres.txt",
			"C:/Work/Plus/Train/onto301abres.txt","C:/Work/Plus/Train/onto302abres.txt","C:/Work/Plus/Train/onto303abres.txt","C:/Work/Plus/Train/onto304abres.txt",	
			"C:/Work/Plus/Train/onto301abres.txt","C:/Work/Plus/Train/onto302abres.txt","C:/Work/Plus/Train/onto303abres.txt","C:/Work/Plus/Train/onto304abres.txt",
			"C:/Work/Plus/Train/onto202abres.txt","C:/Work/Plus/Train/onto206abres.txt",
			"C:/Work/Plus/Train/onto248abres.txt","C:/Work/Plus/Train/onto250abres.txt","C:/Work/Plus/Train/onto251abres.txt","C:/Work/Plus/Train/onto252abres.txt",
			"C:/Work/Plus/Train/onto201abres.txt","C:/Work/Plus/Train/onto204abres.txt","C:/Work/Plus/Train/onto249abres.txt","C:/Work/Plus/Train/onto205abres.txt"};	
			/*"C:/Work/Plus/Train/onto103abres.txt","C:/Work/Plus/Train/onto104abres.txt","C:/Work/Plus/Train/onto203abres.txt",
			"C:/Work/Plus/Train/onto207abres.txt","C:/Work/Plus/Train/onto208abres.txt","C:/Work/Plus/Train/onto209abres.txt","C:/Work/Plus/Train/onto210abres.txt",
			"C:/Work/Plus/Train/onto221abres.txt","C:/Work/Plus/Train/onto222abres.txt","C:/Work/Plus/Train/onto223abres.txt","C:/Work/Plus/Train/onto224abres.txt",
			"C:/Work/Plus/Train/onto225abres.txt","C:/Work/Plus/Train/onto228abres.txt","C:/Work/Plus/Train/onto230abres.txt","C:/Work/Plus/Train/onto231abres.txt",
			"C:/Work/Plus/Train/onto232abres.txt","C:/Work/Plus/Train/onto233abres.txt","C:/Work/Plus/Train/onto236abres.txt","C:/Work/Plus/Train/onto237abres.txt",
			"C:/Work/Plus/Train/onto238abres.txt","C:/Work/Plus/Train/onto239abres.txt","C:/Work/Plus/Train/onto240abres.txt","C:/Work/Plus/Train/onto241abres.txt",
			"C:/Work/Plus/Train/onto246abres.txt","C:/Work/Plus/Train/onto247abres.txt",
			"C:/Work/Plus/Train/onto253abres.txt",
			"C:/Work/Plus/Train/onto254abres.txt","C:/Work/Plus/Train/onto257abres.txt","C:/Work/Plus/Train/onto258abres.txt","C:/Work/Plus/Train/onto259abres.txt",
			"C:/Work/Plus/Train/onto260abres.txt","C:/Work/Plus/Train/onto261abres.txt","C:/Work/Plus/Train/onto262abres.txt","C:/Work/Plus/Train/onto265abres.txt",
			"C:/Work/Plus/Train/onto266abres.txt"*/
//	};	
	private static final String ARFF = "C:/Work/Plus/Train/weka.arff";				//parameters for machine learning
	private static final String COST = "C:/Work/Plus/Train/cost.cost";
	private static final String DECISIONTREEFILE = "C:/Work/Plus/Train/treeNew.obj";
	private static final String CLASSIFIERFILE = "C:/Work/Plus/Train/classifierNewExtra.obj";
	private static final String FINALRULESFILE = "C:/Work/Plus/Train/rulesNewExtra.obj";	
	
	
	/**
	 * The actual training step. The similarity values are to be interpreted in a way
	 * that the correct classification (aligned or not-aligned) is met. Different WEKA classifiers
	 * can be used for this.
	 *
	 */
	public static void train() {
		System.out.println("training started");
		String resultForTraining[] = RESULTFORTRAINING; //loading the example data
		String array1[][][] = new String[resultForTraining.length][][];
		String array2[][][] = new String[resultForTraining.length][][];
		int length = 0;
		int length2 = 0;
		for (int i = 0; i<resultForTraining.length; i++) {
			try {
			System.out.println("loading "+resultForTraining[i]);
			CSVParse csvParse = new CSVParse(resultForTraining[i]);		//first block of examples
			array1[i] = csvParse.getAllValues();		
			length = array1[i].length+length;
//			if (i<27) {
			csvParse = new CSVParse(resultForTraining[i]);				//second block of examples
			array2[i] = csvParse.getAllValues();
			length2 = array2[i].length+length2;
//			}
			} catch (Exception e) {
				UserInterface.print(e.getMessage());	
			}	
		}
		System.out.println("emphasizing structure");
		String array[][] = {{""}};
		array = new String[length+length2][array1[0][0].length];
		int l = 0;
		for (int i = 0; i<resultForTraining.length; i++) {
			array1[i] = setColumnRand(array1[i],0,"-0.3",0.5);			//add complete training values including label values 
			array1[i] = setColumnRand(array1[i],1,"-0.3",0.5);			//remove some to balance value of labels and uris
			array1[i] = setColumnRand(array1[i],2,"-0.3",0.5);
			array1[i] = setColumnRand(array1[i],3,"-0.3",0.5);
			array1[i] = setColumnRand(array1[i],4,"-0.3",0.5);
			array1[i] = setColumnRand(array1[i],5,"0.0",0.5);
			array1[i] = setColumnRand(array1[i],6,"0.0",0.5);
			for (int j = 0; j<array1[i].length; j++) {					
				array[l]=array1[i][j];
				l++;
			}
			array2[i] = setColumnRand(array2[i],0,"-0.3",0.99);			//remove label values to focus on structure for training
			array2[i] = setColumnRand(array2[i],1,"-0.3",0.99);
			array2[i] = setColumnRand(array2[i],2,"-0.3",0.99);
			array2[i] = setColumnRand(array2[i],3,"-0.3",0.99);
			array2[i] = setColumnRand(array2[i],4,"-0.3",0.99);
			array2[i] = setColumnRand(array2[i],5,"0.0",0.99);
			array2[i] = setColumnRand(array2[i],6,"0.0",0.99);
			for (int j = 0; j<array2[i].length; j++) {
				array[l]=array2[i][j];
				l++;
			}
		}	
		int numberOfRules = array[0].length-6;	
		int numberOfInstances = array.length;
		System.out.println(numberOfInstances);
		System.out.println("discretizing");
		for (int i = 0; i<(numberOfRules-1); i++) {
			array = discretize(array,i);								//discretize
		}
		
		System.out.println("saving and loading arff");
		Instances instances = getInstances(array);
		
		array1 = new String[1][1][1];
		array2 = new String[1][1][1];
		array = new String[1][1];
		System.out.println("oversampling");
		oversampling(instances);
		System.out.println("learning classifier");
		Classifier classifier = learnClassifier(instances);
//		double quality = getQuality(instances,classifier);
		String info = "";
		System.out.println("checking rules for importance");
		J48 j48 = new J48();
		try {
			j48 = (J48) classifier;
			info = j48.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ruleNecessary[] = new boolean[numberOfRules];	
		int numberOfRulesLeft = 0;
		for (int i = 0; i<numberOfRules; i++) {							
			Integer number = new Integer(i);
			ruleNecessary[i] = true;
			if (info.indexOf("rule"+number.toString()+" ")!=-1) {
				ruleNecessary[i] = true;	
				System.out.println(i);
				numberOfRulesLeft++;
			} else {
				ruleNecessary[i] = false;
			}
		}	
		System.out.println("saving");
		Rules newRules = new EmptyRule(numberOfRulesLeft);
		Rules rules = (Rules) ObjectIO.load(GENERATEDRULES);
		try {
			FileWriter out = new FileWriter(new File(DECISIONTREEFILE));
			FileWriter out2 = new FileWriter(new File(DECISIONTREEFILE+round));		//just for testing
			System.out.println(j48.toString());
			out.write(j48.toString()+"\n\n");
			out2.write(j48.toString()+"\n\n");
			int c=0;
			for (int k = 0; k<rules.total(); k++) {
				if (ruleNecessary[k]) {
					newRules.addIndividualRule(rules.rule(k));
					c++;
					System.out.println(k);
					System.out.println(" "+rules.rule(k).feature1.toString());
					System.out.println(" "+rules.rule(k).feature2.toString());
					System.out.println(" "+rules.rule(k).heuristic.toString());
					out.write(k+"\n");
					out.write(" "+rules.rule(k).feature1.toString()+"\n");
					out.write(" "+rules.rule(k).feature2.toString()+"\n");
					out.write(" "+rules.rule(k).heuristic.toString()+"\n");
					out2.write(k+"\n");
					out2.write(" "+rules.rule(k).feature1.toString()+"\n");
					out2.write(" "+rules.rule(k).feature2.toString()+"\n");
					out2.write(" "+rules.rule(k).heuristic.toString()+"\n");
				}
			}			
			out.close();
			out2.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
		ObjectIO.save(newRules,FINALRULESFILE);							//saving new rules and classifier
		Combination machinelearned = new MachineLearn(classifier,instances,newRules.total());
		ObjectIO.save(machinelearned,CLASSIFIERFILE);
		
		System.out.println("\nend\n");				
	}

	/**
	 * Changes the array entry in column to value, with a certain rand probability 
	 * @param array
	 * @param column
	 * @param value
	 * @param rand
	 * @return
	 */
	private static String[][] setColumnRand(String[][] array, int column, String value, double rand) {
		for (int i = 0; i<array.length; i++) {
			if (Double.valueOf(array[i][column+5]).doubleValue()>=0.0) {
				double number = Math.random();
				if (number>rand) {
					array[i][column+5] = value;
				}
			}
		}
		return array;
	}
	
	/**
	 * Discretizes the values in the array
	 * @param array
	 * @param column
	 * @return
	 */
	private static String[][] discretize(String[][] array, int column) {
		for (int i = 0; i<array.length; i++) {
			double old = Double.parseDouble(array[i][column+5]);
			String newv = array[i][column+5];
			if ((0.0<=old)&&(old<0.05)) newv = "0.0";
			if ((0.05<=old)&&(old<0.1)) newv = "0.05"; 
			if ((0.1<=old)&&(old<0.2)) newv = "0.1"; 
			if ((0.2<=old)&&(old<0.3)) newv = "0.2"; 
			if ((0.3<=old)&&(old<0.4)) newv = "0.3"; 
			if ((0.4<=old)&&(old<0.5)) newv = "0.4"; 
			if ((0.5<=old)&&(old<0.6)) newv = "0.5"; 
			if ((0.6<=old)&&(old<0.7)) newv = "0.6"; 
			if ((0.7<=old)&&(old<0.8)) newv = "0.7"; 
			if ((0.8<=old)&&(old<0.85)) newv = "0.8"; 
			if ((0.85<=old)&&(old<0.9)) newv = "0.85"; 
			if ((0.9<=old)&&(old<0.95)) newv = "0.9"; 
			if ((0.95<=old)&&(old<1.0)) newv = "0.95";
			if (1.0<=old) newv = "1.0"; 
			array[i][column+5] = newv;
		}
		return array;
	}
	
	/**
	 * Ensures that there are an equal number of positive and negative examples 
	 * @param instances
	 */
	private static void oversampling(Instances instances) {
		int numberOfInstances = instances.numInstances();	
		int positives = 0;
		int negatives = 0;	
		try{
		for (int i = 0; i<numberOfInstances; i++) {
			Instance instance = instances.instance(i);
			double resultassi = instance.value(instance.numAttributes()-1);
			if (resultassi==1) {
				positives++;
			} else {
				negatives++;
			}
		} 
		while (negatives>positives) {
			int ran = (int) Math.round(Math.random()*numberOfInstances);
			Instance instance = instances.instance(ran);
			double resultassi = instance.value(instance.numAttributes()-1);
			if (resultassi==1) {
				Instance newInst = new Instance(1.0,instance.toDoubleArray());
				instances.add(newInst);
				positives++;
			}			
		} 
		while (positives>negatives) {
			int ran = (int) Math.round(Math.random()*numberOfInstances);
			Instance instance = instances.instance(ran);
			double resultassi = instance.value(instance.numAttributes()-1);
			if (resultassi==0) {
				Instance newInst = new Instance(1.0,instance.toDoubleArray());
				instances.add(newInst);
				negatives++;
			}	
		}					
		} catch (Exception e) {
		}
	}

	/**
	 * Saves and loads the instances so WEKA can handle them
	 * @param array
	 * @return
	 */
	private static Instances getInstances(String array[][]) {
		WekaConnector.saveData(array,ARFF);
		Instances instances = WekaConnector.loadData(ARFF,COST);
		return instances;
	}

	/**
	 * The actual learner. All classifier parameters need to be set here.
	 * This is the standard decision tree learner.
	 * @param instances
	 * @return
	 */
	private static Classifier learnClassifier(Instances instances) {
		J48 classifier = new J48();
		try{
		classifier.setConfidenceFactor((float) 0.3);
		classifier.setMinNumObj(3);
		if (instances.numInstances()<=20) {
			classifier.setMinNumObj(3);			
		} else if (instances.numInstances()<=100) {
			classifier.setMinNumObj(5);
		} else if (instances.numInstances()<=200) {
			classifier.setMinNumObj(10);
		} else {
		    classifier.setMinNumObj(30);
		}    
		classifier.setNumFolds(20);
		classifier.setReducedErrorPruning(false);
		classifier.setUseLaplace(true);
		classifier.setSubtreeRaising(true);
		classifier.buildClassifier(instances);
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}	
		return classifier;	
	}

/*	private static Classifier learnClassifier(Instances instances) {
	AdaBoostM1 classifier = new AdaBoostM1();
	try{
	classifier.buildClassifier(instances);
	} catch (Exception e) {
		UserInterface.print(e.getMessage());
	}	
	return classifier;	

	
	private static Classifier learnClassifier(Instances instances) {
		MultilayerPerceptron classifier = new MultilayerPerceptron();
		try{
		classifier.setDecay(true); 
		classifier.setNominalToBinaryFilter(false);
		classifier.setHiddenLayers("t");		
		classifier.buildClassifier(instances);
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}	
		return classifier;	
	}
	
	private static Classifier learnClassifier(Instances instances) {
		SMO classifier = new SMO();
		try{
		classifier.buildClassifier(instances);
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}	
		return classifier;	
	}*/	

	/**
	 * Calculates some quality measure of the results
	 */
/*	private static double getQuality(Instances instances, Classifier classifier) {
		int qualityPP = 0; 	//correct positives
		int qualityPN = 0;	//false negatives
		int qualityNN = 0;	//correct negatives
		int qualityNP = 0;	//false positives
		int numberOfInstances = instances.numInstances();		
		try{
		for (int i = 0; i<numberOfInstances; i++) {
			Instance instance = instances.instance(i);
			double resultcalc = classifier.classifyInstance(instance);
			double resultassi = instance.value(instance.numAttributes()-1);
			if (resultassi==1) {
				if (resultcalc==1) {
					qualityPP++;
				} else {
					qualityPN++;
				}
			} else {
				if (resultcalc==1) {
					qualityNP++;
				} else {
					qualityNN++;
				}
			}
		}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
		System.out.println("quality: "+qualityPP+" "+qualityPN+" "+qualityNN+" "+qualityNP);
		double quality = 2*qualityPP+qualityNN;
		return quality;
	}*/

}
