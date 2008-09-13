package com.ibm.semplore.imports.impl;

/**
 * @author ÜñÂó
 * 1)Read triple of globalID's from distinct files
 * 2)Find corresponding localID of subjects(rel), objects(rel), relations ,categories.
 * 3)Retrieve URL for each globalID used to creat new instances/relations/categories...
 * @return instanceDocument with all the info of a instance.
 * **List is a list of bindings which is in form of (URL, LocalID).
 */
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.InstanceDocumentIterator;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;
import com.ibm.semplore.util.TestUnicode;

public class InstanceDocumentIteratorImpl implements InstanceDocumentIterator {
	static SchemaFactory factory = SchemaFactoryImpl.getInstance();

	static Config config = new Config();

	// "dbpedia + sample = // sample/tmp/"

	private Scanner dataReader;
	private int lineno;

	Long lastID = null;
	
	int objLocalID, sbjLocalID, relLocalID, invRelLocalID, catLocalID;
	HashMap<String, long[]> idMap = new HashMap<String,long[]>();

	InstanceDocumentImpl instance;

    protected RandomAccessFile file = null;

	int findRelLocalID(long rel) {
		return findInFile(config.dir + config.relLocal, rel);
	}

	int findCatLocalID(long cat) {
		return findInFile(config.dir + config.catLocal, cat);
	}

	int findSubLocalID(long rel, long sub) {
		return findInFile(config.dir + "RS_" + rel, sub);
	}

	int findObjLocalID(long rel, long obj) {
		return findInFile(config.dir + "RO_" + rel, obj);
	}

	int findInFile(String filename, long rel) {
		if (idMap.get(filename)!=null) {
			int i = Arrays.binarySearch(idMap.get(filename),rel);
			if (i<0) return -1; else return i;
		}
//		System.out.print("Read "+filename);
		DataInputStream file = null;
		try {
			file = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		long[] list = null;
		try {
			File f = new File(filename);
			long len = f.length()/8;
//			System.out.print("["+len+"]");
			list = new long[(int)len];
			for (int i=0; i<len ; i++) {
				long l = file.readLong();
				list[i]=l;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.print(".");
		idMap.put(filename, list);
//		System.out.print(".");
		int j = Arrays.binarySearch(list,rel);
		if (j<0) return -1; else return j;
	}

	InstanceDocumentIteratorImpl(Scanner dataReader) {
		this.dataReader = dataReader;
		lineno = 1;
	}

	public InstanceDocumentImpl next() {
		long s, p = 0, o;
		String itype;
		String attr = null;
		String obj;
		InstanceDocumentImpl tmpIns;
		Pattern pattern = Pattern.compile("\t");
		
		do {
			tmpIns = null;
			s = dataReader.nextLong();
			itype = dataReader.next();
			dataReader.skip(pattern);
			if (itype.equals(Util4NT.ATTRIBUTE))
				attr = dataReader.next();
			else 
				p = dataReader.nextLong();
			dataReader.skip(pattern);
			obj = null;
			
			if (lastID==null) {
				lastID = s; 
				instance = new InstanceDocumentImpl(factory.createInstance(s));
			} else if (lastID != s) {
				lastID = s;
				tmpIns = instance;
				instance = new InstanceDocumentImpl(factory.createInstance(s));
			}
			
			if (itype.equals("URI")) {
				obj = dataReader.nextLine();
				instance.setURI(obj);
			}
			else if (itype.equals("TYPE")) {
				o = dataReader.nextLong();
				if (o==Md5_BloomFilter_64bit.HASH_TYPE_CATEGORY)
					instance.setObjectType(Category.class);
				else if (o==Md5_BloomFilter_64bit.HASH_TYPE_RELATION)
					instance.setObjectType(Relation.class);
				else if (o==Md5_BloomFilter_64bit.HASH_TYPE_INSTANCE)
					instance.setObjectType(Instance.class);
				else throw new Error("wrong type hash:"+o);
			}
			else if (itype.equals(Util4NT.CATEGORY)) {
				o = dataReader.nextLong();
				catLocalID = findCatLocalID(o);
				instance.addCategory(factory.createCategory(o), catLocalID);
			}
			else if (itype.equals(Util4NT.ATTRIBUTE)) {
				obj = dataReader.nextLine();
				obj = TestUnicode.parse(obj);
				instance.addAttribute(attr, obj);
			}
			else if (itype.equals(Util4NT.RELATION)) {
				o = dataReader.nextLong();
				relLocalID = findRelLocalID(p);
				objLocalID = findObjLocalID(p, o);
				instance.addRelation(factory.createRelation(p),
						factory.createInstance(o), relLocalID,
						objLocalID);
			}
			else if (itype.equals(Util4NT.INVRELATION)) {
				o = dataReader.nextLong();
				invRelLocalID = findRelLocalID(p);
				sbjLocalID = findSubLocalID(p, o);
				instance.addInverseRelation(factory.createRelation(p),
						factory.createInstance(o), invRelLocalID,
						sbjLocalID);
			}
			else throw new Error("wrong itype: "+itype);
			
			if (lineno++ % 100000 == 0) System.out.println("hashdata: "+lineno);
			if (tmpIns != null)	return tmpIns;
		} while (dataReader.hasNextLong());
		return instance;
	}

	public boolean hasNext() {
		return dataReader.hasNextLong();
	}

	public void close() {
		dataReader.close();
	}

	public InstanceDocumentIteratorImpl(String configFile) throws Exception {
		Properties config = Config.readConfigFile(configFile);
		Config.BSDDir = config.getProperty(Config.TMP_DIR)+"/";
		Config.dir = config.getProperty(Config.DATA_FOR_INDEX_DIR)+"/";
	}
	public static void main(String args[]) throws Exception {
		InstanceDocumentIteratorImpl i = new InstanceDocumentIteratorImpl((Scanner)null);
		i.findInFile("d:\\User\\xrsun\\btc\\uscensus\\data/RO_4271085579369040925", 0);
		if (true) return;
		InstanceDocumentIteratorImpl ins = new InstanceDocumentIteratorImpl(args[0]);
		long sbj = 2695145;
		long rel = 3680827;
		long obj = 838565;
//		String cat = "30558";
		System.out.println("2695145	3680827	838565's local = "
				+ ins.findSubLocalID(rel, sbj) + " " /*+ ins.findRelLocalID(rel)*/
				+ " " + ins.findObjLocalID(rel, obj));
//		System.out.println("cat = " + cat + " : " + ins.findCatLocalID(cat));
	}
}