package com.ibm.semplore.imports.impl;

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.LocalCategoryList;
import com.ibm.semplore.model.LocalInstanceList;
import com.ibm.semplore.model.LocalRelationList;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObject;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.LocalCategoryListImpl;
import com.ibm.semplore.model.impl.LocalInstanceListImpl;
import com.ibm.semplore.model.impl.LocalRelationListImpl;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.xir.InstanceDocument;
import com.ibm.semplore.xir.impl.DocumentImpl;

public class InstanceDocumentImpl extends DocumentImpl implements InstanceDocument {

//	protected ArrayList attributeList = new ArrayList();

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
	
	public boolean isCategory = false;
	public boolean isRelation = false;

	public void setObjectType(Class objectType) {
		if (objectType == Category.class) isCategory =true;
		if (objectType == Relation.class) isRelation =true;
	}

	public SchemaObject getThisSchemaObject() {
		return schemaObject;
	}

	public HashMap<String,String> getAttributes() {
		return attributes;
	}

	public ArrayList<Category> getCategories() {
		return categoryList;
	}

	public ArrayList<Relation> getRelationsGivenSubject() {
		return relationList;
	}

	public ArrayList<Relation> getRelationsGivenObject() {
		return inverseRelationList;
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
		Integer index = (Integer) relationIndex.get(r.getIDofURI());
		if (index == null && create) {
			index = new Integer(relationList.size());
			relationList.add(r);
//			System.out.println("                       ****" + r.getURI());
			relationWithID.addRelation(r, relationLocalID);
			objectList.add(new LocalInstanceListImpl()); // ?
			relationIndex.put(r.getIDofURI(), index);
		}
		if (index == null) {
			return -1;
		} else {
			return index.intValue();
		}
	}

	protected int findInverseRelation(Relation r, boolean create,
			int relationLocalID) {
		Integer index = (Integer) inverseRelationIndex.get(r.getIDofURI());
		if (index == null && create) {
			index = new Integer(inverseRelationList.size());
			inverseRelationList.add(r);
			inverseRelationWithID.addRelation(r, relationLocalID);
			subjectList.add(new LocalInstanceListImpl()); // ?
			inverseRelationIndex.put(r.getIDofURI(), index);
		}
		if (index == null) {
			return -1;
		} else {
			return index.intValue();
		}
	}

	public SchemaObjectInfo getSchemaObjectInfo() {
		return info;
	}

	public void setSchemaObjectInfo(SchemaObjectInfo info) {
		this.info = info;

	}
	HashMap<String,String> attributes;
	
	public InstanceDocumentImpl(Instance ins) {
		this.schemaObject = ins;
		attributes = new HashMap<String, String>();
	}
	
	public void setURI(String URI) {
		this.URI = URI;
	}
	
	public void addAttribute(String name, String attr) {
		//ignore language, e.g. "A sense of an adjective word."@en-us --> A sense of an adjective word.
		if (attr.matches(".*\".*\"@.*")) {
			attr = attr.substring(attr.indexOf("\"")+1,attr.lastIndexOf("@")-1);
		}
		
		//ignore datatype, e.g. "1"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> --> 1		
		if (attr.matches(".*\".*\"\\^\\^<.*")) {
			attr = attr.substring(attr.indexOf("\"")+1,attr.lastIndexOf("^^")-1);
		}
		
		//ignore "", e.g. "1" --> 1
		if (attr.matches("\".*\"")) {
			attr = attr.substring(1,attr.length()-1);
		}
		
		//an attribute may have multiple values
		String val = attributes.get(name);
		if (val==null)
			val = attr;
		else if (!val.contains(attr))
			val = val+" "+attr;
		attributes.put(name, val);
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

	public boolean checkThisSchemaObjectType(Class type) {
		if (type == Instance.class) return isCategory==false && isRelation==false;
		if (type == Relation.class) return isRelation;
		if (type == Category.class) return isCategory;
		throw new Error("wrong type: "+type);
	}

}
