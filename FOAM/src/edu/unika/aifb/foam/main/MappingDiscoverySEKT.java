/*
 * Created on 14.04.2005
 *
 */
package edu.unika.aifb.foam.main;

import java.util.Set;

/**
 * This is the standard interface to discover mappings within the SEKT
 * Mediation workpackage (WP4). It will be part of the general 
 * mediation framework. 
 * 
 * @author Marc Ehrig
 * 
 */
public interface MappingDiscoverySEKT {

	/**
	 * Initializes the files required by the alignment algorithm. 
	 * @param classifierFile the file containing the classification information
	 * @param rulesFile the file containing the similarity rules which have to be processed
	 */
	public void initialize(String classifierFile, String rulesFile);
	
	/**
	 * Determines the mappings based on an alignment algorithm.
	 * @param fileNameOntology1 the (temp) file location of the source ontology
	 * @param fileNameOntology2 the (temp) file location of the target ontology
	 * @param mappingObjectsInput a set of already known mappings (ISimpleMapping) 
	 * @return the set of the result mappings (ISimpleMapping), including the confidence value 
	 */
	public Set results(String fileNameOntology1, String fileNameOntology2, Set mappingObjectsInput);
	
	/**
	 * Accesses the logged messages during processing. 
	 * @return normal and error messages created during the process
	 */
	public String messages();

}
