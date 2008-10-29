/**
 * 
 */
package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import com.ibm.semplore.imports.impl.data.load.GZReader;
import com.ibm.semplore.imports.impl.data.load.NTReader;
import com.ibm.semplore.imports.impl.data.load.Split1ntTo3nt_btc;
import com.ibm.semplore.imports.impl.data.load.TarGzReader;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.imports.impl.data.load.WarcReader;
import com.ibm.semplore.imports.impl.data.load.ZipReader;
import com.ibm.semplore.util.Md5_BloomFilter_128bit;

/**
 * @author xrsun
 *
 */
public class Survey {
	static HashSet<String> property = new HashSet<String>();
	static Md5_BloomFilter_128bit md5;
//	static {
//		 try {
//			md5 = new Md5_BloomFilter_128bit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void processTripleLine(String line) throws Exception {
		String[] triple = Util4NT.processTripleLine(line);
		if (triple == null) return;
		String type = Util4NT.checkTripleType(triple);
		if (type == Util4NT.RELATION || type == Util4NT.ATTRIBUTE)
//			if (!md5.set(triple[1]))
//				System.out.println(line);
			if (!property.contains(triple[1])) {
				property.add(triple[1]);
				System.out.println(line);
			}
	}

	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);

		File[] files = dir.listFiles();
		for (int j = 0; j < files.length; j++) {
			File fin = files[j];
			String filetype= Split1ntTo3nt_btc.checkFileType(fin.getName());
			if (filetype==null)
				continue;
			if (filetype.equals(".warc"))
				(new WarcReader(Survey.class)).main(new String[]{fin.getPath()});
			else if (filetype.equals(".nt.tar.gz"))
				(new TarGzReader(Survey.class)).main(new String[]{fin.getPath()});
			else if (filetype.equals(".nt.gz"))
				(new GZReader(Survey.class)).main(new String[]{fin.getPath()});
			else if (filetype.equals(".nt.zip"))
				(new ZipReader(Survey.class)).main(new String[]{fin.getPath()});
			else if (filetype.equals(".nt"))
				(new NTReader(Survey.class)).main(new String[]{fin.getPath()});
		}

	}

}
