/**
 * 
 */
package com.ibm.semplore.btc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.ibm.semplore.config.Config;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * @author Administrator
 *
 */
public class QuerySnippetDB {
	static HashMap<String, PrimaryIndex<String, InstanceSnippetEntityClass>> pidxs = null;
	static File snippetLoc = null;

	public static void init(File datasrc) throws IOException {
		HashMap config = Config.readDSConfigFile(datasrc.getAbsolutePath());
		snippetLoc = new File((String)config.get("snippet"));
	}
	
	public static InstanceSnippetEntityClass getSnippet(String ds, String uri) throws DatabaseException {
		PrimaryIndex<String, InstanceSnippetEntityClass> pidx = pidxs.get(ds);
		if (pidx==null)	{
			try {
				pidx = loaddb(new File(snippetLoc.getPath()+File.separatorChar+ds));
			} catch (Exception e) {
				return null;
			}
			pidxs.put(ds, pidx);
		}
		return pidx.get(uri);
	}
	
	public static PrimaryIndex<String, InstanceSnippetEntityClass> loaddb(File db) throws EnvironmentLockedException, DatabaseException {
		Environment myDbEnvironment = null;
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(false);
		storeConfig.setAllowCreate(false);
		myDbEnvironment = new Environment(db, envConfig);
		EntityStore store = new EntityStore(myDbEnvironment, "EntityStore",
				storeConfig);
		return store.getPrimaryIndex(String.class, InstanceSnippetEntityClass.class);
	}
	
	public static void close() {
//		store.close();
//		myDbEnvironment.close();
	}
	/**
	 * @param args
	 * @throws DatabaseException 
	 * @throws EnvironmentLockedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws EnvironmentLockedException, DatabaseException, IOException {
		//must be called at beginning of servlet
		init(new File("config"+File.separatorChar+"datasrc.cfg"));
		
		System.out.println(getSnippet("dbpedia", "<a>"));
		
		close();
	}

}
