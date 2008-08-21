/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.result;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

/**
 * Yet another implementation of ResultList. Not better than the old
 * one though.
 * 
 * @author Marc Ehrig
 */
public class ResultListImplNew implements ResultList{

	private Hashtable table = new Hashtable();
	private final int MAXOBJECTS = 10000;
	private int maxRanks;
	private Vector entities = new Vector();

	private class Mapping {
		Object object1;
		Object object2;
		double valueForOrder;
		Object addInfo;
	}

	/**
	 * @author Marc Ehrig
	 * The comparator is needed to insert the mappings in the correct order. This is especially
	 * true to find out which mapping result is best.
	 */
/*	public class MyComparator implements Comparator {
		public boolean equals(Object o1) {
			return false;
		}
		public int compare(Object o1, Object o2) {
			Mapping map1 = (Mapping) o1;
			Mapping map2 = (Mapping) o2;
//			if (map1.object1.equals(map2.object1)&&(map1.object2.equals(map2.object2))&&(map1.valueForOrder==map2.valueForOrder)) return 0;
			if (map1.object1.equals(map2.object1)&&(map1.object2.equals(map2.object2))) return 0;
			if (map1.valueForOrder<=map2.valueForOrder) return 1;
			if (map1.valueForOrder>map2.valueForOrder) return -1;
			return 1;
		}		
	}*/
	
//	private MyComparator myComparator = new MyComparator();

/*	public ResultListImpl() {
		this(1);
	}*/
	
	public ResultListImplNew(int ranks) {
		maxRanks = ranks;
	}

	public List objectList() {
		return entities;
	}

	public int maxRanks() {
		return maxRanks;
	}

	public Object getObject(Object object, int rank) {
		LinkedList list1 = (LinkedList) table.get(object);
		if ((list1!=null)&&(rank<list1.size())) {
			Mapping map = (Mapping) list1.get(rank);
			if (map!=null) return map.object2;
		}
		return null;
	}

	public double getValue(Object object, int rank) {
		LinkedList list1 = (LinkedList) table.get(object);
		if ((list1!=null)&&(rank<list1.size())) {
			Mapping map = (Mapping) list1.get(rank);
			if (map!=null) return map.valueForOrder;
		}
		return 0;
	}

	public Object getAddInfo(Object object, int rank) {
		LinkedList list1 = (LinkedList) table.get(object);
		if ((list1!=null)&&(rank<list1.size())) {
			Mapping map = (Mapping) list1.get(rank);
			if (map!=null) return map.addInfo;
		}
		return null;
	}

	public void set(Object object1, Object object2, double valueForOrder) {
		set(object1, object2, valueForOrder, null);
	}

	public void set(Object object1, Object object2,	double valueForOrder, Object additionalInfo) {
		if ((object1!=null)&&(object2!=null)) {
		if ((entities.contains(object1)==false)&&(entities.size()<MAXOBJECTS)) {
			entities.add(object1);
			table.put(object1, new LinkedList());
		}
		if (entities.contains(object1)) {
			LinkedList list1 = (LinkedList) table.get(object1);
			for (int i = 0; i<list1.size(); i++) {
				Mapping map1 = (Mapping) list1.get(i);
				if (map1.object2.equals(object2)) {
					Object obj = list1.remove(i);
					i--;
//					System.out.print("!");
				}			
			}
			Mapping map = new Mapping();
			map.object1 = object1;
			map.object2 = object2;
			map.valueForOrder = valueForOrder;
			map.addInfo = additionalInfo;
			int i = 0;
			boolean found = false;
			while ((i<list1.size())&&(found==false)) {
				Mapping map1 = (Mapping) list1.get(i);
				if (map1.valueForOrder<=valueForOrder) {
					found = true;
				} else i++;
			}
			list1.add(i,map);
			if (list1.size()>maxRanks) {
				Object obj = list1.removeLast();
			}
		}
		}
	}

	public void clear() {
		table.clear();
		entities.clear();
	}
	
	public void removeDoubles() {
		TreeMap tree = new TreeMap();
		Set set = new HashSet(entities);
		Iterator iter = entities.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			LinkedList list1 = (LinkedList) table.get(object);		
			for (int i = 0; i<list1.size(); i++) {			
				Mapping map = (Mapping) list1.get(i);
				tree.put(new Double(map.valueForOrder),map);
			}
		}
		table.clear();
		entities.clear();
		while (tree.size()>0) {
			Object key = tree.lastKey();
			Mapping map = (Mapping) tree.get(key);
			if (set.contains(map.object1)&&set.contains(map.object2)) {
				set(map.object1, map.object2, map.valueForOrder, map.addInfo);
				set.remove(map.object1);
				set.remove(map.object2);			
			}			
		}
	}

	public Vector vectorResult() {
		Vector vector = new Vector();
		Iterator iter = objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			String uri1;
			try{
				OWLEntity entity = (OWLEntity) object1;
				uri1 = entity.getURI();
			} catch (Exception e) {
				uri1 = object1.toString();
			}
			for (int i = 0; i<maxRanks; i++) {
				String[] dataset = new String[3];				
				Object object2 = getObject(object1,i);
				if (object2!=null) {
					dataset[0] = uri1;					
					double value = getValue(object1,i);
					try{
						OWLEntity entity = (OWLEntity) object2;
						String uri2 = entity.getURI();
						dataset[1] = uri2;
					} catch (Exception e) {
						dataset[1] = object2.toString();
					}
					dataset[2] = Double.toString(value);
					vector.add(dataset);					
				}
			}
		}				
		return vector;
	}
	
	public Vector cutoffResult(double cut) {
		Vector vector = new Vector();
		Iterator iter = objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			String uri1;
			try{
				OWLEntity entity = (OWLEntity) object1;
				uri1 = entity.getURI();
			} catch (Exception e) {
				uri1 = object1.toString();
			}
			for (int i = 0; i<maxRanks; i++) {
				String[] dataset = new String[3];				
				Object object2 = getObject(object1,i);
				if (object2!=null) {
					dataset[0] = uri1;					
					double value = getValue(object1,i);
					try{
						OWLEntity entity = (OWLEntity) object2;
						String uri2 = entity.getURI();
						dataset[1] = uri2;
					} catch (Exception e) {
						dataset[1] = object2.toString();
					}
					dataset[2] = Double.toString(value);
					if (value>=cut) {
					vector.add(dataset);
					}
				}
			}
		}				
		return vector;
	}

	public double completeSimilarity() {
		return 0;
	}

	public ResultListImpl removeDoublesNew() {
		return null;
	}

}
