package com.ibm.semplore.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author yrs
 *
 */
public class BitSet_byte {
	private int data[];
	//2^24=2^(5+19)=16777216(0~16777215)
	private final int size=0x0007ffff+1;
	BitSet_byte(){
		data=new int[size];
	}
	
	public void loadSet(DataInputStream fin)throws Exception{
		for(int i=0;i<size;++i) data[i]=fin.readInt();
	}
	
	public void saveSet(DataOutputStream fout)throws Exception{
		for(int i=0;i<size;++i) fout.writeInt(data[i]);
	}
	
	public void set(int index){
		data[index>>5]|=1<<(index&0x1f);
	}
	
	public boolean get(int index){
		return (((data[index>>5]>>>(index&0x1f))&0x00000001)==1);
	}
}
