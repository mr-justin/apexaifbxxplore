package org.ateam.xxplore.core.service.sampling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

public class NTriples2RDFXMLSampler {
	
	private BufferedReader br;
	private NTriplesParser parser;
	
	private BufferedWriter bw;
	private RDFXMLWriter writer;
	
	private CountHandler countHandler;

	private int tripleCount = 0;
	
	private static String sourceDir = "D:/BTC/swetodblp_april_2008-mod.nt";
	private static String targetDir = "res/BTC/target.rdf";
	private static int maxTripleCount = 10000;
	
	public NTriples2RDFXMLSampler(String sourceDir, String targetDir) {
		try {
			br = new BufferedReader(new FileReader(sourceDir));
			parser = new NTriplesParser();
			countHandler = new CountHandler();
			parser.setRDFHandler(countHandler);
			new File(targetDir).getParentFile().mkdirs();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetDir),"UTF-8")); 
			writer = new RDFXMLWriter(bw);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public NTriples2RDFXMLSampler(String sourceDir, String targetDir, int maxTripleCount) {
		try {
			br = new BufferedReader(new FileReader(sourceDir));
			parser = new NTriplesParser(maxTripleCount);
			countHandler = new CountHandler();
			parser.setRDFHandler(countHandler);
			new File(targetDir).getParentFile().mkdirs();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetDir),"UTF-8")); 
			writer = new RDFXMLWriter(bw);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) {
		NTriples2RDFXMLSampler writer = new NTriples2RDFXMLSampler(sourceDir, targetDir, maxTripleCount);;
		try {
			String baseURI = "http://example.org";
			writer.parse(baseURI);
			System.out.println("tripleCount: " + writer.tripleCount());
		} finally {
			writer.close();
		}
	}
	
	public void parse(String baseURI) {
		try {
			parser.parse(br, baseURI);
			tripleCount += countHandler.count;
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int tripleCount() {
		return tripleCount;
	}
	
	public void close() {
		try {
			if(br != null)
				br.close();
			if(bw != null)
				bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public class CountHandler extends RDFHandlerBase {

		private int count = 0;
		
		public void endRDF() throws RDFHandlerException {
			super.endRDF();
			writer.endRDF();
			// System.out.println("Counted " + count + " statements.");
		}

		public void handleStatement(Statement st) {
			try {
				writer.handleStatement(st);
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(st.getSubject());
			System.out.println(st.getPredicate());
			System.out.println(st.getObject());
			System.out.println();
			count++;
		}

		public void startRDF() throws RDFHandlerException {
			super.startRDF();
			writer.startRDF();
			count = 0;
		}

	}

}
