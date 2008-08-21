/*
 * Created on 29.12.2004
 *
 */
package edu.unika.aifb.foam.util;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.kaon2.api.Entity;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

//import com.Ostermiller.util.CSVParser;

import edu.unika.aifb.foam.input.MyOntology;

/**
 * @author meh
 * This class transforms the different existing mapping formats into each other.
 */
public class INRIAAlignmentAPI {
	
//	private static final String DIRECTION = "CSVtoINRIA";
	private static final String DIRECTION = "INRIAtoCSV";
//	private static final String DIRECTION = "LMtoCSV";
	private static final String INPUTFILE = "C:/Work/MLcompleteKAON2/eon04/onto304abMap.rdf";
	private static final String OUTPUTFILE ="C:/Work/MLcompleteKAON2/eon04/onto304abMapCSV.txt";
//	private static final String[] OAEINUMBERS = {"103","104","201","202","203","204","205","206","207","208","209","210","221","222","223","224","225","228","230","231","232","236","237","238","239","240","241","246","247","248","249","250","251","252","253","254","257","258","259","260","261","262","265","266","301","302","303","304"};	
	
	/**
	 * Karlsruhe array format to INRIA alignment API format.
	 * @param array
	 * @return
	 */
	public String ArrayToString(String[][] array, String onto1, String onto2) {
		String string = new String();
		if (array!=null) {
		string = string + 
				"<?xml version='1.0' encoding='utf-8' standalone='no'?>\n" +
				"<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n" +
				"         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
				"         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'>\n"+
				"<Alignment>\n" +
				"  <xml>yes</xml>\n" +
				"  <level>0</level>\n" +
				"  <type>11</type>\n";
		if (onto1.equals("")) {
			onto1 = array[0][0];
			int j = onto1.indexOf("#");
			if (j!=-1) {onto1 = onto1.substring(0,j);}
			onto2 = array[0][1];
			j = onto2.indexOf("#");
			if (j!=-1) {onto2 = onto2.substring(0,j);}
			if (onto1.hashCode()<onto2.hashCode()) {
				String onto3 = onto1;
				onto1 = onto2;
				onto2 = onto3;
			}
		}
		string = string +"  <onto1>"+onto1+"</onto1>\n" +
			"  <onto2>"+ onto2 +"</onto2>\n" +
			"  <uri1>"+ onto1 +"</uri1>\n" +
			"  <uri2>"+ onto2 +"</uri2>\n" +
			"  <map>\n";
		for (int i = 0; i<array.length; i++) {
			String value = "1.0";
			if ((array[i].length>=3)&&(array[i][2].length()>1)) value = array[i][2]; 
			if ((array[i][0].startsWith(onto1))&&(array[i][1].startsWith(onto2))) {
				string = string + "    <Cell>\n"+
					"	<entity1 rdf:resource='"+array[i][0]+"'/>\n"+
					"	<entity2 rdf:resource='"+array[i][1]+"'/>\n"+
					"	<measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>"+value+"</measure>\n"+
					"	<relation>=</relation>\n"+
					"    </Cell>\n";
			}
		}
		string = string + "  </map>\n"+
			"</Alignment>\n"+
			"</rdf:RDF>\n";		
		}
		return string;
	}
	
	public Vector withoutInstances(Vector input, MyOntology ontologies) {
		HashSet uriset = new HashSet();
		Set entitySet = new HashSet();
		try{
		entitySet = ontologies.ontology.createEntityRequest(OWLClass.class).getAll();
		entitySet.addAll(ontologies.ontology.createEntityRequest(ObjectProperty.class).getAll());
		entitySet.addAll(ontologies.ontology.createEntityRequest(DataProperty.class).getAll());
		} catch (Exception e) {}
		Iterator iter = entitySet.iterator();
		while (iter.hasNext()) {
			Entity entity = (Entity) iter.next();
			uriset.add(entity.getURI());
		}
		Vector output = new Vector();
		for (int i = 0; i<input.size(); i++) {
			String[] dataset = (String[]) input.elementAt(i);
			if (uriset.contains(dataset[0])&&(uriset.contains(dataset[1]))) {
				output.add(dataset);
			}
		}
		return output;
	}
	
	/**
	 * INRIA Alignment API format to Karlsruhe array format
	 * @param string
	 * @return
	 */
	public String[][] StringToArray(String string) {
		int size = string.length()/100;
		String array[][] = new String[size][];
		int index = 0;
		int pos1 = string.indexOf("<entity1");
		while (pos1!=-1) {
			String element[] = new String[3];
			pos1 = pos1+23;
			int pos2 = string.indexOf("/>",pos1)-1;
			element[0] = string.substring(pos1,pos2);
			pos1 = string.indexOf("<entity2",pos2)+23;
			pos2 = string.indexOf("/>",pos1)-1;
			element[1] = string.substring(pos1,pos2);
			pos1 = string.indexOf("#float",pos2)+8;
			pos2 = string.indexOf("</measure>",pos1);
			element[2] = string.substring(pos1,pos2);
			pos1 = string.indexOf("<relation>",pos2)+10;
			pos2 = string.indexOf("</relation>",pos1);
			String relation = string.substring(pos1,pos2);
			if (relation.equals("=")) {
				array[index] = element;	
				index++;	
//				System.out.println(element[0]+";"+element[1]+";"+element[2]);
			}
			pos1 = string.indexOf("<entity1",pos2);				
		}
		String array2[][] = new String[index][];
		for (int i = 0; i<index; i++) {
			array2[i]=array[i];
		}
		return array2;
	}

	/**
	 * Lockheed Martin N3 notation to Karlsruhe array format
	 * @param string
	 * @return
	 */
	public String[][] LMStringToArray(String string) {
		int size = string.length()/50;
		String array[][] = new String[size][];
		int index = 0;
		int pos1 = string.indexOf("@prefix a: <")+12;
		int pos2 = string.indexOf(">.",pos1);
		String ns1 = string.substring(pos1,pos2);
		pos1 = string.indexOf("@prefix b: <",pos2)+12;
		pos2 = string.indexOf(">.",pos1);
		String ns2 = string.substring(pos1,pos2);
		pos1 = string.indexOf("ao:elementA a:",pos2);
		while (pos1!=-1) {
			String element[] = new String[3];
			pos1 = pos1+14;
			pos2 = string.indexOf(" ;",pos1);
			element[0] = ns1+string.substring(pos1,pos2);
			pos1 = string.indexOf("ao:elementB b:",pos2)+14;
			pos2 = string.indexOf(" ;",pos1);
			element[1] = ns2+string.substring(pos1,pos2);
			pos1 = string.indexOf("ao:alignmentConfidence ",pos2)+24;
			pos2 = string.indexOf("\".",pos1);
			element[2] = string.substring(pos1,pos2);
			array[index] = element;
			index++;
			pos1 = string.indexOf("ao:elementA a:",pos2);
		}
		String array2[][] = new String[index][];
		for (int i = 0; i<index; i++) {
			array2[i]=array[i];
		}
		return array2;
	}
	
	
	/**
	 * The main program carrying out the conversion.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
		INRIAAlignmentAPI api = new INRIAAlignmentAPI();
/*		String INPUTFILE;
		String OUTPUTFILE;
		for (int j = 0; j<OAEINUMBERS.length; j++) {
			INPUTFILE = "C:/Work/DissData/oaei05/onto"+OAEINUMBERS[j]+"Map.rdf";
			OUTPUTFILE = "C:/Work/DissData/oaei05/onto"+OAEINUMBERS[j]+"MapCSV.txt";*/
		if (DIRECTION.equals("CSVtoINRIA")) {
			/*			InputStream inputStream = new FileInputStream(fileName);	
			CSVParser csvparser = new CSVParser(inputStream, "", "", "");
			csvparser.changeDelimiter(';');		
			String array[][] = csvparser.getAllValues();*/
			CSVParse csvParse = new CSVParse(INPUTFILE);
			String array[][] = csvParse.getAllValues();
			String inria = api.ArrayToString(array,"","");
			File result = new File(OUTPUTFILE);
			FileWriter out = new FileWriter(result);
		    out.write(inria);
		    out.close();
		} else if (DIRECTION.equals("INRIAtoCSV")) {
		    FileReader in = new FileReader(INPUTFILE);
		    BufferedReader filebuf = new BufferedReader(in);
		    String oneLine = filebuf.readLine();
		    String string = "";
		    while (oneLine!=null) {
		    	string = string + oneLine + "\n";
		      oneLine=filebuf.readLine();
		    }
		    in.close();
			File result = new File(OUTPUTFILE);
			FileWriter out = new FileWriter(result);			
			String array[][] = api.StringToArray(string);
			for (int i = 0; i<array.length; i++) {
				out.write(array[i][0]+";"+array[i][1]+";"+array[i][2]+";\n");
			}
			out.close();
		} else if (DIRECTION.equals("LMtoCSV")) {
		    FileReader in = new FileReader(INPUTFILE);
		    BufferedReader filebuf = new BufferedReader(in);
		    String oneLine = filebuf.readLine();
		    String string = "";
		    while (oneLine!=null) {
		    	string = string + oneLine + "\n";
		      oneLine=filebuf.readLine();
		    }
		    in.close();
			File result = new File(OUTPUTFILE);
			FileWriter out = new FileWriter(result);			
			String array[][] = api.LMStringToArray(string);
			for (int i = 0; i<array.length; i++) {
				out.write(array[i][0]+";"+array[i][1]+";"+array[i][2]+";\n");
			}
			out.close();			
		}
//		}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}

}
