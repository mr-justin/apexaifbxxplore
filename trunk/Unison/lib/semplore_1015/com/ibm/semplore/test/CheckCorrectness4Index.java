/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestSearchForDBpedia.java,v 1.6 2007/05/12 07:39:25 lql Exp $
 */
package com.ibm.semplore.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.impl.URI_ID_Dictionary;
import com.ibm.semplore.imports.impl.Util4NT;
import com.ibm.semplore.model.CatRelConstraint;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.search.SchemaQuery;
import com.ibm.semplore.search.SchemaSearchable;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.XFacetedResultSpec;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.XFacetedSearchable;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.IndexFactory;
import com.ibm.semplore.xir.IndexReader;
import com.ibm.semplore.xir.IndexService;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.IndexFactoryImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author liu qiaoling
 *
 */
public abstract class CheckCorrectness4Index
{	
	public static URI_ID_Dictionary dic;
    public static int requiredNumberOfResults = 1000;
    
    public static int FACET_THRESHOLD = 20;
    
    public static int showResults = 1;
    
    public static boolean quit;
    
    public static SchemaSearchable schemaSearcher;
    
    public static XFacetedSearchable xfacetedSearcher;
    public static IndexReader insIndexReader; 
    public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();
    public static IndexFactory indexFactory = IndexFactoryImpl.getInstance();
    
    public static SearchHelper searchHelper;
    
    public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
    public static TermFactory termFactory = TermFactoryImpl.getInstance();
    
    public static boolean print = false;

    public static void printStream(DocStream stream) {
    	print = true;
    	try {
    		stream.init();
    		for (int i=0; i<stream.getLen(); i++,stream.next())
    			System.out.println(stream.doc());
    	} catch (Exception e) {
    		
    	}
    	
    }
    public static boolean matchWithFile(String mes, DocStream stream, String file) {
    	int  tol = 0;
    	try {
	    	BufferedReader in = new BufferedReader(new FileReader(file));
	    	String tmp = null;
	    	stream.init();
	    	while ((tmp=in.readLine())!=null) {
	    		if (stream.doc()!=Integer.valueOf(tmp)) {
	    			System.out.println("mismatch in file "+file+" at line "+(tol+1)+" //"+mes);
	    			in.close();
	    			if (!print)
	    				printStream((DocStream)stream.clone());
	    			return false;
	    		}
	    		stream.next();
	    		tol++;
	    	}
	    	in.close();
    	} catch (Exception e) {
    	}
    	if (tol!=stream.getLen()) {
			System.out.println("mismatch in file "+file+" at line "+(tol+1)+" //"+mes);
			if (!print)
				printStream((DocStream)stream.clone());
			return false;
    	}
    	return true;
    }
    public static void showRelFacets(PrintStream out) throws Exception {
        DocStream relStream = insIndexReader.getDocStream(termFactory.createTermForRootRelations());
        relStream.init();
        SchemaObjectInfo[] rels = schemaSearcher.getRootRelations();
        int tol_wrong =0;
        int tol = 0;
        out.println("relation facets:");
        for (int i=0; i<rels.length; i++,relStream.next()) {
        	int id2 = dic.getID(Util4NT.RELATION, rels[i].getURI());
	    	
            out.println("relFacet "+i+": "+rels[i].getLabel()+" ("+rels[i].getURI()+") from id "+relStream.doc()+", from file "+id2);
            //check objects of a relation
            {
            	DocStream CobjStream = insIndexReader.getDocStream(termFactory.createTermForObjects(
            			schemaFactory.createRelation(rels[i].getURI())));
                        	
//            	try {
//       			 ResultSet resultSet = searchInstance(schemaFactory.createCatRelGraph()
//    					 .add(schemaFactory.createUniversalCategory())
//    					 .add(schemaFactory.createUniversalCategory())
//    					 .add(schemaFactory.createRelation(rels[i].getURI())
//    					 , 0, 1)
//    					 ,1);
//    			 result = resultSet.getLength();
//            	} catch (Exception e) {
//            	}
			 
		    	//get correct answer for object
		    	String file = Config.dir+"/"+"O_"+id2;
		    	if (!matchWithFile("objects", CobjStream, file))
		    		tol_wrong++;
		    	tol++;
            }

            //check subject
		    	{
	            	DocStream CsubjStream = insIndexReader.getDocStream(termFactory.createTermForSubjects(
	            			schemaFactory.createRelation(rels[i].getURI())));
		    		
//			 ResultSet resultSet = searchInstance(schemaFactory.createCatRelGraph()
//					 .add(schemaFactory.createUniversalCategory())
//					 .add(schemaFactory.createUniversalCategory())
//					 .add(schemaFactory.createRelation(rels[i].getURI())
//					 , 0, 1)
//					 ,0);
			 
		    	//get correct answer for object
		    	String file = Config.dir+"/"+"S_"+id2;
		    	if (!matchWithFile("subjects",CsubjStream,file))
		    		tol_wrong++;
		    	tol++;
		    	}
        }
        out.println();
        
        //check types
        {
    		DocStream catDoc = insIndexReader.getDocStream(termFactory.createTermForRootCategories());
	    	String file = Config.dir+"/"+"categoryTemp"; 		 
    		if (!matchWithFile("categories",catDoc,file))
	    		tol_wrong++;
	    	tol++;
        }
        
        //check relations
        {
    		DocStream relDoc = insIndexReader.getDocStream(termFactory.createTermForRootRelations());
	    	String file = Config.dir+"/"+"relationTemp"; 		 
    		if (!matchWithFile("relations",relDoc,file))
	    		tol_wrong++;
	    	tol++;
        }        
        
        System.out.println("total wrong: "+tol_wrong+", total right: "+(tol-tol_wrong)+", wrong rate: "+tol_wrong*1.0/tol);
    }
    
//    public static void showAttrFacets() throws Exception {
//        SchemaObjectInfo[] attrs = schemaSearcher.getAttributes();
//        System.out.println("attribute facets:");
//        for (int i=0; i<attrs.length; i++) {
//            System.out.println("attrFacet "+i+": "+attrs[i].getLabel());
//        }
//        System.out.println();
//    }
    
//    public static void searchInstance(GeneralCategory cat) throws Exception {
//        ResultSet resultSet = searchInstance(schemaFactory.createCategoryRelationExp().append(cat), 0);
//        if (searchHelper != null) {
////            searchHelper.setHint(SearchHelper.CATEGORY_CACHE_HINT, cat, searchFactory.createCacheHint(resultSet));
////            searchHelper.setHint(SearchHelper.START_CACHE_HINT, null, searchFactory.createCacheHint(resultSet));
//        }
//    }
    
    public static ResultSet searchInstance(CatRelConstraint exp, int target) throws Exception {
//        System.out.println("query expression: "+exp.toString());
        XFacetedQuery xfacetedQ = searchFactory.createXFacetedQuery();
        xfacetedQ.setQueryConstraint(exp);
        XFacetedResultSpec resultSpec = searchFactory.createXFacetedResultSpec();
        resultSpec.setCategoryFacetsInfo(false, null);
        resultSpec.setRelationFacetsInfo(false, null);
//        resultSpec.setCategoryFacetsInfo(false, schemaSearcher.getRootCategories());
        resultSpec.setRelationFacetsInfo(false, schemaSearcher.getRootRelations());
        xfacetedQ.setResultSpec(resultSpec);
        xfacetedQ.setSearchTarget(target);
//        xfacetedQ.setRequestedNumberOfResults(requiredNumberOfResults);

        XFacetedResultSet resultSet = null;
    	for (int i=0; i<1; i++) {
	        resultSet = xfacetedSearcher.search(xfacetedQ, searchHelper);
    	}
        return resultSet;
    }
                
	public static void main(String[] args) {
		try {
			for (int ii = 0; ii < args.length; ii++) {
				Properties config = Config.readConfigFile(args[ii]);
				XFacetedSearchService searchService = searchFactory
						.getXFacetedSearchService(config);
				IndexService indexService = indexFactory.getIndexService(config);
				insIndexReader = indexService.getIndexReader(IndexService.IndexType.Instance);
		        
				schemaSearcher = searchService.getSchemaSearchable();
//				xfacetedSearcher = searchService.getXFacetedSearchable();

				// category facets
//				showCatFacets(System.out);

				// relation facets
				Config.dir = config.getProperty(Config.DATA_FOR_INDEX_DIR);
				Config.BSDDir = config.getProperty(Config.TMP_DIR);
				dic = new URI_ID_Dictionary(new File(Config.BSDDir));
				dic.init();
				checkID();
				showRelFacets(System.out);
				dic.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void checkID() {
        try {
			DocStream catStream = insIndexReader.getDocStream(termFactory.createTermForRootCategories());
			catStream.init();
			System.out.println("total cats: "+catStream.getLen()+" start id: "+catStream.doc());
			
	        DocStream relStream = insIndexReader.getDocStream(termFactory.createTermForRootRelations());
	        relStream.init();
	        System.out.println("total rels: "+relStream.getLen()+" start id: "+relStream.doc());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
