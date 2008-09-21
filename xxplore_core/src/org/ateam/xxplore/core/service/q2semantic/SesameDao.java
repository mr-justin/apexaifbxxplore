package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.graph.Pseudograph;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.nativerdf.ValueStore;
import org.openrdf.sail.nativerdf.ValueStoreRevision;
import org.openrdf.sail.nativerdf.model.NativeLiteral;
import org.openrdf.sail.nativerdf.model.NativeURI;
import org.xmedia.oms.model.impl.Property;

public class SesameDao {


	//ROOT PATH
	public static String root;
	//CONSTANT
	public static String CONCEPT="concept", 
						INDIVIDUAL="individual", 
						LITERAL="literal",
						PROPERTY="property", 
						DATATYPEPROP="datatypeprop", 
						OBJPROP="objprop", 
						RDFSPROP="rdfsprop";//predicate type
	
	private static String[] rdfsEdge = new String[]{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",//c
													"http://www.w3.org/2000/01/rdf-schema#subClassOf",//c
													"http://www.w3.org/2000/01/rdf-schema#domain",//p
													"http://www.w3.org/2000/01/rdf-schema#range",//p
													"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",//p
													"http://www.w3.org/2000/01/rdf-schema#label",//c
													"http://www.w3.org/2000/01/rdf-schema#comment",
													"http://www.w3.org/2002/07/owl#ObjectProperty",
													"http://www.w3.org/2002/07/owl#Class"};//c
	
	private static String[] containerEdge = new String[]{
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag",
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq",
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt",
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#List",
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#first",
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest",
													"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil",};
	public static HashSet<String> rdfsEdgeSet, conEdgeSet;
	public static String noBlankNodeNT = "_noblanknode.nt";
	
 	/*
 	 * intital the predefined rdfs edge set
 	 */
	static
	{
		rdfsEdgeSet = new HashSet<String>();
		for(String str: rdfsEdge)
			rdfsEdgeSet.add(str);
		conEdgeSet = new HashSet<String>();
		for(String str: containerEdge)
			conEdgeSet.add(str);
	}
	
	private Repository repository;
	private ValueStoreRevision revision;
	private RepositoryConnection con;
	private RepositoryResult<Statement> res;
	private Statement stmt;
	
	/*
	 * load the index
	 */
	public SesameDao(String index) throws Exception
	{
		File file = new File(index);
		if(!file.exists())
			new File(index).mkdirs();
		repository = new SailRepository(new NativeStore(file));
		revision = new ValueStoreRevision(new ValueStore(file));
		repository.initialize();
		con = repository.getConnection();
	}
	/*
	 * build the index from fn (file or dir)
	 */
	public void insertNTFile(String fn, RDFFormat format) throws Exception
	{
		con = repository.getConnection();
		File dir = new File(fn);
		if(dir.isDirectory())
		{
			File[] nt = dir.listFiles();
			for(File ntt: nt)
			{
				con.add(ntt, "", format);
				System.out.println("insert "+ntt.getName());
			}
		}
		else con.add(new File(fn), "", RDFFormat.N3);
		con.close();
	}
	/*
	 * remove the blank nodes in nt file with the rule s1 r1 nb & nb r2 s2 where r2 is container or collection predicates
	 */
	public void removeBlankNode(String fn) throws Exception
	{
		String blankNode = "_:node";
		String blankNodeFile = fn.substring(0, fn.lastIndexOf('.'))+".blanknode";
		String noBlankNodeFile = fn.substring(0, fn.lastIndexOf('.'))+noBlankNodeNT;
		HashMap<String, String> blankNodeMap = new HashMap<String, String>();
		HashSet<String> bnMeansCollection = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(fn));
		PrintWriter pw1 = new PrintWriter(new FileWriter(blankNodeFile));
		PrintWriter pw2 = new PrintWriter(new FileWriter(noBlankNodeFile));
		String line;
		while((line = br.readLine())!=null)
		{
			String[] parts = line.replaceAll("<", "").replaceAll(">", "").split(" ");
			if(parts[0].startsWith(blankNode) && !parts[2].startsWith(blankNode))
			{
				if(bnMeansCollection.contains(parts[0]))
					pw1.println(line);
				else if(parts[1].equals(rdfsEdge[0]) && conEdgeSet.contains(parts[2]))
					bnMeansCollection.add(parts[0]);
				
			}
			else if(!parts[0].startsWith(blankNode) && parts[2].startsWith(blankNode))
				blankNodeMap.put(parts[2], "<"+parts[0]+"> <"+parts[1]+">");
			else if(!parts[0].startsWith(blankNode) && !parts[2].startsWith(blankNode))
				pw2.println(line);
		}
		br.close();
		pw1.close();
		br = new BufferedReader(new FileReader(blankNodeFile));
		while((line = br.readLine())!=null)
		{
			String[] parts = line.split(" ");
			if(blankNodeMap.containsKey(parts[0]))
				pw2.println(blankNodeMap.get(parts[0])+" "+parts[2]+" .");
		}
		pw2.close();
		br.close();
	}
	public void close() throws Exception
	{
		con.close();
	}
	
	public boolean hasNext() throws Exception
	{
		return res.hasNext();
	}
	//next triple
	public void next() throws Exception
	{
		if(res.hasNext())
			stmt = res.next();
	}
	//get current triple's subj
	public String getSubject()
	{
		String uri = stmt.getSubject().toString();
		return uri;
	}
	public String getSubjectType()
	{
		if( (getPredicate().equals(rdfsEdge[0]) && getObject().equals(rdfsEdge[8])) || getPredicate().equals(rdfsEdge[1]))
			return SesameDao.CONCEPT;
		else if( (getPredicate().equals(rdfsEdge[2]) && getObject().equals(rdfsEdge[7])) || getPredicate().equals(rdfsEdge[3])
				|| getPredicate().equals(rdfsEdge[4]))
			return SesameDao.PROPERTY;
		else if(getPredicate().equals(rdfsEdge[0]) || getPredicateType().equals(SesameDao.OBJPROP) || getPredicateType().equals(SesameDao.DATATYPEPROP))
			return SesameDao.INDIVIDUAL;
		System.err.println("gua la~"+getPredicate());
		return "";
	}
	//get current triple's obj
	public String getObject()
	{
		String uri = stmt.getObject().toString();
		return uri;
	}
	public String getObjectType()
	{
		if(getPredicate().equals(rdfsEdge[0]))
			return SesameDao.CONCEPT;
		else if(getPredicateType().equals(SesameDao.OBJPROP))
			return SesameDao.INDIVIDUAL;
		else if(getPredicateType().equals(SesameDao.DATATYPEPROP))
			return SesameDao.LITERAL;
//		System.err.println("gua la~"+getPredicateType());
		return "";
	}
	//get current triple's predicate
	public String getPredicate()
	{
		String uri = stmt.getPredicate().toString();
		return uri;
	}
	// for example isEdgeTypeOf(Property.IS_INSTANCE_OF)
	public String getPredicateType()
	{
		if(rdfsEdgeSet.contains(getPredicate()))
			return SesameDao.RDFSPROP;
		else if(getObject().startsWith("http"))
			return SesameDao.OBJPROP;
		else if(getObject().startsWith("\""))
			return SesameDao.DATATYPEPROP;
		System.err.println("gua la~"+getObject());
		return "";
	}
	/*
	 * stream read all triples, should be called first followed with hasNext() and next() 
	 */
	public void findAllTriples() throws Exception
	{
		res = con.getStatements(null, null, null, false);
	}
	//find the concept set of specified instance
	public void findTypes(String uri) throws Exception
	{
		res = con.getStatements(new NativeURI(revision, uri), new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), null, false);
	}
	//find all concepts
	public void findAllConcepts() throws Exception
	{
		res = con.getStatements(null, new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), null, false);
	}
	//find subconcepts of concept
	public void findSubConcepts(String concept) throws Exception
	{
		res = con.getStatements(null, new NativeURI(revision, Property.SUBCLASS_OF.getUri()), new NativeURI(revision, concept), false);
	}
	//find the attribute of literal
	public void findProperties(String literal, String lang, String datatype) throws Exception
	{
		if(lang!=null)
			res = con.getStatements(null, null, new NativeLiteral(revision, literal, lang), false);
		else if(datatype!=null)
			res = con.getStatements(null, null, new NativeLiteral(revision, literal, new NativeURI(revision, datatype)), false);
	}
	//find all instance when given concept
	public void findMemberIndividuals(String concept) throws Exception {
		res = con.getStatements(null, new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), new NativeURI(revision, concept), false);
	}
	//find all triples which contains specified property
	public void findMemberProperties(String property) throws Exception{
		res = con.getStatements(null, new NativeURI(revision, property), null, false);
	}
	//find all triples whose subj is specified property
	public void findPropertyAndIndividual(String individual) throws Exception {
		res = con.getStatements(new NativeURI(revision, individual),null,null , false);
	}
	
	public static void main(String[] args) throws Exception {

		root = "D:\\semplore\\";
		String datasource = "wordnet";
		String indexRoot = root+datasource;
		String source = root+"wordnet.nt";
	
		/* part1 indexing sourcedata */
		System.out.println("part1 begin!");
		SesameDao sd = new SesameDao(indexRoot);
		sd.removeBlankNode(source);
		//if no balnknode need to be removed then sourceFile = source
		String sourceFile = source + noBlankNodeNT;
		sd.insertNTFile(sourceFile, RDFFormat.N3);
		System.out.println("part1 finished!");
		
		/* part2 constructing summary & schema graph */
		SummaryGraphIndexServiceForBT sss = new SummaryGraphIndexServiceForBT();
		System.out.println("part2 summray begin!");
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = sss.computeSummaryGraph(indexRoot, true);
//		write summary to obj and rdf
		sss.writeSummaryGraph(graph, root+datasource+"-summary.obj");
		sss.writeSummaryGraphAsRDF(graph, root+datasource+"-summary.rdf");
		System.out.println("part2 summray finished!");

		System.out.println("part2 schema begin!");
		graph = sss.computeSchemaGraph(indexRoot, graph, null);
//		write schema to obj and rdf
		sss.writeSummaryGraph(graph, root+datasource+"-schema.obj");
		sss.writeSummaryGraphAsRDF(graph, root+datasource+"-schema.rdf");
		System.out.println("part2 schema finished!");
		
		/* part3 build keywordindex */
		System.out.println("part3 begin!");
		graph = sss.readGraphIndexFromFile(root+datasource+"-schema.obj");
		new KeywordIndexServiceForBT(SesameDao.root+datasource+"-keywordIndex", true).indexKeywords(sourceFile, datasource, graph, root+"apexaifbxxplore\\keywordsearch\\syn_index");
		System.out.println("part3 finished!");
	}
}
