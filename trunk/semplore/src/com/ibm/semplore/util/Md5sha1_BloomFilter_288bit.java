package com.ibm.semplore.util;
import java.security.MessageDigest;
/**
 * @author yrs
 */
public class Md5sha1_BloomFilter_288bit {
	
	private BitSet_byte sets0[];
	private BitSet_byte sets1[];
	private BitSet_byte sets2[];
	private BitSet_byte sets3[];
	private BitSet_byte sets4[];
	private BitSet_byte sets5[];
	private BitSet_byte sets6[];
	private BitSet_byte sets7[];
	private BitSet_byte sets8[];
	private MessageDigest md5=null;
	private MessageDigest sha1=null;
	
	private int offset0,head0,offset1,head1,offset2,head2,
				offset3,head3,offset4,head4,offset5,head5,
				offset6,head6,offset7,head7,offset8,head8;
	private int collisions;
	public Md5sha1_BloomFilter_288bit() throws Exception{
		md5=MessageDigest.getInstance("MD5");
		sha1=MessageDigest.getInstance("SHA-1");
		
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
		sets4 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets4[i] = new BitSet_byte();
		sets5 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets5[i] = new BitSet_byte();
		sets6 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets6[i] = new BitSet_byte();
		sets7 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets7[i] = new BitSet_byte();
		sets8 = new BitSet_byte[256];
		for (int i = 0; i < 256; ++i)
			sets8[i] = new BitSet_byte();
		collisions=0;
	}
	
	public boolean input(String s)throws Exception{
		boolean p;
		byte index[]=md5.digest(s.getBytes());
		{
			offset0 =  ((index[2] << 16) & 0x00FF0000)
				  	 | ((index[1] <<  8) & 0x0000FF00)
					 | (((int) index[0]) & 0x000000FF);
			head0 =    (((int) index[3]) & 0x000000FF);
			p=sets0[head0].get(offset0);
		}
		{
			offset1 =  ((index[6] << 16) & 0x00FF0000)
					 | ((index[5] <<  8) & 0x0000FF00)
					 | (((int) index[4]) & 0x000000FF);
			head1 =    (((int) index[7]) & 0x000000FF);
			p=p&&sets1[head1].get(offset1);
		}
		{
			offset2 =  ((index[10]<< 16) & 0x00FF0000)
					 | ((index[9] <<  8) & 0x0000FF00)
					 | (((int) index[8]) & 0x000000FF);
			head2 =    (((int)index[11]) & 0x000000FF);
			p=p&&sets2[head2].get(offset2);
		}
		{
			offset3 =  ((index[14]<< 16) & 0x00FF0000)
					 | ((index[13]<<  8) & 0x0000FF00)
					 | (((int)index[12]) & 0x000000FF);
			head3 =    (((int)index[15]) & 0x000000FF);
			p=p&&sets3[head3].get(offset3);
		}
		index=sha1.digest(s.getBytes());
		{
			offset4 =  ((index[2] << 16) & 0x00FF0000)
				  	 | ((index[1] <<  8) & 0x0000FF00)
					 | (((int) index[0]) & 0x000000FF);
			head4 =    (((int) index[3]) & 0x000000FF);
			p=p&&sets4[head4].get(offset4);
		}
		{
			offset5 =  ((index[6] << 16) & 0x00FF0000)
					 | ((index[5] <<  8) & 0x0000FF00)
					 | (((int) index[4]) & 0x000000FF);
			head5 =    (((int) index[7]) & 0x000000FF);
			p=p&&sets5[head5].get(offset5);
		}
		{
			offset6 =  ((index[10]<< 16) & 0x00FF0000)
					 | ((index[9] <<  8) & 0x0000FF00)
					 | (((int) index[8]) & 0x000000FF);
			head6 =    (((int)index[11]) & 0x000000FF);
			p=p&&sets6[head6].get(offset6);
		}
		{
			offset7 =  ((index[14]<< 16) & 0x00FF0000)
					 | ((index[13]<<  8) & 0x0000FF00)
					 | (((int)index[12]) & 0x000000FF);
			head7 =    (((int)index[15]) & 0x000000FF);
			p=p&&sets7[head7].get(offset7);
		}
		{
			offset8 =  ((index[18]<< 16) & 0x00FF0000)
					 | ((index[17]<<  8) & 0x0000FF00)
					 | (((int)index[16]) & 0x000000FF);
			head8 =    (((int)index[19]) & 0x000000FF);
			p=p&&sets8[head8].get(offset8);
		}
		if (p) ++collisions;
		else {
			sets0[head0].set(offset0);
			sets1[head1].set(offset1);
			sets2[head2].set(offset2);
			sets3[head3].set(offset3);
			sets4[head4].set(offset4);
			sets5[head5].set(offset5);
			sets6[head6].set(offset6);
			sets7[head7].set(offset7);
			sets8[head8].set(offset8);
		}
		return p;
	}
	public int collisions(){return collisions;}
}
