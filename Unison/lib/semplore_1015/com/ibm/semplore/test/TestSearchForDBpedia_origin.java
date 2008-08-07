/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestSearchForYagodbpediaNI_revised.java,v 1.1 2007/05/20 09:10:18 bly Exp $
 */
package com.ibm.semplore.test;

import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.search.XFacetedSearchService;

/**
 * @author liu qiaoling
 *
 */
public class TestSearchForDBpedia_origin extends TestSearch
{    
        
    public static void main(String[] args) {
        try {
            for (int ii=0; ii<args.length; ii++) {
                Properties config = Config.readConfigFile(args[ii]);
                XFacetedSearchService searchService = searchFactory.getXFacetedSearchService(config);
                schemaSearcher = searchService.getSchemaSearchable();
                xfacetedSearcher = searchService.getXFacetedSearchable();
                
//                PrintStream out = new PrintStream("facets_dbpedia_origin.txt");              
//                showCatFacets(out);                
//                showRelFacets(out);    
                
                boolean quit = false;
       
                searchHelper = searchFactory.createSearchHelper();
                
                    
                
                              
                while (!quit) {
                    for (int jj=2; jj<3; jj++) {
                    	if (jj==2)
                    		System.out.println("===========jj=3============");
                    	
                    	//test
                        searchInstance(schemaFactory.createCatRelGraph()
                      		  .add(schemaFactory.createUniversalCategory())
                      		  .add(schemaFactory.createUniversalCategory())
                                .add(schemaFactory.createRelation("<http://dbpedia.org/property/starring>")//"<http://dbpedia.org/property/rated>")
                                 , 0, 1)
                         ,1);
                    	
                    	                    	
//                	  query set 1
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "Chinese"));
//
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "North America"));
//                                        
//                      searchSchema(searchFactory.createSchemaQuery().
//                      set(SchemaQuery.TYPE_CATEGORY, "European Union"));
//                    	
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "Wikipedia"));
//                                        
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "world war"));
//
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "Academy Award"));
//
//                      searchSchema(searchFactory.createSchemaQuery().
//                      set(SchemaQuery.TYPE_CATEGORY, "actors"));
//                                  
//                  searchSchema(searchFactory.createSchemaQuery().
//                  set(SchemaQuery.TYPE_CATEGORY, "documentary"));
//
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "players"));
//
//                    searchSchema(searchFactory.createSchemaQuery().
//                            set(SchemaQuery.TYPE_CATEGORY, "books"));

                	
                	//query set 2
                    //find documentary films about "world war"
//                  searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    				.addComponentCategory(schemaFactory.createCategory("<http://dbpedia.org/resource/Category:Documentary_films>"))
//                    				.addComponentCategory(schemaFactory.createKeywordCategory("war"))
//                    				.addComponentCategory(schemaFactory.createKeywordCategory("world")))
//                        ,0);
//                  
//                      searchInstance(schemaFactory.createCatRelGraph()
//                      		.add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                      				.addComponentCategory(schemaFactory.createKeywordCategory("nazi"))
//                      				.addComponentCategory(schemaFactory.createCategory("http://wordnet/book_106013091")))
//                          ,0);      
//              
//                      searchInstance(schemaFactory.createCatRelGraph()
//                        		.add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        				.addComponentCategory(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_OR)
//                                				.addComponentCategory(schemaFactory.createKeywordCategory("artist"))
//                                				.addComponentCategory(schemaFactory.createKeywordCategory("musician")))
//                        				.addComponentCategory(schemaFactory.createCategory("http://wordnet/biography_106113482")))
//                            ,0);      
//                
//                  searchInstance(schemaFactory.createCatRelGraph()
//                          .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("bodybuilder"))
//                        		  .addComponentCategory(schemaFactory.createCategory("http://dbpedia.org/resource/Category___American_film_actors")))
//                        ,0);                  
//                	
//                  searchInstance(schemaFactory.createCatRelGraph()
//                          .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        		  .addComponentCategory(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Chinese_actors"))
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("martial"))
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("arts")))
//                        ,0);                  
//                	
//                      searchInstance(schemaFactory.createCatRelGraph()
//                      .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/singer_109908715"))
//                    		  .addComponentCategory(schemaFactory.createKeywordCategory("queen"))
//                    		  .addComponentCategory(schemaFactory.createKeywordCategory("pop")))
//                      ,0);                  
//            	                    	
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("quantum"))
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("computer"))
//                            		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/scientist_109871938")))
//                              ,0);                  
//                    	
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("desert"))
//                            		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/country_108023668")))
//                              ,0);                  
//                    	
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("drug"))
//                            		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/runner_109855621")))
//                              ,0);                  
//                    	
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("award"))
//                            		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/director_109374434"))
//                            		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/actor_109145973"))
//                            		  .addComponentCategory(schemaFactory.createCategory("http://wordnet/singer_109908715")))
//                              ,0);                  
                    	
                    	
                	//query set 3
//                    	//find films about "terrorism" or "terrorist" directed by American film directors
//                  searchInstance(schemaFactory.createCatRelGraph()
//                		.add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_OR)
//                				.addComponentCategory(schemaFactory.createKeywordCategory("terrorism"))
//                				.addComponentCategory(schemaFactory.createKeywordCategory("terrorist")))
//                        .add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___American_film_directors"))
//                        .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                , 0, 1)
//                        ,0);
//                    
//                //find films reaching 'Academy Award' and starring a 'James Bond' actor
//                searchInstance(schemaFactory.createCatRelGraph()
//                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("academy"))
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("award")))
//                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                      		  .addComponentCategory(schemaFactory.createKeywordCategory("james"))
//                      		  .addComponentCategory(schemaFactory.createKeywordCategory("bond")))
//                        .add(schemaFactory.createRelation("http://dbpedia.org/property/star")
//                                , 0, 1)
//                        ,1);
//                                  	
//                    	//find directors that have directed "harry potter" films
//              searchInstance(schemaFactory.createCatRelGraph()
//              		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                      		.addComponentCategory(schemaFactory.createKeywordCategory("harry"))
//                      		.addComponentCategory(schemaFactory.createKeywordCategory("potter")))
//                        .add(schemaFactory.createUniversalCategory())
//                        .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                , 0, 1)
//                        ,1);
//                    	
//                    	//find scientists reaching nobel prize whose students also have reached nobel prize
//                      searchInstance(schemaFactory.createCatRelGraph()
//                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                       		 .addComponentCategory(schemaFactory.createCategory("http://wordnet/scientist_109871938"))
//                       		 .addComponentCategory(schemaFactory.createKeywordCategory("nobel")))
//                         .add(schemaFactory.createKeywordCategory("nobel"))
//                         .add(schemaFactory.createRelation("http://dbpedia.org/property/doctoraladvisor")
//                                 , 0, 1)
//                         ,1);
//   
                    	//find films starring American actors who are related to "bodybuilder"
//                      searchInstance(schemaFactory.createCatRelGraph()
//                    		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    				  .addComponentCategory(schemaFactory.createKeywordCategory("bodybuilder"))
//                    				  .addComponentCategory(schemaFactory.createCategory("<http://dbpedia.org/resource/Category:American_film_actors>")))
//                    		  .add(schemaFactory.createUniversalCategory())
//                              .add(schemaFactory.createRelation("<http://dbpedia.org/property/starring>")
//                               , 1, 0)
//                       ,0);
//                    	
//                    	//find products developed by developers related to "ibm"
//                      searchInstance(schemaFactory.createCatRelGraph()
//                      .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    		  .addComponentCategory(schemaFactory.createKeywordCategory("ibm")))
//                      .add(schemaFactory.createUniversalCategory())
//                      .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//                               , 1, 0)
//                      ,1);                  
//            	
//                    	//find products designed by people reaching "turing award"
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("turing"))
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("award")))
//                              .add(schemaFactory.createUniversalCategory())
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/design")
//                                       , 1, 0)
//                              ,1);                  
//                    	
//                    	//find albums of artists who are regarded as queen of rock and roll
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("queen"))
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("rock"))
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("roll")))
//                              .add(schemaFactory.createUniversalCategory())
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                                       , 1, 0)
//                              ,1);                  
//
//                  	//find volcanos with last eruption in 2006
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("volcano")))
//                              .add(schemaFactory.createKeywordCategory("2006"))
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/lasteruption")
//                                       , 0, 1)
//                              ,0);      
//                    	
//                    	//find nba teams whose owner reaches "forbes"
//                      searchInstance(schemaFactory.createCatRelGraph()
//                    		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    				  .addComponentCategory(schemaFactory.createKeywordCategory("forbes")))
//                    		  .add(schemaFactory.createKeywordCategory("nba"))
//               				  .add(schemaFactory.createRelation("http://dbpedia.org/property/owner")
//               						  , 1, 0)
//                              ,1);      
//
//                  	//find developers of mp3 player
//                    searchInstance(schemaFactory.createCatRelGraph()
//                  		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                  				  .addComponentCategory(schemaFactory.createKeywordCategory("mp3"))
//                  				  .addComponentCategory(schemaFactory.createKeywordCategory("player")))
//                  		  .add(schemaFactory.createUniversalCategory())
//             				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//             						  , 0, 1)
//                            ,1);
//                    	
//                      	//find writers who wrote some works about assassination 
//                        searchInstance(schemaFactory.createCatRelGraph()
//                        	  .add(schemaFactory.createKeywordCategory("assassination"))
//                        	  .add(schemaFactory.createUniversalCategory())
//                 			  .add(schemaFactory.createRelation("http://dbpedia.org/property/writer")
//                 						  , 0, 1)
//                                ,1);
//                        
//                      	//find races in game "warcraft" 
//                        searchInstance(schemaFactory.createCatRelGraph()
//                        	  .add(schemaFactory.createKeywordCategory("Warcraft"))
//                        	  .add(schemaFactory.createUniversalCategory())
//                 			  .add(schemaFactory.createRelation("http://dbpedia.org/property/race")
//                 						  , 0, 1)
//                                ,0);                        
//                        
//                    	//find directors who have directed films about "AIDS"
//                      searchInstance(schemaFactory.createCatRelGraph()
//                      .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//          				  .addComponentCategory(schemaFactory.createKeywordCategory("AIDS")))
//                  	  .add(schemaFactory.createUniversalCategory())
//           			  .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//           						  , 0, 1)
//                          ,1);
//                                        
//
//                    	//find artists that origin from New York City and have "hip hop" albums                    	
//                      searchInstance(schemaFactory.createCatRelGraph()
//                      .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    		  .addComponentCategory(schemaFactory.createKeywordCategory("hip"))
//                    		  .addComponentCategory(schemaFactory.createKeywordCategory("hop")))
//                      .add(schemaFactory.createUniversalCategory())
//                      .add(schemaFactory.createEnumerationCategory()
//                    		  .addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/New_York_City")))
//                      .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                              , 0, 1)
//                      .add(schemaFactory.createRelation("http://dbpedia.org/property/origin")
//                              , 1, 2)
//                      ,1);
//
//                    	//find writers who have written songs for beatles
//                      searchInstance(schemaFactory.createCatRelGraph()
//                  		  .add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createKeywordCategory("beatles"))
//                  		  .add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                                    , 0, 1)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/writer")
//                                    , 0, 2)
//                            ,2);
//      
//                    	//find football players that have played in barca and milan 
//                      searchInstance(schemaFactory.createCatRelGraph()
//                    		  .add(schemaFactory.createCategory("http://wordnet/player_109762180"))
//                    		  .add(schemaFactory.createKeywordCategory("milan"))
//                    		  .add(schemaFactory.createEnumerationCategory()
//                    				  .addInstanceElement(schemaFactory.createInstance("http://dbpedia.org/resource/FC_Barcelona")))
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/club")
//                               , 0, 1)
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/club")
//                               , 0, 2)
//                       ,1);
//                    	
//                  	//find products influenced by designs of people reaching "turing award"
//                      searchInstance(schemaFactory.createCatRelGraph()
//                              .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("turing"))
//                            		  .addComponentCategory(schemaFactory.createKeywordCategory("award")))
//                              .add(schemaFactory.createUniversalCategory())
//                              .add(schemaFactory.createUniversalCategory())
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/design")
//                                       , 1, 0)
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/influences")
//                                       , 1, 2)
//                              ,2);                  
//                    	
//                  	//find mp3 players with gnu license that could run on Linux operating system 
//                      searchInstance(schemaFactory.createCatRelGraph()
//                      	  .add(schemaFactory.createKeywordCategory("mp3"))
//                   		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    				  .addComponentCategory(schemaFactory.createKeywordCategory("gnu")))
//                   		  .add(schemaFactory.createKeywordCategory("Linux"))
//               			  .add(schemaFactory.createRelation("http://dbpedia.org/property/license")
//               						  , 0, 1)
//               			  .add(schemaFactory.createRelation("http://dbpedia.org/property/operatingsystem")
//               						  , 0, 2)
//                              ,0);
//                      
                      	//find hurricanes that affected "America" and happened in "2006" related hurricane seasons
//                        searchInstance(schemaFactory.createCatRelGraph()
//                        .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("hurricane")))
//                    	  .add(schemaFactory.createKeywordCategory("2005"))
//                    	  .add(schemaFactory.createKeywordCategory("America"))
//             			  .add(schemaFactory.createRelation("<http://dbpedia.org/property/hurricaneSeason>")
//             						  , 0, 1)
//             			  .add(schemaFactory.createRelation("<http://dbpedia.org/property/areasAffected>")
//             						  , 0, 2)
//                            ,1);
                        	
                    	
                    
//                	//query set 4                	
//                    	//find games that could run on "xbox","mobile", and "pc" consoles 
//                        searchInstance(schemaFactory.createCatRelGraph()
//                    		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                    				  .addComponentCategory(schemaFactory.createKeywordCategory("xbox")))
//                          	  .add(schemaFactory.createKeywordCategory("mobile"))
//                          	  .add(schemaFactory.createKeywordCategory("pc"))
//                          	  .add(schemaFactory.createUniversalCategory())
//                   			  .add(schemaFactory.createRelation("http://dbpedia.org/property/platform")
//                   						  , 3, 0)
//                   			  .add(schemaFactory.createRelation("http://dbpedia.org/property/platform")
//                   						  , 3, 1)
//                   			  .add(schemaFactory.createRelation("http://dbpedia.org/property/platform")
//                   						  , 3, 2)
//                                  ,3);
//                          
//                  	searchInstance(schemaFactory.createCatRelGraph() 
//                  			.add(schemaFactory.createKeywordCategory("director"))
//                    		.add(schemaFactory.createKeywordCategory("romantic"))
//                    		.add(schemaFactory.createKeywordCategory("war"))
//                    		.add(schemaFactory.createCategory("<http://dbpedia.org/resource/Category:Best_Actress_Academy_Award_winners>"))
//                  			.add(schemaFactory.createCategory("<http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners>"))                  			                  			                  			
//                    		.add(schemaFactory.createRelation("<http://dbpedia.org/property/director>")
//                            		, 1, 0)
//                            .add(schemaFactory.createRelation("<http://dbpedia.org/property/director>")
//                            		, 2, 0)
//                  			.add(schemaFactory.createRelation("<http://dbpedia.org/property/starring>")
//                  					, 1, 3)
//                  			.add(schemaFactory.createRelation("<http://dbpedia.org/property/starring>")
//                  					, 1, 4)                            
//                    ,0);
//                    	
//                    //find "fifa" related football players who have played for Italian, English, and Spanish clubs
//               		searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createKeywordCategory("fifa"))
//                    		.add(schemaFactory.createKeywordCategory("Spanish"))
//                    		.add(schemaFactory.createKeywordCategory("English"))
//                    		.add(schemaFactory.createKeywordCategory("Italian"))
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/club")
//                                    , 0, 1)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/club")
//                                    , 0, 2)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/club")
//                                    , 0, 3)
//                            ,0);
//                  	
//                    //find artist who have albums of "hip hop", "rock", "pop", and "folk"
//               		searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createUniversalCategory())
//                            .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("hip"))
//                        		  .addComponentCategory(schemaFactory.createKeywordCategory("hop")))
//                    		.add(schemaFactory.createKeywordCategory("rock"))
//                    		.add(schemaFactory.createKeywordCategory("folk"))
//                    		.add(schemaFactory.createKeywordCategory("pop"))
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                                    , 1, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                                    , 2, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                                    , 3, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/artist")
//                                    , 4, 0)
//                            ,0);
//                                      
//               		//find directors who have directed "comedy", "horror", and "action" films 
//               		searchInstance(schemaFactory.createCatRelGraph()
//                    		.add(schemaFactory.createUniversalCategory())
//                    		.add(schemaFactory.createKeywordCategory("comedy"))
//                    		.add(schemaFactory.createKeywordCategory("horror"))
//                    		.add(schemaFactory.createKeywordCategory("action"))
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 1, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 2, 0)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                                    , 3, 0)
//                            ,0);
//                              
//                    	//find "romantic" films directed by Best Actor Academy Award Winners and star Best Actor Academy Award Winners and Best Actress Academy Award Winners
//                  	searchInstance(schemaFactory.createCatRelGraph()
//                  			.add(schemaFactory.createKeywordCategory("romantic"))
//                  			.add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Best_Actress_Academy_Award_winners"))
//                  			.add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Best_Actor_Academy_Award_winners"))
//                  			.add(schemaFactory.createCategory("http://dbpedia.org/resource/Category___Best_Director_Academy_Award_winners"))
//                  			.add(schemaFactory.createRelation("http://dbpedia.org/property/star")
//                  					, 0, 2)
//                  			.add(schemaFactory.createRelation("http://dbpedia.org/property/star")
//                  					, 0, 1)
//                            .add(schemaFactory.createRelation("http://dbpedia.org/property/director")
//                            		, 0, 3)
//                    		,0);            	
//                                        
//                    	//find developers that have developed "audio","video", and "image" products
//                      searchInstance(schemaFactory.createCatRelGraph()
//              		  .add(schemaFactory.createUniversalCategory())
//            		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("audio")))
//            		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("image")))
//            		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("video")))
//       				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//       						  , 1, 0)
//       				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//       						  , 2, 0)
//       				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//       						  , 3, 0)
//                      ,0);
//                    
//                    find developers that have developed "audio","video", and "image" products
//                      searchInstance(schemaFactory.createCatRelGraph()
//              		  .add(schemaFactory.createUniversalCategory())
//            		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("audio")))
//            		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("image")))
//            		  .add(schemaFactory.createCompoundCategory(CompoundCategory.TYPE_AND)
//            				  .addComponentCategory(schemaFactory.createKeywordCategory("video")))
//       				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//       						  , 1, 0)
//       				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//       						  , 2, 0)
//       				  .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//       						  , 3, 0)
//                      ,0);
                    
//                  find "football team" which played in season "2005" and has a "English" manager
//             		searchInstance(schemaFactory.createCatRelGraph()
//                  		  .add(schemaFactory.createKeywordCategory("team"))
//                  		  .add(schemaFactory.createKeywordCategory("football"))                  		                   	
//                  		  .add(schemaFactory.createKeywordCategory("2005"))
//                  		  .add(schemaFactory.createKeywordCategory("English"))
//                  		  .add(schemaFactory.createRelation("http://dbpedia.org/property/league")
//                                  , 0, 1)
//                          .add(schemaFactory.createRelation("http://dbpedia.org/property/season")
//                                  , 0, 2)
//                          .add(schemaFactory.createRelation("http://dbpedia.org/property/manager")
//                                  , 0, 3)
//                          ,0);
//                      
//                    
                    
//                  Find "strategy" genre "pc" "game" which has "American" publisher
//                    searchInstance(schemaFactory.createCatRelGraph()
//                    		  .add(schemaFactory.createKeywordCategory("pc"))
//                    		  .add(schemaFactory.createKeywordCategory("game"))                  		                     		
//                    		  .add(schemaFactory.createKeywordCategory("American"))
//                    		  .add(schemaFactory.createKeywordCategory("strategy"))                    		                      		   
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/developer")
//                                    , 0, 1)
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/publisher")
//                                    , 0, 2)
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/genre")
//                                    , 0, 3)
//                            ,0);
                    
//                    Find comic work whose creator, publisher and illustrator are Japanese.
//                    searchInstance(schemaFactory.createCatRelGraph()
//                    		  .add(schemaFactory.createKeywordCategory("comic"))
//                    		  .add(schemaFactory.createKeywordCategory("Japanese"))                    		  
//                    		  .add(schemaFactory.createKeywordCategory("Japanese"))
//                    		  .add(schemaFactory.createKeywordCategory("Japanese"))                    		  
////                    		  .add(schemaFactory.createKeywordCategory("strategy"))                    		                      		   
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/creator")
//                                    , 0, 1)
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/publisher")
//                                    , 0, 2)
//                              .add(schemaFactory.createRelation("http://dbpedia.org/property/illustrator")
//                                    , 0, 3)
//                            ,0);
                    quit = true;
                    }
                }
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
