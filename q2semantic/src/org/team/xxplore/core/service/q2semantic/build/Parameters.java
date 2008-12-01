package org.team.xxplore.core.service.q2semantic.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

public class Parameters {
	
	//singleton
	public static Parameters single = null;
	public static String configFilePath = null;

	public static void setConfigFilePath(String fn) {
		configFilePath = fn;
	}
	public static Parameters getParameters() {
		if(single == null) {
			single = new Parameters(configFilePath);
		}
		return single;
	}
	
	/* Constant */
	public final String CONCEPT = "concept";
	public final String INDIVIDUAL = "individual";
	public final String LITERAL = "literal";
	public final String PROPERTY = "property";
	public final String DATATYPEPROP = "datatypeprop";
	public final String OBJECTPROP = "objectprop";
	public final String RDFSPROP = "rdfsprop";
	public final String SUBCLASS = "subclass";

	public final String TYPE_FIELD = "type";
	public final String LABEL_FIELD =  "label";
	public final String URI_FIELD =  "uri";
	public final String DS_FIELD =  "ds";
	public final String CONCEPT_FIELD = "concept_field";
	public final String ATTRIBUTE_FIELD = "attribute_field";
	public final String ATTRIBUTE_FIELD_URI = "attribute_field_uri";
	public final String LITERAL_FIELD = "literal_field";

	/* size restriction */
	public final int MAX_CACHE_SIZE = 5000000; //cache of indiv2concepts results
	public final long MAX_OBJPROP_FILESIZE = 1*1024*1024*1024; //relation file length restriction for second scan
	public final long MAX_GRAPHEDGE_FILESIZE = 100*1024*1024; //edge file length restriction for third scan
	public final long MIN_GRAPHEDGE_FILESIZE = 1*1024; //edge file length restriction for third scan

	public final double MIN_OBJPROP_SCORE = 0.000001; //score restriction for third scan
	public final double TOP_ELEMENT_SCORE = 2.0;
	public final float concept_boost = 10.0f;
	public final float relation_boost = 5.0f;
	public final float attribute_boost = 5.0f;
	

	/* edge predefinition */
	private final String[] conEdges = {RDF.BAG.stringValue(), RDF.SEQ.stringValue(), RDF.ALT.stringValue(), 
			RDF.LIST.stringValue(), RDF.FIRST.stringValue(), RDF.REST.stringValue(), RDF.NIL.stringValue()};

	private final String[] rdfsEdges = {RDF.TYPE.stringValue(), RDFS.SUBCLASSOF.stringValue(), RDFS.DOMAIN.stringValue(),
			RDFS.RANGE.stringValue(), RDFS.SUBPROPERTYOF.stringValue(), RDFS.LABEL.stringValue(), RDFS.COMMENT.stringValue(),
			RDFS.CLASS.stringValue(), OWL.CLASS.stringValue(), OWL.OBJECTPROPERTY.stringValue(), OWL.DATATYPEPROPERTY.stringValue(),
			RDFS.SEEALSO.stringValue(), RDFS.ISDEFINEDBY.stringValue(), OWL.ONTOLOGY.stringValue(), OWL.ANNOTATIONPROPERTY.stringValue(),
			RDFS.MEMBER.stringValue(), RDFS.CONTAINER.stringValue()};

	private final String[] dsInsNum = {"wordnet=464843", "dblp=1644086", "freebase=7517743", "dbpedia=19238235",
			"geonames=14051039", "uscensus=82702188"};

	public HashSet<String> conEdgeSet;
	public HashSet<String> rdfsEdgeSet;
	public HashMap<String, Integer> instNumMap;

	private Parameters(String fn) {
		loadPara(fn);
		
		conEdgeSet = new HashSet<String>();
		for(String edge: conEdges){
			conEdgeSet.add(edge);
		}
		
		rdfsEdgeSet = new HashSet<String>();
		for(String edge: rdfsEdges){
			rdfsEdgeSet.add(edge);
		}
		instNumMap = new HashMap<String, Integer>();
		for(String dsNum: dsInsNum){
			instNumMap.put(dsNum.substring(0, dsNum.indexOf('=')), Integer.valueOf(dsNum.substring(dsNum.indexOf('=')+1)));
		}
	}
	
	/* para load */
	public String root;
	public String datasource;
	public String indexRoot;
	public String source;
	public String summaryObj;
	public String summaryRDF;
	public String keywordIndex;
	public String blankNodeFile;
	public String noBlankNodeFile;
	public String conceptFile;
	public String relationFile;
	public String attributeFile;
	public String literalOut;
	public String litAttrOut;
	public String graphEdgePool;
	public String objPropPool;
	public String conceptCountObj;
	public String resultFile;
	
	private void loadPara(String fn) {
		try {
			Properties prop = new Properties();
			InputStream is = new FileInputStream(fn);
			prop.load(is);
			root = prop.getProperty("root")+File.separator;
			datasource = prop.getProperty("domain");
			indexRoot = root+datasource;
			source = prop.getProperty("source");//absolute path
			System.out.println("Root:"+root+"\r\nDataSource:"+datasource+"\r\nIndexRoot:"+indexRoot+"\r\nFileSource:"+source);
			is.close();
			/* Output File Para */
			summaryObj = root + datasource + "-summary.obj";
			summaryRDF = root + datasource + "-summary.rdf";
			keywordIndex = root +datasource + "-keywordIndex";
			blankNodeFile = root + datasource + ".blanknode";
			noBlankNodeFile = root + datasource + "-noblanknode.nt";
			conceptFile = root + datasource + "-concept.txt";
			relationFile = root + datasource + "-relation.txt";
			attributeFile = root + datasource + "-attribute.txt";
			literalOut = root + datasource + "-literal.txt";
			litAttrOut = root + datasource + "-statement.txt";
			graphEdgePool =  root + datasource + "-graphEdgePool";
			objPropPool =  root + datasource + "-objPropPool";
			conceptCountObj =  root + datasource + "-conceptCount.obj";
			resultFile =  root + datasource + "-result.txt";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
