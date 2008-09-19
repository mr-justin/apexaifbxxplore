/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SemploreAnalyzer.java,v 1.4 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.xir.FieldType;

/**
 * @author zhangjie
 *
 */
public class SemploreAnalyzer extends StandardAnalyzer {

	protected final static char CONCATE = '_';
	
	public SemploreAnalyzer(){
		super();
	}
	
	/**
	 * Get the term string given a {@link Relation} id and {@link Instance} id.
	 * 	E.g., 3_4, where 3 is relation id and 4 is instance id
	 * 
	 * @param relation
	 * @param obj
	 * @return
	 */
	public static String getTermString(String relationId, String insId){
		return relationId + String.valueOf(CONCATE) + insId;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
	 */
	public TokenStream tokenStream(FieldType fieldType, Reader reader) {
		if (fieldType == FieldType.INSTANCE_TYPE_OF) {
			return new Tokenizer4SemploreTermReader(reader);
		}
		if(( fieldType == FieldType.RELATIONS) ||
			(fieldType == FieldType.INVERSERELATIONS) ||
			(fieldType == FieldType.CATEGORIES) ||
			(fieldType == FieldType.INSTANCE_SUBJECT_OF) ||
			(fieldType == FieldType.INSTANCE_OBJECT_OF)
			) {
			return new Tokenizer4SemploreTermPositionReader(reader);
		}
		if (fieldType == FieldType.TEXT) {
			return new Tokenizer4SemploreTermReader4Attributes(reader);
		}
		return super.tokenStream("", reader);
	}
	
	/**
     * Tokenize 'x y' to two token x, y
     * Only split by " "
     * 
     * @author zhangjie
     *
     */
    class Tokenizer4SemploreTermReader extends Tokenizer {
		public Tokenizer4SemploreTermReader(Reader input) {
			super(input);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.TokenStream#next()
		 */
		public Token next() throws IOException {	
			SemploreTermReader myinput = (SemploreTermReader)input;
			String str = myinput.next();
			if (str==null)
				return null;
			else 
				return new Token(str, 0, 0);
		}
		
	} 
    
    /**
     * Tokenize 'x_w y_z' to two token x, y. x has position w, y has position z
     * @author zhangjie
     *
     */
    class Tokenizer4SemploreTermPositionReader extends Tokenizer {
		
		public Tokenizer4SemploreTermPositionReader(Reader arg0) {
			super(arg0);
		}

		private int lastPosition = 0;

		public Token next() throws IOException {	
			SemploreTermPositionReader myinput = (SemploreTermPositionReader)input;
			String str = myinput.next();
			if (str==null)
				return null;
			else {
				Token token = new Token(str, 0, 0);
				int position = myinput.position()+1;				
				token.setPositionIncrement( position-lastPosition );
				lastPosition = position;
				return token;
			}
		}
	}

    class Tokenizer4SemploreTermReader4Attributes extends Tokenizer {
		public Tokenizer4SemploreTermReader4Attributes(Reader input) {
			super(input);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.TokenStream#next()
		 */
		public Token next() throws IOException {	
			SemploreTermReader4Attributes myinput = (SemploreTermReader4Attributes)input;
			String str = myinput.next();
			if (str==null)
				return null;
			else 
				return new Token(str, 0, 0);
		}
		
	} 
    
}
