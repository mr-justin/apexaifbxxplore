package org.ateam.xxplore.core.service.sampling;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.MeasurableInputStream;
import it.unimi.dsi.law.warc.filters.Filter;
import it.unimi.dsi.law.warc.filters.Filters;
import it.unimi.dsi.law.warc.io.GZWarcRecord;
import it.unimi.dsi.law.warc.io.WarcFilteredIterator;
import it.unimi.dsi.law.warc.io.WarcRecord;
import it.unimi.dsi.law.warc.util.BURL;
import it.unimi.dsi.law.warc.util.WarcHttpResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

public class Warc2RDFXMLSampler {
	
	private FastBufferedInputStream in;
	private GZWarcRecord record;
	private Filter<WarcRecord> filter;
	private WarcFilteredIterator it;
	private WarcHttpResponse response; 
	private ValueFactory factory;
	
	private NTriplesParser parser;
	private CountHandler countHandler;
	
	private BufferedWriter bw;
	private RDFXMLWriter writer;
	
	private String targetDirectory;
	
	private int urlCount = 0;
	private int maxUrlCount = -1; 
	private String currentUrl = "";
	private int tripleCountAllUrls = 0;
	private int tripleCountThisUrl = 0;
	private int lineCountAllUrls = 0;
	private int lineCountThisUrl = 0;

	private static String sourceDir = "D:/BTC/watson.warc";
	private static String targetDir = "res/BTC/watson/";
	private static int urls = 20;
	
	public Warc2RDFXMLSampler(String sourceDir, String targetDir) {
		try {
			in = new FastBufferedInputStream(new FileInputStream(new File(sourceDir)));
			record = new GZWarcRecord();
			filter = Filters.adaptFilterBURL2WarcRecord(new TrueFilter());
			it = new WarcFilteredIterator(in, record, filter);
			response = new WarcHttpResponse();
			parser = new NTriplesParser();
			countHandler = new CountHandler();
			parser.setRDFHandler(countHandler);
			targetDirectory = targetDir;
			new File(targetDir).mkdirs();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public Warc2RDFXMLSampler(String sourceDir, String targetDir, int maxUrlCount) throws FileNotFoundException, RepositoryException{
		this(sourceDir, targetDir);
		this.maxUrlCount = maxUrlCount;
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws Exception {
		
		Warc2RDFXMLSampler writer = new Warc2RDFXMLSampler(sourceDir, targetDir, urls);
		try {
			while (writer.hasNext() && ((writer.getUrlCount() < writer.getMaxUrlCount()) || (writer.getMaxUrlCount() == -1))) {
				WarcRecord nextRecord = writer.next();
				// Get the HttpResponse
				try {
					writer.getResponse().fromWarcRecord(nextRecord);

					// This will dump the content of the record
//					reader.dumpContent(reader.getResponse().contentAsStream());
//					System.out.println("Processing: " + nextRecord.header.subjectUri);
//					System.out.println("lineCount this Url: " + lineCountThisUrl);
//					System.out.println("lineCount all Urls: " + lineCountAllUrls);

					// This will count the number of triples by parsing the RDF
					writer.parseTriples(writer.getResponse().contentAsStream(), nextRecord.header.subjectUri.toString());
					System.out.println("urlCount: " + writer.getUrlCount());
					System.out.println("Processing Url: " + nextRecord.header.subjectUri);
					System.out.println("tripleCount for this Url: " + writer.getTripleCountThisUrl());
					System.out.println("tripleCount for all Urls: " + writer.getTripleCountAllUrls());

				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (RuntimeException re) {}
		finally {
			writer.close();
		}
	}

	private int getTripleCountAllUrls() {
		return tripleCountAllUrls;
	}


	private int getTripleCountThisUrl() {
		return tripleCountThisUrl;
	}


	private int getUrlCount() {
		return urlCount;
	}


	private int getMaxUrlCount() {
		return maxUrlCount;
	}


	public boolean hasNext(){
		return it.hasNext();
	}
	
	public WarcRecord next() {
		return it.next();
	}
	
	public WarcHttpResponse getResponse() {
		return response;
	}
	
	public void close() {
		try {
			if(in != null)
				in.close();
			if(bw != null)
				bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	public void dumpContent(MeasurableInputStream block) throws IOException {
		int c = 0;
		lineCountThisUrl = 0;
		while ((c = block.read()) != -1) {
			if (c == '\n') {
				lineCountThisUrl++;
				lineCountAllUrls++;
			}
			System.out.write(c);
		}
	}
	
	public void parseTriples(MeasurableInputStream block, String base) {
		try {
			currentUrl = base;
			parser.parse(block, base);
			urlCount++;
			tripleCountAllUrls += countHandler.count;
			tripleCountThisUrl = countHandler.count;
			
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String fsTransduceUri(String uri) {

		uri = StringUtils.replace(uri, ":", "COLON");
		uri = StringUtils.replace(uri, "/", "SLASH");
		uri = StringUtils.replace(uri, "#", "SHARP");

		return uri;
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
			System.out.println(st);
			count++;
		}

		public void startRDF() throws RDFHandlerException {
			super.startRDF();
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}	
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetDirectory + fsTransduceUri(currentUrl) + ".rdf"),"UTF-8")); 
				writer = new RDFXMLWriter(bw);
				writer.startRDF();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count = 0;
		}

	}
	
	public static class TrueFilter extends Filter<BURL> {

		@Override
		public boolean accept(BURL x) {
			return true;
		}

		@Override
		public String toExternalForm() {

			return "true";
		}
	}

}
