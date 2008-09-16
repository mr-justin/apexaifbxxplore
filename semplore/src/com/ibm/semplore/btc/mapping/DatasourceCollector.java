/**
 * 
 */
package com.ibm.semplore.btc.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author xrsun
 * collect datasource information for each instance in each datasource
 * for calculating datasource facet
 */
public class DatasourceCollector {
	private File dsDir;
	private HashMap<String, BufferedWriter> files;

	public DatasourceCollector(File dsDir) {
		this.dsDir = dsDir;
		files = new HashMap<String, BufferedWriter>();
	}
	
	public void collect(String from_ds, int docid, int to_ds) throws IOException {
		BufferedWriter fwrite = files.get(from_ds);
		if (fwrite==null) {
			fwrite = new BufferedWriter(new FileWriter(dsDir.getPath() + File.separatorChar + from_ds + ".ds", true));
			files.put(from_ds, fwrite);
		}
		fwrite.write(docid+"\t"+to_ds+"\n");
	}
	
	public void close() throws IOException {
		Iterator<BufferedWriter> itr = files.values().iterator();
		while (itr.hasNext()) {
			itr.next().close();
		}
	}
	
	public static void main(String[] args) {

	}

}
