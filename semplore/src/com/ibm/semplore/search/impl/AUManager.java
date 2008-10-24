/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: AUManager.java,v 1.4 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;
import java.util.Properties;

import com.ibm.semplore.btc.mapping.MappingIndexReaderFactory;
import com.ibm.semplore.config.Config;
import com.ibm.semplore.search.impl.alu.BinaryInterAU;
import com.ibm.semplore.search.impl.alu.BinaryInterAU_RS;
import com.ibm.semplore.search.impl.alu.BinaryInterAU_RS_Score;
import com.ibm.semplore.search.impl.alu.MassUnionAU;
import com.ibm.semplore.search.impl.alu.MassUnionAU_BV;
import com.ibm.semplore.search.impl.alu.MassUnionAU_BV_Score;
import com.ibm.semplore.search.impl.alu.MassUnionAU_BV_XFacet;
import com.ibm.semplore.search.impl.alu.MassUnionAU_Mapping_XFacet;
import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;

/**
 * AUManager provides management of several arithmetic units and execution of corresponding operations.
 * @author liu Qiaoling
 *
 */
public class AUManager
{
    
    /**
     * AU for binary intersection operation
     */
    protected BinaryInterAU binaryInterAU;
    protected BinaryInterAU binaryInterAU_Score;
            
    /**
     * AU for mass union operation
     */
    protected MassUnionAU massUnionAU;
    protected MassUnionAU massUnionAU_Score;
        
    protected MassUnionAU massUnion_BV_Facet;
	protected MassUnionAU_Mapping_XFacet massUnion_Mapping_Facet;
    /**
     * Create AUManager according to the configuration.
     * @param AUconfig
     * @throws Exception
     */
    public AUManager(Properties AUconfig) throws Exception {
        String className;
        className = AUconfig.getProperty(Config.BINARY_INTER_AU, BinaryInterAU_RS.class.getName());
        binaryInterAU = (BinaryInterAU)Class.forName(className).newInstance();
        binaryInterAU_Score = new BinaryInterAU_RS_Score();
//        binaryInterAU_Score = new BinaryInterAU_RS();
        
        className = AUconfig.getProperty(Config.MASS_UNION_AU,MassUnionAU_BV.class.getName());
        massUnionAU = (MassUnionAU)Class.forName(className).newInstance();
        massUnionAU_Score = new MassUnionAU_BV_Score();
//        massUnionAU_Score = new MassUnionAU_BV();
        
        massUnion_BV_Facet = new MassUnionAU_BV_XFacet();
        
        massUnion_Mapping_Facet = new MassUnionAU_Mapping_XFacet();
    }
    
    /**
     * Perform binary intersection operation, and it allows to specify whether to do accurate computation. Notice that if one of the stream is null then it returns the other stream as result.
     * @param stream1
     * @param stream2
     * @param accurate
     * @return
     * @throws IOException
     */
    public DocStream binaryInter(DocStream stream1, DocStream stream2, boolean accurate, int threshold) throws IOException {
        if (stream1 == null)
            return stream2;
        else if (stream2 == null)
            return stream1;
        
        binaryInterAU.setParameters(stream1, stream2);
        return binaryInterAU.getResult();
    }
        
    public DocStream binaryInter_Score(DocStream stream1, DocStream stream2, boolean accurate, int threshold) throws IOException {
        if (stream1 == null)
            return stream2;
        else if (stream2 == null)
            return stream1;
        
        binaryInterAU_Score.setParameters(stream1, stream2);
        return binaryInterAU_Score.getResult();
    }
        
    /**
     * Perform mass union operation, and it allows to specify whether to do accurate computation.
     * @param relationStream
     * @param subjectStream
     * @param objectStream
     * @param accurate
     * @return
     * @throws IOException
     */
    public DocStream massUnion(DocPositionStream relationStream, DocStream subjectStream, DocStream CobjStream, boolean accurate, int threshold) throws IOException {
        massUnionAU.setParameters(relationStream, subjectStream, CobjStream);
        return massUnionAU.getResult();
    }
    public DocStream massUnion_Score(DocPositionStream relationStream, DocStream subjectStream, DocStream CobjStream, boolean accurate, int threshold) throws IOException {
        massUnionAU_Score.setParameters(relationStream, subjectStream, CobjStream);
        return massUnionAU_Score.getResult();
    }
    public DocStream massUnion_Facet(String ds, String type, DocPositionStream relationStream, DocStream subjectStream, DocStream CobjectStream) throws IOException {
    	if (subjectStream.getLen()>30000 || !MappingIndexReaderFactory.hasMappingIndex(ds + "_"+type+"_facet")) 
    		return massUnion_BV_Facet(relationStream, subjectStream, CobjectStream);
    	else 
    		return massUnion_Mapping_Facet(ds,type,subjectStream,CobjectStream);
    }
    public DocStream massUnion_BV_Facet(DocPositionStream relationStream, DocStream subjectStream, DocStream CobjectStream) throws IOException {
        massUnion_BV_Facet.setParameters(relationStream, subjectStream, CobjectStream);
        return massUnion_BV_Facet.getResult();
    }
    public DocStream massUnion_Mapping_Facet(String ds, String type, DocStream subjectStream, DocStream CobjectStream) throws IOException {
        massUnion_Mapping_Facet.setParameters(null, subjectStream, CobjectStream);
        massUnion_Mapping_Facet.setDataSource(ds);
        massUnion_Mapping_Facet.setFacetType(type);
        return massUnion_Mapping_Facet.getResult();
    }
        
}
