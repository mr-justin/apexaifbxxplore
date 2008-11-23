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
 * @author xrsun
 *
 */
public class QuerySnippetDB {
	static HashMap<String, PrimaryIndex<String, InstanceEntityClass>> pidxs = null;
	static File snippetLoc = null;

	public static void init(String datasrc) throws IOException {
		HashMap config = Config.readDSConfigFile(datasrc);
		snippetLoc = new File((String)config.get("snippet"));
		pidxs = new HashMap<String, PrimaryIndex<String,InstanceEntityClass>>();
	}
	
	public static void init(File snippetloc) {
		snippetLoc = snippetloc;
		pidxs = new HashMap<String, PrimaryIndex<String,InstanceEntityClass>>();
	}
	public static InstanceEntityClass getSnippet(String ds, String uri) throws DatabaseException {
		PrimaryIndex<String, InstanceEntityClass> pidx = pidxs.get(ds);
		if (pidx==null)	{
			try {
				pidx = loaddb(new File(snippetLoc.getPath()+File.separatorChar+ds));
				System.out.println("Loaded SnippetDB of " + ds);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			pidxs.put(ds, pidx);
		}
		return pidx.get(uri);
	}
	
	public static PrimaryIndex<String, InstanceEntityClass> loaddb(File db) throws EnvironmentLockedException, DatabaseException {
		Environment myDbEnvironment = null;
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setReadOnly(true);
		envConfig.setAllowCreate(false);
		storeConfig.setAllowCreate(false);
		storeConfig.setReadOnly(true);
		myDbEnvironment = new Environment(db, envConfig);
		EntityStore store = new EntityStore(myDbEnvironment, "EntityStore",
				storeConfig);
		return store.getPrimaryIndex(String.class, InstanceEntityClass.class);
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
		init("datasrc.cfg");
		
//		System.out.println(getSnippet("dbpedia", "<http://dbpedia.org/resource/Epol/Apple>"));
//		System.out.println(getSnippet("dbpedia", "<http://dbpedia.org/resource/apple>"));
		System.out.println(getSnippet("wordnet", "<http://www.w3.org/2006/03/wn/wn20/instances/wordsense-tom-tom-noun-1>").getData());
		System.out.println(getSnippet("wordnet", "<http://www.w3.org/2006/03/wn/wn20/instances/word-Tom>").getData());
		
		System.out.println(getSnippet("dblp", "<http://www.informatik.uni-trier.de/~ley/db/indices/a-tree/a/Andrew:A=.html>").getData());
		

		
		close();
	}

}
