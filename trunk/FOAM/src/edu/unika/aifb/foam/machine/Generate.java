package edu.unika.aifb.foam.machine;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.rules.EmptyRule;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.rules.RulesCompleteForML2;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class generates the basic data needed for training. These are the rules
 * which will be trained on. And second the example alignments the user needs to
 * validate.
 * @author meh
 *
 */
public class Generate {

	/**
	 * Starting the first step of training
	 * @param args
	 */
	public static void main(String[] args) {
		generateRules();
//		generateExamples();
	}
	
	private static final String GENERATEDRULES = "C:/Work/Plus/Train/rulesNew.obj";
//	private static final String[] ONTOLOGYFILESFORRULES = {"C:/Train/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto304.owl"}; 	//ontologies
//	private static final String ALLFEATURES = "";	//parameters for rule generation
//	private static final int FEATURELEVEL = 2;
	
	/**
	 * Generates rules from existing rule collections e.g. ManualRuleComplex and
	 * adds other features which are part of a list. These will contain domain-specific
	 * features. The rules are saved for later use.
	 */
	public static void generateRules() {
		System.out.println("generation of rules");
		Rules manualRules = new RulesCompleteForML2();
//		Structure ontology = new MyOntology(ONTOLOGYFILESFORRULES);
//		Rules allFeatureRules = new AllFeaturesRule(ALLFEATURES,ontology,FEATURELEVEL, new ResultTableImpl());
//		Rules allRules = new EmptyRule(manualRules.total()+allFeatureRules.total());
		Rules allRules = new EmptyRule(manualRules.total());
		for (int i = 0; i<manualRules.total(); i++) {
			allRules.addIndividualRule(manualRules.rule(i));
		}
/*		for (int i = 0; i<allFeatureRules.total(); i++) {
			allRules.addIndividualRule(allFeatureRules.rule(i));
		}*/
		allRules.setPreviousResult(null);
		ObjectIO.save(ObjectIO.toSerializable(allRules),GENERATEDRULES);
		UserInterface.print(" "+allRules.total()+" rules generated\n");
	}

	private static final String[] ONTOLOGYFILES = {"C:/Train/oaei05/onto101.owl","C:/Train/oaei05/onto304.owl"};
	private static final String[] RESULTSFORVALIDATION = {"C:/Work/Train/onto304abres.txt"};
	private static final String EXPLICITFILE = "";								//parameters for example generation
	private static final String MANUALMAPPINGS = "";	
	private static final int MAXITERATIONS = 10;							
	private static final int STRATEGY = Parameter.DECISIONTREE;	
	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
	private static final boolean EFFICIENTAGENDA = Parameter.COMPLETE;
	private static final String CLASSIFIERFOREXAMPLE = "C:/Train/treeOld.obj";
	private static final String RULESFOREXAMPLE = "C:/Train/rulesOld.obj";	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final double MAXERROR = 0.95;	
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
	private static final double CUTOFF = 0.5;									//should be considerably lower than normally to also save non-alignments  
	
	/**
	 * To train the system with machine learning, we need examples. These examples
	 * are created with a simpler strategy and given to the user for validation.
	 *
	 */
	public static void generateExamples() {
		UserInterface.print("generation of example mappings\n");
		int numberOfOntologyPairs = ONTOLOGYFILES.length/2;	
		for (int i = 0; i<numberOfOntologyPairs; i++) {
			String[] myOntologyFiles = new String[2];
			myOntologyFiles[0] = ONTOLOGYFILES[2*i];
			myOntologyFiles[1] = ONTOLOGYFILES[2*i+1];
			UserInterface.print(myOntologyFiles[0]+" ");
			UserInterface.print(myOntologyFiles[1]+"\n");
			Align align = new Align();
			align.ontology = new MyOntology(myOntologyFiles);	
			align.p = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFOREXAMPLE,RULESFOREXAMPLE,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,myOntologyFiles);	
			align.explicit = new ExplicitRelation(EXPLICITFILE,align.ontology);	
			align.align();	
			Save.saveCompleteEval(align.ontology,align.resultListLatest,MANUALMAPPINGS,align.p.cutoff,RESULTSFORVALIDATION[i]);
			UserInterface.print("\n");
		}
	}
}
