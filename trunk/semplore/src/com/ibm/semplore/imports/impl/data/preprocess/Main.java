package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

import com.ibm.semplore.config.Config;

public class Main {

	/**
	 * @param args
	 */
	static Config config = new Config();

	String RSO = config.dir + "RSO";

	String SRO = config.dir + "SRO";

	String ORS = config.dir + "ORS";

	String ROS = config.dir + "ROS";

	void unique(String in, String out) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(in));
			PrintWriter writer = new PrintWriter(new FileWriter(out));
			String temp;
			String last = "";
			while ((temp = reader.readLine()) != null) {
				if (temp.equals(last))
					continue;
				writer.println(temp);
				last = temp;
			}
			writer.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void sort(String hashdataFile, String oriCat, String oriRel, String oriAtt) throws Exception {
			FileSort filesort = new FileSort();

//  use GNU sort and uniq instead
//			filesort.sortHashTuple(hashdataFile, hashdataFile+".sort");

			//for relation
			filesort.sortTriple(oriRel, RSO, "RSO");
			new File(oriRel).delete();
//			unique(RSO + ".temp", RSO);
			filesort.sortTriple(RSO, ROS, "ROS");
			
			//for category
			filesort.sortPair(oriCat, oriCat+".temp");
//			unique(oriCat+".temp", oriCat+".temp1");
			(new GetOneColumn()).getOneColumn(oriCat+".temp", oriCat, 1);
			new File(oriCat+".temp").delete();
//			filesort.sortTriple(oriCat, config.dir + "catTemp", "ORS");
//			unique(config.dir+"catTemp", config.dir+"uniqued");
//			filesort.sortTriple(config.dir+"uniqued", config.dir + config.cat, "SRO");
			
			filesort.sortPair(oriAtt, oriAtt+".temp");
//			unique(oriAtt+".temp", oriAtt+".temp1");
			(new GetOneColumn()).getOneColumn(oriAtt+".temp", oriAtt, 1);
			new File(oriAtt+".temp").delete();
			//for attribute
//			filesort.sortTriple(oriAtt, config.dir + config.att+"Temp", "SRO");
//			unique(config.dir+config.att+"Temp", config.dir+config.att);
	}

	void genRS_RO_() {
		RelInfoDiv spl = new RelInfoDiv(RSO, "S_", 1);
		spl.divide();
		new File(RSO).delete();
		spl = new RelInfoDiv(ROS, "O_", 3);
		spl.divide();
		new File(ROS).delete();
	}

	void genCatRel() {
		GetOneColumn getone = new GetOneColumn();
		getone.getOneColumn(RSO, config.dir + "relationTemp",2);
		getone.getOneColumn(config.dir + "catTemp", config.dir + "categoryTemp", 3);
	}

	public static void main(String hashdataFile, String catFile, String relInsFile, String relFile, String config_file) {
		try {
			Properties pro = Config.readConfigFile(config_file);
			Config.BSDDir = pro.getProperty(Config.TMP_DIR)+"/";
			Config.dir = pro.getProperty(Config.DATA_FOR_INDEX_DIR)+"/";
			if (!new File(Config.dir).exists())
				new File(Config.dir).mkdirs();
			
			Main main = new Main();
			main.sort(hashdataFile, catFile, relInsFile, relFile);
//			main.genCatRel();
			 main.genRS_RO_();
			DataTransForm trans = new DataTransForm();
			trans.transForm(config.dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
