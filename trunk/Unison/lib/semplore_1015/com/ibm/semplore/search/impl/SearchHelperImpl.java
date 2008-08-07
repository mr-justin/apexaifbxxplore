/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SearchHelperImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.util.HashMap;

import com.ibm.semplore.search.SearchHelper;

/**
 * @author liu Qiaoling
 *
 */
public class SearchHelperImpl implements SearchHelper
{

    /**
     * the hash map of hints by type 
     */
    protected HashMap byType;
    
    /**
     * 
     */
    protected SearchHelperImpl() {
        byType = new HashMap();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchHelper#getHint(int, java.lang.Object)
     */
    public Object getHint(int hint_type, Object hint_key)
    {
        if (hint_key == null)
            hint_key = "";
        HashMap byKey = (HashMap)(byType.get(new Integer(hint_type)));
        if (byKey == null) 
            return null;
        
        return byKey.get(hint_key); 
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchHelper#setHint(int, java.lang.Object, java.lang.Object)
     */
    public void setHint(int hint_type, Object hint_key, Object hint)
    {
        if (hint_key == null)
            hint_key = "";
        HashMap byKey = (HashMap)(byType.get(new Integer(hint_type)));
        if (byKey == null) {
            byKey = new HashMap();
            byType.put(new Integer(hint_type), byKey);
        }
        byKey.put(hint_key, hint);
    }

}
