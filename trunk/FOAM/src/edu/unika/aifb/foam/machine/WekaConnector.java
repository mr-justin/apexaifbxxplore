/*
 * Created on 23.06.2004
 *
 */
package edu.unika.aifb.foam.machine;

import java.io.*;
//import java.util.Random;

import edu.unika.aifb.foam.util.UserInterface;

import weka.classifiers.Classifier;
//import weka.classifiers.CostMatrix;
import weka.core.Instances;

/**
 * The WekaConnector creates the files in a weka's specific format.
 * They are saved and loaded via this interface.
 * ({@link http://www.cs.waikato.ac.nz/~ml/weka/})
 * 
 * @author Marc Ehrig
 */
public class WekaConnector {

	public class ClassifierObject {
		Classifier classifier;
		Instances instances;
	}

	public static void saveData(String[][] array, String fileName) {	
		try {
		File result = new File(fileName);
		result.delete();
		FileWriter out = new FileWriter(result);	
		out.write("@relation mapping\n\n");
		int rulestotal = array[0].length-6;
		for (int i = 0; i<rulestotal; i++) {
			out.write("@attribute rule"+i+" real\n");
		}
//		out.write("@attribute result real\n\n@data\n");
		out.write("@attribute result {0,1}\n\n@data\n");
		for (int i = 0; i<array.length; i++) {
			for (int j = 0; j<rulestotal; j++) {
				out.write(array[i][j+5]+",");
			}
			out.write(array[i][rulestotal+5]+"\n");
		}	
		out.close();
		} catch (Exception e) {
			UserInterface.print(e.getMessage());		
		}
	}				
	
	public static Instances loadData(String fileName, String costmatrix) {
		try{
		FileReader reader = new FileReader(costmatrix);
//		CostMatrix costs = new CostMatrix(reader);
//		Random random = new Random();
		reader = new FileReader(fileName); 						// Read all the instances in the file
		Instances instances = new Instances(reader);
		instances.setClassIndex(instances.numAttributes() - 1);	//	Make the last attribute be the class 
//		instances = costs.applyCostMatrix(instances,random);
		return instances;
		} catch (Exception e) {			UserInterface.print(e.getMessage());
		return null;}
	}
	
/*	public static void saveClassifier(Classifier classifier, Instances instances, String fileName) {
		try{
		ClassifierObject classObj = new ClassifierObject();
		classObj.classifier = classifier;
		classObj.instances = instances;
		FileOutputStream modelOutFile =	new FileOutputStream(fileName);
		ObjectOutputStream modelOutObjectFile =	new ObjectOutputStream(modelOutFile);
		modelOutObjectFile.writeObject(classObj);
		modelOutObjectFile.flush();
		modelOutFile.close();
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}
	
	public static Classifier loadClassifier(String fileName) {
		try{
		FileInputStream modelInFile = new FileInputStream(fileName);
		ObjectInputStream modelInObjectFile = new ObjectInputStream(modelInFile);
		ClassifierObject classObj = (ClassifierObject) modelInObjectFile.readObject();
		modelInFile.close();
		return classObj.classifier;
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}		
		return null;
	}*/

}
