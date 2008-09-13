/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionAU_BV_Score.java,v 1.1 2008/01/10 11:13:25 lql Exp $
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
public class MassUnionAU_BV_Score extends MassUnionAU
{
 
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.alu.ArithmeticUnit#getEstimatedResult(int)
     */
    public DocStream getEstimatedResult(int threshold) throws IOException
    {
    	return getResult();
    }
    protected float resScore[];
    protected int[] res = null;
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
            resScore[p] = 1-score[index];
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
    private float score[];
    public DocStream getResult() throws IOException
    {        
        int objRSize = CobjStream.getLen();
        BitVector bv2 = new BitVector(objRSize);
        score = new float[objRSize];
                
        subjectStream.init();
        CobjStream.init();
        relationStream.init();
        int setCount = 0;
//        try {
        for (int i=0; i<subjectStream.getLen(); i++,subjectStream.next()) {
            if (!relationStream.skipTo(subjectStream.doc())) break;
            if (relationStream.doc()!=subjectStream.doc()) continue;
            while(relationStream.hasNextPosition()){
                int inner = relationStream.nextPosition();
                try {
	                if (!bv2.getQuick(inner)) {
	                	score[inner] = 1;
	                    bv2.set(inner);
	                    setCount++;
	                } 
	                score[inner] *= (1-subjectStream.score());
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
//        } catch (Exception e) {
//        	throw new IOException(e.getMessage());
//        }
        }
         
        res = new int[setCount];
        resScore = new float[res.length];
        ResultProcedure resP = new ResultProcedure(CobjStream);
        bv2.forEachIndexFromToInState(0, bv2.size()-1, true, resP);
        
        return new MEMDocStream_Score(res,resScore, res.length);
    }

}
