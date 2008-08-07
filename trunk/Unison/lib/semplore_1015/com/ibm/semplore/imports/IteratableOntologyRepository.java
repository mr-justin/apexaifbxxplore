/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IteratableOntologyRepository.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.imports;

/**
 * an OntologyRepository that can produce iterators.
 * 
 * @author zhangjie
 *
 */
public interface IteratableOntologyRepository extends OntologyRepository,
		IteratorFactory {

}
