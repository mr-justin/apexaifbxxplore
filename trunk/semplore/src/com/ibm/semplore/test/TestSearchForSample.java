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
			{
				Properties config = Config.readConfigFile(args[0]);
				XFacetedSearchService searchService = searchFactory
						.getXFacetedSearchService(config);
				schemaSearcher = searchService.getSchemaSearchable();
				searcher = searchService.getXFacetedSearchable();

				// category facets
//				showCatFacets(System.out);

				// relation facets
//				showRelFacets(System.out);

				boolean quit = false;

				searchHelper = searchFactory.createSearchHelper();

				while (!quit) {

	/*				//query1: find all the instances.				
					
					 searchInstance(schemaFactory.createCatRelGraph()
							 .add(schemaFactory.createEnumerationCategory().addInstanceElement(schemaFactory.createInstance(Md5_BloomFilter_64bit.URItoID(args[1]))))
							 ,0);

					 //query2: find all the instances of category <American_film_directors>. 
					 searchInstance(schemaFactory.createCatRelGraph()
							 .add(schemaFactory.createCategory(new Long(args[2]).longValue()))
							 ,0);

					 //query3: find all the instances related to keyword "tom".
					 searchInstance(schemaFactory.createCatRelGraph()
					 .add(schemaFactory.createAttributeKeywordCategory("<http://www.w3.org/2000/01/rdf-schema#label>", "Letter to the Editor"))
					 ,0);
				*/	
					
					 //query3: find all the instances related to keyword "tom".
					 searchInstance(schemaFactory.createCatRelGraph()
					 .add(schemaFactory.createKeywordCategory("paris"))
					 ,0);
					 //query4: find all the films that are directed by American film directors named "Tom".
					 searchInstance(schemaFactory.createCatRelGraph()
					 .add(schemaFactory.createKeywordCategory("paris"))
					 .add(schemaFactory.createUniversalCategory())
					 .add(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID("<http://www.w3.org/2006/03/wn/wn20/schema/similarTo>"))
					 , 0, 1)
					 ,0);


					quit = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
