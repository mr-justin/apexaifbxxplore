/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2004,2007 Oracle.  All rights reserved.
 *
 * $Id: SimpleExample.java,v 1.48.2.1 2007/02/01 14:49:36 cwl Exp $
 */

package com.ibm.semplore.imports.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * SimpleExample creates a database environment, a database, and a database
 * cursor, inserts and retrieves data.
 */
public class URI_ID_Dictionary {
	private static final int EXIT_SUCCESS = 0;
	private static final int EXIT_FAILURE = 1;
	
	private File envDir;
	private File relInsFile;
	private File attrInsFile;
	private File catInsFile;
	
	private Database DB_URI2ID_i;
	public Database DB_ID2URI_i;
	private Database DB_URI2ID_cra;
	public Database DB_ID2URI_cra;
	private Environment exampleEnv;
	private boolean exist;

	public URI_ID_Dictionary(File envDir) {
		this(envDir,null,null,null);
	}

	public URI_ID_Dictionary(File envDir, File relInsFile, File catInsFile, 
			File attrInsFile) {
		this.envDir = envDir;
		this.relInsFile = relInsFile;
		this.catInsFile = catInsFile;
		this.attrInsFile = attrInsFile;		
	}
	
	public void close() throws DatabaseException {
		DB_URI2ID_cra.close();
		DB_ID2URI_cra.close();
		DB_URI2ID_i.close();
		DB_ID2URI_i.close();
		exampleEnv.close();
	}
	
	public void init() throws DatabaseException {
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		exampleEnv = new Environment(envDir, envConfig);

//		try {
//			exampleEnv.removeDatabase(null, "URI2ID");
//			exampleEnv.removeDatabase(null, "ID2URI");
//		} catch (Exception e) {			
//		}
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		List names = exampleEnv.getDatabaseNames();		
		exist = false;
		for (int i=0; i<names.size(); i++) {
			if (((String)(names.get(i))).equals("URI2ID_i"))
				exist = true;
		}
		DB_URI2ID_i = exampleEnv.openDatabase(null, "URI2ID_i", dbConfig);
		DB_ID2URI_i = exampleEnv.openDatabase(null, "ID2URI_i", dbConfig);
		DB_URI2ID_cra = exampleEnv.openDatabase(null, "URI2ID_cra", dbConfig);
		DB_ID2URI_cra = exampleEnv.openDatabase(null, "ID2URI_cra", dbConfig);
	}

	/**
	 * Usage string
	 */
	public static void usage() {
		System.out
				.println("usage: java "
						+ "je.SimpleExample "
						+ "<dbEnvHomeDirectory> "
						+ "[relationInstance nt file] [categoryInstance nt file] [attributeInstance nt file]");
		System.exit(EXIT_FAILURE);
	}

	/**
	 * Main
	 */
	public static void main(String argv[]) {

		if (argv.length < 1) {
			usage();
			return;
		}

		try {
			File relInsFile = null;
			File attrInsFile = null;
			File catInsFile = null;
			if (argv.length >= 2) relInsFile = new File(argv[1]);
			if (argv.length >= 3) catInsFile = new File(argv[2]);
			if (argv.length >= 4) attrInsFile = new File(argv[3]);
			URI_ID_Dictionary app = new URI_ID_Dictionary(new File(argv[0]),
					relInsFile, catInsFile, attrInsFile);
			app.init();
			app.buildDictionary();
			app.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.exit(EXIT_FAILURE);
		}
		System.exit(EXIT_SUCCESS);
	}

	private void addToDB(Database DB_URI2ID, Database DB_ID2URI, String URI, int ID
			, boolean addToDB_URI2ID, boolean addToDB_ID2URI) throws DatabaseException {
		
		DatabaseEntry URIEntry = new DatabaseEntry();
		DatabaseEntry IDEntry = new DatabaseEntry();
		StringBinding.stringToEntry(URI, URIEntry);
		IntegerBinding.intToEntry(ID, IDEntry);
		
		if (addToDB_URI2ID) {
			OperationStatus status = DB_URI2ID.put(null, URIEntry, IDEntry);
			if (status != OperationStatus.SUCCESS) {
				throw new DatabaseException("Data insertion got status "
						+ status);
			}
		}

		if (addToDB_ID2URI) {
			OperationStatus status = DB_ID2URI.put(null, IDEntry, URIEntry);		
			if (status != OperationStatus.SUCCESS) {
				throw new DatabaseException("Data insertion got status "
						+ status);
			}
		}
	}
	
	private Database getDB_URI2ID(String type) {
		if (type == null)
			return null;
		if (type != Util4NT.INSTANCE) 
			return DB_URI2ID_cra;
		else
			return DB_URI2ID_i;
	}
	
	private Database getDB_ID2URI(String type) {
		if (type == null)
			return null;
		if (type != Util4NT.INSTANCE) 
			return DB_ID2URI_cra;
		else
			return DB_ID2URI_i;
	}
	
	private void addToDB(String type, String URI, int ID
			, boolean addToDB_URI2ID, boolean addToDB_ID2URI) throws DatabaseException {
		
		if (type==null) {
			throw new DatabaseException("URI type cannot be null");
		}
		URI = type + URI;
		addToDB(getDB_URI2ID(type),getDB_ID2URI(type),URI,ID,addToDB_URI2ID,addToDB_ID2URI);		
	}
	
	private void URI2ID() throws IOException,DatabaseException {
		File URIfile = new File(relInsFile.getParent()+File.separator+"URI2ID.txt");
		PrintStream out = new PrintStream(URIfile);
		DatabaseEntry URIEntry = new DatabaseEntry();
		DatabaseEntry IDEntry = new DatabaseEntry();
		
		Database DB_URI2ID = DB_URI2ID_i;
		Database DB_ID2URI = DB_ID2URI_i;
		Cursor cursor = DB_URI2ID.openCursor(null, null);
		int id = 0;
		while (cursor.getNext(URIEntry, IDEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			String URI = StringBinding.entryToString(URIEntry); 
			addToDB(DB_URI2ID,DB_ID2URI,URI,id,true,true);
			out.println(StringBinding.entryToString(URIEntry)
					+ " " + id++);
		}
		cursor.close();
		
		DB_URI2ID = DB_URI2ID_cra;
		DB_ID2URI = DB_ID2URI_cra;
		cursor = DB_URI2ID.openCursor(null, null);
		while (cursor.getNext(URIEntry, IDEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			String URI = StringBinding.entryToString(URIEntry); 
			addToDB(DB_URI2ID,DB_ID2URI,URI,id,true,true);
			out.println(StringBinding.entryToString(URIEntry)
					+ " " + id++);
		}
		cursor.close();
		out.close();		
	}
	
	public int getID(String type, String URI) throws DatabaseException {
		if (type==null) {
			throw new DatabaseException("URI type cannot be null");
		}
		URI = type + URI;
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		StringBinding.stringToEntry(URI, keyEntry);
		OperationStatus status = getDB_URI2ID(type).get(null, keyEntry, dataEntry, LockMode.DEFAULT);				
		if (status == OperationStatus.SUCCESS) {
			return IntegerBinding.entryToInt(dataEntry);
		} else {
//			System.out.println("not found: "+type+" "+URI);
			return -1;
//			throw new DatabaseException("fail to get data "
//					+ status);
		}
	}
	
	public String getURI(String type, int id) throws DatabaseException {
		if (type==null) {
			throw new DatabaseException("URI type cannot be null");
		}
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();
		IntegerBinding.intToEntry(id, keyEntry);
		OperationStatus status = getDB_ID2URI(type).get(null, keyEntry, dataEntry, LockMode.DEFAULT);				
		if (status == OperationStatus.SUCCESS) {
			return StringBinding.entryToString(dataEntry).replaceFirst(type, "");
		} else {
			throw new DatabaseException("fail to get data "
					+ status);
		}
	}
	
	/**
	 * Insert or retrieve data
	 */
	public void buildDictionary() throws DatabaseException {
		long time_b = System.currentTimeMillis();
		try {
			if (exist) {
				System.out.println("dictionary exists!");
			} else {			
				BufferedReader in = null;
					//relInsFile
					System.out.println("reading file:" + relInsFile.getName());
					in = new BufferedReader(new FileReader(relInsFile));
					String str2 = null;
					while ((str2=in.readLine())!=null) {
						String[] triple = str2.replaceAll("\t", " ").split(" ");
						if (Util4NT.checkTripleType(triple)!=Util4NT.RELATION)
							continue;
						
						addToDB(Util4NT.INSTANCE,triple[0],0,true,false);
						addToDB(Util4NT.RELATION,triple[1],0,true,false);
						addToDB(Util4NT.INSTANCE,triple[2],0,true,false);
					}
					in.close();
					
					// catInsFile
					if (catInsFile!=null) {
						System.out.println("reading file:" + catInsFile.getName());
						in = new BufferedReader(new FileReader(catInsFile));
						str2 = null;
						addToDB(Util4NT.RELATION,Util4NT.TYPE,0,true,false);
						while ((str2=in.readLine())!=null) {
							String[] triple = str2.replaceAll("\t", " ").split(" ");
							if (Util4NT.checkTripleType(triple)!=Util4NT.CATEGORY)
								continue;
							
							addToDB(Util4NT.INSTANCE,triple[0],0,true,false);
							addToDB(Util4NT.CATEGORY,triple[2],0,true,false);
						}
						in.close();
					}
					
					// attrInsFile
					if (attrInsFile!=null) {
						System.out.println("reading file:" + attrInsFile.getName());
						in = new BufferedReader(new FileReader(attrInsFile));
						str2 = null;
						while ((str2=in.readLine())!=null) {
							String[] triple = str2.replaceAll("\t", " ").split(" ");
							if (Util4NT.checkTripleType(triple)!=Util4NT.ATTRIBUTE)
								continue;
							
							addToDB(Util4NT.INSTANCE,triple[0],0,true,false);
							addToDB(Util4NT.ATTRIBUTE,triple[1],0,true,false);
						}
						in.close();
					}
					
					// print URI2ID.txt
					System.out.println("print URI2ID.txt file...");
					URI2ID();
			}

			int relInsSize = 0;
			int catInsSize = 0;
			int attrInsSize = 0;

			//generate relInsFile.ID
			System.out.println("generating file:" + relInsFile.getName()+".relID");
			BufferedReader in = new BufferedReader(new FileReader(relInsFile));
			PrintStream out = new PrintStream(new File(relInsFile.getAbsoluteFile()+".relID"));
			String str2 = null;
			while ((str2=in.readLine())!=null) {
				String[] triple = str2.replaceAll("\t", " ").split(" ");
				if (Util4NT.checkTripleType(triple)!=Util4NT.RELATION)
					continue;

				relInsSize++;
				StringBuffer sb = new StringBuffer();
				int id = getID(Util4NT.INSTANCE,triple[0]);
				sb.append(id);
				sb.append("\t");
				id = getID(Util4NT.RELATION,triple[1]);
				sb.append(id);
				sb.append("\t");
				id = getID(Util4NT.INSTANCE,triple[2]);
				sb.append(id);
				sb.append("\t");
				sb.append(".");
				out.println(sb.toString());
			}
			in.close();
			out.close();
			
			//generate catInsFile.ID
			if (catInsFile!=null) {
				System.out.println("generating file:" + catInsFile.getName()+".catID");
				in = new BufferedReader(new FileReader(catInsFile));
				out = new PrintStream(new File(catInsFile.getAbsoluteFile()+".catID"));
				int id1 = getID(Util4NT.RELATION,Util4NT.TYPE);
				while ((str2=in.readLine())!=null) {
					String[] triple = str2.replaceAll("\t", " ").split(" ");
					if (Util4NT.checkTripleType(triple)!=Util4NT.CATEGORY)
						continue;
	
					catInsSize++;
					StringBuffer sb = new StringBuffer();
					int id = getID(Util4NT.INSTANCE,triple[0]);
					sb.append(id);
					sb.append("\t");
					sb.append(id1);
					sb.append("\t");
					id = getID(Util4NT.CATEGORY,triple[2]);
					sb.append(id);
					sb.append("\t");
					sb.append(".");
					out.println(sb.toString());
				}
				in.close();
				out.close();
			}

			//generate attrInsFile.ID
			if (attrInsFile!=null) {
				System.out.println("generating file:" + attrInsFile.getName()+".attrID");
				in = new BufferedReader(new FileReader(attrInsFile));
				out = new PrintStream(new File(attrInsFile.getAbsoluteFile()+".attrID"));
				while ((str2=in.readLine())!=null) {
					String[] triple = str2.replaceAll("\t", " ").split(" ");
					if (Util4NT.checkTripleType(triple)!=Util4NT.ATTRIBUTE)
						continue;
	
					StringBuffer sb = new StringBuffer();
					int id = getID(Util4NT.INSTANCE,triple[0]);
					if (id<0) continue;
					sb.append(id);
					sb.append("\t");
//					id = getID(Util4NT.ATTRIBUTE,triple[1]);
//					sb.append(id);
					sb.append(triple[1]);
					sb.append("\t");
					for (int i=2; i<triple.length; i++) {
						sb.append(triple[i]);
						sb.append(" ");
					}
					out.println(sb.toString());
					attrInsSize++;
				}
				in.close();
				out.close();
			}
			
			out = new PrintStream(relInsFile.getParent()+File.separator+"ontologyDimension.txt");
			out.println("instance size: "+DB_URI2ID_i.count());
			out.println("category+relation+attribute size: "+DB_URI2ID_cra.count());
			out.println("relation instance size: "+relInsSize);
			out.println("category instance size: "+catInsSize);
			out.println("attribute instance size: "+attrInsSize);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long time_e = System.currentTimeMillis();
		System.out.println("time: "+(time_e-time_b)+" ms");
	}
}
