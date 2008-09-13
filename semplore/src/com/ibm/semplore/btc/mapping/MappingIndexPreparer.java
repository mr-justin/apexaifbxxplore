package com.ibm.semplore.btc.mapping;
import java.io.File;
import java.io.InputStream;

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

	public MappingIndexPreparer(InputStream input, File path_of_ds1, File path_of_ds2,
			int strategy) {
		this.input = input;
		this.path_of_ds1 = path_of_ds1;
		this.path_of_ds2 = path_of_ds2;
		this.strategy = strategy;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out
					.println("Usage: cat mapping.data | java MappingIndexPreparer <path_of_ds1> <path_of_ds2> <strategy>");
			System.out.println("  mapping.data: <uri1>\t<uri2>");
			System.out
					.println("  strategy: 1->M*log(N); 2->N*log(M); 3->p*log(M)*log(N)");
			return;
		}
		File path_of_ds1 = new File(args[0]);
		File path_of_ds2 = new File(args[1]);
		int strategy = Integer.parseInt(args[2]);

		MappingIndexPreparer builder;
		if (path_of_ds1.isDirectory())
			builder = new MappingIndexPreparer_ForLucene(System.in, path_of_ds1,
					path_of_ds2, strategy);
		else
			builder = new MappingIndexPreparer_ForHashdata(System.in,
					path_of_ds1, path_of_ds2, strategy);
		builder.build();
	}
}
