/**
 * 
 */
package com.ibm.semplore.btc.mapping;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author xrsun
 * 
 */
public class MappingIndexReader {
	//              docid1 -> pos_in_index_map
	private HashMap<Integer, Integer> fhead;
	private RandomAccessFile fmap;

	/**
	 * initiate a mapping index with filename of file this will read two files
	 * for index: file.head and file.map
	 * 
	 * @param file
	 * @throws IOException
	 */
	public MappingIndexReader(String file) throws IOException {
		fhead = loadIndexHead(file);
		fmap = new RandomAccessFile(file + ".map", "r");
	}

	public void close() throws IOException {
		fmap.close();
		fhead = null;
	}

	private HashMap<Integer, Integer> loadIndexHead(String file)
			throws IOException {
		DataInputStream fhead;
		try {
			fhead = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file + ".head")));
		} catch (Exception e) {
			return null;
		}
		int len = fhead.available() / 8;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < len; i++) {
			int docid = fhead.readInt();
			int pos = fhead.readInt();
			map.put(docid, pos);
		}
		fhead.close();
		return map;
	}

	/**
	 * find integers to which d has mapping
	 * 
	 * @param d
	 * @return
	 * @throws IOException
	 */
	public Iterator<Integer> getMappings(int d) throws IOException {
		Integer pos = fhead.get(d);
		if (pos == null)
			return new Iterator<Integer>() {
				public boolean hasNext() {
					return false;
				}

				public Integer next() {
					throw new NoSuchMethodError();
				}

				public void remove() {
					throw new NoSuchMethodError();
				}
			};
		LinkedList<Integer> list = new LinkedList<Integer>();
		synchronized (this) {
			fmap.seek(pos * 4);
			int doc2;
			while ((doc2 = fmap.readInt()) != -1)
				list.add(doc2);
		}
		return list.iterator();
	}
}
