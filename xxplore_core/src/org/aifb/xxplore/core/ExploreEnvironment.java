package org.aifb.xxplore.core;

import java.util.Map;

import org.aifb.xxplore.core.service.labelizer.OntologyConcept;
import org.aifb.xxplore.core.service.labelizer.Labelizer.CNode;
import org.aifb.xxplore.core.service.labelizer.Labelizer.REdge;
import org.jgrapht.graph.SimpleGraph;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.impl.Datatype;

public class ExploreEnvironment {

	/********** Eclipse related contants  *****************************/
	public static final String XXPLORE_NATURE = ".XXploreNature";
	
	public static final String NAME_QUALIFIER = "XXplore";
	
	public static final String DATASOURCE_LOCALNAME = "xxplore.datasource";
	
	public static final String VIEWDEFINITION_LOCALNAME = "xxplore.view_definition";

	public static final String DATASOURCE = "datasource";
	
	public static final String DATASOURCE_FILENAME = "datasource.filename";
			
	public static final String PROJECT_WIZARD_DESC = "This Wizard is used to create an xxplore project!";
	
	public static final String PROJECT_WIZARD_MSG = "Creation of an Exploration Project!";
	
	public static final String PROJECT_WIZARD_PAGE1_TITLE = "Specify Explore Project Data!"; 
	
	public static final String CONCEPTUAL_ZOOMING_PERFORMED = "Zooming performed!"; 
	
	public static final String ITEM_ACTIVATED_PERFORMED = "Item activated performed!"; 
	
	public static final String ITEM_PRESSED = "Item pressed"; 
	
	/************ constants use for setting up prefuse data model *****/
	public static final String LABEL = "label";
	
	public static final int MAX_LABEL_LENGTH = 1000;
		
	public static final String URI = "uri";
	
	public static final String OID = "oid";
	
	public static final String RESOURCE = "resource";
	
	public static final String SUBCLASS_OF = "is_a";
	
	public static final String SUBCLASS_OF_URI = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	
	public static final String IS_INSTANCE_OF ="instance_of";
	
	public static final String IS_INSTANCE_OF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
	public static final String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";
	
	public static final String OWL_NOTHING = "http://www.w3.org/2002/07/owl#Nothing";
	
	public static final IDatatype DEFAULT_DATATYPE = new Datatype("http://www.w3.org/2001/XMLSchema#string");
	
	public static final String DEFAULT_LITERAL_LANGUAGE = "en";
	
	public static final String IMAGE = "image";
	
	public static final String MODEL_DEFINITION_LABEL = "View Definition";
	
	public static final String PREDICATE_LABEL = "Predicate";
	
	public static final String SUBJECT_LABEL = "Subject";
	
	public static final String OBJECT_LABEL = "Object";
	
	/*
	 * Please add approriate variables (in Ecplipse: Window > Preferences > Java > Build Path > Classpath Variables)
	 */
	public static final String DOC_INDEX_DIR = System.getenv("workspace")+"\\XXplore_core\\res\\doc_index";
		
	public static final String KB_INDEX_DIR = System.getenv("workspace")+"\\XXplore_core\\res\\kb_index";
	
	public static final String SYN_INDEX_DIR = System.getenv("workspace")+"\\XXplore_core\\res\\syn_index";
	
	public static final String SYN_DIR = System.getenv("workspace")+"\\XXplore_core\\res\\wn_s.pl";
		
	public static final String DOC_DIR = System.getenv("workspace")+"\\XXplore_core\\res\\docs";
	
	public static final String STOREDQUERY_DIR = System.getenv("workspace")+"\\XXplore_core\\res\\StoredQueries.xml";
	
	/*
	 * 
	 */
	
	public static final int DEFAULT_WIDTH_FOR_TERMINTERPRETATION = 2;
	
	public static final int DEFAULT_DEPTH_FOR_TERMINTERPRETATION = 2;
	
	//the max number of individuals to be traverse given keyword is a concept or a property 
	public static final int MAX_NO_OF_TRAVERSALS = 2;
	
	public static final String IS_EXPANDED = "is_expanded";

	public static final String IS_LABEL_VISIBLE = "is_label_visible";
	
	public static int HISTORY_LENGTH = 3;
	
	public static final String POLICY_ONTOLOGY_URI = "policy.ontology.uri";
	
	public static final String BASE_POLICY_ONTOLOGY_URI = "policy.base_ontology.uri";
	
	public static final String ADAPTATION_ONTOLOGY_URI = "adaptation.ontology.uri";

	public static final String BASE_ADAPTATION_ONTOLOGY_URI = "adaptation.base_ontology.uri";
	
	public static Map<String, OntologyConcept> MIDDLE_LAYER;
	
	/******* CONSTANTS FOR SELECTION EVENTS ON BUTTONS **************/
	public static final int XXPLORE = 1;
	
	public static final int CLEAR = 2;
	
	public static final int ADD = 3;

//	public static final int SEARCH = 4;
	
	public static final int STORE = 5;
	
	public static final int META_SEARCH = 6;
	
	public static final int F_SEARCH = 7;
	
	public static final int D_SEARCH = 8;

	public static SimpleGraph<CNode,REdge> INDEX_GRAPH;
	
	//CONSTANTS FOR LOADING THE DATA TO THE REPO > USED IN EXPLORE-PLUGIN
	
	public static final String  ONTOLOGY_FILE_PATH = "ontology.file.path";
	
	public static final String  ONTOLOGY_FILE_NAME = "ontology.file.name";
	
	public static final String  BASE_ONTOLOGY_URI = "base_ontology.uri";
	
	public static final String  LANGUAGE = "language";
	
}
