package edu.unika.aifb.foam.main;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.result.Evaluation;

/**
 * This class is the main interface to use the Align algorithm.
 * It has been used to allow testing different ontologies and strategies.
 *  
 * @author Marc Ehrig
 * @version 2.0
 * @date 17/02/05
 * 
 */
public class MainMany {

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
	 * The alignment process can also be started as a Java application. The
	 * specified parameters are given to the alignment process. After it
	 * has been run we can access the results.
	 */
	public static void application() {
		double threshold = 0.9;
		String CLASSIFIERFILE = "C:/Work/Plus/Train/treeNewPartsc.obj";
		for (int k = 2; k<4; k++) {
			switch (k) {
				case 0:
					threshold = 0.9;
					CLASSIFIERFILE = "C:/Work/Plus/Train/treeNewPartsc.obj";
					break;
				case 1:
					threshold = 0.9;
					CLASSIFIERFILE = "C:/Work/Plus/Train/treeNewParts+c.obj";
					break;
				case 2:
					threshold = 0.95;
					CLASSIFIERFILE = "C:/Work/Plus/Train/treeNewPartsc.obj";
					break;
				case 3:
					threshold = 0.95;
					CLASSIFIERFILE = "C:/Work/Plus/Train/treeNewParts+c.obj";
					break;
			}
		for (int j = 0; j<14; j++) {
			String[] ontologyFiles = ONTOLOGYFILES[j];
			String manualMappingsFile = MANUALMAPPINGSFILE[j];
			String resultFile = RESULTFILE[j];
			MyOntology ontologies = new MyOntology(ontologyFiles);	//assigning the ontologies
			Parameter parameter = new Parameter(1,Parameter.EQUALLABELS,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.FULLAUTOMATIC,0.7,20,Parameter.REMOVEDOUBLES,0.7,ontologyFiles);	
			for (int i = 1; i<7; i++) {
//				if ((k==0)&&(j==0)&&(i==1)) {
//					k=2; j=1; i=1;}
				ExplicitRelation explicit = new ExplicitRelation("",ontologies);					
//			Parameter parameter = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFILE,RULESFILE,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,ONTOLOGYFILES);	//assigning the parameters
				switch (i) {
/*				case 1:
					parameter = new Parameter(1,Parameter.EQUALLABELS,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.FULLAUTOMATIC,0.9,20,Parameter.REMOVEDOUBLES,0.9,ontologyFiles);
					break;
				case 2:
					parameter = new Parameter(1,Parameter.ONLYLABELS,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.FULLAUTOMATIC,0.7,20,Parameter.REMOVEDOUBLES,0.7,ontologyFiles);
					break;
				case 3:
					parameter = new Parameter(10,Parameter.MANUALWEIGHTED,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.FULLAUTOMATIC,0.31,20,Parameter.REMOVEDOUBLES,0.31,ontologyFiles);
					break;
				case 4:
					parameter = new Parameter(10,Parameter.MANUALSIGMOID,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.FULLAUTOMATIC,0.25,20,Parameter.REMOVEDOUBLES,0.25,ontologyFiles);
					break;
				case 5:
					parameter = new Parameter(30,Parameter.MANUALSIGMOID,Parameter.EXTERNAL,Parameter.EFFICIENT,"","",Parameter.FULLAUTOMATIC,0.25,20,Parameter.REMOVEDOUBLES,0.25,ontologyFiles);
					break;*/
				case 1:
					parameter = new Parameter(10,Parameter.DECISIONTREE,Parameter.EXTERNAL,Parameter.COMPLETE,CLASSIFIERFILE,RULESFILE,Parameter.FULLAUTOMATIC,threshold,20,Parameter.REMOVEDOUBLES,threshold,ontologyFiles);
					break;
				case 2:
					parameter = new Parameter(30,Parameter.DECISIONTREE,Parameter.EXTERNAL,Parameter.EFFICIENT,CLASSIFIERFILE,RULESFILE,Parameter.FULLAUTOMATIC,threshold,20,Parameter.REMOVEDOUBLES,threshold,ontologyFiles);
					break;
/*				case 8:
					parameter = new Parameter(5,Parameter.ONLYLABELS,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.SEMIAUTOMATIC,0.7,20,Parameter.REMOVEDOUBLES,0.7,ontologyFiles);
					break;
				case 9:
					parameter = new Parameter(10,Parameter.MANUALSIGMOID,Parameter.EXTERNAL,Parameter.COMPLETE,"","",Parameter.SEMIAUTOMATIC,0.25,20,Parameter.REMOVEDOUBLES,0.25,ontologyFiles);
					break;*/
				case 3:
					parameter = new Parameter(10,Parameter.DECISIONTREE,Parameter.EXTERNAL,Parameter.COMPLETE,CLASSIFIERFILE,RULESFILE,Parameter.SEMIAUTOMATIC,threshold,20,Parameter.REMOVEDOUBLES,threshold,ontologyFiles);
					break;
				case 4:
					parameter = new Parameter(30,Parameter.DECISIONTREE,Parameter.EXTERNAL,Parameter.EFFICIENT,CLASSIFIERFILE,RULESFILE,Parameter.SEMIAUTOMATIC,threshold,20,Parameter.REMOVEDOUBLES,threshold,ontologyFiles);
					break;
				case 5:
					parameter = new Parameter(Parameter.QUERYREWRITING,ontologyFiles);
					break;
				case 6:
					parameter = new Parameter(Parameter.ONTOLOGYMERGING,ontologyFiles);
					break;
				}
				parameter.ontologies = ontologyFiles;
				parameter.manualmappingsFile = manualMappingsFile;
				parameter.resultFile = resultFile;
				AlignMany align = new AlignMany();						//creating the new alignment method
				align.name = String.valueOf(i);
				align.ontology = ontologies;	
				align.p = parameter;
				align.explicit = explicit;
				align.align();									//process
				Evaluation evaluation = new Evaluation(manualMappingsFile);
				evaluation.doEvaluation(align.ontology,align.resultListLatest,align.p.cutoff);
				evaluation.printEvaluation();					//just for information
			}
		}
		}
	}
/*	private static final String[][] ONTOLOGYFILES = {{"C:/Work/DissData/i3con04/animalsA.owl","C:/Work/DissData/i3con04/animalsB.owl"},{"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto303.owl"}}; 	//ontologies
	private static final String[] RESULTFILE = {"C:/Work/Plus/Eval/animalsAB","C:/Work/Plus/Eval/onto303"};	//output
	private static final String[] MANUALMAPPINGSFILE = {"C:/Work/DissData/i3con04/animalsABMapCSV.txt","C:/Work/DissData/oaei05/onto303MapCSV.txt"};*/		
	private static final String[][] ONTOLOGYFILES = {{"C:/Work/DissData/first03/russia1.owl","C:/Work/DissData/first03/russia2.owl"},{"C:/Work/DissData/first03/russiaC.owl","C:/Work/DissData/first03/russiaD.owl"},
			{"C:/Work/DissData/i3con04/animalsA.owl","C:/Work/DissData/i3con04/animalsB.owl"},{"C:/Work/DissData/i3con04/people+petsA.owl","C:/Work/DissData/i3con04/people+petsB.owl"},
			{"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto303.owl"},{"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto304.owl"},
			{"C:/Work/DissData/first03/russiaA.owl","C:/Work/DissData/first03/russiaB.owl"},{"C:/Work/DissData/first03/tourismA.owl","C:/Work/DissData/first03/tourismB.owl"},
			{"C:/Work/DissData/i3con04/csA.owl","C:/Work/DissData/i3con04/csB.owl"},{"C:/Work/DissData/i3con04/hotelA.owl","C:/Work/DissData/i3con04/hotelB.owl"},{"C:/Work/DissData/i3con04/networkA.owl","C:/Work/DissData/i3con04/networkB.owl"},
			{"C:/Work/DissData/second04/sportEvent.owl","C:/Work/DissData/second04/sportSoccer.owl"},{"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto301.owl"},{"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto302.owl"}}; 	//ontologies
	private static final String[] RESULTFILE = {"C:/Work/Plus/Eval/russia12","C:/Work/Plus/Eval/russiaCD","C:/Work/Plus/Eval/animalsAB","C:/Work/Plus/Eval/people+petsAB","C:/Work/Plus/Eval/onto303","C:/Work/Plus/Eval/onto304",
			"C:/Work/Plus/Eval/russiaAB","C:/Work/Plus/Eval/tourismAB","C:/Work/Plus/Eval/csAB","C:/Work/Plus/Eval/hotelAB","C:/Work/Plus/Eval/networkAB","C:/Work/Plus/Eval/sportAB","C:/Work/Plus/Eval/onto301","C:/Work/Plus/Eval/onto302"};	//output
	private static final String[] MANUALMAPPINGSFILE = {"C:/Work/DissData/first03/russia12Map.txt","C:/Work/DissData/first03/russiaCDMap.txt","C:/Work/DissData/i3con04/animalsABMapCSV.txt","C:/Work/DissData/i3con04/people+petsABMapCSV.txt","C:/Work/DissData/oaei05/onto303MapCSV.txt","C:/Work/DissData/oaei05/onto304MapCSV.txt",
			"C:/Work/DissData/first03/russiaABMap.txt","C:/Work/DissData/first03/tourismABMap.txt","C:/Work/DissData/i3con04/csABMapCSV.txt","C:/Work/DissData/i3con04/hotelABMapCSV.txt","C:/Work/DissData/i3con04/networkABMapCSV.txt","C:/Work/DissData/second04/sportSoccerEventMap.txt","C:/Work/DissData/oaei05/onto301MapCSV.txt","C:/Work/DissData/oaei05/onto302MapCSV.txt"};	
	private static final String RULESFILE = "C:/Work/Plus/Train/rulesNew.obj";
}
