package com.ibm.semplore.imports.impl;

/**
 * @author ÜñÂó
 * 1)Read triple of globalID's from distinct files
 * 2)Find corresponding localID of subjects(rel), objects(rel), relations ,categories.
 * 3)Retrieve URL for each globalID used to creat new instances/relations/categories...
 * @return instanceDocument with all the info of a instance.
 * **List is a list of bindings which is in form of (URL, LocalID).
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.InstanceDocumentIterator;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;

public class InstanceDocumentIteratorImpl implements InstanceDocumentIterator {
	static SchemaFactory factory = SchemaFactoryImpl.getInstance();

	static Config config = new Config();

	static URI_ID_Dictionary dic = null;

	// "dbpedia + sample = // sample/tmp/"

	private BufferedReader catReader;

	private BufferedReader relReader;

	private BufferedReader invRelReader;
	private BufferedReader attReader;
	private String relTemp, sbj, rel, obj;

	private String invRelTemp, invSbj, invRel, invObj;
	private String attTemp, attIns, text;
	private String catTemp, ins, cat;

	private String split[];

	int objLocalID, sbjLocalID, relLocalID, invRelLocalID, catLocalID;

	InstanceDocumentImpl instance;

    protected RandomAccessFile file = null;

    String lastSbj = null;

	String lastInvObj = null;

	String lastIns = null;
	String lastAttIns = null;
	String min;

	String URL, sbjURL, objURL, relURL, catURL, attInsURL;

	/*
	 * The following three is to split a line and deliver them into certain
	 * strings.
	 */
	private void splitRel() {
		relTemp = relTemp.replaceAll(" ", "\t");
		split = relTemp.split("\t");
		sbj = split[0];
		rel = split[1];
		obj = split[2];
		relLocalID = findRelLocalID(rel);
		objLocalID = findObjLocalID(rel, obj);
	}

	private void splitInvRel() {
		invRelTemp = invRelTemp.replaceAll(" ", "\t");
		split = invRelTemp.split("\t");
		invSbj = split[0];
		invRel = split[1];
		invObj = split[2];
		invRelLocalID = findRelLocalID(invRel);
		sbjLocalID = findSubLocalID(invRel, invSbj);
	}

	private void splitCat() {
		catTemp = catTemp.replaceAll(" ", "\t");
		split = catTemp.split("\t");
		ins = split[0];
		cat = split[2];
		catLocalID = findCatLocalID(cat);
	}
	private void splitAtt(){
//		attTemp = attTemp.replaceAll(" ", "\t");
		split = attTemp.split("\t");
		attIns = split[0];
		text = split[2];
	}
	int findRelLocalID(String rel) {
		return findInFile(config.dir + config.relLocal, new Integer(rel).intValue());
	}

	int findCatLocalID(String cat) {
		return findInFile(config.dir + config.catLocal, new Integer(cat).intValue());
	}

	int findSubLocalID(String rel, String sub) {
		return findInFile(config.dir + "RS_" + rel, new Integer(sub).intValue());
	}

	int findObjLocalID(String rel, String obj) {
		return findInFile(config.dir + "RO_" + rel, new Integer(obj).intValue());
	}

	int findInFile(String filename, int k) {
		try {
			// System.out.println(filename);
			file = new RandomAccessFile(filename, "r");
			int a = 0;
			int b = (int) (file.length() >> 2) - 1;
			int c;
			int cc;
			while (a <= b) {
				c = (a + b) / 2;
				file.seek(c << 2); // each integer 2 bytes. 32bit.
				cc = file.readInt();
				if (k < cc) {
					b = c - 1;
					continue;
				}
				if (k > cc) {
					a = c + 1;
					continue;
				}
				return c;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                }
            }
        }
		return -1;
	}

	InstanceDocumentIteratorImpl(BufferedReader relReader,
			BufferedReader inverseRelReader, BufferedReader catReader, BufferedReader attReader) {
		try {
			dic = new URI_ID_Dictionary(new File(Config.BSDDir));
			dic.init();
			this.relReader = relReader;
			this.catReader = catReader;
			this.invRelReader = inverseRelReader;
			this.attReader = attReader;
			relTemp = this.relReader.readLine();
			catTemp = this.catReader.readLine();
			invRelTemp = this.invRelReader.readLine();
			attTemp = this.attReader.readLine();
			if (catTemp != null)
				splitCat();
			if (relTemp != null)
				splitRel();
			if (invRelTemp != null)
				splitInvRel();
			if (attTemp != null) 
				splitAtt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * The three of the following is used to add a block of data with the same
	 * key instance in corresponding file.
	 */

	void addRel() {
			try {
				do {
					relURL = dic.getURI(Util4NT.RELATION, new Integer(rel)
							.intValue());
					objURL = dic.getURI(Util4NT.INSTANCE, new Integer(obj)
							.intValue());
					instance.addRelation(factory.createRelation(relURL),
							factory.createInstance(objURL), relLocalID,
							objLocalID);
					relTemp = relReader.readLine();
					// System.out.println(" "+relTemp);
					if (relTemp == null) {
						sbj = rel = obj = null;
						return;
					}
					lastSbj = sbj;
					splitRel();
					if (!lastSbj.equals(sbj))
						return;
				} while (true);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	void addInvRel() {
			try {

				do {
					relURL = dic.getURI(Util4NT.RELATION, new Integer(invRel)
							.intValue());
					sbjURL = dic.getURI(Util4NT.INSTANCE, new Integer(invSbj)
							.intValue());
					instance.addInverseRelation(factory.createRelation(relURL),
							factory.createInstance(sbjURL), invRelLocalID,
							sbjLocalID);
					invRelTemp = invRelReader.readLine();
					if (invRelTemp == null) {
						invSbj = invRel = invObj = null;
						return;
					}
					lastInvObj = invObj;
					splitInvRel();
					if (!lastInvObj.equals(invObj))
						return;
				} while (true);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	void addAtt(){
			try {
				do {
					attInsURL = dic.getURI(Util4NT.INSTANCE, new Integer(attIns)
							.intValue());
//					text = text.replaceAll("\"", "").replaceAll("\\.", "");
					instance.addText(text);
					attTemp = attReader.readLine();
					if (attTemp == null) {
						attIns = text = null;
						return;
					}
					lastAttIns = attIns;
					splitAtt();
					if (!lastAttIns.equals(attIns))
						return;
				} while (true);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	void addCat() {
		while (true) {
			try {
				catURL = dic.getURI(Util4NT.CATEGORY, new Integer(cat)
						.intValue());
				instance
						.addCategory(factory.createCategory(catURL), catLocalID);
				catTemp = catReader.readLine();
				if (catTemp == null) {
					ins = cat = null;
					return;
				}
				lastIns = ins;
				splitCat();
				if (!lastIns.equals(ins))
					return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public InstanceDocumentImpl next() {
		try {
			if (sbj == null && invSbj == null && ins == null && attIns == null)
				return null;
			/*
			 * Find the minist instance in s of relation ,o of inverse relation,
			 * i of catgory.
			 */

			min = sbj;
			if (ins != null
					&& ((min != null && new Integer(ins).compareTo(new Integer(
							min)) < 0) || min == null))
				min = ins;
			if ((invObj != null
					&& ((min != null && new Integer(invObj)
							.compareTo(new Integer(min)) < 0)) || min == null))
				min = invObj;
			if ((attIns != null
					&& ((min != null && new Integer(attIns)
							.compareTo(new Integer(min)) < 0)) || min == null))
				min = attIns;
//			System.out.println(min);
			/*
			 * Create the new instance of name min and add corresponding
			 * relations and categories.
			 */
			URL = dic.getURI(Util4NT.INSTANCE, new Integer(min).intValue());
			instance = new InstanceDocumentImpl(factory.createInstance(URL));
			
			if (sbj != null && sbj.equals(min))
				addRel();
			if (ins != null && ins.equals(min))
				addCat();
			if (invObj != null && invObj.equals(min))
				addInvRel();
			if (attIns != null && attIns.equals(min))
				addAtt();
//			Relation[] temp = instance.getRelationsGivenSubject();
//
//			if (min.equals("369"))
//				for (int i = 0; i < temp.length; i++)
//					System.out.println(temp[i].getURI() + " "
//							+ temp[i].getIDofURI());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return instance;
	}

	public boolean hasNext() {
		if (sbj == null && invSbj == null && ins == null)
			return false;
		return true;
	}

	public void close() {
		try {
			dic.close();
			relReader.close();
			catReader.close();
			invRelReader.close();
			attReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InstanceDocumentIteratorImpl(String configFile) throws Exception {
		Properties config = Config.readConfigFile(configFile);
		Config.BSDDir = config.getProperty(Config.TMP_DIR)+"/";
		Config.dir = config.getProperty(Config.DATA_FOR_INDEX_DIR)+"/";
	}
	public static void main(String args[]) throws Exception {
		InstanceDocumentIteratorImpl ins = new InstanceDocumentIteratorImpl(args[0]);
		String sbj = "2695145";
		String rel = "3680827";
		String obj = "838565";
//		String cat = "30558";
		System.out.println("2695145	3680827	838565's local = "
				+ ins.findSubLocalID(rel, sbj) + " " + ins.findRelLocalID(rel)
				+ " " + ins.findObjLocalID(rel, obj));
//		System.out.println("cat = " + cat + " : " + ins.findCatLocalID(cat));
	}
}