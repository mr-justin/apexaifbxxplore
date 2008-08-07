/**
 * <copyright>
 * 
 * Copyright (c) 2004-2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License - v 1.0 which accompanies this distribution, and
 * is available at http://opensource.org/licenses/eclipse-1.0.txt
 * 
 * Contributors: IBM - Initial API and implementation
 * 
 * </copyright>
 *  
 */
package com.ibm.semplore.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.ibm.semplore.model.LocalRelationList;
import com.ibm.semplore.model.Relation;

public class LocalRelationListImpl implements LocalRelationList {

	 class Binding {
		 Relation relation;
		 int localID;
	    	int globalID;
	    	public Binding(Relation rel, int local) {
	    		relation = rel;
	    		localID = local;
	    	}
	    }
	protected Binding[] getSortedArray() {
		if (array == null) {
			array = (Binding[])list.toArray(new Binding[0]);
			Arrays.sort(array,new BindingComparator());
		}
		return array;
	}
	
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.LocalInstanceList#getLocalID(int)
     */
    public int getLocalID(int index) {
    	return getSortedArray()[index].localID;
    }

	/* (non-Javadoc)
	 * @see com.ibm.semplore.model.LocalInstanceList#getInstance(int)
	 */
	public void addRelation(Relation rel, int localID) {
		list.add(new Binding(rel, localID));
    }

	public int size() {
		return list.size();
	}

	protected Binding[] array = null;
	
	protected ArrayList list = new ArrayList();
	
    
    class BindingComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			Binding a = (Binding)arg0;
			Binding b = (Binding)arg1;
			if (a.localID < b.localID)
				return -1;
			else if (a.localID > b.localID)
				return 1;
			else
				return 0;
		}
    	
    }

	public Relation getRelation(int index) {
		return array[index].relation;
	}

}

