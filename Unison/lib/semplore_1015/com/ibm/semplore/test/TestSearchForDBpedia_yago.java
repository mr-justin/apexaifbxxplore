/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestSearchForDBpedia.java,v 1.6 2007/05/12 07:39:25 lql Exp $
 */
package com.ibm.semplore.test;

import java.io.PrintStream;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.search.XFacetedSearchService;

/**
 * @author liu qiaoling
 *
 */
public class TestSearchForDBpedia_yago extends TestSearch
{
    public static void main(String[] args) {
        try {
            for (int ii=0; ii<args.length; ii++) {
                Properties config = Config.readConfigFile(args[ii]);
                XFacetedSearchService searchService = searchFactory.getXFacetedSearchService(config);
                schemaSearcher = searchService.getSchemaSearchable();
                xfacetedSearcher = searchService.getXFacetedSearchable();
                
                PrintStream out = new PrintStream("facets_dbpedia_yago.txt");
                //category facets
                showCatFacets(out);
                
                //relation facets
                showRelFacets(out);
                
                out.close();
                
                boolean quit = false;
       
                searchHelper = searchFactory.createSearchHelper();
                
                while (!quit) {
                	//test
//                    searchInstance(schemaFactory.createCatRelGraph()
//                            .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
////                            		.addComponentCategory(schemaFactory.createCategory("<http://dbpedia.org/class/yago/Person100007846>"))
//                            		.addComponentCategory(schemaFactory.createKeywordCategory("james bond")))
//                    		.add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createRelation("<http://dbpedia.org/property/director>")
//                                    , 0, 1)
//                            ,0);

                	
                	
                	//query set 1
//                  searchInstance(schemaFactory.createCatRelGraph()
//            		.add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				.addComponentCategory(schemaFactory.createCategory("<http://dbpedia.org/class/yago/Physicist110428004>"))
//            				.addComponentCategory(schemaFactory.createKeywordCategory("relativity")))//"quantum"
//                ,0);          
//                    
//                    searchInstance(schemaFactory.createCatRelGraph()
//                          .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        		  .addComponentCategory(schemaFactory.createCategory("<http://dbpedia.org/class/yago/City108524735>"))
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("earthquake")))
//                    		,0);
//
//                    searchInstance(schemaFactory.createCatRelGraph()
//                            .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                          		  .addComponentCategory(schemaFactory.createCategory("<http://dbpedia.org/class/yago/Company108058098>"))
//                          		  .addComponentCategory(schemaFactory.createKeywordCategory("computer game")))
//                      		,0);
                	
                	//query set 2                    
                  //find directors that who have directed 'James Bond' films
                  searchInstance(schemaFactory.createCatRelGraph()
                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
                        		.addComponentCategory(schemaFactory.createKeywordCategory("james bond")))
                		.add(schemaFactory.createUniversalCategory())
                        .add(schemaFactory.createRelation("<http://dbpedia.org/property/starring>")
                                , 1, 0)
                        ,0);

//                    searchInstance(schemaFactory.createCatRelGraph()
//                            .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("AIDS")))
//                      		.add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createRelation("<http://dbpedia.org/property/director>")
//                                    , 0, 1)
//                            ,0);
//                                      	
//                    searchInstance(schemaFactory.createCatRelGraph()
//                            .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("beatles")))
//                      		.add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createRelation("<http://dbpedia.org/property/artist>")
//                                    , 0, 1)
//                            ,1);
//                                      	
//                //find films reaching 'Academy Award' and starring a 'James Bond' actor
//                searchInstance(schemaFactory.createCatRelGraph()
//                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("academy award")))
//                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                      		  .addComponentCategory(schemaFactory.createKeywordCategory("james bond")))
//                        .add(schemaFactory.createRelation("<http://dbpedia.org/property/starring>")
//                                , 0, 1)
//                        ,0);
//                                  	
//                  searchInstance(schemaFactory.createCatRelGraph()
//                          .add(schemaFactory.createKeywordCategory("Forbes"))
//                  		.add(schemaFactory.createKeywordCategory("nba"))
//                          .add(schemaFactory.createRelation("<http://dbpedia.org/property/owner>")
//                                  , 1, 0)
//                          ,0);
                                 
                  
                  
//                	//query set 4                	
//                    //find films directed by A and starring B and C
//               		searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createUniversalCategory())
//                    		.add(schemaFactory.createEnumerationCategory()
//                    				.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Zhang_Yimou")))
//                            .add(schemaFactory.createEnumerationCategory()
//                            		.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Gong_Li")))
//                            .add(schemaFactory.createEnumerationCategory()
//                            		.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Zhang_Ziyi")))
////                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
////                                    , 0, 2)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 0, 1)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//                                    , 0, 3)
//                            ,0);
//                                      
//               		//find actors who have cooperation with director A and actor B
//               		searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createUniversalCategory())
//                    		.add(schemaFactory.createUniversalCategory())
//                    		.add(schemaFactory.createEnumerationCategory()
//                    				.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Zhang_Yimou")))
//                            .add(schemaFactory.createEnumerationCategory()
//                            		.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Zhang_Ziyi")))
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//                                    , 0, 1)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 0, 2)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//                                    , 0, 3)
//                            ,1);
//                                        
//               		//find directors who have cooperation with actor A and actor B
//               		searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createUniversalCategory())
//                    		.add(schemaFactory.createUniversalCategory())
//                    		.add(schemaFactory.createEnumerationCategory()
//                    				.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Steven_Spielberg")))
//                            .add(schemaFactory.createEnumerationCategory()
//                            		.addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/Tom_Hanks")))//Richard_Dreyfuss//Tom_Cruise
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//                                    , 0, 1)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 0, 2)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//                                    , 0, 3)
//                            ,1);
                                        
//                    //q5: find singers who are also actor and director
//                    searchInstance(schemaFactory.createCatRelGraph()
//                            .add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/starring")
//                                    , 1, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 2, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/Artist")
//                                    , 3, 0)
//                            ,3);
                    
                    
                    
                    quit = true;
                }
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
