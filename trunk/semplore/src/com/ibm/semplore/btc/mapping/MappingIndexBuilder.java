package com.ibm.semplore.btc.mapping;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 
 */

/**
 * @author xrsun
 *
 */
public class MappingIndexBuilder {
	File indexhead;
	File indexmap;

	public void build(InputStream dataStream) throws IOException {
		BufferedReader fin = new BufferedReader(new InputStreamReader(dataStream));
		DataOutputStream fhead = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexhead))); 
		DataOutputStream fmap = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexmap))); 
		
		Integer lastid = null;
		int pos = 0;
		
		String line;
		while ((line=fin.readLine())!=null) {
			String[] split = line.split("\\t");
			int d1 = Integer.parseInt(split[0]);
			int d2 = Integer.parseInt(split[1]);
			if (lastid==null || !lastid.equals(d1)) {
				if (lastid!=null) {
					fmap.writeInt(-1);
					pos ++;
				}
				lastid=d1;
				fhead.writeInt(d1);
				fhead.writeInt(pos);
				fmap.writeInt(d2);
				pos ++;
			}
			else if (lastid.equals(d1)) {
				fmap.writeInt(d2);
				pos ++;
			}
		}
		if (lastid!=null) fmap.writeInt(-1);
		fhead.close();
		fmap.close();
	}
	
	public MappingIndexBuilder(File indexhead, File indexmap) {
		this.indexhead = indexhead;
		this.indexmap = indexmap;
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out
					.println("Usage: cat mapping.prepared | java BuildMappingIndex <index.head> <index.map>");
			System.out.println("  mapping.prepared: <docid1>\t<docid2>   sorted by docid1");
			return;
		}
		(new MappingIndexBuilder(new File(args[0]), new File(args[1]))).build(System.in);
	}

}
