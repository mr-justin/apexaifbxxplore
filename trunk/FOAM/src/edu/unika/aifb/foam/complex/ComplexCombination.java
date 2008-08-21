package edu.unika.aifb.foam.complex;

import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.combination.Combination;

public class ComplexCombination implements Combination{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX = 200;
	private double[] value = new double[MAX];
	private double result = 0;
	private int total = 0;
	double weights[] = new double[MAX];
	Object object1;
	Object object2;
	
	public ComplexCombination(int totalT) {
		weights[0] = 2.0;			
		weights[1] = 1.0;			
		weights[2] = 1.0;			
		weights[3] = 1.5;			
		weights[4] = 0.5;			
		weights[5] = 0.5;			
		weights[6] = 0.5;			
		weights[7] = 1.0;			
		weights[8] = 1.0;			
		
		total = totalT;
	}

	public double[] getAddInfo() {
		double[] addInfo = new double[total];
		for (int i = 0; i<total; i++) {
			addInfo[i] = value[i];
		}
		return addInfo;
	}

	public double getValue(int index) {
		return value[index];
	}

	public void process() {
		if ((object1 instanceof OWLClass)&&(object2 instanceof OWLClass)) {
			for (int i = 0; i<=8; i++) {
				double calc = weights[i]*value[i];
				result = result + calc;
			}	
			result = result/6.5;				
			if (result>1) result = 1;	
			if (result<0) result = 0;	
		}
	}

	public void reset() {
		for (int i=0; i<MAX; i++) {
			value[i]=0;
		}
		result = 0;
		object1 = null;
		object2 = null;
	}

	public double result() {
		return result;
	}

	public void setObjects(Object object1T, Object object2T) {
		object1 = object1T;
		object2 = object2T;	
	}

	public void setValue(int index, double valueToSet) {
		value[index] = valueToSet;
	}
	
	
}
