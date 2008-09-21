package com.ibm.semplore.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author yrs
 */
public class Md5_BloomFilter_128bit {
	public static long HASH_URI = 1936434897019030473l;
	public static long HASH_TYPE = 8736192962590431624l;
	public static long HASH_TYPE_INSTANCE = 2683022113649976747l;
	public static long HASH_TYPE_RELATION = -816450795805011627l;
	public static long HASH_TYPE_CATEGORY = -5772458573049287457l;
	public static long HASH_UNIVERSAL_CATEGORY_URI = -2437815354339154748l;
	public static long HASH_UNIVERSAL_RELATION_URI = -561082763347132051l;

	private BitSet_byte sets0[];
	private BitSet_byte sets1[];
	private BitSet_byte sets2[];
	private BitSet_byte sets3[];
	private MessageDigest md5 = null;
	private int offset0, head0, offset1, head1, offset2, head2, offset3, head3;
	private int collisions;
	private byte index[] = null;
	private boolean duplicate = false;;

	public Md5_BloomFilter_128bit() throws Exception {
		md5 = MessageDigest.getInstance("MD5");
		sets0 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets0[i] = new BitSet_byte();
		sets1 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets1[i] = new BitSet_byte();
		sets2 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets2[i] = new BitSet_byte();
		sets3 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets3[i] = new BitSet_byte();
		collisions = 0;
	}

	public byte[] getByteID() {
		return index;
	}

	/**
	 * 
	 * @return low 64bit ID in a long type. You should use it after calling the
	 *         function set() or calling the function genMd5Code(), or you will
	 *         get an unexpected answer.
	 */
	public long getLongIDlow() {
		long l = index[7] & 0x000000FF;
		l = (l << 8) | (index[6] & 0x000000FF);
		l = (l << 8) | (index[5] & 0x000000FF);
		l = (l << 8) | (index[4] & 0x000000FF);
		l = (l << 8) | (index[3] & 0x000000FF);
		l = (l << 8) | (index[2] & 0x000000FF);
		l = (l << 8) | (index[1] & 0x000000FF);
		l = (l << 8) | (index[0] & 0x000000FF);
		return l;
	}

	/**
	 * 
	 * @return high 64bit ID in a long type. You should use it after calling the
	 *         function set() or calling the function genMd5Code(), or you will
	 *         get an unexpected answer.
	 */
	public long getLongIDhigh() {
		long l = index[15] & 0x000000FF;
		l = (l << 8) | (index[14] & 0x000000FF);
		l = (l << 8) | (index[13] & 0x000000FF);
		l = (l << 8) | (index[12] & 0x000000FF);
		l = (l << 8) | (index[11] & 0x000000FF);
		l = (l << 8) | (index[10] & 0x000000FF);
		l = (l << 8) | (index[9] & 0x000000FF);
		l = (l << 8) | (index[8] & 0x000000FF);
		return l;
	}

	/**
	 * 
	 * @return 64bit ID in a long type. You should use it after calling the
	 *         function set(), or you will get an unexpected answer.
	 */
	public long getLongIDlow_set() {
		long l = head1;
		l = (l << 24) | offset1;
		l = (l << 8) | head0;
		l = (l << 24) | offset0;
		return l;
	}

	/**
	 * 
	 * @return 64bit ID in a long type. You should use it after calling the
	 *         function set(), or you will get an unexpected answer.
	 */
	public long getLongIDhigh_set() {
		long l = head3;
		l = (l << 24) | offset3;
		l = (l << 8) | head2;
		l = (l << 24) | offset2;
		return l;
	}

	public void genMd5Code(String s) {
		index = md5.digest(s.getBytes());
	}

	public byte[] getBytes() {
		return index;
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	public boolean set(String s) throws Exception {
		index = md5.digest(s.getBytes());
		{
			offset0 = ((index[2] << 16) & 0x00FF0000)
					| ((index[1] << 8) & 0x0000FF00)
					| (((int) index[0]) & 0x000000FF);
			head0 = (((int) index[3]) & 0x000000FF);
			duplicate = sets0[head0].get(offset0);
		}
		{
			offset1 = ((index[6] << 16) & 0x00FF0000)
					| ((index[5] << 8) & 0x0000FF00)
					| (((int) index[4]) & 0x000000FF);
			head1 = (((int) index[7]) & 0x000000FF);
			duplicate = duplicate && sets1[head1].get(offset1);
		}
		{
			offset2 = ((index[10] << 16) & 0x00FF0000)
					| ((index[9] << 8) & 0x0000FF00)
					| (((int) index[8]) & 0x000000FF);
			head2 = (((int) index[11]) & 0x000000FF);
			duplicate = duplicate && sets2[head2].get(offset2);
		}
		{
			offset3 = ((index[14] << 16) & 0x00FF0000)
					| ((index[13] << 8) & 0x0000FF00)
					| (((int) index[12]) & 0x000000FF);
			head3 = (((int) index[15]) & 0x000000FF);
			duplicate = duplicate && sets3[head3].get(offset3);
		}
		if (duplicate)
			++collisions;
		else {
			sets0[head0].set(offset0);
			sets1[head1].set(offset1);
			sets2[head2].set(offset2);
			sets3[head3].set(offset3);
		}
		return duplicate;
	}

	public boolean get(String s) {
		index = md5.digest(s.getBytes());
		{
			offset0 = ((index[2] << 16) & 0x00FF0000)
					| ((index[1] << 8) & 0x0000FF00)
					| (((int) index[0]) & 0x000000FF);
			head0 = (((int) index[3]) & 0x000000FF);
			duplicate = sets0[head0].get(offset0);
		}
		{
			offset1 = ((index[6] << 16) & 0x00FF0000)
					| ((index[5] << 8) & 0x0000FF00)
					| (((int) index[4]) & 0x000000FF);
			head1 = (((int) index[7]) & 0x000000FF);
			duplicate = duplicate && sets1[head1].get(offset1);
		}
		{
			offset2 = ((index[10] << 16) & 0x00FF0000)
					| ((index[9] << 8) & 0x0000FF00)
					| (((int) index[8]) & 0x000000FF);
			head2 = (((int) index[11]) & 0x000000FF);
			duplicate = duplicate && sets2[head2].get(offset2);
		}
		{
			offset3 = ((index[14] << 16) & 0x00FF0000)
					| ((index[13] << 8) & 0x0000FF00)
					| (((int) index[12]) & 0x000000FF);
			head3 = (((int) index[15]) & 0x000000FF);
			duplicate = duplicate && sets3[head3].get(offset3);
		}
		return duplicate;
	}

	public int collisions() {
		return collisions;
	}

	public void saveData(String saveFileName) throws Exception {
		try {
			DataOutputStream fout = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(saveFileName)));
			for (int i = 0; i < 256; ++i)
				sets0[i].saveSet(fout);
			for (int i = 0; i < 256; ++i)
				sets1[i].saveSet(fout);
			for (int i = 0; i < 256; ++i)
				sets2[i].saveSet(fout);
			for (int i = 0; i < 256; ++i)
				sets3[i].saveSet(fout);
			fout.close();
		} catch (Exception e) {
			throw new Exception("Error while save Sets!");
		}
	}

	public void loadData(String loadFileName) throws Exception {
		try {
			DataInputStream fin = new DataInputStream(new BufferedInputStream(
					new FileInputStream(loadFileName)));
			for (int i = 0; i < 256; ++i)
				sets0[i].loadSet(fin);
			for (int i = 0; i < 256; ++i)
				sets1[i].loadSet(fin);
			for (int i = 0; i < 256; ++i)
				sets2[i].loadSet(fin);
			for (int i = 0; i < 256; ++i)
				sets3[i].loadSet(fin);
			fin.close();
		} catch (Exception e) {
			throw new Exception("Error while load Sets!");
		}
	}

	public void clear() {
		sets0 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets0[i] = new BitSet_byte();
		sets1 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets1[i] = new BitSet_byte();
		sets2 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets2[i] = new BitSet_byte();
		sets3 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets3[i] = new BitSet_byte();
		collisions = 0;
	}

	public void clearCollisions() {
		collisions = 0;
	}

	public static long URItoID(String URI) throws NoSuchAlgorithmException {
		byte[] index = MessageDigest.getInstance("MD5").digest(URI.getBytes());
		long l = index[15] & 0x000000FF;
		l = (l << 8) | (index[14] & 0x000000FF);
		l = (l << 8) | (index[13] & 0x000000FF);
		l = (l << 8) | (index[12] & 0x000000FF);
		l = (l << 8) | (index[11] & 0x000000FF);
		l = (l << 8) | (index[10] & 0x000000FF);
		l = (l << 8) | (index[9] & 0x000000FF);
		l = (l << 8) | (index[8] & 0x000000FF);
		return l;
	}

	public static void main(String args[]) throws NoSuchAlgorithmException {
		String[] ss = { "URI", "TYPE", "TYPE_INSTANCE", "TYPE_RELATION",
				"TYPE_CATEGORY", "<TOP_Category>", "UNIVERSAL_RELATION" };
		for (int i = 0; i < ss.length; i++) {
			System.out.println(URItoID(ss[i]));
		}
	}
}
