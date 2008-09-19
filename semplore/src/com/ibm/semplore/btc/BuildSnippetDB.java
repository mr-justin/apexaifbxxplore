/**
 * 
 */
package com.ibm.semplore.btc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author xrsun
 * 
 */
public class BuildSnippetDB {

	/**
	 * @param args
	 * @throws DatabaseException
	 * @throws EnvironmentLockedException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws EnvironmentLockedException,
			DatabaseException, IOException {
		Environment myDbEnvironment = null;
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		myDbEnvironment = new Environment(new File(args[0]), envConfig);
		EntityStore store = new EntityStore(myDbEnvironment, "EntityStore",
				storeConfig);
		PrimaryIndex<String, InstanceEntityClass> pidx = store.getPrimaryIndex(String.class, InstanceEntityClass.class);


		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		String last = null;
		InstanceEntityClass ent = new InstanceEntityClass();
		while ((line=reader.readLine())!=null) {
			String[] triple = line.replaceAll("\t", " ").split(" ");
			int i = 3;
			while (triple.length > i) {
				if (triple[i].equals("."))
					break;
				triple[2] = triple[2] + " " + triple[i];
				i++;
			}
			if (triple.length < 3)
				continue;

			if (last==null || !triple[0].equals(last)) {
				if (last!=null) pidx.put(ent);
				ent = new InstanceEntityClass();
				ent.setPKey(triple[0]);
				last = triple[0];
			}
			ent.addSnippet(triple[1]+"\t"+triple[2]);
		}

		store.close();
		myDbEnvironment.cleanLog();
		myDbEnvironment.close();
	}

}

@Entity
class InstanceEntityClass {
	// Primary key is pKey
	@PrimaryKey
	private String uri;
	private String data = "";

	public void setPKey(String data) {
		uri = data;
	}

	public String getPKey() {
		return uri;
	}
	
	public void addSnippet(String s) {
		data += s + "\n";
	}
	
	public String getData() {
		return data;
	}
}