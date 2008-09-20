/**
 * 
 */
package com.ibm.semplore.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

/**
 * @author xrsun
 * 
 */
public class HashID implements Comparable<HashID> {
	/** how many long is one hashid */
	public static final int idlen = 2;

	public long[] id;

	/** id[0] is the highest bit */
	public HashID(long[] id) {
		this.id = id.clone();
	}

	public HashID(String code) {
		StringTokenizer st = new StringTokenizer(code, ";");
		id = new long[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
			id[i] = Long.valueOf(st.nextToken());
	}

	public static String encode(long[] id) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < id.length; i++) {
			if (i > 0)
				sb.append(";");
			sb.append(String.valueOf(id[i]));
		}
		return sb.toString();
	}

	public static String encode2(String uri) throws NoSuchAlgorithmException {
		byte[] index = MessageDigest.getInstance("MD5").digest(uri.getBytes());
		long h = index[15] & 0x000000FF;
		h = (h << 8) | (index[14] & 0x000000FF);
		h = (h << 8) | (index[13] & 0x000000FF);
		h = (h << 8) | (index[12] & 0x000000FF);
		h = (h << 8) | (index[11] & 0x000000FF);
		h = (h << 8) | (index[10] & 0x000000FF);
		h = (h << 8) | (index[9] & 0x000000FF);
		h = (h << 8) | (index[8] & 0x000000FF);
		long l = index[7] & 0x000000FF;
		l = (l << 8) | (index[6] & 0x000000FF);
		l = (l << 8) | (index[5] & 0x000000FF);
		l = (l << 8) | (index[4] & 0x000000FF);
		l = (l << 8) | (index[3] & 0x000000FF);
		l = (l << 8) | (index[2] & 0x000000FF);
		l = (l << 8) | (index[1] & 0x000000FF);
		l = (l << 8) | (index[0] & 0x000000FF);
		return String.valueOf(h) + ";" + String.valueOf(l);
	}

	public String toString() {
		return encode(id);
	}

	public int compareTo(HashID o) {
		for (int i = 0; i < id.length; i++)
			if (id[i] != o.id[i])
				return id[i] < o.id[i] ? -1 : 1;
		return 0;
	}

	public static void main(String[] args) {
		long[] a = new long[2];
		a[0] = -1;
		a[1] = 1;
		System.out.println(encode(a));
		System.out.println("should equals");
		System.out.println((new HashID(encode(a))).toString());
	}
}
