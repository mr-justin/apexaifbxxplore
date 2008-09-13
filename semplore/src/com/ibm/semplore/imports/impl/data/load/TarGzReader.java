package com.ibm.semplore.imports.impl.data.load;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.zip.GZIPInputStream;

import com.ice.tar.TarInputStream;

public class TarGzReader {
	Class processer;
	public TarGzReader(Class p) {
		processer = p;
	}
//	public static String testDir = "\\\\Poseidon\\team\\Semantic Search\\BillionTripleData\\wordnet.nt.tar.gz";
		
	public void main(String[] args) throws Exception {
		File targzFile = new File(args[0]);
		GZIPInputStream gzinput = new GZIPInputStream(new FileInputStream(targzFile));
		TarInputStream targzinput = new TarInputStream(gzinput);
		Method method = processer.getDeclaredMethod("processTripleLine", new Class[]{String.class});
		while (targzinput.getNextEntry()!= null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(targzinput));
			String line = br.readLine();
			for (int i = 0; line != null; i++, line = br.readLine()) { 
//				System.out.println(line);
//				Split1ntTo3nt_btc.processTripleLine(line);
				method.invoke(processer, line);
			}
		}
	}
}
