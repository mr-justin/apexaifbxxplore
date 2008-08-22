package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.util.UserInterface;

public class MappingComputationService {
	
	private static String[] ONTOLOGIES = {"D:/BTC/schema.rdf","D:/BTC/swrc.owl"};   //ontologies 
	private static String[] DATASOURCES = {"schema.rdf","swrc.owl"};
	private static String OUTPUTDIR  = "D:/BTC/sampling/mappingResult";
	
	private String ontology1;
	private String ontology2;
	private String datasource1;
	private String datasource2;
	private String resultFile;
	
	private static final String EXPLICITFILE = "";				
//	private static final int SCENARIO = Parameter.NOSCENARIO;
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
	private static final double CUTOFF = 0.9;  //0.25;0.31;0.35(0.7);0.9(0.95)
	private static final String MANUALMAPPINGSFILE = "D:/BTC/sampling/mapping/MapCSV.txt";	
	
	
	/**
	 * This is just a general testing method.
	 * @param args
	 */
	public static void main(String[] args) {
		MappingComputationService service = new MappingComputationService(ONTOLOGIES, DATASOURCES, OUTPUTDIR);
		service.computeMappings();
	}
	
	public MappingComputationService(String[] ontologies, String[] datasources, String outputDir) {
		this.ontology1 = ontologies[0];
		this.ontology2 = ontologies[1];
		this.datasource1 = datasources[0];
		this.datasource2 = datasources[1];
		this.resultFile = outputDir.endsWith(File.separator) ? 
				outputDir + fsTransduceUri(datasource1) + "+" + fsTransduceUri(datasource2) : 
				outputDir + File.separator + fsTransduceUri(datasource1) + "+" + fsTransduceUri(datasource2)
				+ ".mapping";
 	}
	
	public MappingComputationService(String ontology1, String ontology2, String datasource1, String datasource2, String outputDir) {
		this.ontology1 = ontology1;
		this.ontology2 = ontology2;
		this.datasource1 = datasource1;
		this.datasource2 = datasource2;
		this.resultFile = outputDir.endsWith(File.separator) ? 
				outputDir + fsTransduceUri(datasource1) + "+" + fsTransduceUri(datasource2) : 
				outputDir + File.separator + fsTransduceUri(datasource1) + "+" + fsTransduceUri(datasource2)
				+ ".mapping";
	}
	
	public void computeMappings() {
		checkFile(resultFile);
		saveDatasources(resultFile);
		align(new String[]{ontology1,ontology2}, resultFile);
	}
	
	public void align(String[] ontologyFiles, String resultFile) {
		Align align = new Align();								//creating the new alignment method
		MyOntology ontologies = new MyOntology(ontologyFiles);	//assigning the ontologies
		if (ontologies.ok == false) {System.exit(1);}
		ExplicitRelation explicit = new ExplicitRelation(EXPLICITFILE,ontologies);	//assigning pre-known alignments
//		Parameter parameter = new Parameter(Parameter.NOSCENARIO,ONTOLOGYFILES); 
		Parameter parameter = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFILE,RULESFILE,SEMI,MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,ontologyFiles);	//assigning the parameters
		parameter.manualmappingsFile = MANUALMAPPINGSFILE;
		align.name = "Application";
		align.ontology = ontologies;	
		align.p = parameter;
		align.explicit = explicit;
		align.align();									//process
		saveVector(align.cutoff, resultFile);
		Evaluation evaluation = new Evaluation(MANUALMAPPINGSFILE);
		evaluation.doEvaluation(align.ontology,align.resultListLatest,align.p.cutoff);
		evaluation.printEvaluation();
	}
	
	public void saveVector(Vector vector, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
			Iterator iter = vector.iterator();
			while (iter.hasNext()) {
				String[] element = (String[]) iter.next();
				writer.write(element[0]+";"+element[1]+";"+element[2]+"\n");
			}
			writer.close();
			UserInterface.print("Saved "+fileName+"\n");
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}
	
	public void saveDatasources(String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(datasource1 + ";" + datasource2);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void checkFile(String filename) {
		File results = new File(filename);
		if(!results.exists()){
			results.getParentFile().mkdirs();
			try {
				results.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 
	
	private static String fsTransduceUri(String uri) {

		uri = StringUtils.replace(uri, ":", "COLON");
		uri = StringUtils.replace(uri, "/", "SLASH");
		uri = StringUtils.replace(uri, "#", "SHARP");

		return uri;
	}

}
