package com.ibm.semplore.imports.impl.data.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;

public class NTReader {
	Class processer;
	public NTReader(Class p) {
		processer = p;
	}
	public void main(String[] args) throws Exception {
		File NTFile = new File(args[0]);
		BufferedReader input = new BufferedReader(new FileReader(NTFile));
		String line = null;
		Method method = processer.getDeclaredMethod("processTripleLine", new Class[]{String.class});
		while ((line = input.readLine()) != null) {
			method.invoke(processer, line);
//			Split1ntTo3nt_btc.processTripleLine(line);
		}
	}
}
