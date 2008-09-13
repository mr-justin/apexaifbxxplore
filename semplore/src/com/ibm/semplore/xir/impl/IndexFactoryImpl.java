/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IndexFactoryImpl.java,v 1.3 2008/01/10 11:15:29 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.util.Properties;

import com.ibm.semplore.xir.IndexFactory;
import com.ibm.semplore.xir.IndexService;

/**
 * @author zhangjie
 *
 */
public class IndexFactoryImpl implements IndexFactory {
	
	private static final IndexFactory factory;
	
	static{
		factory = new IndexFactoryImpl();
	}
	
	public static IndexFactory getInstance(){
		return factory;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexFactory#getIndexService(java.util.Properties)
	 */
	public IndexService getIndexService(Properties config) {
		if( config==null ) throw new NullPointerException();		
        IndexService service = new IndexServiceImpl(config);
//        if (config.getProperty(Config.INDEX_SERVICE,"").contains("For3"))
//            service = new IndexServiceImplFor3(config);
		return service;
	}

}
