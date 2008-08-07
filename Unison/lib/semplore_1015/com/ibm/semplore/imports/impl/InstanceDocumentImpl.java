package com.ibm.semplore.imports.impl;

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.LiteralsOfProperty;
import com.ibm.semplore.model.LocalCategoryList;
import com.ibm.semplore.model.LocalInstanceList;
import com.ibm.semplore.model.LocalRelationList;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.LocalCategoryListImpl;
import com.ibm.semplore.model.impl.LocalInstanceListImpl;
import com.ibm.semplore.model.impl.LocalRelationListImpl;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.xir.InstanceDocument;

public class InstanceDocumentImpl implements InstanceDocument {

	protected Instance ins;

	protected ArrayList attributeList = new ArrayList();

	protected ArrayList categoryList = new ArrayList();

	protected ArrayList relationList = new ArrayList();

	protected ArrayList inverseRelationList = new ArrayList();

	protected ArrayList objectList = new ArrayList(); // List of

	// LocalInstanceListImpl
	// for each relation
	// relevented.

	protected ArrayList subjectList = new ArrayList();

	/* The following two ArrayList is enough */
	protected LocalInstanceListImpl globalObject = new LocalInstanceListImpl();

	protected LocalInstanceListImpl globalSubject = new LocalInstanceListImpl();

	protected LocalCategoryListImpl categoryWithID = new LocalCategoryListImpl();

	protected LocalRelationListImpl relationWithID = new LocalRelationListImpl();

	protected LocalRelationListImpl inverseRelationWithID = new LocalRelationListImpl();

	protected HashMap relationIndex = new HashMap();

	protected HashMap inverseRelationIndex = new HashMap();

	protected HashMap attributeIndex = new HashMap();

	protected HashMap value = new HashMap();

//	protected HashMap subIndex = new HashMap();

//	protected HashMap objIndex = new HashMap();

	SchemaFactory factory = SchemaFactoryImpl.getInstance();

	public Instance getThisInstance() {
		return ins;
	}

	public Attribute[] getAttributes() {
		return (Attribute[]) (attributeList.toArray(new Attribute[0]));
	}

	public Category[] getCategories() {
		return (Category[]) (categoryList.toArray(new Category[0]));
	}

	public Relation[] getRelationsGivenSubject() {
		return (Relation[]) (relationList.toArray(new Relation[0]));
	}

	public Relation[] getRelationsGivenObject() {
		return (Relation[]) (inverseRelationList.toArray(new Relation[0]));
	}

	public LocalCategoryList getCategoriesWithID() {
		return categoryWithID;
	}

	public LocalRelationList getInverseRelationsWithID() {
		return inverseRelationWithID;
	}

	public LocalRelationList getRelationsWithID() {
		return relationWithID;
	}

	public LocalInstanceList getObjectsOfUniversalRelation() {
		return globalObject;
	}

	public LocalInstanceList getSubjectsOfUniversalRelation() {
		return globalSubject;
	}

	protected ArrayList lopList = new ArrayList();

	protected ArrayList valueList = new ArrayList();

	private SchemaObjectInfo info = null;

	public LocalInstanceList getObjects(Relation rel) {
		int index = findRelation(rel, false, 0); // index is the
		// lower_address of rel in
		// ArrayList Relation and
		// Array of objectList.
		if (index == -1) {
			return null;
		} else {
			return (LocalInstanceList) objectList.get(index);
		}
	}

	public LocalInstanceList getSubjects(Relation rel) {
		int index = findInverseRelation(rel, false, 0);
		if (index == -1) {
			return null;
		} else {
			return (LocalInstanceList) subjectList.get(index);
		}
	}

	protected int findRelation(Relation r, boolean create, int relationLocalID) {
		Integer index = (Integer) relationIndex.get(r.getURI());
		if (index == null && create) {
			index = new Integer(relationList.size());
			relationList.add(r);
//			System.out.println("                       ****" + r.getURI());
			relationWithID.addRelation(r, relationLocalID);
			objectList.add(new LocalInstanceListImpl()); // ?
			relationIndex.put(r.getURI(), index);
		}
		if (index == null) {
			return -1;
		} else {
			return index.intValue();
		}
	}

	protected int findInverseRelation(Relation r, boolean create,
			int relationLocalID) {
		Integer index = (Integer) inverseRelationIndex.get(r.getURI());
		if (index == null && create) {
			index = new Integer(inverseRelationList.size());
			inverseRelationList.add(r);
			inverseRelationWithID.addRelation(r, relationLocalID);
			subjectList.add(new LocalInstanceListImpl()); // ?
			inverseRelationIndex.put(r.getURI(), index);
		}
		if (index == null) {
			return -1;
		} else {
			return index.intValue();
		}
	}

	public String[] getValues(Attribute attr) {
		// TODO Auto-generated method stub
		return null;
	}

	public SchemaObjectInfo getSchemaObjectInfo() {
		if (info==null) {
			info = factory.createSchemaObjectInfo(ins.getURI(), null, null, text.toString());
		}
		return info;
	}

	public void setSchemaObjectInfo(SchemaObjectInfo info) {
		this.info = info;

	}
	StringBuffer text;
	public InstanceDocumentImpl(Instance ins) {
		this.ins = ins;
		text = new StringBuffer();
	}
	
	public void addText(String text){
		this.text.append(text);
	}
	
	public void addCategory(Category c, int catLocalID) {
		categoryList.add(c);
		categoryWithID.addCategory(c, catLocalID);
	}

	public void addRelation(Relation r, Instance object, int relationLocalID,
			int instanceLocalID) {
		int index = findRelation(r, true, relationLocalID);
		((LocalInstanceListImpl) objectList.get(index)).addInstance(object,
				instanceLocalID);
//		if (objIndex.get(object.getURI()) != null)
//			return;
//		objIndex.put(object.getURI(), null);
//		globalObject.addInstance(object, 0); // Need not to concern with the relation.
	}

	public void addInverseRelation(Relation r, Instance subject,
			int inverseRelationLocalID, int instanceLocalID) {
		int index = findInverseRelation(r, true, inverseRelationLocalID);
		((LocalInstanceListImpl) subjectList.get(index)).addInstance(subject,
				instanceLocalID);
//		if (subIndex.get(subject.getURI()) != null)
//			return;
//		subIndex.put(subject.getURI(), null);
//		globalSubject.addInstance(subject, 0);
	}

	/** *********************** Unused ************************** */
	public void addValue(Attribute a, String value) {
		int index = findAttribute(a, true);
		((ArrayList) valueList.get(index)).add(value);
		lopList.add(SchemaFactoryImpl.getInstance().createLiteralsOfProperty(
				a.getURI(), value));
	}

	public void addAnnotation(String property, String value) {
		lopList.add(SchemaFactoryImpl.getInstance().createLiteralsOfProperty(
				property, value));
	}

	protected int findAttribute(Attribute a, boolean create) {
		Integer index = (Integer) attributeIndex.get(a.getURI());
		if (index == null && create) {
			index = new Integer(attributeList.size());
			attributeList.add(a);
			valueList.add(new ArrayList());
			attributeIndex.put(a.getURI(), index);
		}
		if (index == null) {
			return -1;
		} else {
			return index.intValue();
		}
	}

	public LiteralsOfProperty[] getLiteralsOfProperties() {
		return (LiteralsOfProperty[]) lopList
				.toArray(new LiteralsOfProperty[0]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
