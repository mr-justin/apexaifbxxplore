/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestSearchForSample.java,v 1.3 2007/05/07 04:11:33 lql Exp $
 */
package com.ibm.semplore.test;

import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.search.SchemaQuery;
import com.ibm.semplore.search.XFacetedSearchService;

/**
 * @author liu qiaoling
 * 
 */
public class TestSearchForSample extends TestSearch {
	public static void main(String[] args) {
		try {
			for (int ii = 0; ii < args.length; ii++) {
				Properties config = Config.readConfigFile(args[ii]);
				XFacetedSearchService searchService = searchFactory
						.getXFacetedSearchService(config);
				schemaSearcher = searchService.getSchemaSearchable();
				xfacetedSearcher = searchService.getXFacetedSearchable();

				// category facets
//				showCatFacets(System.out);

				// relation facets
				showRelFacets(System.out);

				boolean quit = false;

				searchHelper = searchFactory.createSearchHelper();

				while (!quit) {

//					searchSchema(searchFactory.createSchemaQuery().set(
//							SchemaQuery.TYPE_CATEGORY, "Raj"));
//					
//					searchSchema(searchFactory.createSchemaQuery().set(
//							SchemaQuery.TYPE_CATEGORY, "author"));
//
//					 searchInstance(schemaFactory.createCatRelGraph()
//							 .add(schemaFactory.createCategory("<TOP>"))
//							 ,0);
//
//					 searchInstance(schemaFactory.createCatRelGraph()
//					 .add(schemaFactory.createKeywordCategory("author"))
//					 ,0);
//					
//					 searchInstance(schemaFactory.createCatRelGraph()
//					 .add(schemaFactory.createUniversalCategory())
//					 .add(schemaFactory.createUniversalCategory())
//					 .add(schemaFactory.createRelation("<origin>")
//					 , 0, 1)
//					 ,1);

					 
					 
							// //q5: find singers who are also actor and director
					// searchInstance(schemaFactory.createCatRelGraph()
					// .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
					// , 1, 0)
					// .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
					// , 2, 0)
					// .add(schemaFactory.createRelation("http://dbpedia.org/property/Artist")
					// , 3, 0)
					// ,3);

					 //q4: find films directed by American directors and Chinese actors
//					 searchInstance(schemaFactory.createCatRelGraph()
//					 .add(schemaFactory.createUniversalCategory())
//					 .add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___American_film_directors"))
//					 .add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Chinese_actors"))
//					 .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//					 , 0, 1)
//					 .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//					 , 0, 2)
//					 ,0);

					// //q3: find American directors that who have directed
					// films about 'war'
					// searchInstance(schemaFactory.createCatRelGraph()
					// .add(schemaFactory.createKeywordCategory("war"))
					// // .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createCategory("American_film_directors"))
					// .add(schemaFactory.createRelation("director")
					// , 0, 1)
					// ,0);

					// q2: find comedy films which are acted by James Bond
					// actors
					// searchInstance(schemaFactory.createCatRelGraph()
					// //
					// .add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Comedy_films"))
					// .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
					// .addComponentCategory(schemaFactory.createKeywordCategory("james"))
					// .addComponentCategory(schemaFactory.createKeywordCategory("bond")))
					// .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
					// , 0, 1)
					// ,1);

					// q1: find films about "world war"
					// searchInstance(schemaFactory.createCatRelGraph()
					// //
					// .add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Comedy_films"))
					// .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
					// .addComponentCategory(schemaFactory.createKeywordCategory("world"))
					// .addComponentCategory(schemaFactory.createKeywordCategory("war")))
					// .add(schemaFactory.createUniversalCategory())
					// .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
					// , 0, 1)
					// ,0);

					quit = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
