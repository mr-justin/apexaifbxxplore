package org.xmedia.oms.model.owl.api;

import java.util.Map;
import java.util.Set;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IOntology;


public interface IOWLEntity extends IEntity{
	
    public Map<IAnnotationProperty,Set<Object>> getAnnotationValues(IOntology ontology);

    public Set<Object> getAnnotationValues(IOntology ontology, IAnnotationProperty annotationProperty);
    
    public Object getAnnotationValue(IOntology ontology, IAnnotationProperty annotationProperty);


}
