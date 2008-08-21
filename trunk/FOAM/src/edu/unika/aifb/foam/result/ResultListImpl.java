/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.result;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

/**
 * This class implements the ResultList in an efficient way. 
 * 
 * @author Marc Ehrig
 */
public class ResultListImpl implements ResultList{

	private Hashtable table = new Hashtable();
	private final int MAXOBJECTS = 100000;
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
	public class MyComparator implements Comparator {
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
	}
	
	/**
	 * @author Marc Ehrig
	 * The comparator is needed to insert the mappings in the correct order. This is especially
	 * true to find out which mapping result is best.
	 */
	public class MyComparator2 implements Comparator {
		public boolean equals(Object o1) {
			return false;
		}
		public int compare(Object o1, Object o2) {
			Mapping map1 = (Mapping) o1;
			Mapping map2 = (Mapping) o2;
			if (map1.valueForOrder<=map2.valueForOrder) return 1;
			if (map1.valueForOrder>map2.valueForOrder) return -1;
			return 1;
		}		
	}	
	
	private MyComparator myComparator = new MyComparator();
	private MyComparator2 myComparator2 = new MyComparator2();	

/*	public ResultListImpl() {
		this(1);
	}*/
	
	public ResultListImpl(int ranks) {
		maxRanks = ranks;
	}

	/**
	 * Allows direct acces on the vector with entities. This method should normally not been
	 * called for access.
	 */
	public List objectList() {
		return entities;
	}

	public int maxRanks() {
		return maxRanks;
	}

	public Object getObject(Object object, int rank) {
		TreeSet tree = (TreeSet) table.get(object);
		if (tree!=null) {
			Vector vector = new Vector(tree);			//I really doubt that this is fast.
			if (vector.size()>rank) {
				Mapping map = (Mapping) vector.elementAt(rank);
				if (map!=null) return map.object2;
			}
		}
		return null;
	}

	public double getValue(Object object, int rank) {
		TreeSet tree = (TreeSet) table.get(object);
		if (tree!=null) {
			Vector vector = new Vector(tree);			//I really doubt that this is fast.
			if (vector.size()>rank) {
				Mapping map = (Mapping) vector.elementAt(rank);
				if (map!=null) return map.valueForOrder;
			}
		}
		return 0;
	}

	public Object getAddInfo(Object object, int rank) {
		TreeSet tree = (TreeSet) table.get(object);
		if (tree!=null) {
			Vector vector = new Vector(tree);			//I really doubt that this is fast.
			if (vector.size()>rank) {
				Mapping map = (Mapping) vector.elementAt(rank);
				if (map!=null) {
					return map.addInfo;
				}
			}
		}
		return null;
	}

	public void set(Object object1, Object object2, double valueForOrder) {
		set(object1, object2, valueForOrder, null);
	}

	public void set(Object object1, Object object2,	double valueForOrder, Object additionalInfo) {
		if ((entities.contains(object1)==false)&&(entities.size()<MAXOBJECTS)) {
			entities.add(object1);		
			table.put(object1, new TreeSet(myComparator));
		}
		if (entities.contains(object1)) {
			TreeSet tree = (TreeSet) table.get(object1);
			Mapping map = new Mapping();
			map.object1 = object1;
			map.object2 = object2;
			map.valueForOrder = valueForOrder;
			map.addInfo = additionalInfo;
			Iterator iter = tree.iterator();
			Set forRemoval = new HashSet();
			while (iter.hasNext()) {
				Mapping map1 = (Mapping) iter.next();
				if (map1.object2.equals(object2)) {
					forRemoval.add(map1);
				}
			}
			tree.removeAll(forRemoval);
			if (tree.size()>(maxRanks-1)) {
				tree.remove(tree.last());	
			}			
			tree.add(map);				
		}
	}

	public void clear() {
		table.clear();
		entities.clear();
	}
	
	public void removeDoubles() {
		TreeSet tree = new TreeSet(myComparator2);
		Set set = new HashSet(entities);
		int counter = 0;
		Iterator iter = entities.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			TreeSet treeset = (TreeSet) table.get(object);		
			Vector vector = new Vector(treeset);
			for (int i = 0; i<vector.size(); i++) {			
				Mapping map = (Mapping) vector.elementAt(i);
				tree.add(map);
				counter++;
			}
		}
		table.clear();
		entities.clear();
		iter = tree.iterator();
		while (iter.hasNext()) {
			Mapping map = (Mapping) iter.next();
			if (set.contains(map.object1)&&set.contains(map.object2)) {
				set(map.object1, map.object2, map.valueForOrder, map.addInfo);
				set(map.object2, map.object1, map.valueForOrder, map.addInfo);				
				set.remove(map.object1);
				set.remove(map.object2);			
			}
		}
	}
	
	/**
	 * Removes doubles without deleting the old structure.
	 * @return
	 */
	public ResultListImpl removeDoublesNew() {
		TreeSet tree = new TreeSet(myComparator2);
		Set set = new HashSet(entities);
		int counter = 0;
		Iterator iter = entities.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			TreeSet treeset = (TreeSet) table.get(object);		
			Vector vector = new Vector(treeset);
			for (int i = 0; i<vector.size(); i++) {			
				Mapping map = (Mapping) vector.elementAt(i);
				tree.add(map);
				counter++;
			}
		}
		ResultListImpl newOne = new ResultListImpl(2);
		iter = tree.iterator();
		while (iter.hasNext()) {
			Mapping map = (Mapping) iter.next();
			if (set.contains(map.object1)&&set.contains(map.object2)) {
				newOne.set(map.object1, map.object2, map.valueForOrder, map.addInfo);
				newOne.set(map.object2, map.object1, map.valueForOrder, map.addInfo);				
				set.remove(map.object1);
				set.remove(map.object2);			
			}
		}
		return newOne;
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
		double complete = 0.0;
		Iterator iter = objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			for (int i = 0; i<maxRanks; i++) {			
				Object object2 = getObject(object1,i);
				if (object2!=null) {
					complete = complete + getValue(object1,i);
				}
			}
		}				
		return complete;
	}

}
