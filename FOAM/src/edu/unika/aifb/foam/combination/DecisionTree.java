/*
 * Created on 27.01.2005
 *
 */
package edu.unika.aifb.foam.combination;

import java.io.BufferedReader;
import java.io.FileReader;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * Through machine learning a decision tree has been built. This tree is the basis
 * for this combiner. 
 * 
 * @author Marc Ehrig
 */
public class DecisionTree implements Combination {

	private static final long serialVersionUID = 1L;
	private static final int MAX = 500;
	private double[] value = new double[MAX];
	private boolean[] calculated = new boolean[MAX];
	private double result = 0;
	private int total = 0;
	private Object object1;
	private Object object2;
	private Rules rules;
	private DTElement root;
	private Structure ontology;

	private class DTElement {
		DTElement parent;
		int classification;
		double confidence;
		int rule;
		double compvalue;
		DTElement lower;
		DTElement higher;
	}
	
	public double getValue(int index) {
		return value[index];
	}

	public double result() {
		return result;
	}

	public void reset() {
		for (int i=0; i<MAX; i++) {
			value[i]=-1.0;
			calculated[i]=false;
		}
		result = 0;
	}

	public void setValue(int index, double valueToSet) {
//		value[index] = valueToSet;
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
	
	public DecisionTree(String fileName, Rules rulesT, Structure structureT) {
		rules = rulesT;
		total = rules.total();
		ontology = structureT;
		root = new DTElement();
		DTElement current = root;
		int depth = 0;
		int lastdepth  = 0;
		try{
	    FileReader in = new FileReader(fileName);
	    BufferedReader filebuf = new BufferedReader(in);
	    String oneLine = filebuf.readLine();
	    oneLine = filebuf.readLine();
	    oneLine = filebuf.readLine();
	    oneLine = filebuf.readLine();
	    while ((oneLine!=null)&&(oneLine.contains("rule"))) {
/*	    	System.out.println(oneLine);
	    	if (oneLine.contains("rule105 > 0.8")) {
	    		System.out.println("!");
	    	}*/
	    	int pos = oneLine.indexOf("rule");
	    	int pos2 = oneLine.indexOf(" ",pos);
	    	int rule = Integer.parseInt(oneLine.substring(pos+4,pos2));
	    	String comp = oneLine.substring(pos2+1,pos2+2);
	    	if (comp.equals("<")) {									//create new node
	    		int pos3 = oneLine.indexOf(" ",pos2+2);
	    		int pos4 = oneLine.indexOf(":");
	    		if (pos4==-1) {pos4 = oneLine.length();}
	    		double compvalue = Double.parseDouble(oneLine.substring(pos3+1,pos4));
	    		current.compvalue = compvalue;
	    		current.rule = rule;
	    		current.lower = new DTElement();
	    		current.lower.parent = current;
	    		current.higher = new DTElement();
	    		current.higher.parent = current;
	    		current = current.lower;
	    	} 
	    	
    		int pos5 = oneLine.indexOf(":");
	    	if (pos5!=-1) {											//solution in leaf
	    		int classification = Integer.parseInt(oneLine.substring(pos5+2,pos5+3));
	    		int pos6 = oneLine.indexOf("(");
	    		int pos7 = oneLine.indexOf("/");
	    		int pos8 = oneLine.indexOf(")");
	    		double confidence = 0.0;
	    		if (pos7!=-1) {
	    			double first = Double.parseDouble(oneLine.substring(pos6+1,pos7-1));
	    			double second = Double.parseDouble(oneLine.substring(pos7+1,pos8-1));
	    			confidence = (first-second)/first;
	    		} else {
	    			confidence = 1.0;
	    		}
	    		current.confidence = confidence;
	    		current.classification = classification;
	    		current.lower = null;
	    		current.higher = null;
	    	} 
	    	    	
	    	lastdepth = depth;										//take next open node
	    	oneLine=filebuf.readLine();
	    	pos = oneLine.indexOf("rule");
	    	depth = pos/4;
	    	int diff = lastdepth-depth;
	    	if (diff==-1) {
//	    		current = current;
	    	} else {
	    		while (diff>=0) {
	    			current = current.parent;
	    			diff--;
	    		}
	    		current = current.higher;
	    	}
	    }
	    in.close();
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}
	
	public void process() {
		reset();
		result = processDTElement(root);		
	}
	
	private double processDTElement(DTElement current) {
		if (current.lower==null) {
			if (current.classification==1) {
				return current.confidence;
			} else {
				return 1-current.confidence;
			}
		} else {
			if (calculated[current.rule]==false) {
				value[current.rule]=rules.process(object1,object2,current.rule,ontology);
				calculated[current.rule]=true;
			}
			if (value[current.rule]<=current.compvalue) {
				return processDTElement(current.lower);
			} else {
				return processDTElement(current.higher);
			}
		}
	}
	
	public static void main(String[] args) {
//		DecisionTree dtCombination = new DecisionTree("C:/Work/MLCompleteKAON2/tree2.txt",null,null);
	}
}