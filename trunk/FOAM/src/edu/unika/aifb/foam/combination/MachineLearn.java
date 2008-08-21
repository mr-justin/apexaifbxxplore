/*
 * Created on 23.06.2004
 *
 */
package edu.unika.aifb.foam.combination;

import edu.unika.aifb.foam.util.UserInterface;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * The results from a machine learned combiner are fed back into 
 * the system.
 * 
 * @author Marc Ehrig
 */
public class MachineLearn implements Combination {

	private static final long serialVersionUID = 1L;
	private static final int MAX = 100;
	private double[] value = new double[MAX];
	private double result = 0;
	private int total = 0;
	private Classifier classifier;
	private Instances instances;

	public MachineLearn(Classifier classifierT, Instances instancesT, int totalT) {
		classifier = classifierT;
		instances = instancesT;
		total = totalT;
	}

	public double getValue(int index) {
		return value[index];
	}

	public void process() {
		Instance inst = new Instance(total); 	// Create empty instance with three attribute values
		for (int i = 0; i<total; i++) {
			inst.setValue(i,value[i]);
		}
		inst.setDataset(instances);
		result = 0;
		double[] prob;			 		
		try {
		result = classifier.classifyInstance(inst);
		prob = classifier.distributionForInstance(inst);
		result = prob[1];
//		result = result*(value[0]+0.1)*10/11;				
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
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

	public double[] getAddInfo() {
		double[] addInfo = new double[total];
		for (int i = 0; i<total; i++) {
			addInfo[i] = value[i];
		}
		return addInfo;
	}

	public void setObjects(Object object1, Object object2) {
	}		
}
