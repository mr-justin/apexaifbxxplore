/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: Config.java,v 1.7 2008/09/07 05:28:58 lql Exp $
 */
package com.ibm.semplore.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

/**
 * @author liu qiaoling
 *
 */
public class Config
{
//	public static final String TYPE = "type"; //this property is to set the URI denoting type, e.g. "<http://www.w3.org/2004/02/skos/core#subject>", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	public static final String TMP_DIR = "tmp_dir"; 
	public static final String DATA_FOR_INDEX_DIR = "data_for_index_dir"; 
	public static final String NT_DIR = "nt_dir"; 
	public static final String STATIC_SCORE_FILE = "static_score_file";
	public static String BSDDir = null; //"E:/User/AllisQM/Semplore/data/dbpedia origin/tmp";
	public static String dir = null; //"E:/User/AllisQM/Semplore/data/dbpedia origin/DataForIndex/";
	public static final String cat = "catID";
	public static final String rel = "SRO";
    public static final String invRel = "ORS";
    public static final String relLocal = "relation";
    public static final String catLocal = "category";
    public static final String att = "att";
    public static final String hashdata = "hashdata.sort";
    /**
     * the index service implementation
     */
    public static final String INDEX_SERVICE = "index_service"; 

    /**
     * memory size limit 
     */
    public static final String MEMORY_SIZE = "memory_size";
    
    /**
     * index path
     */
    public static final String INDEX_PATH = "index_path";
    
    /**
     * result path
     */
    public static final String RESULT_PATH = "result_path";
        
    /**
     * label of the schema object 
     */
    public static final String LABEL = "label";
    
    /**
     * summary of the schema object
     */
    public static final String SUMMARY = "summary";
    
    /**
     * 
     */
    public static final String BINARY_INTER_AU = "com.ibm.semplore.search.impl.alu.BinaryInterAU.class";
        
    /**
     * 
     */
    public static final String MASS_UNION_AU = "com.ibm.semplore.search.impl.alu.MassUnionAU.class";
    
    /**
     * 
     */
    public static final String MASS_UNION_THEN_INTER_AU = "com.ibm.semplore.search.impl.alu.MassUnionThenInterAU.class";
    
     /**
     * Read the configurations from the configuration file
     * @param filename
     * @throws Exception
     */
    public static Properties readConfigFile(String filename) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String str = null;
        System.err.println("================config begin==================");
        Properties config = new Properties();
        int i =0;
        while ((str=in.readLine()) != null) {
            if (str.indexOf('=') < 0) 
                continue;
            str = str.trim();
            String name = str.substring(0,str.indexOf('=')).trim();
            String value = str.substring(str.indexOf('=')+1,str.length()).trim();
            config.put(name, value);
            config.put(name+"_i", i);
            i++;
            System.err.println(name+" = "+value);
        }
        in.close();
        System.err.println("================config end==================");
        return config;
    }
     
}
