package org.ateam.xxplore.core.service.sampling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.openrdf.sail.memory.MemoryStore;

/**
 * 
 */
public class NTriplesReader {

	public BufferedReader br;
	
	private NTriplesParser parser;
	private CountHandler countHandler;

	private Repository myRepository;
	private RepositoryConnection connection;
	private ValueFactory factory;
	
	private String context;
	
	private int tripleCount = 0;
	
	private static String sourceDir = "D:\\swetodblp_april_2008-mod.nt";
	private static String repositoryDir = "D:\\sampling\\repository\\NT";
	private static int maxTripleCount = 100;
	
	public NTriplesReader(String sourceDir, String repositoryDir) {
		try {
			br = new BufferedReader(new FileReader(sourceDir));
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
	
	public NTriplesReader(String sourceDir, String repositoryDir, String context) {
		this(sourceDir, repositoryDir);
		this.context = context;
	}
	
	public NTriplesReader(String fileName, String repositoryDir, int maxTripleCount) {
		try {
			br = new BufferedReader(new FileReader(fileName));
			parser = new NTriplesParser(maxTripleCount);
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
	
	public NTriplesReader(String fileName, String repositoryDir, int maxTripleCount, String context) {
		this(sourceDir, repositoryDir, maxTripleCount);
		this.context = context;
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) {
		NTriplesReader reader = new NTriplesReader(sourceDir, repositoryDir, maxTripleCount);;
		try {
			String baseURI = "http://example.org";
			reader.parse(baseURI);
			System.out.println("tripleCount: " + reader.tripleCount());
		} finally {
			reader.close();
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
			connection.close();
			myRepository.shutDown();
			br.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
				if(context != null && context.length() != 0)
					connection.add(st, factory.createURI(context));
				else 
					connection.add(st);
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

}
