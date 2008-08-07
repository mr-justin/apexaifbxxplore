/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaObjectInfo.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.model;

/**
 * @author liu qiaoling
 *
 */
public interface SchemaObjectInfo
{
  
    /**
     * Returns the URI of this schema object.
     * @return
     */
    public String getURI();
        
    /**
     * Returns the label of this schema object.
     * @return
     */
    public String getLabel();
    
    /**
     * Returns the label of this schema object.
     * @return
     */
    public String getSummary();
    
    /**
     * Returns the text description of this schema object.
     * @return
     */
    public String getTextDescription();

}
