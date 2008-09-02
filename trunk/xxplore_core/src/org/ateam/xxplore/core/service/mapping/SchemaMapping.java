package org.ateam.xxplore.core.service.mapping;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;

public class SchemaMapping extends Mapping{
	public SchemaMapping(String source, String target, String sourceDsURI, String targetDsUri, double confidence) { 
		setSource(source);
		setTarget(target);
		setConfidence(confidence);
		setSourceDs(sourceDsURI);
		setTargetDsURI(targetDsUri);
	}

	public String toString(){
		return getSourceDsURI()+getTargetDsURI();
	}
}

