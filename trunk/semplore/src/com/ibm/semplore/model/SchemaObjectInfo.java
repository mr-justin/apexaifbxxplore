/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaObjectInfo.java,v 1.3 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.model;

/**
 * @author liu qiaoling
 *
 */
public interface SchemaObjectInfo
{
  
	public long getID();
	
    /**
     * Returns the URI of this schema object.
     * @return
     */
    public String getURI();
    
    /**
     * @return unescaped local name of URI
     */
    public String getURILocalName();
        
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
