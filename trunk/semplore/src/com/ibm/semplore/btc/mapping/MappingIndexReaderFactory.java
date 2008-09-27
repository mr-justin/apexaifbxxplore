/**
 * 
 */
package com.ibm.semplore.btc.mapping;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author xrsun
 *
 */
public class MappingIndexReaderFactory {
	private static File mappingIndexLoc;
	
	private static HashMap<String, MappingIndexReader> readerPool;
	
	public static void init(File _mappingIndexLoc) {
		mappingIndexLoc = _mappingIndexLoc;
		if (readerPool==null) readerPool = new HashMap<String, MappingIndexReader>();
	}
	
	public static MappingIndexReader getMappingIndexReader(String file) throws IOException {
		MappingIndexReader reader = readerPool.get(file);
		if (reader == null) {
			reader = new MappingIndexReader(mappingIndexLoc.getPath() + File.separatorChar + file);
			readerPool.put(file, reader);
		}
		return reader;
	}

}
