/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionThenInterAU_BV.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import java.io.IOException;

import com.ibm.semplore.search.impl.MEMDocStream;
import com.ibm.semplore.util.BitVector;
import com.ibm.semplore.xir.DocStream;

/**
 * MassUnionThenInterAU_BV implements the arithmetic functionality of MassUnionThenInterAU by Bit Vector. 
 * @author liu Qiaoling
 *
 */
public class MassUnionThenInterAU_BV extends MassUnionThenInterAU
{
 
    /**
     * the default value of sample size
     */
    protected final int DEFAULT_SAMPLE_SIZE = 1000;
    
    /**
     * the sample size when sampling 
     */
    protected int sample_size = DEFAULT_SAMPLE_SIZE;

    protected class ResultProcedure implements BitVector.IntProcedure {
        private int[] res = null;
        private int p = 0;
        private DocStream uniIDstream = null ;
        public ResultProcedure(int[] results, DocStream stream){
            super();
            res = results;
            uniIDstream = stream;
        }
        public int[] getResult() {
            return res;
        }
        public boolean apply(int index) {
            try{
                uniIDstream.skipToIndex(index);
            }catch(IOException e){}
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
        boolean breakSignal = false;
        for (int i=0; i<subjectStream.getLen(); i++,subjectStream.next()) {
            if (!relationStream.skipTo(subjectStream.doc())) break;
            if (relationStream.doc()!=subjectStream.doc()) continue;
            while(relationStream.hasNextPosition()){
                int inner = relationStream.nextPosition();
                if(bv.getQuick(inner)){
                    bv.clear(inner);
                    bv2.set(inner);
                    setCount++;
                    if(setCount==relInterCCount){ // all bits are clear
                        breakSignal = true;
                        break;
                    }                           
                }
            }
            if(breakSignal) 
                break;
        }
         
        int[] result = new int[setCount];
        ResultProcedure res = new ResultProcedure(result, CobjStream);
        bv2.forEachIndexFromToInState(0, bv2.size()-1, true, res);
        
        return new MEMDocStream(result, result.length);
    }

}
