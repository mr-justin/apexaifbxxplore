package edu.unika.aifb.foam.main;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.InputStream;
import java.util.Vector;

//import com.Ostermiller.util.CSVParser;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.result.Save;
import edu.unika.aifb.foam.util.CSVParse;
import edu.unika.aifb.foam.util.INRIAAlignmentAPI;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This class is the main interface to use the Align algorithm.
 * This one is especially used for the tests of the OAEI.
 *  
 * @author Marc Ehrig
 * @date 05/08/05
 * 
 */
public class MainOAEI {

	/**
	 * This is just a general testing method.
	 * @param args
	 */
	public static void main(String[] args) {
		application();
	}
	
	/**
	 * The alignment process can also be started as a Java application. The
	 * specified parameters are given to the alignment process. After it
	 * has been run we can access the results.
	 */
	public static void application() {
		INRIAAlignmentAPI api = new INRIAAlignmentAPI();
		long time = 0;
		String[] ontologyurl = new String[2];
		for (int i = 0; i<BENCHNUMBERS.length; i++) {
//		for (int i = 0; i<(BENCHNUMBERS.length+MAXEV); i++) {
//			if (BENCHNUMBERS[i].equals("259")) {
//			if (i==20) {
			System.out.println();
			time = 0;
			if (i<BENCHNUMBERS.length) {
				ONTOLOGYFILES[0] = ONTOLOGYFILESPATH+"benchmarks/101/onto.rdf";
				ONTOLOGYFILES[1] = ONTOLOGYFILESPATH+"benchmarks/"+BENCHNUMBERS[i]+"/onto.rdf";
				ontologyurl[0] = "http://oaei.inrialpes.fr/2005/benchmarks/101/onto.rdf";				
				ontologyurl[1] = "http://oaei.inrialpes.fr/2005/benchmarks/"+BENCHNUMBERS[i]+"/onto.rdf";
				RESULTFILE = RESULTFILESPATH+"benchmark/"+BENCHNUMBERS[i]+"/foam.rdf";
				RESULTFILECSV = ONTOLOGYFILESPATH+"benchmarks/"+BENCHNUMBERS[i]+"/resonto.txt";
				RESULTDIR = RESULTFILESPATH+"benchmark/"+BENCHNUMBERS[i];
				MANUALMAPPINGSFILE = ONTOLOGYFILESPATH+"benchmarks/"+BENCHNUMBERS[i]+"/refalign.rdf";
				MANUALMAPPINGSFILECSV = ONTOLOGYFILESPATH+"benchmarks/"+BENCHNUMBERS[i]+"/ontoCSV.txt";				
			} else {
				Integer ev_int = new Integer(i-BENCHNUMBERS.length+1);
				String ev = ev_int.toString(); 
				ONTOLOGYFILES[0] = ONTOLOGYFILESPATH+"ev_subs/"+ev+"/source.owl";
				ONTOLOGYFILES[1] = ONTOLOGYFILESPATH+"ev_subs/"+ev+"/target.owl";
				ontologyurl[0] = "http://matching.com/source/"+ev+".owl";				
				ontologyurl[1] = "http://matching.com/target/"+ev+".owl";	
				RESULTFILE = RESULTFILESPATH+"directory/"+ev+"/foam.rdf";
				RESULTFILECSV = ONTOLOGYFILESPATH+"ev_subs/"+ev+"/resonto.txt";
				RESULTDIR = RESULTFILESPATH+"directory/"+ev;
				MANUALMAPPINGSFILE = "";				
			}
			if (MANUALMAPPINGSFILE!="") {
				try {
				FileReader in = new FileReader(MANUALMAPPINGSFILE);
				BufferedReader filebuf = new BufferedReader(in);
				String oneLine = filebuf.readLine();
				String string = "";
				while (oneLine!=null) {
					string = string + oneLine + "\n";
					oneLine=filebuf.readLine();
				}
				in.close();
				File result = new File(MANUALMAPPINGSFILECSV);
				FileWriter out = new FileWriter(result);			
				String array[][] = api.StringToArray(string);
				for (int j = 0; j<array.length; j++) {
					out.write(array[j][0]+";"+array[j][1]+";"+array[j][2]+";\n");
				}
				out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
			Align align = new Align();								//creating the new alignment method
			MyOntology ontologies = new MyOntology(ONTOLOGYFILES);	//assigning the ontologies
			if (ontologies.ok == true) {
			ExplicitRelation explicit = new ExplicitRelation(EXPLICITFILE,ontologies);	//assigning pre-known alignments
			Parameter parameter = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFILE,RULESFILE,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,ONTOLOGYFILES);	//assigning the parameters
			parameter.manualmappingsFile = MANUALMAPPINGSFILE;
			align.name = "Application";
			align.ontology = ontologies;	
			align.p = parameter;
			align.explicit = explicit;
			long timestart = System.currentTimeMillis();
			align.align();									//process
			time = System.currentTimeMillis()-timestart;
			
//			Save.saveCompleteEval(align.ontology,align.resultListLatest,MANUALMAPPINGSFILE,align.p.cutoff,RESULTFILE);	//save mappings
//			Save.saveVector(align.alignments, RESULTFILE); 	//align.alignments is a vector, with each element containing entity 1, entity 2, relation, and confidence
			Vector newResult = api.withoutInstances(align.cutoff,ontologies);
			Save.saveVector(newResult, RESULTFILECSV);
			try {
			/*			InputStream inputStream = new FileInputStream(fileName);	
			CSVParser csvparser = new CSVParser(inputStream, "", "", "");
			csvparser.changeDelimiter(';');		
			String array[][] = csvparser.getAllValues();*/
			CSVParse csvParse = new CSVParse(RESULTFILECSV);
			String array[][] = csvParse.getAllValues();
			String inria = api.ArrayToString(array,ontologyurl[0],ontologyurl[1]);
			File resultdir = new File(RESULTDIR);
			resultdir.mkdir();
			File result = new File(RESULTFILE);
			FileOutputStream fos = new  FileOutputStream(result);
			fos.close();
			FileWriter out = new FileWriter(result);
		    out.write(inria);
		    out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
			Evaluation evaluation = new Evaluation(MANUALMAPPINGSFILECSV);
			evaluation.doEvaluation(ontologies,newResult,CUTOFF);
			evaluation.printEvaluation();
			double[] evaluationResults = evaluation.returnEvaluation();
			FileWriter writer = new FileWriter("C:/Work/OAEI/log.txt",true);
			writer.write(ONTOLOGYFILES[0]+";"+ONTOLOGYFILES[1]+";"+RESULTFILE+";");
			for (int l = 0; l<4; l++) {writer.write(String.valueOf(evaluationResults[l])+";");}
			writer.write(String.valueOf(time));
			writer.write("\n");
			writer.close();
			System.out.println("Saved log.txt");
			} catch (Exception e) {UserInterface.errorPrint(e.getMessage());}}
			else {
			try{
			FileWriter writer = new FileWriter("C:/Work/OAEI/log.txt",true);
			writer.write(ONTOLOGYFILES[0]+";"+ONTOLOGYFILES[1]+";"+RESULTFILE+"; ontology error\n");
			writer.close();
			System.out.println("Saved log.txt");
			} catch (Exception e) {UserInterface.errorPrint(e.getMessage());}
			}
//		}
		}
	}

	private static final String ONTOLOGYFILESPATH = "C:/Work/OAEI/";
	private static String[] ONTOLOGYFILES = {"C:/Work/DissData/oaei05/onto101.owl","C:/Work/DissData/oaei05/onto302.owl"}; 	//ontologies		
	private static final String[] BENCHNUMBERS = {"101","102","103","104","201","202","203","204","205",
			"206","207","208","209","210","221","222","223","224",
			"225","228","230","231","232","233","236","237","238",
			"239","240","241","246","247","248","249","250","251",
			"252","253","254","257","258","259","260","261","262",
			"265","266","301","302","303","304"};
//	private static final int MAXEV = 2265;
	private static final String RESULTFILESPATH = "C:/Work/OAEI/results/";
	private static String RESULTFILE = "";	//output
	private static String RESULTFILECSV = "";
	private static String RESULTDIR = "";
	private static String MANUALMAPPINGSFILE = "";		
	
	private static final String EXPLICITFILE = "";				
//	private static final int SCENARIO = Parameter.NOSCENARIO;
//	private static final String CUTOFFFILE = "C:/Work/OAEI/testCut.txt";
	private static final int MAXITERATIONS = 10;		
	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
	private static final boolean EFFICIENTAGENDA = Parameter.COMPLETE;
	private static final int STRATEGY = Parameter.DECISIONTREE;
//	private static final String CLASSIFIERFILE = "C:/Work/DissTraining/tree25a.obj";
	private static final String CLASSIFIERFILE = "C:/Work/OAEI/tree13a.obj";
//	private static final String RULESFILE = "C:/Work/DissTraining/rules.obj";	
	private static final String RULESFILE = "C:/Work/OAEI/rules3.obj";	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final double MAXERROR = 0.8; 
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
	private static final double CUTOFF = 0.8;  //0.25;0.31;0.35(0.7);0.95
//	private static final String QUESTIONFILE = "C:/Work/testQues.txt";
	private static String MANUALMAPPINGSFILECSV = "C:/Work/OAEI/mappings.txt";
	
}
