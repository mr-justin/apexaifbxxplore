/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.result;

import java.util.List;
import java.util.Vector;

/**
 * Mapping results are internally stored in this structure. Before
 * they are finally saved some cleansing (min. threshold) and removal 
 * of duplicates is done.
 * 
 * @author Marc Ehrig
 */
public interface ResultList {

	public List objectList();
	public int maxRanks();
	public Object getObject(Object object, int rank);
	public double getValue(Object object, int rank);
	public Object getAddInfo(Object object, int rank);
	public void set(Object object1, Object object2, double valueForOrder);
	public void set(Object object1, Object object2,	double valueForOrder, Object additionalInfo);
	public void clear();
	public void removeDoubles();
	public ResultListImpl removeDoublesNew();
	public Vector vectorResult();
	public Vector cutoffResult(double cutoff);
	public double completeSimilarity();

}
