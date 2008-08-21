/*
 * Created on 16.07.2005
 *
 */
package edu.unika.aifb.foam.nonOntologies;

import edu.unika.aifb.foam.combination.Combination;

/**
 * Weights have been set by an expert. 
 * 
 * @author Marc Ehrig
 */
public class PetriCombination implements Combination {

	private static final int MAX = 200;
	private double[] value = new double[MAX];
	private double result = 0;
	private int total = 18;
	double weights[] = new double[MAX];
	Object object1;
	Object object2;

	public PetriCombination() {
		weights[0] = 1.0;		//Name
		weights[1] = 1.0;		
		//weights[2] = 1.0;
		//weights[3] = 1.0;
		weights[2] = 1.0;	   //Places
		weights[3] = 1.5;		
		weights[4] = 1.5;
		weights[5] = 0.5;
		weights[6] = 0.5;
		weights[7] = 1.5;		
		weights[8] = 1.0;      //Transition
		weights[9] = 1.5;
		weights[10] = 1.0;		//Attribute
		weights[11] = 1.0;
		weights[12] = 1.0;
		weights[13] = 1.0;		
		weights[14] = 1.0;		//other
		weights[15] = 0.5;
		weights[16] = 0.5;
		weights[17] = 1.0;		
//		weights[18] = 1.0;
//		weights[19] = 1.5;		//FromPlace
//		weights[20] = 1.0;
//		weights[21] = 1.0;
//		weights[22] = 1.0;
//		weights[23] = 1.5;		//ToPlace
//		weights[24] = 1.0;
//		weights[25] = 1.0;
//		weights[26] = 1.0;
//		weights[27] = 1.0;		//other
//		weights[28] = 1.0;
//		weights[29] = 1.0;
//		weights[30] = 1.0;
//	
	}

	public double getValue(int index) {
		return value[index];
	}

	public void process() {
		double resultPlace = value[2]*(value[0]*weights[0]+value[1]*weights[1]+value[3]*weights[3]+value[4]*weights[4]+value[5]*weights[5]+value[6]*weights[6]+value[7]*weights[7])/(weights[0]+weights[1]+weights[3]+weights[4]+weights[5]+weights[6]+weights[7]);
		double resultTransition = value[8]*(value[0]*weights[0]+value[1]*weights[1]+value[9]*weights[9])/(weights[0]+weights[9]);
		double resultAttribute = value[10]*(value[0]*weights[0]+value[1]*weights[1]+value[11]*weights[11]+value[12]*weights[12]+value[13]*weights[13])/(weights[0]+weights[11]+weights[12]+weights[13]);
		double resultOthers = value[14]*(value[0]*weights[0]+value[1]*weights[1]+value[15]*weights[15]+value[16]*weights[16]+value[17]*weights[17])/(weights[0]+weights[15]+weights[16]+weights[17]);
		
//		double resultFromPlace = value[19]*(value[0]*weights[0]+value[20]*weights[20]+value[21]*weights[21]+value[22]*weights[22])/(weights[0]+weights[20]+weights[21]+weights[22]);
//		double resultToPlace = value[23]*(value[0]*weights[0]+value[24]*weights[24]+value[25]*weights[25]+value[26]*weights[26])/(weights[0]+weights[24]+weights[25]+weights[26]);
//		double resultOthers = value[27]*(value[0]*weights[0]+value[28]*weights[28]+value[29]*weights[29]+value[30]*weights[30])/(weights[0]+weights[28]+weights[29]+weights[30]);
			
		//+value[10]*weights[10]+value[11]*weights[11]+value[12]*weights[12] +weights[10]+weights[11]+weights[12]
		
		result = resultPlace;
		if (resultTransition > result) {result = resultTransition;}
		if (resultAttribute > result) {result = resultAttribute;}
		//if (resultValue > result) {result = resultValue;}
		//if (resultFromPlace > result) {result = resultFromPlace;}
		//if (resultToPlace > result) {result = resultToPlace;}
		if (result==0.0) {result = resultOthers;}
//		result = result*2;
		if (result>1) result = 1;
		if (result<0) result = 0;
	
	}

	public double result() {
		return result;
	}

	public void reset() {
		for (int i=0; i<MAX; i++) {
			value[i]=0;
		}
		result = 0;
		object1 = null;
		object2 = null;
	}

	public void setValue(int index, double valueToSet) {
		value[index] = valueToSet;
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
