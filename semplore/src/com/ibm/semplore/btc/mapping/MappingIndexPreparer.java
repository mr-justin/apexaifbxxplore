package com.ibm.semplore.btc.mapping;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.ibm.semplore.config.Config;

/**
 * @author xrsun
 *
 */
public class MappingIndexPreparer {
	InputStream input;
	File path_of_ds1;
	File path_of_ds2;
	int strategy;
	
	public void build() throws Exception {
		throw new Exception("Not Implemented");
	}

	public MappingIndexPreparer(File datasrc, InputStream input, String ds1, String ds2,
			int strategy) {
		this.input = input;
		this.strategy = strategy;
		try {
			Properties config = Config.readConfigFile(datasrc.getAbsolutePath());
			path_of_ds1 = new File(config.getProperty(ds1));
			path_of_ds2 = new File(config.getProperty(ds2));
		} catch (Exception e) {
			System.err.println(String.format("Error while reading datasrc.cfg for %s, %s", ds1, ds2));
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out
					.println("Usage: cat mapping.data | java MappingIndexPreparer datasrc.cfg <name_of_ds1> <name_of_ds2> <strategy>");
			System.out.println("  mapping.data: <uri1>\t<uri2>");
			System.out
					.println("  strategy: 1->M*log(N); 2->N*log(M); 3->p*log(M)*log(N)");
			return;
		}
		File datasrc = new File(args[0]);
		String ds1 = args[1];
		String ds2 = args[2];
		int strategy = Integer.parseInt(args[3]);

		MappingIndexPreparer builder;
		builder = new MappingIndexPreparer_ForLucene(datasrc, System.in, ds1, ds2, strategy);
		builder.build();
	}
}
