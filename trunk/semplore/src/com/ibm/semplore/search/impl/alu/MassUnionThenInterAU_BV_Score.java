/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionThenInterAU_BV_Score.java,v 1.1 2008/01/10 11:13:25 lql Exp $
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
public class MassUnionThenInterAU_BV_Score extends MassUnionThenInterAU
{
 
    /**
     * the default value of sample size
     */
    protected final int DEFAULT_SAMPLE_SIZE = 1000;
    
    /**
     * the sample size when sampling 
     */
    protected int sample_size = DEFAULT_SAMPLE_SIZE;

    /**
     * Set the sample size.
     * @param sample_size
     */
    public void setSampleSize(int sample_size) {
        this.sample_size = sample_size; 
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
    private float score[];
    public DocStream getResult() throws IOException
    {        
        BitVector bv = intersectionBVOf((DocStream)CobjStream.clone(), interStream);        
        int relInterCCount = bv.cardinality(); // The size of relation intersect inter
        int objRSize = CobjStream.getLen();
        BitVector bv2 = new BitVector(objRSize);
                
        subjectStream.init();
        CobjStream.init();
        relationStream.init();
        int setCount = 0;
        int count = 0;
        for (int i=0; i<subjectStream.getLen(); i++,subjectStream.next()) {
            if (!relationStream.skipTo(subjectStream.doc())) break;
            if (relationStream.doc()!=subjectStream.doc()) continue;
            count = 0; 
            while(relationStream.hasNextPosition()){
                int inner = relationStream.nextPosition();
                if(bv.getQuick(inner)){
                    bv.clear(inner);
                    bv2.set(inner);
                    score[inner] += subjectStream.score();
                    setCount++;
                    count ++;
                    if(count==relInterCCount){ // all bits are clear
                        break;
                    }                           
                }
            }
        }
         
        res = new int[setCount];
        resScore = new float[setCount];
        ResultProcedure resP = new ResultProcedure(CobjStream);
        bv2.forEachIndexFromToInState(0, bv2.size()-1, true, resP);
        
        return new MEMDocStream_Score(res,resScore, res.length);
    }

}
