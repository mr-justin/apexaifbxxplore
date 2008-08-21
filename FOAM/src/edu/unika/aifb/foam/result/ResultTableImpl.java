/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.result;

import java.util.Hashtable;
import java.util.Iterator;
//import java.util.List;
import java.util.Map;
//import java.util.Vector;

/**
 * This represents an efficient implementation of the ResultTable.
 * Empty fields are removed for space and efficiency purposes.
 * 
 * @author Marc Ehrig
 */
public class ResultTableImpl implements ResultTable{

//	private final int MAXOBJECTS = 100000;
	private final int TABLEMAX = 1000000;
	private Hashtable table1 = new Hashtable();
	private Hashtable table2 = new Hashtable();
	
	public void set(Object object1, Object object2, double value) {
		if ((table1.size()+table2.size())<TABLEMAX) {
			int hash1 = object1.hashCode();
			int hash2 = object2.hashCode();
			Double newvalue = new Double(value);			
			Integer code = new Integer(hash1+hash2);	
			table1.put(code,newvalue);			
		}
	}
	
	public double get(Object object1, Object object2) {
		if ((object1==null)||(object2==null)) {
			return -1000;
		}
		int hash1 = object1.hashCode();
		int hash2 = object2.hashCode();		
		Integer code = new Integer(hash1+hash2);	
		Double returnvalue;
		returnvalue = (Double) table1.get(code);
		if (returnvalue != null) {
			return returnvalue.doubleValue();
		} 		
		return -1000;
	}

	public void clear() {
		table1.clear();
		table2.clear();
	}	
	
	public void copy(ResultTable other) {
		clear();
		table1.putAll(other.map()[0]);
		table2.putAll(other.map()[1]);		
	}

	public Map[] map() {
		Map[] map = new Map[2];
		map[0] = table1;
		map[1] = table2;
		return map;
	}

	public void copy(ResultList list, int howmany, double minthreshold) {
		clear();
		Iterator iter = list.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			for (int i = 0; i<howmany; i++) {
				Object object2 = list.getObject(object1,i);
				if (object2 != null) {
					double value = list.getValue(object1,i);
					if (value>minthreshold) {					
//					value = 1/(i+1);			//first = 1; second = 1/2; third = 1/3
/*					switch (i) {
								case 0:  value = 1.0; break;
								case 1:  value = 0.7; break;
								case 2:  value = 0.5; break;
					}*/										
						set(object1, object2, value);
					}
				}
			}
		}
	}	
	
}
