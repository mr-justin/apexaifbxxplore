package com.ibm.semplore.imports.impl.data.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.lang.reflect.Method;

public class ZipReader {
	Class processer;

	public ZipReader(Class p) {
		processer = p;
	}

	public void main(String[] args) throws Exception {
		File file = new File(args[0]);

		ZipInputStream in = new ZipInputStream(new FileInputStream(file));
		Method method = processer.getDeclaredMethod("processTripleLine",
				new Class[] { String.class });

		ZipEntry z = null;
		while ((z = in.getNextEntry()) != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			for (int i = 0; line != null; i++, line = br.readLine()) {
				method.invoke(processer, line);
			}
		}
		in.close();
	}
}
