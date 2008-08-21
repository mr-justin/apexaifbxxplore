/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.result;

import java.util.Map;

/**
 * This represents an internal mapping table of size n x m. The
 * indvidual elements can be easily set and returned. This is required
 * by all the rules when they acces previously known alignments.
 * 
 * @author Marc Ehrig
 */
public interface ResultTable {

	public void set(Object object1, Object object2, double value);
	public double get(Object object1, Object object2);
	public Map[] map();
	public void clear();
	public void copy(ResultTable toCopy);
	public void copy(ResultList resultList, int i, double d);

}
