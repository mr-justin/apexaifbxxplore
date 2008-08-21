/*
 * Created on 29.06.2004
 *
 */
package edu.unika.aifb.foam.machine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.unika.aifb.foam.rules.IndividualRule;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.rules.feature.Feature;
import edu.unika.aifb.foam.rules.feature.special.FeatureSpecial;
import edu.unika.aifb.foam.rules.feature.special.FeatureSpecialImpl;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * Simply saves and loads objects as files. This only works, if
 * the objects are serializable.
 * 
 * @author Marc Ehrig
 */
public class ObjectIO {

	public static void save(Object object, String fileName) {
		try{	
		FileOutputStream modelOutFile =	new FileOutputStream(fileName);
		ObjectOutputStream modelOutObjectFile =	new ObjectOutputStream(modelOutFile);
		modelOutObjectFile.writeObject(object);
		modelOutObjectFile.flush();
		modelOutFile.close();
		} catch (Exception e) {
			UserInterface.errorPrint(e.getMessage());
		}
	}

	public static Rules toSerializable(Rules rules) {
		for (int i = 0; i<rules.total(); i++) {
			IndividualRule rule = rules.rule(i);
			Feature feature1 = rule.feature1;
			if (feature1 instanceof FeatureSpecialImpl) {
				FeatureSpecial feature1sp = (FeatureSpecial) feature1;
				feature1sp.serialize();
			}
			Feature feature2 = rule.feature2;
			if (feature2 instanceof FeatureSpecialImpl) {
				FeatureSpecial feature2sp = (FeatureSpecial) feature2;
				feature2sp.serialize();
			}
		}
		return rules;
	}
	
	public static Object load(String fileName) { 	
		try {	
		FileInputStream modelInFile = new FileInputStream(fileName);
		ObjectInputStream modelInObjectFile = new ObjectInputStream(modelInFile);
		Object obj = modelInObjectFile.readObject();
		modelInFile.close();
		return obj;
		} catch (Exception e) {
			UserInterface.errorPrint(e.getMessage());
		}		
		return null;
	}
	
	public static Rules fromSerializable(Rules rules) {
		for (int i = 0; i<rules.total(); i++) {
			IndividualRule rule = rules.rule(i);
			Feature feature1 = rule.feature1;
			if (feature1 instanceof FeatureSpecialImpl) {
				FeatureSpecial feature1sp = (FeatureSpecial) feature1;
				feature1sp.deserialize();
			}
			Feature feature2 = rule.feature2;
			if (feature2 instanceof FeatureSpecialImpl) {
				FeatureSpecial feature2sp = (FeatureSpecial) feature2;
				feature2sp.deserialize();
			}
		}
		return rules;
	}

}
