package com.ibm.semplore.imports.impl.data.util;

public class HashTuple implements Comparable{
	public long sub, pred, o;
	public String obj;
	public String type;
	public HashTuple(long s,  String type, long p, String obj) {
		sub = s; this.type = type; pred = p; this.obj = obj;
		int len = obj.length();
		boolean neg = false;
		o = 0;
		int i;
		for (i=0; i<len; i++) {
			char c = obj.charAt(i);
			if(c!='\t') break;
		}
		for (; i<len; i++) {
			char c = obj.charAt(i);
			if (c>='0' && c<='9') o = o*10+c-'0';
			else if (c=='-') neg = true;
			else if (c=='\n') {
				o = neg?-o:o;
				return;
			} else {
				o = obj.charAt(1);
				return;
			}
		}
		o = obj.charAt(1);
	}
	public int compareTo(Object arg0) {
		HashTuple a = (HashTuple)arg0;
		return compareTo(a.sub,sub, a.type.compareTo(type), a.pred,pred, a.o, o);
	}
	private int compareTo(long a0, long a1, int typecom, long b0, long b1, long c0, long c1){
		if (a1!=a0) return a1>a0?1:-1;
		if (typecom!=0) return typecom;
		if (b1!=b0) return b1>b0?1:-1;
		if (c1!=c0) return c1>c0?1:-1;
		return 0;
	}
}
