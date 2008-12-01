package com.ibm.semplore.search.impl.alu;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ibm.semplore.btc.impl.QueryEvaluatorImpl;
import com.ibm.semplore.btc.mapping.MappingIndexReader;
import com.ibm.semplore.btc.mapping.MappingIndexReaderFactory;
import com.ibm.semplore.search.impl.MEMDocStream_Score;
import com.ibm.semplore.util.BitVector;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;

public class MassUnionAU_Mapping_XFacet extends MassUnionAU {
	static Logger logger = Logger.getLogger(MassUnionAU_Mapping_XFacet.class);
	
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
    
    private String ds;
    private String type;
    public void setDataSource(String ds) {
    	this.ds = ds;
    }
    public void setFacetType(String type) {
    	this.type = type;
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
        CobjStream.init();
        MappingIndexReader reader = MappingIndexReaderFactory.getMappingIndexReader(ds + "_"+type+"_facet");
        
        // for evaluation
        int mapBar[] = new int[11];
        int mapN = 0;
        for (int i=0; i<subjectStream.getLen(); i++, subjectStream.next()) {
        	Iterator<Integer> itr = reader.getMappings(subjectStream.doc());
        	mapN = 0;
            while(itr.hasNext()){
                int inner = itr.next();
                mapN ++;
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
            if (mapN>=10) mapN = 10;
            mapBar[mapN] ++;
        }
        
        String mapS = "";
        for (int i=0; i<mapBar.length; i++) mapS += mapBar[i] + " ";
        logger.info("mapping hist: " + mapS);
        
        res = new int[setCount];
    	resScore = new float[setCount];
        ResultProcedure resP = new ResultProcedure(CobjStream);
        bv.forEachIndexFromToInState(0, bv.size()-1, true, resP);
        return new MEMDocStream_Score(res,resScore, res.length);
    }

}
