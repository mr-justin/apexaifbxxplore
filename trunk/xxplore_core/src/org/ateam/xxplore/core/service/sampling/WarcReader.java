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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.sail.memory.MemoryStore;


/**
 * 
 */
public class WarcReader {
	
	private FastBufferedInputStream in;
	private GZWarcRecord record;
	private Filter<WarcRecord> filter;
	private WarcFilteredIterator it;
	private WarcHttpResponse response; 
	private ValueFactory factory;
	
	private NTriplesParser parser;
	private CountHandler countHandler;
	
	private int urlCount = 0;
	private int maxUrlCount = -1; 
	private String currentUrl = "";
	private int tripleCountAllUrls = 0;
	private int tripleCountThisUrl = 0;
	private int lineCountAllUrls = 0;
	private int lineCountThisUrl = 0;

	private Repository myRepository;
	private RepositoryConnection connection;
	
	private static String sourceDir = "D:\\watson.warc";
	private static String repositoryDir = "D:\\sampling\\repository\\WARC";
	private static int urls = 5;
	
	public WarcReader(String sourceDir, String repositoryDir) {
		try {
			in = new FastBufferedInputStream(new FileInputStream(new File(sourceDir)));
			record = new GZWarcRecord();
			filter = Filters.adaptFilterBURL2WarcRecord(new TrueFilter());
			it = new WarcFilteredIterator(in, record, filter);
			response = new WarcHttpResponse();
			parser = new NTriplesParser();
			countHandler = new CountHandler();
			parser.setRDFHandler(countHandler);
			myRepository = new SailRepository(new MemoryStore(new File(repositoryDir)));
			myRepository.initialize();
			connection = myRepository.getConnection();
			factory = myRepository.getValueFactory();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public WarcReader(String sourceDir, String repositoryDir, int maxUrlCount) throws FileNotFoundException, RepositoryException{
		this(sourceDir, repositoryDir);
		this.maxUrlCount = maxUrlCount;
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws Exception {
		
		WarcReader reader = new WarcReader(sourceDir, repositoryDir, urls);
		try {
			while (reader.hasNext() && ((reader.getUrlCount() < reader.getMaxUrlCount()) || (reader.getMaxUrlCount() == -1))) {
				WarcRecord nextRecord = reader.next();
				// Get the HttpResponse
				try {
					reader.getResponse().fromWarcRecord(nextRecord);

					// This will dump the content of the record
//					reader.dumpContent(reader.getResponse().contentAsStream());
//					System.out.println("Processing: " + nextRecord.header.subjectUri);
//					System.out.println("lineCount this Url: " + lineCountThisUrl);
//					System.out.println("lineCount all Urls: " + lineCountAllUrls);

					// This will count the number of triples by parsing the RDF
					reader.parseTriples(reader.getResponse().contentAsStream(), nextRecord.header.subjectUri.toString());
					System.out.println("urlCount: " + reader.getUrlCount());
					System.out.println("Processing Url: " + nextRecord.header.subjectUri);
					System.out.println("tripleCount for this Url: " + reader.getTripleCountThisUrl());
					System.out.println("tripleCount for all Urls: " + reader.getTripleCountAllUrls());

				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (RuntimeException re) {}
		finally {
			reader.close();
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
			if(connection != null)
				connection.close();
			if(myRepository != null)
				myRepository.shutDown();
			if(in != null)
				in.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public class CountHandler extends RDFHandlerBase {

		private int count = 0;
		
		public void endRDF() throws RDFHandlerException {
			super.endRDF();
			// System.out.println("Counted " + count + " statements.");
		}

		public void handleStatement(Statement st) {
			try {
				connection.add(st, factory.createURI(currentUrl));
				System.out.println(st);
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}

		public void startRDF() throws RDFHandlerException {
			super.startRDF();
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
