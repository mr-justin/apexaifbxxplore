/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaObjectInfoImpl.java,v 1.6 2008/09/10 06:02:04 xrsun Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.SchemaObjectInfo;

/**
 * @author liu qiaoling
 *
 */
public class SchemaObjectInfoImpl implements SchemaObjectInfo
{
	protected long id;
    /**
     * the label info 
     */
    protected String label;
    
    /**
     * the summary info
     */
    protected String summary;
    
    /**
     * the text description info
     */
    protected String text;
    
    /**
     * the uri info 
     */
    protected String uri;
    
    /**
     * @param uri
     * @param label
     * @param summary
     * @param text
     */
    protected SchemaObjectInfoImpl(long id, String uri, String label, String summary, String text) {
    	this.id = id;
        this.uri = uri;
        this.label = label;
        this.summary = summary;
        this.text = text;
        
        if (this.label == null)
            this.label = getDefaultLabel();
        if (this.summary == null)
            this.summary = getDefaultSummary();
        if (this.text == null || this.text.length()==0)
            this.text = getDefaultTextDescription();
    }
    
    /**
     * Get default label based on URI.
     * @param uri
     * @return
     */
    protected String getDefaultLabel() {
    	String label = null;
    	if (uri==null) return "";
        if (uri.lastIndexOf('#') >= 0) {
            label = uri.substring(uri.lastIndexOf('#')+1);
        } else if (uri.lastIndexOf('(') >=0 && uri.lastIndexOf(')') >=0 && uri.lastIndexOf(')')>uri.lastIndexOf('(')) {
        	label = uri.substring(uri.lastIndexOf('(')+1,uri.lastIndexOf(')'));
        } else if (uri.lastIndexOf('/') >=0) {
            label = uri.substring(uri.lastIndexOf('/')+1);
        } else
        	label = uri;
        label = label.replace("_"," ").replace("-"," ").replace("<", " ").replace(">"," ").replace("(", " ").replace(")"," ").trim();
        if (label.equals(""))
            label = uri;
        label = label.replace("_"," ").replace("-"," ").replace("<", " ").replace(">"," ").replace("(", " ").replace(")"," ").trim();
        return label;
    }
    
    /**
     * Get default summary based on label.
     * @return
     */
    protected String getDefaultSummary() {
        return getDefaultLabel();
    }
    
    /**
     * Get default text description based on label.
     * @return
     */
    protected String getDefaultTextDescription() {
        return getDefaultLabel();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObjectInfo#getLabel()
     */
    public String getLabel()
    {
        return label;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObjectInfo#getSummary()
     */
    public String getSummary()
    {
        return summary;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObjectInfo#getTextDescription()
     */
    public String getTextDescription()
    {
        return text;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObjectInfo#getURI()
     */
    public String getURI()
    {
        return uri;
    }
    
    public long getID()
    {
    	return id;
    }
    

}
