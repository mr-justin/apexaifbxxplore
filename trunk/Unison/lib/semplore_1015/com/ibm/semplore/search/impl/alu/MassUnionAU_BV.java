/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionAU_BV.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import java.io.IOException;

import com.ibm.semplore.search.impl.MEMDocStream;
import com.ibm.semplore.util.BitVector;
import com.ibm.semplore.xir.DocStream;

/**
 * MassUnionAU_BV implements the arithmetic functionality of MassUnionAU by Bit Vector. 
 * @author liu Qiaoling
 *
 */
public class MassUnionAU_BV extends MassUnionAU
{

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
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.alu.ArithmeticUnit#getResult()
     */
    public DocStream getResult() throws IOException
    {
    	
        int objRSize = CobjStream.getLen();
        BitVector bv = new BitVector(objRSize);                
        int setCount = 0;
        boolean breakFlag = false;
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
                    if(!bv.get(inner)){
                        bv.set(inner);
                        setCount++;
                        if(setCount==objRSize){ // all bits are set
                            breakFlag = true; 
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(breakFlag) break;
        }
         
        int[] result = new int[setCount];
        ResultProcedure res = new ResultProcedure(result, CobjStream);
        bv.forEachIndexFromToInState(0, bv.size()-1, true, res);
        return new MEMDocStream(result,result.length);
    }

}
