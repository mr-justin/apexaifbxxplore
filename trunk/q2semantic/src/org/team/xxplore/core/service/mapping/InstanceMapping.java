package org.team.xxplore.core.service.mapping;



public class InstanceMapping extends Mapping{

	SchemaMapping m_mapping; 

	public InstanceMapping(String source, String target, String sourceDsURI, String targetDsUri, SchemaMapping mapping, double confidence) {
		setSource(source);
		setTarget(target);
		setConfidence(confidence);
		setSourceDs(sourceDsURI);
		setTargetDsURI(targetDsUri);
		m_mapping = mapping;
	}

	public SchemaMapping getMapping(){
		return m_mapping;
	}
}
