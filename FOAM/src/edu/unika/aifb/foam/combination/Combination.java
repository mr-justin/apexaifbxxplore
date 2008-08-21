/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.combination;

import java.io.Serializable;

/** 
 * Combinations are used to aggregate the output of individual
 * rules. This can be done with a manual approach or a machine
 * learned approach.
 * 
 * @author Marc Ehrig
 */
public interface Combination extends Serializable{

	public void reset();
//	public void setParameter(Object object);
	public void setObjects(Object object1, Object object2);
	public void setValue(int index, double valueToSet);
	public void process();
	public double result();
	public double getValue(int index);
	public double[] getAddInfo();

}
