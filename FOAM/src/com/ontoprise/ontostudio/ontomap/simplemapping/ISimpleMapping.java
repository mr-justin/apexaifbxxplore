/* 
 * Created on 12.04.2005
 * Created by Mika Maier-Collin 
 *
 * Function: 
 * Keywords: 
 * 
 * Copyright (c) 2005 ontoprise GmbH.
 */
package com.ontoprise.ontostudio.ontomap.simplemapping;


/**
 * @author Mika Maier-Collin
 */
public interface ISimpleMapping {
    
    /**
     * sets the id of the ontology the mapping source is from
     * @param ontology
     */
    void setSourceOntology(String ontology);

    /**
     * gets the id of the ontology the mapping source is from
     * @return
     */
    String getSourceOntology();

    /**
     * sets the id of the mapping source
     * @param source
     */
    void setSource(String source);

    /**
     * gets the id of the mapping source
     * @return
     */
    String getSource();
    
    /**
     * sets the id of the ontology the mapping target is from
     * @param ontology
     */
    void setTargetOntology(String ontology);
    
    /**
     * gets the id of the ontology the mapping target is from
     * @return
     */
    String getTargetOntology();

    /**
     * sets the id of the mapping target
     * @param target
     */
    void setTarget(String target);

    /**
     * gets the id of the mapping target
     * @return
     */
    String getTarget();
    
    /**
     * sets the probability of the mapping, between 0 and 1
     * @param prob
     */
    void setProbability(double prob);
    
    /**
     * gets the probability of the mapping
     * @return
     */
    double getProbability();
}
