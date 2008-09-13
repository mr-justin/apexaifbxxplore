package com.ibm.semplore.imports.impl.data.load;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.zip.GZIPInputStream;

public class GZReader {
	Class processer;
	public GZReader(Class p) {
		processer = p;
	}
//	public static String testDir = "\\\\Poseidon\\team\\Semantic Search\\BillionTripleData\\wordnet.nt.tar.gz";
		
	public void main(String[] args) throws Exception {
		File targzFile = new File(args[0]);
		GZIPInputStream gzinput = new GZIPInputStream(new FileInputStream(targzFile));
		Method method = processer.getDeclaredMethod("processTripleLine", new Class[]{String.class});
		BufferedReader br = new BufferedReader(new InputStreamReader(gzinput));
		String line = br.readLine();
		for (int i = 0; line != null; i++, line = br.readLine()) { 
//				System.out.println(line);
//				Split1ntTo3nt_btc.processTripleLine(line);
			method.invoke(processer, line);
		}
	}
}
