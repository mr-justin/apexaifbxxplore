package org.ateam.xxplore.core.service.q2semantic;

import java.io.File;
import java.util.TreeSet;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


public class BerkeleyDB {

	// for ref
	// http://cllovelf.blog.chinajavaworld.com/entry.jspa?id=2824
	private Environment myDbEnvironment = null;
	private Database myDatabase = null;
	private Cursor myCursor = null;
	private boolean dup;
	
	//refresh the db when buffer is full
	private int buffer =  10000000, count =0;
	private String env, name;
	
	/**
	 * open the db
	 * @param envPath
	 * @param dbName
	 * @throws Exception
	 */
	public void openDB(String envPath, String dbName, boolean dup)throws Exception 
	{
		this.env = envPath;
		this.name = dbName;
		
		File dir = new File(envPath);
		if (!dir.exists())
			dir.mkdir();
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		myDbEnvironment = new Environment(new File(envPath), envConfig);
		
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setSortedDuplicates(dup);

		this.dup = dup;
		myDatabase = myDbEnvironment.openDatabase(null, dbName, dbConfig);
		myCursor = myDatabase.openCursor(null, null);
	}

	/**
	 * close the DB
	 * @throws Exception
	 */
	public void closeDB() throws Exception {
		if (myCursor != null)
			myCursor.close();
		if (myDatabase != null)
			myDatabase.close();
		if (myDbEnvironment != null) 
			myDbEnvironment.close();
	}
	
	public void flush() throws Exception
	{
		closeDB();
		openDB(env, name, dup);
	}

	/**
	 * set the buffersize
	 * @param b
	 */
	public void setBufferSize(int b)
	{
		this.buffer = b;
	}
	
	/**
	 * get the buffersize
	 * @return
	 */
	public int getBufferSize()
	{
		return this.buffer;
	}
	
	/**
	 * put the pair into db
	 * @param key
	 * @param data
	 * @throws Exception
	 */
	public void put(String key, String data) throws Exception 
	{
		//refresh
		if(++count%buffer==0)
		{
			closeDB();
			openDB(env, name, dup);
		}
		if (myDbEnvironment == null || myDatabase == null || myCursor == null) {
			System.err.println("DB not open!");
			return;
		}
		DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
		DatabaseEntry theData = new DatabaseEntry(data.getBytes("UTF-8"));
		myCursor.put(theKey, theData);
	}

	/**
	 * search the key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public TreeSet<String> search(String key) throws Exception 
	{
		if(--count%buffer==0)
		{
			closeDB();
			openDB(env, name, dup);
		}
		TreeSet<String> res = null;
		DatabaseEntry foundKey = new DatabaseEntry(key.getBytes("UTF-8"));
		DatabaseEntry foundData = new DatabaseEntry();

		OperationStatus retVal = myCursor.getSearchKey(foundKey, foundData,LockMode.DEFAULT);

		if (retVal == OperationStatus.SUCCESS && myCursor.count() >= 1)
		{
			res = new TreeSet<String>();
			while (retVal == OperationStatus.SUCCESS) 
			{
				String dataString = new String(foundData.getData(), "UTF-8");
				res.add(dataString);
				if(!dup) return res;
				// System.out.println("Key | Data : " + keyString + " | " + dataString + "");
				retVal = myCursor.getNextDup(foundKey, foundData,LockMode.DEFAULT);
			}
		}
		return res;
	}

	/**
	 * reset the cursor
	 * @throws Exception
	 */
	public void reSetCursor() throws Exception {
		if (myCursor != null) 
			myCursor.close();
		if (myDatabase != null)
			myCursor = myDatabase.openCursor(null, null);
	}

	/**
	 * print all the pairs
	 * @throws Exception
	 */
	public void printAllTriple() throws Exception {
		reSetCursor();
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();

		while (myCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			String keyString = new String(foundKey.getData(), "UTF-8");
			String dataString = new String(foundData.getData(), "UTF-8");

			System.out.println("K: " + keyString + " | D: " + dataString);
		}
	}
	
	public static void main(String[] args) throws Exception {
		BerkeleyDB db = new BerkeleyDB();
		db.openDB("D:/semplore/berkeley", "db", false);
		db.put("key1", "data1");
		db.put("key1", "data2");
		db.put("key2", "data1");
		db.put("key2", "data1");
		db.printAllTriple();
		db.closeDB();
	}
}
