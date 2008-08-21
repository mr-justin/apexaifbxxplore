/*
 * Created on 01.03.2005
 *
 */
package edu.unika.aifb.foam.main;

import java.io.FileWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * @author meh
 *
 */
public class WebService {

	private static final String[] ONTOLOGYFILES = {"http://www.aifb.uni-karlsruhe.de/WBS/meh/foam/ontologies/animalsA.owl","http://www.aifb.uni-karlsruhe.de/WBS/meh/foam/ontologies/animalsB.owl"}; 	//ontologies
	private static final String EXPLICITFILE = "";				
	private static final int SCENARIO = Parameter.NOSCENARIO;
//	private static final int MAXITERATIONS = 20;		
//	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
//	private static final boolean EFFICIENTAGENDA = Parameter.COMPLETE;
//	private static final int STRATEGY = Parameter.DECISIONTREE;
	private static final String CLASSIFIERFILE = "tree.obj";
	private static final String RULESFILE = "rules.obj";	
//	private static final boolean SEMI = Parameter.SEMIAUTOMATIC;
//	private static final double MAXERROR = 0.95;
//	private static final int NUMBERQUESTIONS = 5;
//	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
//	private static final double CUTOFF = 0.95;
	private static final String MANUALMAPPINGSFILE = "http://www.aifb.uni-karlsruhe.de/WBS/meh/foam/ontologies/animalsABMap.txt";
	private static final String LOGFILE = "foamLogfile.txt";
	private static final String LOCALPATH = "C:/FOAM/";
//	private static final String LOCALPATH = "C:/Programme/Apache Software Foundation/Tomcat 5.5/webapps/foam/";
	
	public static String alignService(String ontology1, String ontology2, String referenceAlignment, String email, String localPath) {
//	public static String alignService(String ontology1, String ontology2, String referenceAlignment, String email) {
//		String localPath = LOCALPATH;
		String logFile = localPath+LOGFILE;
		String classifierFile = localPath+CLASSIFIERFILE;
		String rulesFile = localPath+RULESFILE;
		String theResult = new String();
		boolean error = true;
		double[] evaluationResults = {0.0,0.0,0.0};
		UserInterface.useOutput = false;
		try{
		Align align = new Align();						//creating the new alignment method
		theResult = theResult+"Alignment of <br>"+ontology1+" "+ontology2+"<br><br>";
		String[] ontologyfiles = new String[2];
		ontologyfiles[0] = ontology1;
		ontologyfiles[1] = ontology2;
		MyOntology ontologies = new MyOntology(ontologyfiles);	//assigning the ontologies
		if (ontologies.ok == true) {
		ExplicitRelation explicit = new ExplicitRelation(EXPLICITFILE,ontologies);	//assigning pre-known alignments
		Parameter parameter = new Parameter(SCENARIO,ontologyfiles); 
//		Parameter parameter = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,classifierFile,rulesFile,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,ontologyfiles);	//assigning the parameters
		parameter.semi = Parameter.FULLAUTOMATIC;				//not every strategy fits the web service
		if (parameter.maxiterations>20) {						// some parts have to be set manually
			parameter.maxiterations = 20;
		}
		parameter.classifierFile = classifierFile;
		parameter.rulesFile = rulesFile;
//		if (parameter.timeest<10000) { 
		align.name = "Alignment_"+ontology1+"_"+ontology2;
		align.ontology = ontologies;	
		align.p = parameter;
		align.explicit = explicit;
		align.align();									//process
		Vector vector = align.alignments;
		theResult = theResult + "<h3>Alignments:</h3> <table border=\"1\"> <tr> <th>entity 1</th><th>entity 2</th><th>degree of similarity</th></tr>";
		Iterator iter = vector.iterator();
		while (iter.hasNext()) {
			String[] element = (String[]) iter.next();
			theResult = theResult + "<tr><td>"+element[0]+"</td><td>"+element[1]+"</td><td>"+element[2]+"</td></tr>";
		}
		theResult = theResult + "</table>"; 
		if ((referenceAlignment.equals("http://")||referenceAlignment.equals(""))==false) {
		theResult = theResult + "<h3>Evaluation Measures:</h3> <table border=\"1\"> <tr> <th></th><th>precision</th><th>recall</th><th>f-measure</th></tr>";
		Evaluation evaluation = new Evaluation(referenceAlignment);
		evaluation.doEvaluation(align.ontology,align.resultListLatest,align.p.cutoff);
		evaluationResults = evaluation.returnEvaluation();
		theResult = theResult+
			"<tr><td>Point of threshold (0.9) </td><td>"+evaluationResults[0]+"</td><td>"+evaluationResults[1]+"</td><td>"+evaluationResults[2]+"</td></tr>"+
			"<tr><td>Point of maximum precision (ex post) </td><td>"+evaluationResults[4]+"</td><td>"+evaluationResults[5]+"</td><td>"+evaluationResults[6]+"</td></tr>"+
			"<tr><td>Point of maximum f-measure (ex post) </td><td>"+evaluationResults[8]+"</td><td>"+evaluationResults[9]+"</td><td>"+evaluationResults[10]+"</td></tr>"+
			"<tr><td>Point of maximum recall (ex post) </td><td>"+evaluationResults[16]+"</td><td>"+evaluationResults[17]+"</td><td>"+evaluationResults[18]+"</td></tr>";
		theResult = theResult+"</table>";}
		} else {
			theResult = theResult+"Error, could not read ontologies. Please ensure they are codified in OWL-Lite or OWL-DLP.<br>";
		}
		error = false;
		} catch (Exception e) {
			theResult = theResult+"<br>Sorry, an error occurred.<br>" +
					UserInterface.errorBuffer+"<br>Notice sent to Administrator.";
			UserInterface.errorBuffer = "";}
		try {
		FileWriter writer = new FileWriter(logFile,true);
		writer.write(error+";"+Calendar.getInstance().getTime().toString()+";"+ontology1+";"+ontology2+";"+referenceAlignment+";"+evaluationResults[0]+";"+evaluationResults[1]+";"+evaluationResults[2]+"\n");
		writer.close();
		} catch (Exception e) {}
		return theResult;
	}
	
	public static void main (String args[]) {
		String result = alignService(ONTOLOGYFILES[0],ONTOLOGYFILES[1],MANUALMAPPINGSFILE,"",LOCALPATH);
//		String result = alignService(ONTOLOGYFILES[0],ONTOLOGYFILES[1],MANUALMAPPINGSFILE,"");
		result = result.replaceAll("<br>","\n");
		result = result.replaceAll("</tr>","\n");
		System.out.println(result);
	}
	
}
