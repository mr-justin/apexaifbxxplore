package org.xmedia.accessknow.sesame.examples;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openrdf.repository.RepositoryException;
import org.xmedia.accessknow.sesame.model.SesameSparqlEvaluator;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ITransaction;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyDeletionException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.QueryEvaluatorUnavailableException;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryException;
import org.xmedia.oms.query.QueryWrapper;
import org.xmedia.uris.impl.XMURIFactoryInsulated;



/**
 * This class demonstrates how to query sources with and without respect to their provenance objects.
 * 
 */
public class AKSesame2AdapterQueryingMetaknowExample {

	private IKbConnection m_con;
	private IOntology m_onto;
	private SesameSessionFactory m_sessionFactory;
	private ISession m_session;
	private String m_query;
	private Boolean m_filePathStored;

	
	// *****************************************************
	// Default paths ...  
	// *****************************************************
	private String m_repository_path = "";
	private String m_file_path = "";
	
	private String QUERY_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
	"PREFIX ub: <http://www.lehigh.edu/%7Ezhp2/2004/0401/univ-bench.owl#> "+
	"SELECT * " +
	"WHERE { "+
	"?x rdf:type ub:GraduateStudent . "+
	"?x ub:takesCourse <http://www.Department1.University0.edu/GraduateCourse10> " +
	"}"; 
	
	/**
	 * Dependencies:
	 * <ul>
	 * 	<li>AccessKnow: trunk/OMS, trunk/util</li>
	 * 	<li>openrdf-sesame-2.0-beta5-onejar.jar</li>
	 * 	<li>aduna-io-1.2.jar</li>
	 * 	<li>slf4j-api-1.3.0.jar</li>
	 * 	<li>slf4j-jdk14-1.3.0.jar</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		AKSesame2AdapterQueryingMetaknowExample example = new AKSesame2AdapterQueryingMetaknowExample();
					
		//get parameters ...
		example.readQuery(args);
		example.readRespPath(args);
		example.readOntoPath(args);
		
		example.prepareRepository();
		example.evalQueries();
		
	}
	
	private void readQuery(String[] args){
		
		m_query = "";
		System.out.println("READING QUERY ... ");
		
		if(args.length == 0)
		{
			System.out.println("No query has been entered! (see readme for details)");
			System.out.println("a predefined query will be used:");
			m_query = QUERY_1;
			System.out.println(m_query);
		}
		else
		{
			System.out.println("query parsed!");
			m_query = args[0];
		}

	}
	
	private void readOntoPath(String[] args){
		
		if(m_filePathStored) return;
		
		System.out.println("READING PATH TO ONTOLOGIES ... ");
		
		if(args.length < 2 || (args.length == 2 && !(args[1].startsWith("-onto:"))
				|| (args.length == 3 && !(args[2].startsWith("-onto:") || args[1].startsWith("-onto:")))))
		{
			System.out.println("ontology folder parameter not set!");
			System.out.println("use default location: ");
			System.out.println(System.getProperty("user.dir")+m_file_path);
			
			m_file_path = System.getProperty("user.dir")+"/";
		}
		else
		{			
			String path;
			
			if(args[1].startsWith("-onto:")) path = args[1];
			else path = args[2];
			
			if(!path.endsWith("/"))
			{
				m_file_path = path.substring(path.indexOf(":")+1) + "/";
			}
			else
			{
				m_file_path = path.substring(path.indexOf(":")+1);
			}
				
			System.out.println("ontology location '"+path.substring(path.indexOf(":")+1)+"' STORED.");
			m_filePathStored = true;
					
		}
	}
	
	private void readRespPath(String[] args){
		
		m_filePathStored = false;
		System.out.println("READING PATH FOR SESAME RESPOSITORY ... ");
		
		if(args.length < 2)
		{
			System.out.println("Repository folder parameter not set!");
			System.out.println("use default location: ");
			System.out.println(System.getProperty("user.dir")+m_repository_path);
			
			m_repository_path = System.getProperty("user.dir")+"/";
		}
		else
		{
			if(args[1].startsWith("-onto:"))
			{
				readOntoPath(args);
			}		
			else
			{
				if(args[1].startsWith("-repo:"))
				{
					System.out.println("repository location '"+args[1].substring(args[1].indexOf(":")+1)+"' STORED.");
					m_repository_path = args[1].substring(args[1].indexOf(":")+1);
				}
				else
				{
					System.out.println("repository location '"+args[1].substring(args[1].indexOf(":")+1)+"' NOT PROPERLY ENTERED.");
					System.out.println("see readme for details. repository location. ");
				}
			}
		}

	}
	

	private void prepareRepository(){
		
	   System.out.println("DO YOU WANT TO IMPORT THE ONTOLOGIES INTO THE SESAME RESPOSITORY? [Y|N].");
	   System.out.println("Note you need to import ontologies only once. After that,");
	   System.out.println("you can skip this step and continue querying.");
		
	   BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	   String input = null;
   
	   try 
	   {
	    	input = br.readLine();
	   } 
	   catch (IOException ioe){}
	
	    
	   if(input.equals("Y")||input.equals("y"))
	   {
		   importOntos();
	   }
	   else
	   {
		   if(input.equals("N")||input.equals("n"))
		   {
			   System.out.println("SKIP IMPORTING.");
		   }
		   else
		   {
			   System.out.println("cannot read input. must be: [Y,y, N, n]. System will import ontologyies from default location!");
			   importOntos();
		   }
	   }	
	}
	
	
	/**
	 * Importing Ontologies. We are currently using the following ontologies:
	 * 
	 * <ul>
	 * 	<li>univ-bench.owl</li>
	 * 	<li>University0_1_mod.owl</li>
	 * </ul>
	 * 
	 * Notice that the *_mod file contains IProvenance objects. Of course, you can use any other ontology.
	 * Actually this method needs to be called only once. After that, the ontologies are already stored in the
	 * repository and are ready for usage.
	 * 
	 */
	private void importOntos(){
		
		System.out.println("START IMPORTING ONTOLOGIES IN REPOSITORY...");
		System.out.println("your specified 'ontology folder' is: "+m_file_path);
		
		getConnection();

		//delete ontologies -> should be done once for cleaning
		try 
		{
			((SesameConnection)m_con).deleteAllOntologies();
		} 
		catch (OntologyDeletionException e)
		{
			e.printStackTrace();
		}

		//Open a new / existing ontology
		openOntology();
		
		//Open a new session
		openSession();
		
		//Creating file for our file directory
		File directory = new File(m_file_path);
		
		//Search all ontologies in this path
		File[] list = directory.listFiles();

		for(File file : list)
		{			
			if(file.getName().endsWith(".owl"))
			{				
				try
				{
					ITransaction transaction = m_session.beginTransaction();
				
					try 
					{				
						m_onto.importOntology(IOntology.RDF_XML_LANGUAGE,"http://www.lehigh.edu/%7Ezhp2/2004/0401/univ-bench.owl", new FileReader(m_file_path+file.getName()));
						System.out.println("imported ontology: "+file.getName());
						
						transaction.commit();
					} 
					catch (FileNotFoundException e1) 
					{
						e1.printStackTrace();
					} 
					catch (OntologyImportException e1)
					{
						transaction.rollback();
						transaction = m_session.beginTransaction();
						
						try
						{
							m_onto.importOntology(IOntology.TRIX_LANGUAGE,"http://www.lehigh.edu/%7Ezhp2/2004/0401/univ-bench.owl", new FileReader(m_file_path+file.getName()));
							System.out.println("imported ontology: "+file.getName());
						
							transaction.commit();
						}
						catch(OntologyImportException e)
						{
							transaction.rollback();
							e.printStackTrace();
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}	
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
				
		System.out.println("finished importing!");
		closeConnection();	
		
	}
	
	/**
	 * 
	 * Evaluate the queries above.
	 * 
	 * @param AKSesame2AdapterQueryingMetaknowExample example
	 * 
	 */
	private void evalQueries(){

		//Establish Connection
		getConnection();
		
		//Open ontology
		openOntology();
		
		//Open session
		openSession();
		
		//Query ontologies
		query();

		//Clean up before you leave ...
		cleanup();

	}
	
	/**
	 * Establish connection to our repository. For a more detailed explanation on that,
	 * please see the other examples.
	 *
	 */	
	private void getConnection(){

		try
		{
			m_con = new SesameConnection(m_repository_path);
			m_sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());			
		}
		catch(RepositoryException e)
		{
			System.out.println("could not connect to repository");
			System.out.println("EXIT NOW!");
			e.printStackTrace();
			
			System.exit(1);
		}

		System.out.println("got connection!");
	}
	
	/**
	 * Opening a new session. For a more detailed explanation on that,
	 * please see the other examples.
	 *
	 */
	private void openSession(){
		
		try 
		{
			m_session = m_sessionFactory.openSession(this.m_con, this.m_onto);
		} 
		catch (OpenSessionException e) 
		{
			e.printStackTrace();	
			this.closeConnection();
			System.out.println("EXIT NOW!");
			
			System.exit(1);
		}
	}
	
	/**
	 * Opening/creating ontology. For a more detailed explanation on that,
	 * please see the other examples.
	 *
	 */
	private void openOntology(){

		try
		{
			Map<String, Object> parameters = new Hashtable<String, Object>();
			parameters.put(KbEnvironment.ONTOLOGY_URI, new URI("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"));
			parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT));

			m_onto = m_con.loadOrCreateOntology(parameters);
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}
		catch(InvalidParameterException e)
		{
			e.printStackTrace();
		}
		catch(OntologyCreationException e)
		{
			e.printStackTrace();
		}
		catch(MissingParameterException e)
		{
			e.printStackTrace();
		}
	}
	

	/**
	 * The actual querying takes place here. Please notice the evaluate() and evaluateWithProvenance() method calls.
	 * 
	 * <ul>
	 * 	<li>evaluate(): evaluates the given query without respect to the IProvenance objects</li>
	 *  <li>evaluateWithProvenance(): evaluates the query with respect to the IProvenance objects</li>
	 * </ul>
	 * 
	 * Results are printed out.
	 *
	 */
	private void query(){

		Set<ITuple> results;

		try
		{
			//Retrieve available QueryEvaluator instance
			IQueryEvaluator evaluator = m_session.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);

			//evaluates the given query without respect to the IProvenance objects
			results = ((SesameSparqlEvaluator)evaluator).evaluate(new QueryWrapper(m_query, null)).getResult();			
			print(results, m_query, Boolean.FALSE);
				
			//evaluates the query with respect to the IProvenance objects
			results = evaluator.evaluateWithProvenance(new QueryWrapper(m_query, null)).getResult();
			print(results, m_query, Boolean.TRUE);
			
		}
		catch(QueryEvaluatorUnavailableException e)
		{
			e.printStackTrace();
		}
		catch(QueryException e)
		{
			e.printStackTrace();
		}
	}

	private void print(Set<ITuple> results, String query, Boolean withProvenance){

		
		if(withProvenance){
			System.out.println();
			System.out.println("RESULTS OF ENHANCED QUERY WITH PROVENANCES:");
		}
		else{
			System.out.println("QUERY:");
			System.out.println(m_query);
			System.out.println();
			System.out.println("RESULTS  WITHOUT PROVENANCES:");

		}
		for (ITuple result : results) 
		{
			for (int i = 0; i < result.getArity(); i++)
			{
				System.out.print("Variable: '" + result.getLabelAt(i) + "' :: Value: '" + result.getElementAt(i) + "'");
				if(i != result.getArity()-1) System.out.print(" || ");
			}

			System.out.println();
		}
	}

	
	/**
	 * Close connection.
	 * 
	 */
	private void closeConnection(){

		try
		{
			m_con.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

	}
	
	/**
	 * Clean up the mess.
	 *
	 */
	private void cleanup(){

		System.out.println("DONE :)");
		m_session.close();
		closeConnection();	
	
	}
}

