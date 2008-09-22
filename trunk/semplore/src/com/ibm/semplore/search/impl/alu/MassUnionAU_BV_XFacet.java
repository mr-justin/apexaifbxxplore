/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionAU_BV_XFacet.java,v 1.2 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import java.io.IOException;

import com.ibm.semplore.search.impl.MEMDocStream_Score;
import com.ibm.semplore.util.BitVector;
import com.ibm.semplore.xir.DocStream;

/**
 * MassUnionThenInterAU_BV implements the arithmetic functionality of MassUnionThenInterAU by Bit Vector. 
 * @author liu Qiaoling
 *
 */
public class MassUnionAU_BV_XFacet extends MassUnionAU
{
 
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.alu.ArithmeticUnit#getEstimatedResult(int)
     */
    public DocStream getEstimatedResult(int threshold) throws IOException
    {
    	return null;
    }
    protected float resScore[];
    private int[] res = null;
    protected class ResultProcedure implements BitVector.IntProcedure {
        private int p = 0;
        private DocStream uniIDstream = null ;
        public ResultProcedure(DocStream stream){
            super();
            uniIDstream = stream;
        }
        public boolean apply(int index) {
            try{
                uniIDstream.skipToIndex(index);
            }catch(IOException e){}
            resScore[p] = score[index];
            res[p++] = uniIDstream.doc();
            return true;
        }        
    }
    
    protected BitVector intersectionBVOf(DocStream matrix, DocStream filter) throws IOException {
        matrix.init();
        filter.init();
        BitVector bv = new BitVector(matrix.getLen());
        while(true){
            if(!filter.skipTo(matrix.doc()))
                break;
            if(!matrix.skipTo(filter.doc()))
                break;
            if(matrix.doc()==filter.doc()){
                bv.set(matrix.count()-1);
                if(!matrix.next()) break;
            }
        }
        return bv;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.alu.ArithmeticUnit#getResult()
     */
    private int score[];
    public DocStream getResult() throws IOException
    {        
    	int objRSize = CobjStream.getLen();
    	score = new int[objRSize];
        BitVector bv = new BitVector(objRSize);                
        int setCount = 0;
        subjectStream.init();
        relationStream.init();
        CobjStream.init();
        for (int i=0; i<subjectStream.getLen(); i++, subjectStream.next()) {
            if (!relationStream.skipTo(subjectStream.doc())) 
                break;
            if (relationStream.doc() != subjectStream.doc()) //can not find?
                continue;
            while(relationStream.hasNextPosition()){
                int inner = relationStream.nextPosition();
                try {
                    score[inner]+=1;
                    if(!bv.get(inner)){
                        bv.set(inner);
                        setCount++;
                    }
                } catch (Exception e) {
                	System.out.println("CobjStream len:"+CobjStream.getLen());
                	System.out.println("subjectStream doc:"+subjectStream.doc());
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }
            }
        }
         
        res = new int[setCount];
    	resScore = new float[setCount];
//        ResultProcedure resP = new ResultProcedure(CobjStream);
//        bv.forEachIndexFromToInState(0, bv.size()-1, true, resP);
        return new MEMDocStream_Score(res,resScore, res.length);
    }
}
