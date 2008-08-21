/*
 * Created on 03.06.2004
 *
 */
package edu.unika.aifb.foam.combination;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

/**
 * Weights have been set by an ontology expert. In detail not linear
 * weights are used, but a sigmoid function is applied. In general
 * this returns very good results.
 * 
 * @author Marc Ehrig
 */
public class ManualWeightsSigmoid implements Combination {

	private static final long serialVersionUID = 1L;
	private static final int MAX = 200;
	private double[] value = new double[MAX];
	private double result = 0;
	private int total = 0;
	double weights[][] = new double[MAX][];
	Object object1;
	Object object2;

	public ManualWeightsSigmoid(int totalT) {
		double pre0[] = {0.0,1.5,0.6,2.0}; weights[0] = pre0;			//label
		double pre1[] = {0.0,3.0,0.99,10.0}; weights[1] = pre1;			//URI

		double pre2[] = {-0.1,0.2,0.5,1.0}; weights[2] = pre2;			//superDir
		double pre3[] = {-0.2,0.7,0.3,1.0}; weights[3] = pre3;			//subDir
		double pre4[] = {-0.1,0.35,0.3,1.0}; weights[4] = pre4;			//domDir
		double pre5[] = {-0.1,0.35,0.3,1.0}; weights[5] = pre5;			//domDir
		double pre6[] = {-0.1,0.35,0.3,1.0}; weights[6] = pre6;			//ranDir
		double pre7[] = {-0.2,0.7,0.3,1.0}; weights[7] = pre7;			//instDir
		double pre8[] = {-1.0,0.0,0.8,-1.0}; weights[8] = pre8;			//sub and sup contra
		double pre9[] = {-1.0,0.0,0.8,-1.0}; weights[9] = pre9;			//sub and sup contra

		double pre10[] = {-0.05,0.4,0.8,1.0}; weights[10] = pre10;		//domDir
		double pre11[] = {-0.05,0.4,0.5,1.0}; weights[11] = pre11;		//super
		double pre12[] = {-0.1,1.0,0.5,1.0}; weights[12] = pre12;		//sub
		double pre13[] = {-0.3,1.1,0.3,1.0}; weights[13] = pre13;		//propinst

		double pre14[] = {-0.05,0.4,0.8,1.0}; weights[14] = pre14;		//domDir
		double pre15[] = {-0.05,0.4,0.8,1.0}; weights[15] = pre15;		//ranDir
		double pre16[] = {-0.05,0.4,0.5,1.0}; weights[16] = pre16;		//super
		double pre17[] = {-0.1,1.0,0.5,1.0}; weights[17] = pre17;		//sub
		double pre18[] = {-0.3,1.1,0.3,1.0}; weights[18] = pre18;		//propinst
		
		double pre19[] = {-0.1,0.35,0.4,1.0}; weights[19] = pre19;		//typeDir
		double pre20[] = {-0.2,1.0,0.3,3.0}; weights[20] = pre20;		//predobj
		double pre21[] = {-0.2,1.0,0.3,3.0}; weights[21] = pre21;		//predobj
		double pre22[] = {-0.2,1.0,0.3,3.0}; weights[22] = pre22;		//subjpred
		
		total = totalT;
	}

/*	public void setParameter(Object object) {
		Object[] object2 = (Object[]) object;
		Integer number = (Integer) object2[2];
		total = number.intValue();		
	}*/

	public double getValue(int index) {
		return value[index];
	}

	public void process() {
		if ((object1 instanceof OWLClass)&&(object2 instanceof OWLClass)) {
			for (int i = 0; i<=9; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}	
			result = (result+0.5)/2.5;	
			if (result>1) result = 1;	
			if (result<0) result = 0;	
		} else 
		if ((object1 instanceof DataProperty) && (object2 instanceof DataProperty)) {
			for (int i = 0; i<2; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}
			for (int i = 10; i<=13; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}			
			result = (result+0.5)/5;
			if (result>1) result = 1;	
			if (result<0) result = 0;	
		} else 
		if ((object1 instanceof ObjectProperty) && (object2 instanceof ObjectProperty)) {
			for (int i = 0; i<2; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}
			for (int i = 14; i<=18; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}			
			result = (result+0.5)/5;
			if (result>1) result = 1;	
			if (result<0) result = 0;	
		} else 			
		if ((object1 instanceof Individual) && (object2 instanceof Individual)) {
			for (int i = 0; i<2; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}
			for (int i = 19; i<=22; i++) {
				double calc = sigmoid(weights[i][0],weights[i][1],weights[i][2],weights[i][3],value[i]);
				if (value[i]<0) calc=0.0;
				result = result + calc;
			}				
			result = (result+0.5)/5;			
			if (result>1) result = 1;
			if (result<0) result = 0;	
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
		object1 = null;
		object2 = null;
	}

	public void setValue(int index, double valueToSet) {
		value[index] = valueToSet;
	}
	
	/**
	 * Returns the sigmoid of the input value.
	 * @param min: minimum value to be returned.
	 * @param max: maximum value to be returned.
	 * @param middle: the middle of the function
	 * @param rate: the slope of the sigmoid function
	 * @param input: the input value
	 * @return the output value
	 */
	private double sigmoid(double min, double max, double middle, double rate, double input) {
		double value = (max-min)/(1+Math.exp(-1*rate*(input-middle)*20))+min;
		if (input==-1000) {value = 0;} 		
		if ((value>0.999)&&(value<1.001)) {value = 1;}
		if ((value<0.001)&&(value>-0.001)) {value = 0;}
		return value;
	}

	public void setObjects(Object object1T, Object object2T) {
		object1 = object1T;
		object2 = object2T;		
	}

	public double[] getAddInfo() {
		double[] addInfo = new double[total];
		for (int i = 0; i<total; i++) {
			addInfo[i] = value[i];
		}
		return addInfo;
	}	

}
