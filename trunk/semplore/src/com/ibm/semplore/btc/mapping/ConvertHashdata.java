package com.ibm.semplore.btc.mapping;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.ibm.semplore.util.Md5_BloomFilter_64bit;

public class ConvertHashdata {
	public static void main(String[] args) throws NumberFormatException,
			IOException {
//		BufferedReader fin = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader fin = new BufferedReader(new FileReader("d:\\sxr\\data\\doc\\hashtest"));

		String line;

		Long id = null;
		boolean isInstance = false;
		String uri = null;

		while ((line = fin.readLine()) != null) {
			String[] split = line.split("\t");
			Long lid = Long.parseLong(split[0]);
			if (!lid.equals(id) && id != null) {
				if (isInstance && uri != null)
					System.out.println(id);
				id = lid;
				isInstance = false;
				uri = null;
			}
			if (id==null) id=lid;
			if (split[1].equals("URI"))
				uri = split[3];
			if (split[1].equals("TYPE")
					&& split[3]
							.equals(String.valueOf(Md5_BloomFilter_64bit.HASH_TYPE_INSTANCE)))
				isInstance = true;
		}
		if (isInstance && uri != null)
			System.out.println(id);

		fin.close();

	}
}
