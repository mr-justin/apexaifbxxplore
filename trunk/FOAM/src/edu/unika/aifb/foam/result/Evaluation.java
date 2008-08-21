/*
 * Created on 15.08.2003
 *
 */
package edu.unika.aifb.foam.result;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

//import com.Ostermiller.util.CSVParser;

//import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.util.CSVParse;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * Allows to load a mapping file. During runtime it can directly check whether
 * two entities are regarded as equivalent in the mapping file. This is required 
 * for the Saving class.
 * 
 * @author Marc Ehrig
 */
public class Evaluation {

	private Set mappings = new HashSet();
	private boolean mappingsfound = false;
	
	public int size = 0;

	public Evaluation(String fileName) {
		if (fileName!="") {
		try {
			CSVParse csvParse = new CSVParse(fileName);
			String array[][] = csvParse.getAllValues();
			for (int i = 0; i<(array.length); i++){
				if ((array[i].length<3)||(array[i][2].equals("0")==false)) {
					String mapping = array[i][0].replace(" ","")+array[i][1].replace(" ","");
					mappings.add(mapping);
					size++;
				}
			}
			mappingsfound = true;
		} catch (Exception e) {
			UserInterface.print(e.getMessage());		
		}
		}
	}

	private double cuprecision = 0;
	private double curecall = 0;
	private double pmprecision = 1.0;
	private double pmrecall = 0;
	private double fmprecision = 1.0;
	private double fmrecall = 0;
	private double rmprecision = 0;
	private double rmrecall = 0;
	private double omprecision = 1.0;
	private double omrecall = 0;
	
	public void doEvaluation(Structure structure, ResultList list, double cutoff) {
/*		if (fileName!="") {
			try {
				InputStream inputStream; 
				if (fileName.startsWith("http://")) {
					URL address = new URL(fileName);
			        URLConnection urlConnection = address.openConnection();
			        inputStream = urlConnection.getInputStream();
				} else {
					inputStream = new FileInputStream(fileName);	
				}		
				CSVParser csvparser = new CSVParser(inputStream, "", "", "");
				csvparser.changeDelimiter(';');		
				String array[][] = csvparser.getAllValues();
				for (int i = 0; i<(array.length); i++){
					if ((array[i].length<3)||(array[i][2].equals("0")==false)) {
						String mapping = array[i][0]+array[i][1];
						mappings.add(mapping);
						size++;
					}
				}
				mappingsfound = true;
			} catch (Exception e) {
				UserInterface.print(e.getMessage());		
			}
		}*/
//		MyOntology myOntology = (MyOntology) structure; 
		cuprecision = 0.0;		//
		curecall = 0.0;			//
		pmprecision = 0.0;		//
		pmrecall = 0.0;			//
		fmprecision = 1.0;		//
		fmrecall = 0.0;			//
		omprecision = 1.0;		//
		omrecall = 0.0;			//
		rmprecision = 0.0;		//
		rmrecall = 0.0;			//
		boolean cuset = false;
		int found = 0;
//		int lastfound = 0;
		int counter = 0;
		double value = 1.0;
		double lastvalue = value;
		try {
		Iterator iter = list.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			Object object2 = list.getObject(object1,0);
			lastvalue = value;
			value = list.getValue(object1,0);
//			Object addInfo = list.getAddInfo(object1,0);
//			lastfound = found;
			if ((lastvalue!=value)&&(counter>0)) {
				double precisionnew = 1.0*found/counter;
				double recallnew = 1.0*found/(size*2);
				if ((value<cutoff)&&(cuset==false)) {
					cuprecision = precisionnew;
					curecall = recallnew;
					cuset = true;
				}
				if (precisionnew>=pmprecision) {
					pmprecision = precisionnew;
					pmrecall = recallnew;				
				}
				if ((2.0*precisionnew*recallnew/(precisionnew+recallnew))>(2.0*fmprecision*fmrecall/(fmprecision+fmrecall))) {
					fmprecision = precisionnew;
					fmrecall = recallnew;
				}
				if ((recallnew*(2.0-1.0/precisionnew))>(omrecall*(2.0-1.0/omprecision))) {
					omprecision = precisionnew;
					omrecall = recallnew;
				}
				if (recallnew>rmrecall) {
					rmprecision = precisionnew;
					rmrecall = recallnew;
				}
			}
			if (entities(object1,object2).equals("1")) {
				found++;
			}
			counter++;
		}
		value = 0.0;
//		lastfound = found;
		double precisionnew = 1.0*found/counter;
		double recallnew = 1.0*found/(size*2);
		if ((value<cutoff)&&(cuset==false)) {
			cuprecision = precisionnew;
			curecall = recallnew;
			cuset = true;
		}
		if (precisionnew>=pmprecision) {
			pmprecision = precisionnew;
			pmrecall = recallnew;				
		}
		if ((2.0*precisionnew*recallnew/(precisionnew+recallnew))>(2.0*fmprecision*fmrecall/(fmprecision+fmrecall))) {
			fmprecision = precisionnew;
			fmrecall = recallnew;
		}
		if ((recallnew*(2.0-1.0/precisionnew))>(omrecall*(2.0-1.0/omprecision))) {
			omprecision = precisionnew;
			omrecall = recallnew;
		}
		if (recallnew>rmrecall) {
			rmprecision = precisionnew;
			rmrecall = recallnew;
		}
/*		double pmprecisionnew = 1.0*found/counter;
		if (pmprecisionnew>=pmprecision) {
			pmprecision = pmprecisionnew;
			pmrecall = 1.0*found/(size*2);				
		}
		if ((value<cutoff)&&(cuset==false)) {
			cuprecision = 1.0*found/counter;
			curecall = 1.0*found/(size*2);
		}
		double rmrecallnew = 1.0*found/(size*2);
		if (rmrecallnew>rmrecall) {
			rmprecision = 1.0*found/counter;
			rmrecall = rmrecallnew;
		}*/
		} catch (Exception e) {
			UserInterface.print(e.getMessage());		
		}
	}

	public void doEvaluation(Structure structure, Vector vector, double cutoff) {
//		MyOntology myOntology = (MyOntology) structure; 
		cuprecision = 0.0;		//
		curecall = 0.0;			//
		pmprecision = 0.0;		//
		pmrecall = 0.0;			//
		fmprecision = 1.0;		//
		fmrecall = 0.0;			//
		omprecision = 1.0;		//
		omrecall = 0.0;			//
		rmprecision = 0.0;		//
		rmrecall = 0.0;			//
		boolean cuset = false;
		int found = 0;
//				int lastfound = 0;
		int counter = 0;
		double value = 1.0;
		double lastvalue = value;
		for (int i = 0; i<vector.size(); i++) {
			String[] dataset = (String[]) vector.get(i);
			Object object1 = dataset[0];
			Object object2 = dataset[1];
			lastvalue = value;
			value = Double.valueOf(dataset[2]).doubleValue();
//					Object addInfo = list.getAddInfo(object1,0);
//					lastfound = found;
			if ((lastvalue!=value)&&(counter>0)) {
				double precisionnew = 1.0*found/counter;
				double recallnew = 1.0*found/(size*2);
				if ((value<cutoff)&&(cuset==false)) {
					cuprecision = precisionnew;
					curecall = recallnew;
					cuset = true;
				}
				if (precisionnew>=pmprecision) {
					pmprecision = precisionnew;
					pmrecall = recallnew;				
				}
				if ((2.0*precisionnew*recallnew/(precisionnew+recallnew))>(2.0*fmprecision*fmrecall/(fmprecision+fmrecall))) {
					fmprecision = precisionnew;
					fmrecall = recallnew;
				}
				if ((recallnew*(2.0-1.0/precisionnew))>(omrecall*(2.0-1.0/omprecision))) {
					omprecision = precisionnew;
					omrecall = recallnew;
				}
				if (recallnew>rmrecall) {
					rmprecision = precisionnew;
					rmrecall = recallnew;
				}
			}
			if (entities(object1,object2).equals("1")) {
				found++;
			}
			counter++;
		}
		value = 0.0;
//				lastfound = found;
		double precisionnew = 1.0*found/counter;
		double recallnew = 1.0*found/(size*2);
		if ((value<cutoff)&&(cuset==false)) {
			cuprecision = precisionnew;
			curecall = recallnew;
			cuset = true;
		}
		if (precisionnew>=pmprecision) {
			pmprecision = precisionnew;
			pmrecall = recallnew;				
		}
		if ((2.0*precisionnew*recallnew/(precisionnew+recallnew))>(2.0*fmprecision*fmrecall/(fmprecision+fmrecall))) {
			fmprecision = precisionnew;
			fmrecall = recallnew;
		}
		if ((recallnew*(2.0-1.0/precisionnew))>(omrecall*(2.0-1.0/omprecision))) {
			omprecision = precisionnew;
			omrecall = recallnew;
		}
		if (recallnew>rmrecall) {
			rmprecision = precisionnew;
			rmrecall = recallnew;
		}
	}

	
	/**
	 * Checks its internal list if the two objects are equivalent according to the
	 * loaded mapping file.
	 * @param object1
	 * @param object2
	 * @return yes/no
	 */
	public String entities(Object object1, Object object2) {
		if (mappingsfound==false) {
			return "?";
		}
		String string1 = object1.toString();
		String string2 = object2.toString();
		try{
			if (object1 instanceof OWLEntity) {
				OWLEntity entity1 = (OWLEntity) object1;
				string1 = entity1.getURI();
			}
			if (object2 instanceof OWLEntity) {
				OWLEntity entity2 = (OWLEntity) object2;
				string2 = entity2.getURI();
			}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());	
		}
		if ((mappings.contains(string1+string2))||(mappings.contains(string2+string1))) {
			return "1";
		}
		return "0";
	}
	
	/**
	 * Prints the evaluation results to the standard output.
	 * @param structure
	 * @param list
	 * @param evalFile
	 * @param cutoff
	 */
	public void printEvaluation() {
//		doEvaluation(structure,list,evalFile,cutoff);
//		UserInterface.print("\nEvaluation:\n");
		UserInterface.print("\nPoint of cutoff:  precision "+cuprecision+"  recall "+curecall+"  f-measure "+2*cuprecision*curecall/(cuprecision+curecall)+" overlap "+curecall*(2.0-1.0/cuprecision));
		UserInterface.print("\nPoint of maximum precision (ex post):  precision "+pmprecision+"  recall "+pmrecall+"  f-measure "+2*pmprecision*pmrecall/(pmprecision+pmrecall)+" overlap "+pmrecall*(2.0-1.0/pmprecision));
		UserInterface.print("\nPoint of maximum f-measure (ex post):  precision "+fmprecision+"  recall "+fmrecall+"  f-measure "+2*fmprecision*fmrecall/(fmprecision+fmrecall)+" overlap "+fmrecall*(2.0-1.0/fmprecision));
		UserInterface.print("\nPoint of maximum overlap (ex post):  precision "+omprecision+"  recall "+omrecall+"  f-measure "+2*omprecision*omrecall/(omprecision+omrecall)+" overlap "+omrecall*(2.0-1.0/omprecision));
		UserInterface.print("\nPoint of maximum recall (ex post):  precision "+rmprecision+"  recall "+rmrecall+"  f-measure "+2*rmprecision*rmrecall/(rmprecision+rmrecall)+" overlap "+rmrecall*(2.0-1.0/rmprecision)+"\n");		
	}		
	
	/**
	 * Returns the evaluation results in an array (cutoff (p,r,f), maximum precision (prec,rec,f), maximum f-measure (p,r,f), maximum recall (p,r,f)).
	 * @param structure
	 * @param list
	 * @param evalFile
	 * @param cutoff
	 * @return
	 */
	public double[] returnEvaluation() {
//		doEvaluation(structure,list,evalFile,cutoff);
		double[] eval = new double[20];
		eval[0] = cuprecision;
		eval[1] = curecall;
		eval[2] = 2*cuprecision*curecall/(cuprecision+curecall);		
		eval[3] = curecall*(2.0-1.0/cuprecision);
		eval[4] = pmprecision;
		eval[5] = pmrecall;
		eval[6] = 2*pmprecision*pmrecall/(pmprecision+pmrecall);
		eval[7] = pmrecall*(2.0-1.0/pmprecision);
		eval[8] = fmprecision;
		eval[9] = fmrecall;
		eval[10] = 2*fmprecision*fmrecall/(fmprecision+fmrecall);
		eval[11] = fmrecall*(2.0-1.0/fmprecision);
		eval[12] = omprecision;
		eval[13] = omrecall;
		eval[14] = 2*omprecision*omrecall/(omprecision+omrecall);
		eval[15] = omrecall*(2.0-1.0/omprecision);
		eval[16] = rmprecision;
		eval[17] = rmrecall;
		eval[18] = 2*rmprecision*rmrecall/(rmprecision+rmrecall);		
		eval[19] = rmrecall*(2.0-1.0/rmprecision);
		return eval;
	}
	
	public static void main (String args[]) {
		Evaluation eval = new Evaluation("C:/Work/DissData/first03/russia12Map.txt");
	}

}
