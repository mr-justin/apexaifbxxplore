package com.ibm.semplore.btc.mapping;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.ibm.semplore.util.Md5_BloomFilter_64bit;

/**
 * @author xrsun
 *
 */
public class MappingIndexPreparer_ForHashdata extends MappingIndexPreparer {
	HashMap<Long, Integer> idmap1 = new HashMap<Long, Integer>();
	HashMap<Long, Integer> idmap2 = new HashMap<Long, Integer>();

	public MappingIndexPreparer_ForHashdata(InputStream input, File path_of_ds1, File path_of_ds2,
			int strategy) {
		super(input, path_of_ds1, path_of_ds2, strategy);
	}

	public void readDS(File path, HashMap<Long, Integer> map)
			throws NumberFormatException, IOException {
		BufferedReader fin = new BufferedReader(new FileReader(path));
		String line;
		int i = 0; // first
		while ((line = fin.readLine()) != null) {
			map.put(Long.parseLong(line), i);
			i++;
		}
		fin.close();
	}

	public void build() throws Exception {
		readDS(path_of_ds1, idmap1);
		readDS(path_of_ds2, idmap2);
		BufferedReader fin = new BufferedReader(new InputStreamReader(input));
		String line;
		while ((line=fin.readLine())!=null) {
			String[] split = line.split("\\t");
			long hashid1 = Long.parseLong(split[0]);
			long hashid2 = Long.parseLong(split[1]);
			System.out.println(idmap1.get(hashid1)+"\t"+ idmap2.get(hashid2));
		}
	}
}
