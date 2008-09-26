/**
 * 
 */
package com.ibm.semplore.btc.mapping;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.ibm.semplore.util.BufferedRandomAccessFile;

/**
 * @author xrsun
 * 
 */
public class MappingIndexReader {
	private boolean twolevel;
	private int[] head2_d;
	private int[] head2_pos;
	//              docid1 -> pos_in_index_map
	private HashMap<Integer, Integer> headmap;
	private BufferedRandomAccessFile fhead = null;
	private RandomAccessFile fmap = null;
	
	static final private Iterator<Integer> nullItr = new Iterator<Integer>() {
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

	/**
	 * initiate a mapping index with filename of file this will read two files
	 * for index: file.head and file.map
	 * 
	 * @param file
	 * @throws IOException
	 */
	public MappingIndexReader(String file) throws IOException {
		loadIndexHead(file);
		fmap = new RandomAccessFile(file + ".map", "r");
	}

	public void close() throws IOException {
		if (fhead!=null) fhead.close();
		fmap.close();
		headmap = null;
		head2_d = null;
		head2_pos = null;
	}

	private void loadIndexHead(String file)
			throws IOException {
		DataInputStream fhead;
		try {
			fhead = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file + ".head")));
		} catch (Exception e) {
			return;
		}
		twolevel = fhead.readInt()==1;
		if (!twolevel) {
			int len = fhead.available() / 8;
			headmap = new HashMap<Integer, Integer>();
			for (int i = 0; i < len; i++) {
				int docid = fhead.readInt();
				int pos = fhead.readInt();
				headmap.put(docid, pos);
			}
			fhead.close();
		} else {
			DataInputStream fhead2 = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file + ".head.2")));
			int len = fhead2.available() / 8;
			head2_d = new int[len];
			head2_pos = new int[len];
			for (int i = 0; i < len; i++) {
				head2_d[i] = fhead2.readInt();
				head2_pos[i] = fhead2.readInt();
			}
			fhead2.close();
			this.fhead = new BufferedRandomAccessFile(file+".head","r");
		}
	}

	
	synchronized private boolean skipTo(int d) throws IOException {
		while (d!=fhead.readInt()) {
			if (fhead.readInt()<0) return false;
		}
		return true;
	}
	/**
	 * find integers to which d has mapping
	 * 
	 * @param d
	 * @return
	 * @throws IOException
	 */
	public Iterator<Integer> getMappings(int d) throws IOException {
		if (twolevel) {
			int head2 = Arrays.binarySearch(head2_d, d);
			int pos = 0;
			if (head2>=0) pos = head2_pos[head2];
			else {
				if (head2==-1) return nullItr;
				pos=head2_pos[-head2-2];
			}
			synchronized (this) {
				fhead.seek(pos*4);
				if (!skipTo(d)) return nullItr;
				pos = fhead.readInt();

				LinkedList<Integer> list = new LinkedList<Integer>();
				fmap.seek(pos * 4);
				int doc2;
				while ((doc2 = fmap.readInt()) != -1)
					list.add(doc2);
				return list.iterator();
			}
		} else {
			Integer pos = headmap.get(d);
			if (pos == null)
				return nullItr ;
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
}
