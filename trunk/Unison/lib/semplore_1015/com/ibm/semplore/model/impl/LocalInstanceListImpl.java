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

import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.LocalInstanceList;

public class LocalInstanceListImpl implements LocalInstanceList {
	class Binding {
		Instance instance;

		int localID;

		int globalID = -1;

		public Binding(Instance ins, int local, int global) {
			instance = ins;
			localID = local;
			globalID = global;
		}

		public Binding(Instance ins, int local) {
			instance = ins;
			localID = local;
		}
	}

	private boolean sorted = false;

	protected Binding[] array = null;

	protected ArrayList list = new ArrayList();

	public int getLocalID(int index) {
		return getSortedArray()[index].localID;
	}

	public int getGlobalID(int index) {
		return getSortedArray()[index].globalID;
	}

	public void addInstance(Instance ins, int localID) {
		list.add(new Binding(ins, localID));
	}

	public int size() {
		return list.size();
	}

	public void addInstance(Instance ins, int localID, int globalID) {
		list.add(new Binding(ins, localID, globalID));
	}

	public Instance getInstance(int index) {
		return getSortedArray()[index].instance;
	}

	protected Binding[] getSortedArray() {
		if (array == null && !sorted) {
			array = (Binding[]) list.toArray(new Binding[0]);
			Arrays.sort(array, new BindingComparator());
			sorted = true;
		}
		return array;
	}

	class BindingComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			Binding a = (Binding) arg0;
			Binding b = (Binding) arg1;
			if (a.localID < b.localID)
				return -1;
			else if (a.localID > b.localID)
				return 1;
			else
				return 0;
		}

	}

}
