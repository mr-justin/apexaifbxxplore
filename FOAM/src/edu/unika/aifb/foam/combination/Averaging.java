package edu.unika.aifb.foam.combination;

/**
 * All values of the calculated rules are taken and an average 
 * is built.
 * 
 * @author Marc Ehrig
 */
public class Averaging implements Combination {

	private static final long serialVersionUID = 1L;
	private static final int MAX = 10000;
	private double[] value = new double[MAX];
	private double result = 0;
	private int additionelements = 0;
	private double dividend = 1;
	private int infoplus = 0;
	public Averaging(int additionelementsT, int dividendT, int infoplusT) {
		additionelements = additionelementsT;
		dividend = dividendT;
		infoplus = infoplusT;
	}
	
	public double getValue(int index) {
		return value[index];
	}

	public void process() {
		for (int i = 0; i<additionelements; i++) {
			if (value[i]>0) {
				result = result + value[i];
			}
		}
		result = result/dividend;
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
