package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
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

	void sort(String oriCat, String oriRel, String oriAtt) throws Exception {
			FileSort filesort = new FileSort();
			filesort.sortTriple(oriRel, SRO + ".temp", "SRO");
			unique(SRO + ".temp", SRO);
			filesort.sortTriple(SRO, ORS, "ORS");
			filesort.sortTriple(SRO, RSO, "RSO");
			filesort.sortTriple(SRO, ROS, "ROS");
			filesort.sortTriple(oriCat, config.dir + "catTemp", "ORS");
			unique(config.dir+"catTemp", config.dir+"uniqued");
			filesort.sortTriple(config.dir+"uniqued", config.dir + config.cat, "SRO");
			filesort.sortTriple(oriAtt, config.dir + config.att+"Temp", "SRO");
			unique(config.dir+config.att+"Temp", config.dir+config.att);
	}

	void genRS_RO_() {
		RelInfoDiv spl = new RelInfoDiv(RSO, "S_", 1);
		spl.divide();
		spl = new RelInfoDiv(ROS, "O_", 3);
		spl.divide();
	}

	void genCatRel() {
		GetOneColumn getone = new GetOneColumn();
		getone.getOneColumn(RSO, config.dir + "relationTemp",2);
		getone.getOneColumn(config.dir + "catTemp", config.dir + "categoryTemp", 3);
	}

	public static void main(String[] args) {
		try {
			Properties pro = Config.readConfigFile(args[3]);
			Config.BSDDir = pro.getProperty(Config.TMP_DIR)+"/";
			Config.dir = pro.getProperty(Config.DATA_FOR_INDEX_DIR)+"/";
			
			Main main = new Main();
			main.sort(args[0], args[1], args[2]);
			main.genCatRel();
			 main.genRS_RO_();
			DataTransForm trans = new DataTransForm();
			trans.transForm(config.dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
