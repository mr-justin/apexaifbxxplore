/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SeekBug.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.test;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.Term;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author liu qiaoling
 *
 */
public class SeekBug
{
    public static void main(String[] args) throws Exception {
        IndexReader reader = IndexReader.open("E:\\lql\\data\\index\\ontoworld\\instance");
        TermFactory termFactory = TermFactoryImpl.getInstance();
        SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
        Term ins = termFactory.createTerm(schemaFactory.createInstance("http://ontoworld.org/index.php/_Shanghai_Jiao_Tong_University").getIDofURI(), FieldType.ID);
        DocStream res = (DocStream)reader.termDocs(new org.apache.lucene.index.Term(
                ins.getFieldType().toString(), ins.getString() ));
        res.next();
        int sjtu = res.doc(); 
        
        Term rel_obj = termFactory.createTermForObjects(schemaFactory.createRelation("http://ontoworld.org/index.php/_Relation-3AMember_of"));
        DocPositionStream poss = (DocPositionStream)reader.termPositions(new org.apache.lucene.index.Term(
                rel_obj.getFieldType().toString(), rel_obj.getString() ));
        System.out.print("subjects of relation Member_of with object SJTU: ");
        while (poss.next()) {
            if (poss.doc() == sjtu) {
                while (poss.hasNextPosition()) {
                    System.out.print(poss.nextPosition()+" ");
                }
            }
        }
        System.out.println();
        
        System.out.println("http://ontoworld.org/index.php/_Relation-3AMember_of".hashCode());
        Term rel_subj = termFactory.createTermForSubjects(schemaFactory.createRelation("http://ontoworld.org/index.php/_Relation-3AMember_of"));
        DocStream docs = (DocStream)reader.termDocs(new org.apache.lucene.index.Term(
                rel_subj.getFieldType().toString(), rel_subj.getString() ));
        System.out.println("subjects of relation Member_of: ");
        while (docs.next()) {
            Document doc = reader.document(docs.doc());            
            System.out.println(docs.doc()+" "+doc.getField(FieldType.URI.toString()));
        }
        System.out.println();        
        
//        System.out.println("total doc number: "+reader.numDocs());
//        for (int i=0; i<reader.numDocs(); i++) {
//            Document doc = reader.document(i);
//            System.out.println(i+" "+doc.getField(FieldType.URI.toString()));            
//        }
//        System.out.println();        
    }
}
