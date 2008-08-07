package com.ibm.semplore.imports.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.InstanceDocumentIterator;
import com.ibm.semplore.imports.IteratableOntologyRepository;
import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.LiteralsOfProperty;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.sleepycat.je.DatabaseException;

public class OntologyRepositoryImpl implements IteratableOntologyRepository {
	Config config = new Config();

	URI_ID_Dictionary dic = null;

	String categoryDir = null;

	String inverseRelationDir = null;

	String relationDir = null;
	String attributeDir = null;
	int relationSize;

	int attributeSize = 0;

	int instanceSize = 0;

	int relationTripleSize = 0;

	int categoryTripleSize = 0;
	int attributeTripleSize = 0;
	int categorySize = 0;
	
	
	SchemaFactory factory = SchemaFactoryImpl.getInstance();

	public OntologyRepositoryImpl(Properties config) {
		try {
			Config.BSDDir = config.getProperty(Config.TMP_DIR)+"/";
			Config.dir = config.getProperty(Config.DATA_FOR_INDEX_DIR)+"/";
			dic = new URI_ID_Dictionary(new File(config.getProperty(Config.TMP_DIR)));
			dic.init();
			categoryDir = Config.dir + Config.cat;

			inverseRelationDir = Config.dir + Config.invRel;

			relationDir = Config.dir + Config.rel;
			attributeDir = Config.dir + Config.att;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public int getAttributeSize() {
		return attributeSize;
	}

	public Attribute[] getAttributes() {
		return new Attribute[0];
	}

	public int getInstanceOfTripleSize() {
		return categoryTripleSize;
	}

	public Category[] getCategories() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					config.dir + config.catLocal + "Temp"));
			ArrayList cat = new ArrayList();
			String temp;
			while ((temp = reader.readLine()) != null) {
				cat.add(factory.createCategory(dic.getURI(Util4NT.CATEGORY,
						new Integer(temp).intValue())));
				categorySize++;
			}
			reader.close();
			return (Category[]) cat.toArray(new Category[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getCategorySize() {
		return categorySize;
	}

	public int getInstanceSize() {
		return instanceSize;
	}

	public LiteralsOfProperty[] getLiteralsOfProperties(Category cat) {
		return new LiteralsOfProperty[0];
	}

	public LiteralsOfProperty[] getLiteralsOfProperties(Relation rel) {
		return new LiteralsOfProperty[0];
	}

	public int getRelationSize() {
		return relationSize;
	}

	public Relation[] getRelations() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					config.dir + config.relLocal + "Temp"));
			ArrayList rel = new ArrayList();
			String temp;
			while ((temp = reader.readLine()) != null) {
				rel.add(factory.createRelation(dic.getURI(Util4NT.RELATION,
						new Integer(temp).intValue())));
				relationSize++;
			}
			reader.close();
			return (Relation[]) rel.toArray(new Relation[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Category[] getSubCategories(Category parent) {
		return new Category[0];
	}

	public Relation[] getSubRelations(Relation parent) {
		return new Relation[0];
	}

	public Category[] getSuperCategories(Category son) {
		return new Category[0];
	}

	public Relation[] getSuperRelations(Relation son) {
		return new Relation[0];
	}

	public int getTripleSize() {
		return relationTripleSize;
	}

	public InstanceDocumentIterator createInstanceDocumentIterator(
			Properties prop) {
		try {
			BufferedReader relReader = new BufferedReader(new FileReader(
					relationDir));
			BufferedReader inverseRelReader = new BufferedReader(
					new FileReader(inverseRelationDir));
			BufferedReader catReader = new BufferedReader(new FileReader(
					categoryDir));
			BufferedReader attReader = new BufferedReader(new FileReader(attributeDir));
			return new InstanceDocumentIteratorImpl(relReader,
					inverseRelReader, catReader, attReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param args
	 */
	// public static void main(String[] args) {
	// // System.out.println("yes!");
	// try {
	//
	// BufferedReader relReader = new BufferedReader(new FileReader(
	// relationDir));
	// BufferedReader inverseRelReader = new BufferedReader(
	// new FileReader(inverseRelationDir));
	// BufferedReader catReader = new BufferedReader(new FileReader(
	// categoryDir));
	// InstanceDocumentIterator temp = new InstanceDocumentIteratorImpl(
	// relReader, inverseRelReader, catReader);
	// InstanceDocument ins = null;
	// //
	// while ((ins = temp.next()) != null) {
	// System.out.println(ins);
	//
	// Relation temps = factory.createRelation("rel1");
	// LocalInstanceList instance = ins.getObjects(temps);
	//
	// for (int i = 0; i < instance.size(); i++)
	// System.out.println(instance.getInstance(i).getURI());
	//
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
}
