/**
 * 
 */
package com.ibm.semplore.btc.mapping;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * A factory creating MappingIndexReader. The index's head will be read in only at the first time.
 * 
 * @author xrsun
 *
 */
public class MappingIndexReaderFactory {
	private static File mappingIndexLoc;
	
	private static HashMap<String, MappingIndexReader> readerPool;

	public static boolean hasMappingIndex(String file)
	{
		if (readerPool==null) return false;
		else 
			return new File(mappingIndexLoc.getPath() + File.separatorChar + file + ".head").exists();
	}
	
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
