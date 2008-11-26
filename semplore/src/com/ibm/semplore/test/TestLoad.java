package com.ibm.semplore.test;

import java.io.File;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.impl.data.load.Split1ntTo3nt_btc;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.imports.impl.data.preprocess.Main;

public class TestLoad {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length<1) {
			System.out.println("usage: java TestLoad [semplore_config_file]");
			System.exit(1);
		}

		try {
			Properties config = Config.readConfigFile(args[0]);
			String nt_dir = config.getProperty(Config.NT_DIR);
			String data_dir = config.getProperty(Config.DATA_FOR_INDEX_DIR);
			File datadir = new File(data_dir);
			if (!datadir.exists()) datadir.mkdir();
			
			File dir = new File(nt_dir);
			String catFile = data_dir+File.separator+"categoryTemp";
			String relInsFile = dir+File.separator+"relsplit";
			String relFile = data_dir+File.separator+"relationTemp";
			String hashdataFile = data_dir+File.separator+"hashdata";
//			String dic_dir = config.getProperty(Config.TMP_DIR);
				
			/*****1: revise Util4NT settings according to dataset*****/
			String type_p = config.getProperty(Config.TYPE, Util4NT.TYPE);
			Util4NT.setTYPE(type_p);
			
	//		Util4NT.setNameSpace("<http://dbpedia.org/resource/Category:", "<http://dbpedia.org/property/", "<http://dbpedia.org/resource/", "");
			//for tap
	//		Util4NT.setTYPE("rel_typeOf");
	//		Util4NT.setNameSpace("cat_", "rel_", "ins_", "");
			
			/*****2: Split1ntTo3nt*****/
			Split1ntTo3nt_btc.main(nt_dir, catFile, relInsFile, relFile, hashdataFile);
			
			/*****2.1: (optional) Filter4RelationInstance*****/
			
			/*****3: URI_ID_Dictionary*****/
//			URI_ID_Dictionary.main(dic_dir, catInsFile, relInsFile, attrInsFile);
			
			/*****3.1: AddTop4AllInstances*****/
//			AddTOP4AllInstance.main(dic_dir, catInsFile+".catID");
			
			/*****4: dataprocess.Main*****/
			Main.main(hashdataFile, catFile, relInsFile, relFile, args[0]);
			
			/*****5: TestIndex*****/
			
			/*****6: TestSearch*****/
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
