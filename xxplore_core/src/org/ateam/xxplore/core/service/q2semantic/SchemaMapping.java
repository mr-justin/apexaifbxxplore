package org.ateam.xxplore.core.service.q2semantic;


public class SchemaMapping extends Mapping{
	public static final String SEPERATOR = "_";
	public SchemaMapping(String source, String target, String sourceDsURI, String targetDsUri, double confidence) { 
		setSource(source);
		setTarget(target);
		setConfidence(confidence);
		setSourceDs(sourceDsURI);
		setTargetDsURI(targetDsUri);
	}

	public String toString(){
		return getSource()+SEPERATOR+getTarget()+SEPERATOR+getSourceDsURI()+
		SEPERATOR+getTargetDsURI()+SEPERATOR+getConfidence();
	}
	
	public static SchemaMapping getMappingFromString(String mapping){
		String source = mapping.substring(0,mapping.indexOf(SEPERATOR));
		mapping = mapping.substring(mapping.indexOf(SEPERATOR), mapping.length());
		
		String targtet = mapping.substring(0,mapping.indexOf(SEPERATOR));
		mapping = mapping.substring(mapping.indexOf(SEPERATOR), mapping.length());
		
		String sourceDS = mapping.substring(0,mapping.indexOf(SEPERATOR));
		mapping = mapping.substring(mapping.indexOf(SEPERATOR), mapping.length());
		
		String targtetDS = mapping.substring(0,mapping.indexOf(SEPERATOR));
		
		String conf = mapping.substring(mapping.indexOf(SEPERATOR), mapping.length());
		
		double confValue = Double.valueOf(conf);
		
		return new SchemaMapping(source, targtet, sourceDS, targtetDS, confValue);
		
	}
}

