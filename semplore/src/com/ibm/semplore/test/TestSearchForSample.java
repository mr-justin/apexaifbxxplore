/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestSearchForSample.java,v 1.5 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.test;

import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

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
				searcher = searchService.getXFacetedSearchable();

				// category facets
				showCatFacets(System.out);

				// relation facets
				showRelFacets(System.out);

				boolean quit = false;

				searchHelper = searchFactory.createSearchHelper();

				while (!quit) {

					//query1: find all the instances.
					 searchInstance(schemaFactory.createCatRelGraph()
							 .add(schemaFactory.createCategory(Md5_BloomFilter_64bit.URItoID("<TOP>")))
							 ,0);

					 //query2: find all the instances of category <American_film_directors>. 
					 searchInstance(schemaFactory.createCatRelGraph()
							 .add(schemaFactory.createCategory(Md5_BloomFilter_64bit.URItoID("<American_film_directors>")))
							 ,0);

					 //query3: find all the instances related to keyword "tom".
					 searchInstance(schemaFactory.createCatRelGraph()
					 .add(schemaFactory.createAttributeKeywordCategory("<http://purl.org/dc/elements/1.1/identifier>", "http://research.microsoft.com/users/lamport/tla/book.html"))
					 ,0);
					
					 //query4: find all the films that are directed by American film directors named "Tom".
					 searchInstance(schemaFactory.createCatRelGraph()
					 .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//							 .addComponentCategory(schemaFactory.createKeywordCategory("Tom"))
							 .addComponentCategory(schemaFactory.createCategory(Md5_BloomFilter_64bit.URItoID("<American_film_directors>"))))
					 .add(schemaFactory.createUniversalCategory())
					 .add(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID("<cinematography>"))
					 , 1, 0)
					 ,0);


					quit = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
