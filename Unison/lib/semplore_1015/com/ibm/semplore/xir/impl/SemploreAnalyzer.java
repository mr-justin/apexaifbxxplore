/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SemploreAnalyzer.java,v 1.2 2007/04/18 06:55:33 lql Exp $
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
	public TokenStream tokenStream(String fieldName, Reader reader) {
		if( fieldName.equals(FieldType.RELATIONS.toString()) )
			return new WSDashTokenizer(reader);
		if( fieldName.equals(FieldType.INVERSERELATIONS.toString()) )
			return new WSDashTokenizer(reader);
		if( fieldName.equals(FieldType.CATEGORIES.toString()) )
			return new WSDashTokenizer(reader);
		if( fieldName.equals(FieldType.INSTANCE_SUBJECT_OF.toString()) )
			return new WSDashTokenizer(reader);
        if( fieldName.equals(FieldType.INSTANCE_OBJECT_OF.toString()) )
            return new WSDashTokenizer(reader);
		if( fieldName.equals(FieldType.TEXT.toString()) )
			return super.tokenStream(fieldName, reader);
		return new WSTokenizer(reader);
	}
	
	/**
     * Tokenize 'x y' to two token x, y
     * Only split by " "
     * 
     * @author zhangjie
     *
     */
    class WSTokenizer extends Tokenizer {

		public WSTokenizer(Reader input) {
			super(input);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.TokenStream#next()
		 */
		public Token next() throws IOException {	
			StringBuffer b = new StringBuffer();			
			int read = input.read();
			while( read != ' ' && read!=-1 ){
				b.append((char)read);
				read = input.read();
			}
			if(b.length()==0) return null;
			return new Token(b.toString(), 0, 0);
		}
		
	} 
    
    /**
     * Tokenize 'x_w y_z' to two token x, y. x has position w, y has position z
     * @author zhangjie
     *
     */
    class WSDashTokenizer extends Tokenizer {
		
		public WSDashTokenizer(Reader arg0) {
			super(arg0);
		}

		private int lastPosition = 0;

		public Token next() throws IOException {
			StringBuffer b = new StringBuffer();
			Token token = null;
			
			int read = input.read();
			while( read != ' ' && read!=-1 ){
				if(read==CONCATE)
					token = new Token(b.toString(), 0, 0);
				else
					b.append((char)read);
				read = input.read();
			}
			if(b.length()==0) return null;
			int position = Integer.parseInt(
					b.substring(token.termText().length(), b.length()));
			token.setPositionIncrement( position-lastPosition );
			lastPosition = position;
			return token;
		}
	}

}
