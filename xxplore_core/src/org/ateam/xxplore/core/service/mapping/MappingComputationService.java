package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.commons.lang.StringUtils;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;
import edu.unika.aifb.foam.result.Evaluation;
import edu.unika.aifb.foam.util.UserInterface;

public class MappingComputationService {

	private String explicitFile = "";

//	private static final int SCENARIO = Parameter.NOSCENARIO;
	private static final int MAXITERATIONS = 5;		
	private static final boolean INTERNALTOO = Parameter.EXTERNAL;	
	private static final boolean EFFICIENTAGENDA = Parameter.COMPLETE;
	private static final int STRATEGY = Parameter.DECISIONTREE;
	private static final String CLASSIFIERFILE = "res/tree.obj";
	private static final String RULESFILE = "res/rules.obj";	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final double MAXERROR = 0.9; 
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;	
	private static final double CUTOFF = 0.8;  //0.25;0.31;0.35(0.7);0.9(0.95)
	private static final String MANUALMAPPINGSFILE = "";	


	public MappingComputationService(){}

	public Collection<SchemaMapping> computeSchemaMappings(String ontology1, String ontology2){
		Collection<SchemaMapping> results = new ArrayList<SchemaMapping>();
		MyOntology ontologies = new MyOntology(new String[]{ontology1,ontology2});
		Vector mappings = align(ontologies, ontology1, ontology2, null);
		Iterator iter = mappings.iterator();
		while(iter.hasNext()) {
			String[] element = (String[]) iter.next();
			results.add(new SchemaMapping(element[0], element[1], ontology1, ontology2, (Double.valueOf(element[2])).doubleValue()));
		}		
		
		return results;
	}
	
	public Collection<SchemaMapping> computeSchemaMappings(MyOntology onto, String onto1, String onto2){
		Collection<SchemaMapping> results = new ArrayList<SchemaMapping>();
		Vector mappings = align(onto, onto1, onto2, null);
		Iterator iter = mappings.iterator();
		while(iter.hasNext()) {
			String[] element = (String[]) iter.next();
			results.add(new SchemaMapping(element[0], element[1], onto1, onto2, (Double.valueOf(element[2])).doubleValue()));
		}		
		
		return results;
	}
	
	public Collection<InstanceMapping> computeInstanceMappings(String onto1, String onto2, SchemaMapping mapping){
		Emergency.checkPrecondition(mapping != null, "mapping != null");
		Collection<InstanceMapping> results = new ArrayList<InstanceMapping>();
		MyOntology ontologies = new MyOntology(new String[]{onto1,onto2});
		Vector mappings = align(ontologies, onto1, onto2, null);
		Iterator iter = mappings.iterator();
		while(iter.hasNext()) {
			String[] element = (String[]) iter.next();
			results.add(new InstanceMapping(element[0], element[1], onto1, onto2, mapping, (Double.valueOf(element[2])).doubleValue()));
		}		
		
		return results;
	}
	
	public Collection<InstanceMapping> computeInstanceMappings(MyOntology onto, String onto1, String onto2, SchemaMapping mapping){
		Emergency.checkPrecondition(mapping != null, "mapping != null");
		Collection<InstanceMapping> results = new ArrayList<InstanceMapping>();
		Vector mappings = align(onto, onto1, onto2, null);
		Iterator iter = mappings.iterator();
		while(iter.hasNext()) {
			String[] element = (String[]) iter.next();
			results.add(new InstanceMapping(element[0], element[1], onto1, onto2, mapping, (Double.valueOf(element[2])).doubleValue()));
		}		
		return results;
	}


	public void computeMappings(String ontology1, String ontology2, String resultFile) {
		checkFile(resultFile);
		saveDatasources(ontology1, ontology2, resultFile);
		MyOntology ontologies = new MyOntology(new String[]{ontology1,ontology2});
		align(ontologies, ontology1, ontology2, resultFile);
	}

	private Vector align(MyOntology ontologies, String onto1, String onto2, String resultFile){
		Align align = new Align();								//creating the new alignment method

		if (ontologies.ok == false) {System.exit(1);}
		ExplicitRelation explicit = new ExplicitRelation(explicitFile,ontologies);	//assigning pre-known alignments
//		Parameter parameter = new Parameter(Parameter.NOSCENARIO,ONTOLOGYFILES); 
		Parameter parameter = new Parameter(MAXITERATIONS,STRATEGY,INTERNALTOO,EFFICIENTAGENDA,CLASSIFIERFILE,RULESFILE,SEMI,
				MAXERROR,NUMBERQUESTIONS,REMOVEDOUBLES,CUTOFF,new String[] {onto1, onto2});	//assigning the parameters
		parameter.manualmappingsFile = MANUALMAPPINGSFILE;
		align.name = "Application";
		align.ontology = ontologies;	
		align.p = parameter;
		align.explicit = explicit;
		align.align();									//process

		Vector mappings = null;
		mappings = align.cutoff;
		
		if (resultFile != null) saveVector(mappings, resultFile);
		
		return mappings;
		
//		Evaluation evaluation = new Evaluation(resultFile);
//		evaluation.doEvaluation(align.ontology,align.resultListLatest,align.p.cutoff);
//		evaluation.printEvaluation();
	}

	private void saveVector(Vector vector, String fileName) {
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

	public void saveDatasources(String ontology1, String ontology2, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(fsTransduceUri(ontology1) + ";" + fsTransduceUri(ontology2));
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
