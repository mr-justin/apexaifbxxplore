/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import java.io.IOException;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaObjectInfo;

/**
 * This is the search interface to the schema to get information about the categories, attributes and relations, and also to perform queries over them.
 * @author liu Qiaoling
 *	
 */
public interface SchemaSearchable extends Searchable {
	
	/**
	 * Returns all the subcategories of a given category.
	 * @param parentCat
	 * @return
	 */
	public SchemaObjectInfo[] getSubCategories(Category parentCat) throws IOException;
	
    /**
     * Returns all the supercategories of a given category.
     * @param cat
     * @return
     * @throws IOException
     */
    public SchemaObjectInfo[] getSuperCategoryPath(Category cat) throws IOException;

    /**
	 * Returns all the subrelations of a given relation.
	 * @param parentRel
	 * @return
	 */
	public SchemaObjectInfo[] getSubRelations(Relation parentRel) throws IOException;
	
    /**
     * Returns all the superrelations of a given relation.
     * @param rel
     * @return
     * @throws IOException
     */
    public SchemaObjectInfo[] getSuperRelationPath(Relation rel) throws IOException;

    /**
	 * Returns all the root categories in the ontology, those that have no non-trivial parents.
	 * @return
	 */
	public SchemaObjectInfo[] getRootCategories() throws IOException;
	
	/**
	 * Returns all the root relations in the ontology, those that have no non-trivial parents.
	 * @return
	 */
	public SchemaObjectInfo[] getRootRelations() throws IOException;
	
	/**
	 * Returns all the attributes in the ontology.
	 * @return
	 */
	public SchemaObjectInfo[] getAttributes() throws IOException;
    
	/**
	 * Runs a schema query and returns a set of results. This method never returns null. If there are no matching results, then the method ResultSet.getLength() will return 0.
	 * @param schemaQuery
	 * @return
	 */
	public ResultSet search(SchemaQuery schemaQuery) throws Exception;

}
