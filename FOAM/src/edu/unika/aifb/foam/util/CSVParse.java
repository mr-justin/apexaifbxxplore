package edu.unika.aifb.foam.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.Vector;

public class CSVParse {

	private String input;
	private String delimiter = ";";
	
	public CSVParse(String inputT) {
		input = inputT;
	}
	
	public void changeDelimiter(String delimiterT){
		delimiter = delimiterT;
	}
	
	public String[][] getAllValues() {
		String[][] numbers = {{""}};
		try {
		Vector vector = new Vector();
		InputStream inputStream; 
		if (input.startsWith("http://")) {
			URL address = new URL(input);
		    URLConnection urlConnection = address.openConnection();
		    inputStream = urlConnection.getInputStream();
		} else {
			inputStream = new FileInputStream(input);	
		}
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);	
		BufferedReader bufRdr  = new BufferedReader(inputStreamReader);
		String line = null;
		int maxrow = 0;
		int maxcol = 0;	 
		while((line = bufRdr.readLine()) != null) {
			if (maxcol==0) {
				StringTokenizer st = new StringTokenizer(line,delimiter);
				maxcol = st.countTokens();
			}
			vector.add(line);
		}
		bufRdr.close(); 
		maxrow = vector.size();	
		numbers = new String[maxrow][maxcol];
		for (int row = 0; row<maxrow; row++) {
			int col = 0;
			line = (String) vector.elementAt(row);
			StringTokenizer st = new StringTokenizer(line,delimiter);
			while (st.hasMoreTokens())
			{	
				String token = st.nextToken();
				numbers[row][col] = token;
				col++;
			}
		}
		} catch (Exception e) {
			UserInterface.errorPrint(e.getMessage());
		}
		return numbers;
	}
	
	public static void main(String args[]) {
		CSVParse csvParse = new CSVParse("C:/FOAM/onto/onto303MapCSV.txt");
		String[][] result = csvParse.getAllValues();
		System.out.println (result.length+" "+result[0].length);
		for (int i = 0; i< result.length; i++) {
			for (int j = 0; j<result[0].length; j++) {
				System.out.print(result[i][j]+" ");
			}
			System.out.println();
		}
	}
	
}
