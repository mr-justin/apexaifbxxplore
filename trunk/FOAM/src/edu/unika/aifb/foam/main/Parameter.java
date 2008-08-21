package edu.unika.aifb.foam.main;

import java.io.BufferedReader;
import java.io.FileReader;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.Ontology;

import edu.unika.aifb.foam.agenda.ClosestLabelAgenda;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class contains all the parameters needed to initialize
 * the alignment process. Further it allows to load them from
 * a file.
 * 
 * @author Marc Ehrig
 */
public class Parameter {
	
	public int maxiterations = 10;						//output parameters
	public int strategy = MANUALSIGMOID;		
	public boolean internaltoo = EXTERNAL;	
	public boolean efficientAgenda = EFFICIENT;	
	public String classifierFile = "";
	public String rulesFile = "";		
	public boolean semi = FULLAUTOMATIC;
	public double maxError = 0.9;
	public int numberQuestions = 5;
	public boolean removeDoubles = REMOVEDOUBLES;	
	public double cutoff = 0.9;

	public static final int EQUALLABELS = 0;			//strategy
	public static final int ONLYLABELS = 1;				
	public static final int MANUALWEIGHTED = 2;
	public static final int MANUALSIGMOID = 3;
	public static final int MACHINE = 4;	
	public static final int DECISIONTREE = 5;
	public static final boolean EXTERNAL = false;		//to compare
	public static final boolean INTERNAL = true;
	public static final boolean COMPLETE = false;		//efficiency parameters
	public static final boolean EFFICIENT = true;
	public static final boolean FULLAUTOMATIC = false;	//user interaction
	public static final boolean SEMIAUTOMATIC = true;
	public static final boolean ALLOWDOUBLES = false;	//bijective?
	public static final boolean REMOVEDOUBLES = true;	
	
	public String[] ontologies = {};					//additional parameters
	public String explicitFile = "";	
	public String resultFile = "";
	public String cutoffFile = "";
	public String questionFile = "";
	public String manualmappingsFile = "";
	
	private int scenario = NOSCENARIO;					//input parameters
	private int complexity;					
	private int size;
	private int overlap;
	private int ratio;
//	private int modelling;
	private int resources;
	
	public static final int NOSCENARIO = 0;				//scenarios
	public static final int QUERYREWRITING = 1;
	public static final int ONTOLOGYMERGING = 2;
	public static final int DATAINTEGRATION = 3;
	public static final int REASONING = 4;
	public static final int ONTOLOGYEVOLUTION = 5;
		
//	private static final int TAXONOMY = 0;				//complexity
//	private static final int RDFS = 1;
	private static final int OWL = 2;
	private static final int SMALL = 0;					//ontology size
	private static final int MEDIUM = 1;
	private static final int LARGE = 2;
	private static final int LOW = 0;					//overlap
	private static final int HIGH = 1;
	private static final int SCHEMA = 0;				//schema/instance ratio
	private static final int INSTANCE = 1;
//	private static final int CLEAN = 0;					//ontology modelling
//	private static final int MESSY = 1;
//	private static final int LITTLE = 0;				//computational resources
	private static final int MANY = 1;
	
	private static final String CLASSIFIERFILE = "config/tree.obj";
	private static final String RULESFILE = "config/rules.obj";
	
	public int numberOfClasses = 0;
	public int numberOfProperties = 0;
	public int numberOfInstances = 0;
	private int calc1000 = 1000;
	public int timeest = 0;
	public int qualityest = 0;
	
	/**
	 * The standard parameters are taken.
	 *
	 */
	public Parameter() {
	}
	
	/**
	 * Parameters can be assigned directly. This is only recommended for users who really know what each
	 * parameter triggers.
	 * @param maxiterationT
	 * @param strategyT
	 * @param internaltooT
	 * @param efficientT
	 * @param classifierFileT
	 * @param rulesFileT
	 * @param semiT
	 * @param maxErrorT
	 * @param removeDoublesT
	 * @param ontologiesT
	 */
	public Parameter(int maxiterationT, int strategyT, boolean internaltooT, boolean efficientT, String classifierFileT, String rulesFileT, boolean semiT, double maxErrorT, int numberQuestionsT, boolean removeDoublesT, double cutoffT, String[] ontologiesT) {
		maxiterations = maxiterationT;
		strategy = strategyT;
		internaltoo = internaltooT;
		efficientAgenda = efficientT;
		classifierFile = classifierFileT;
		rulesFile = rulesFileT;
		semi = semiT; 
		maxError = maxErrorT;
		numberQuestions = numberQuestionsT;
		removeDoubles = removeDoublesT;
		cutoff = cutoffT;
		ontologies = ontologiesT;
		output();
	}
	
	/**
	 * The parameters can be loaded directly from a parameter file. This file does not have 
	 * to be complete. Missing parameters are derived where possible.
	 * @param parameterFile
	 */
	public Parameter(String parameterFile) {
		boolean stratfound = false;		
		try{
		BufferedReader in = new BufferedReader(new FileReader(parameterFile));
		String line = in.readLine();
		ontologies = new String[2];
		while (line!=null) {
			int poseq = line.indexOf("= ");
			int possem = line.indexOf(";");
			if ((poseq!=-1)&&(possem!=-1)) {
				if (line.startsWith("ontology1")) {ontologies[0] = line.substring(poseq+2,possem);}
				if (line.startsWith("ontology2")) {ontologies[1] = line.substring(poseq+2,possem);}
				if (line.startsWith("explicit")) {explicitFile = line.substring(poseq+2,possem);}
				if (line.startsWith("scenario")) {
					String strat = line.substring(poseq+2,possem);
					if (strat.equals("NOSCENARIO")) {scenario = NOSCENARIO;}
					if (strat.equals("QUERYREWRITING")) {scenario = QUERYREWRITING;}
					if (strat.equals("ONTOLOGYMERGING")) {scenario = ONTOLOGYMERGING;}	
					if (strat.equals("DATAINTEGRATION")) {scenario = DATAINTEGRATION;}
					if (strat.equals("REASONING")) {scenario = REASONING;}					
					if (strat.equals("ONTOLOGYEVOLUTION")) {scenario = ONTOLOGYEVOLUTION;}
				}	
				if (line.startsWith("cutoffFile")) {cutoffFile = line.substring(poseq+2,possem);}			
				
			if (line.startsWith("maxiteration")) {
				maxiterations = Integer.parseInt(line.substring(poseq+2,possem));
			}
			if (line.startsWith("strategy")) {
				String strat = line.substring(poseq+2,possem);
				if (strat.equals("EQUALLABELS")) {strategy = EQUALLABELS; stratfound=true;}
				if (strat.equals("ONLYLABELS")) {strategy = ONLYLABELS; stratfound=true;}
				if (strat.equals("MANUALWEIGHTED")) {strategy = MANUALWEIGHTED; stratfound=true;}
				if (strat.equals("MANUAL")) {strategy = MANUALSIGMOID; stratfound=true;}
				if (strat.equals("MACHINE")) {strategy = MACHINE; stratfound=true;}	
				if (strat.equals("DECISIONTREE")) {strategy = DECISIONTREE; stratfound=true;}
			}
			if (line.startsWith("internal")) {
				String strat = line.substring(poseq+2,possem);
				if (strat.equals("INTERNAL")) {internaltoo = INTERNAL;}
				if (strat.equals("EXTERNAL")) {internaltoo = EXTERNAL;}
			}
			if (line.startsWith("efficient")) {
				String strat = line.substring(poseq+2,possem);
				if (strat.equals("EFFICIENT")) {efficientAgenda = EFFICIENT;}
				if (strat.equals("COMPLETE")) {efficientAgenda = COMPLETE;}
			}
			if (line.startsWith("class")) {classifierFile = line.substring(poseq+2,possem);}
			if (line.startsWith("rules")) {rulesFile = line.substring(poseq+2,possem);}
			if (line.startsWith("semi")) {
				String strat = line.substring(poseq+2,possem);
				if (strat.equals("FULLAUTOMATIC")) {semi = FULLAUTOMATIC;}
				if (strat.equals("SEMIAUTOMATIC")) {semi = SEMIAUTOMATIC;}
			}
			if (line.startsWith("maxerror")) {maxError = Double.parseDouble(line.substring(poseq+2,possem));}
			if (line.startsWith("numberQuestions")) {numberQuestions = Integer.parseInt(line.substring(poseq+2,possem));}			
			if (line.startsWith("remove")) {
				String strat = line.substring(poseq+2,possem);
				if (strat.equals("REMOVEDOUBLES")) {removeDoubles = REMOVEDOUBLES;}
				if (strat.equals("ALLOWDOUBLES")) {removeDoubles = ALLOWDOUBLES;}
			}
			if (line.startsWith("result")) {resultFile = line.substring(poseq+2,possem);}
			if (line.startsWith("cutoffvalue")) {cutoff = Double.parseDouble(line.substring(poseq+2,possem));}
			if (line.startsWith("question")) {questionFile = line.substring(poseq+2,possem);}
			if (line.startsWith("manualmapping")) {manualmappingsFile = line.substring(poseq+2,possem);}
			}
			line = in.readLine();			
		}
		in.close();
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
		if (stratfound==false) {
			setInputParametersFromOntologies();
			setOutputParameters();
		}
		output();
	}
	
	/**
	 * The parameters are automatically derived from the given ontologies and the
	 * application scenario.
	 * @param scenarioT
	 * @param ontologiesT
	 */
	public Parameter (int scenarioT, String[] ontologiesT) {
		scenario = scenarioT;
		ontologies = ontologiesT;
		setInputParametersFromOntologies();		
		setOutputParameters();
		output();
	}
	
	/**
	 * Internal parameters of the ontologies are calculated.
	 *
	 */
	private void setInputParametersFromOntologies() {
		MyOntology myOntology = new MyOntology(ontologies);
		Ontology ontology = myOntology.ontology;
		try{
/*		complexity = TAXONOMY;
		if (ontology.createEntityRequest(ObjectProperty.class).getAll().size()>3) {
			complexity = RDFS;}
		if (ontology.createAxiomRequest(SubClassOf.class).getAll().size()>3) {
			complexity = OWL;}*/
		complexity = OWL;		
		size = LARGE;
		numberOfClasses = ontology.createEntityRequest(OWLClass.class).getAll().size();
		numberOfProperties = ontology.createEntityRequest(ObjectProperty.class).getAll().size()+
			ontology.createEntityRequest(DataProperty.class).getAll().size();
		numberOfInstances = ontology.createEntityRequest(Individual.class).getAll().size();
		int numberOfEntities = numberOfClasses + numberOfInstances + numberOfProperties;
		if (numberOfEntities<400) {
			size = MEDIUM;}
		if (numberOfEntities<100) {
			size = SMALL;}	
		overlap = LOW;
		ClosestLabelAgenda agenda = new ClosestLabelAgenda();
		agenda.create(myOntology,internaltoo);
		int labelagendasize = agenda.size();
		int generalagendasize = (numberOfClasses*numberOfClasses+numberOfProperties*numberOfProperties+numberOfInstances*numberOfInstances)/4;
		if (labelagendasize*100>generalagendasize) {
			overlap = HIGH;}
		ratio = SCHEMA;
		if ((numberOfClasses*2+numberOfProperties*2)<(numberOfInstances)) {
			ratio = INSTANCE;}
		resources = LOW;
		long starttime = System.currentTimeMillis();
		double value = 0;
		for (int i = 0; i<20000000; i++) {
			value = value + 3.1412;
		}
		long endtime = System.currentTimeMillis();
		calc1000 = (int) (endtime - starttime)*10;
		if (calc1000<3000) {
			resources = HIGH;
		}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}	
	}
	
	/**
	 * The internal parameters are transformed to real parameters for the algorithm.
	 *
	 */
	private void setOutputParameters() {
		strategy = DECISIONTREE;
		classifierFile = CLASSIFIERFILE;
		rulesFile = RULESFILE;	
		maxError = 0.9;
		cutoff = 0.9;
		if ((scenario==QUERYREWRITING)||(scenario==REASONING)||(size==LARGE)) {
			efficientAgenda = EFFICIENT;
		} else {
			efficientAgenda = COMPLETE;
		}
		if ((scenario==ONTOLOGYMERGING)||(scenario==DATAINTEGRATION)||(scenario==REASONING)) {
			cutoff = 0.95;}
		if (scenario==QUERYREWRITING) {
			cutoff = 0.8;}
		maxError = cutoff;
		maxiterations = 0;
		if ((scenario==NOSCENARIO)||(scenario==ONTOLOGYMERGING)||(scenario==DATAINTEGRATION)||(scenario==REASONING)) {
			maxiterations++;}
//		if (complexity==OWL) {
//			maxiterations++;}
		if (size==SMALL) {maxiterations = maxiterations+2;}
		if (size==MEDIUM) {maxiterations++;}
		if (overlap==LOW) {maxiterations++;}
		if (ratio==SCHEMA) {maxiterations++;}
		if (resources==MANY) {maxiterations++;}
		maxiterations = maxiterations*3 +10;
		if (scenario==QUERYREWRITING) {
			maxiterations = 5;
		}
		if (efficientAgenda==COMPLETE) {
			maxiterations = 10;
		}
/*		if (maxiterations>20) {
			maxiterations = 20;
		}*/
		int semiauto = 0;
		if ((scenario==ONTOLOGYMERGING)||(scenario==ONTOLOGYEVOLUTION)||(scenario==DATAINTEGRATION)) {
			semiauto++;}
		if (size==SMALL) {
			numberQuestions = 5;}
		if (size==MEDIUM) {
			numberQuestions = 5;}
		if (size==LARGE) {
			numberQuestions = 20;}		
		if (overlap==SMALL) {
			semiauto++;}
		if (semiauto>=1) {
			semi = SEMIAUTOMATIC;
		} else {
			semi = FULLAUTOMATIC;
		}
		if ((scenario==NOSCENARIO)||(scenario==QUERYREWRITING)) {
			semi = FULLAUTOMATIC;
		}
	}
	
	public void output() {
		for (int i = 0; i< ontologies.length; i++) {
			UserInterface.print("Ontology "+(i+1)+": "+ontologies[i]+"\n");
		}
//		UserInterface.print("Scenario: "+scenario+"\n");
//		UserInterface.print("Parameters for Alignment:\n");
		if (strategy==DECISIONTREE) {
			UserInterface.print("Strategy: DECISIONTREE\n");
		} else {
			UserInterface.print("Strategy: "+strategy+"\n");
		}
		UserInterface.print("Iterations: "+maxiterations+"\n");
		if (efficientAgenda==EFFICIENT) {
			UserInterface.print("Comparisons: EFFICIENT\n");
		} else {
			UserInterface.print("Comparisons: COMPLETE\n");
		}		
		UserInterface.print("Threshold: "+cutoff+"\n");
		if (semi==SEMIAUTOMATIC) {
			UserInterface.print("Automation: SEMIAUTOMATION\n");
		} else {
			UserInterface.print("Automation: FULLAUTOMATION\n");
		}	
//		UserInterface.print("\nEstimations:\n");
		if (numberOfClasses==0) {
			setInputParametersFromOntologies();			
		}
		if ((strategy==EQUALLABELS)||(strategy==ONLYLABELS)) {
			timeest = (numberOfClasses*numberOfClasses+numberOfProperties*numberOfProperties+numberOfInstances*numberOfInstances)*maxiterations*calc1000/1000/10+1000;
		} else {
			timeest = (numberOfClasses*numberOfClasses+numberOfProperties*numberOfProperties+numberOfInstances*numberOfInstances)*maxiterations*calc1000/1000*1+1000;
		}
		if (efficientAgenda==EFFICIENT) {
			timeest = timeest/6;
		}
		if (timeest>=5000000) {timeest = 10000000;}		
		if ((timeest>=500000)&&(timeest<5000000)) {timeest = 1000000;}
		if ((timeest>=50000)&&(timeest<500000)) {timeest = 100000;}
		if ((timeest>=5000)&&(timeest<50000)) {timeest = 10000;}
		if (timeest<5000) {timeest = 1000;}
		UserInterface.print("Time esitimation: "+timeest/1000+" s (without user interaction)\n");
		qualityest = 0;
		if (efficientAgenda==COMPLETE) {
			qualityest++;}
		if (maxiterations>12) {
			qualityest++;}
		if (semi==SEMIAUTOMATIC) {
			qualityest++;}
		UserInterface.print("Quality estimation: "+qualityest+" (3 highest, 0 lowest)\n");
		if (semi==SEMIAUTOMATIC) {		
			UserInterface.print("Number of questions for user: "+numberQuestions*3+"\n");
		}
	}
	
	/**
	 * Testing method for this class.
	 * @param args
	 */
/*	public static void main (String[] args) {
		String[] ontologyFiles = {"C:/Work/alignmentJournal/animalsA.owl","C:/Work/alignmentJournal/animalsB.owl"}; 	
		Parameter parameter = new Parameter(ONTOLOGYEVOLUTION,ontologyFiles);
		parameter.output();
	}*/
	
	public static void main (String[] args) {
		long starttime = System.currentTimeMillis();
		double value = 0;
		for (int i = 0; i<200000000; i++) {
			value = value + 3.1412;
		}
		long endtime = System.currentTimeMillis();
		int calc1000 = (int) (endtime - starttime);
		System.out.println(calc1000);
	}
	
}
