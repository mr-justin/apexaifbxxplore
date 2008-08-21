package edu.unika.aifb.foam.combination;

/**
 * All values of the calculated rules are taken and the best is taken.
 * 
 * @author Marc Ehrig
 */
public class BestValue implements Combination {

	private static final long serialVersionUID = 1L;
	private static final int MAX = 10000;
	private double[] value = new double[MAX];
	private double result = 0;
	private int additionelements = 0;
	private int infoplus = 0;
	public BestValue(int additionelementsT, int infoplusT) {
		additionelements = additionelementsT;
		infoplus = infoplusT;
	}
	
	public double getValue(int index) {
		return value[index];
	}

	public void process() {
		result = 0;
		for (int i = 0; i<additionelements; i++) {
			if ((value[i]>0)&&(value[i]>result)) {
				result = value[i];
			}
		}
	}

	public double result() {
		return result;
	}

	public void reset() {
		for (int i=0; i<MAX; i++) {
			value[i]=-1.0;
		}
		result = 0;
	}

	public void setValue(int index, double valueToSet) {
		value[index] = valueToSet;
		
	}
	
	public void setObjects(Object object1T, Object object2T) {		
	}

	public double[] getAddInfo() {
		double[] addInfo = new double[infoplus];
		for (int i = 0; i<infoplus; i++) {
			addInfo[i] = value[i];
		}
		return addInfo;
	}	

}
