/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestIndex.java,v 1.3 2008/01/10 10:12:45 lql Exp $
 */
package com.ibm.semplore.test;

import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.impl.OntologyRepositoryImpl;
import com.ibm.semplore.xir.IndexFactory;
import com.ibm.semplore.xir.IndexService;
import com.ibm.semplore.xir.impl.IndexFactoryImpl;

/**
 * @author liu qiaoling
 * 
 */
public class TestIndex {

	public static void main(String[] args) {
		try {
			IndexFactory indexFactory = IndexFactoryImpl.getInstance();
			for (int ii = 0; ii < args.length; ii++) {
				Properties config = Config.readConfigFile(args[ii]);
				IndexService indexService = indexFactory
						.getIndexService(config);
				indexService
						.buildWholeIndex(new OntologyRepositoryImpl(config));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
