package com.ibm.semplore.xir.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import com.ibm.semplore.model.LocalList;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.InstanceDocument;

public class SemploreTermPositionReader extends Reader {
	protected ArrayList<Relation> rels;
	protected int relsize;
	protected InstanceDocument insDoc;
	protected FieldType ft = null;
	
	protected int nowTerm = -1;
	protected int nowPos = -1;
	protected LocalList nowPosList = null;
	protected String nowTermStr = null;

	public SemploreTermPositionReader(ArrayList<Relation> rels, InstanceDocument insDoc, FieldType ft) {
		this.rels = rels;
		if (rels!=null) relsize = rels.size(); else relsize = 1; 
		this.insDoc = insDoc;
		this.ft = ft;		
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String next() {
		int pos = nextPosition();
		if (pos!=-1) {
			return term();
		} else {
			String term = nextTerm();
			if (term==null)
				return null;
			return next();
		}
	}
	public int position() {
		return nowPosList.getLocalID(nowPos);
	}
	protected int nextPosition() {
		nowPos++;
		if (!(nowPosList!=null && nowPos<nowPosList.size())) {
			return -1;
		}
		return position();
	}
	protected String term() {
		return nowTermStr;
	}
	protected String nextTerm() {
		nowTerm++;
		if (!(nowTerm<relsize)) {
			return null;
		}
		nowTermStr = null;
		nowPosList = null;
		if (ft==FieldType.INSTANCE_SUBJECT_OF) {
			nowTermStr = String.valueOf(rels.get(nowTerm).getIDofURI());
			nowPosList = insDoc.getObjects(rels.get(nowTerm));
		} else if (ft==FieldType.INSTANCE_OBJECT_OF) {
			nowTermStr = String.valueOf(rels.get(nowTerm).getIDofURI());
			nowPosList = insDoc.getSubjects(rels.get(nowTerm));
		} else if (ft==FieldType.CATEGORIES) {
			nowTermStr = FieldType.CATEGORIES.toString();
			nowPosList = insDoc.getCategoriesWithID();
		} else if (ft==FieldType.RELATIONS) {
			nowTermStr = FieldType.RELATIONS.toString();
			nowPosList = insDoc.getRelationsWithID();
		} else if (ft==FieldType.INVERSERELATIONS) {
			nowTermStr = FieldType.INVERSERELATIONS.toString();
			nowPosList = insDoc.getInverseRelationsWithID();
		}
		nowPos = -1;
		return nowTermStr;
	}
}
