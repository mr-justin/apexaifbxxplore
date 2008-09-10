package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.nativerdf.ValueStore;
import org.openrdf.sail.nativerdf.ValueStoreRevision;
import org.openrdf.sail.nativerdf.model.NativeLiteral;
import org.openrdf.sail.nativerdf.model.NativeURI;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;

public class SesameDao {

	private String index;
	private Repository repository;
	private ValueStoreRevision revision;
	private RepositoryConnection con;
	private RepositoryResult<Statement> res;
	private Statement stmt;
	
	public static String indexRoot = "D:\\semplore\\wordnet";
	public static String CONCEPT="concept", INDIVIDUAL="individual", LITERAL="literal", PROPERTY="property"//subj & obj type
		, DATATYPEPROP="datatypeprop", OBJPROP="objprop", RDFSPROP="rdfsprop";//predicate type
	private static String[] rdfsEdge = new String[]{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",//c
													"http://www.w3.org/2000/01/rdf-schema#subClassOf",//c
													"http://www.w3.org/2000/01/rdf-schema#domain",//p
													"http://www.w3.org/2000/01/rdf-schema#range",//p
													"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",//p
													"http://www.w3.org/2000/01/rdf-schema#label",//c
													"http://www.w3.org/2000/01/rdf-schema#comment",
													"http://www.w3.org/2002/07/owl#ObjectProperty",
													"http://www.w3.org/2002/07/owl#Class"};//c
	private static HashSet<String> rdfsEdgeSet;
 	static
	{
		rdfsEdgeSet = new HashSet<String>();
		for(String str: rdfsEdge)
			rdfsEdgeSet.add(str);
	}
	/*
	 * load the index
	 */
	public SesameDao(String index) throws Exception
	{
		repository = new SailRepository(new NativeStore(new File(index)));
		revision = new ValueStoreRevision(new ValueStore(new File(index)));
		repository.initialize();
	}
	/*
	 * build the index from fn
	 */
	public void insertNTFile(String fn) throws Exception
	{
		con = repository.getConnection();
		con.add(new File(fn), "", RDFFormat.N3);
		con.close();
	}
	public void close() throws Exception
	{
		con.close();
	}
	/*
	 * stream read all triples, should be called first followed with hasNext() and next() 
	 */
	public void findAllTriples() throws Exception
	{
		con = repository.getConnection();
		res = con.getStatements(null, null, null, false);
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
//		if(uri.startsWith("http://"))
//			return new NamedIndividual(uri);
//		return null;
	}
	public String getSubjectType()
	{
		if( (getPredicate().equals(rdfsEdge[0]) && getObject().equals(rdfsEdge[8])) || getPredicate().equals(rdfsEdge[1])
				|| getPredicate().equals(rdfsEdge[5]) || getPredicate().equals(rdfsEdge[6]))
			return SesameDao.CONCEPT;
		else if( (getPredicate().equals(rdfsEdge[2]) && getObject().equals(rdfsEdge[7])) || getPredicate().equals(rdfsEdge[3])
				|| getPredicate().equals(rdfsEdge[4]))
			return SesameDao.PROPERTY;
		else if(getPredicate().equals(rdfsEdge[0]) || getPredicateType().equals(SesameDao.OBJPROP) || getPredicateType().equals(SesameDao.DATATYPEPROP))
			return SesameDao.INDIVIDUAL;
		System.err.println("gua la~");
		return "";
	}
	//get current triple's obj
	public String getObject()
	{
		//if(stmt.getObject() instanceof NativeLiteral)
		//System.out.println(((NativeLiteral)stmt.getObject()).getLabel());
		String uri = stmt.getObject().toString();
		return uri;
//		if(isEdgeTypeOf(Property.IS_INSTANCE_OF))
//			return new NamedConcept(uri);
//		else if(uri.startsWith("http://"))
//			return new NamedIndividual(uri);
//		else if(uri.startsWith("\""))
//			return new Literal(uri);
//		return null;
	}
	public String getObjectType()
	{
		if(getPredicateType().equals(SesameDao.RDFSPROP))
			return SesameDao.CONCEPT;
		else if(getPredicateType().equals(SesameDao.OBJPROP))
			return SesameDao.INDIVIDUAL;
		else if(getPredicateType().equals(SesameDao.DATATYPEPROP))
			return SesameDao.LITERAL;
		System.err.println("gua la~");
		return "";
	}
	//get current triple's predicate
	public String getPredicate()
	{
		String uri = stmt.getPredicate().toString();
		return uri;
		//if(uri.equals("http://www.w3.org/2006/03/wn/wn20/schema/lexicalForm"))
		//	System.out.println(stmt.getObject().toString());
//		if(stmt.getObject().toString().startsWith("http://"))
//			return new ObjectProperty(uri);
//		else if(stmt.getObject().toString().startsWith("\""))
//			return new DataProperty(uri);
//		return null;
	}
	// for example isEdgeTypeOf(Property.IS_INSTANCE_OF)
	public String getPredicateType()
	{
		if(rdfsEdgeSet.contains(getPredicate()))
			return SesameDao.RDFSPROP;
		else if(getObject().startsWith("http://"))
			return SesameDao.OBJPROP;
		else if(getObject().startsWith("\""))
			return SesameDao.DATATYPEPROP;
		System.err.println("gua la~"+getObject());
		return "";
	}
	//find the concept set of specified instance
	public void findTypes(String uri) throws Exception
	{
		con = repository.getConnection();
		res = con.getStatements(new NativeURI(revision, uri), new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), null, false);
	}
	//find all concepts
	public void findAllConcepts() throws Exception
	{
		con = repository.getConnection();
		res = con.getStatements(null, new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), null, false);
	}
	//find subconcepts of concept
	public void findSubConcepts(String concept) throws Exception
	{
		con = repository.getConnection();
		res = con.getStatements(null, new NativeURI(revision, Property.SUBCLASS_OF.getUri()), new NativeURI(revision, concept), false);
	}
	
	public void findProperties(String literal, String lang, String datatype) throws Exception
	{
		con = repository.getConnection();
		if(lang!=null)
			res = con.getStatements(null, null, new NativeLiteral(revision, literal, lang), false);
		else if(datatype!=null)
			res = con.getStatements(null, null, new NativeLiteral(revision, literal, new NativeURI(revision, datatype)), false);
	}
	public void findAllIndividuals() throws Exception {
		con = repository.getConnection();
		res = con.getStatements(null, new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), null, false);
	}
	public void findMemberIndividuals(String concept) throws Exception {
		con = repository.getConnection();
		res = con.getStatements(null, new NativeURI(revision, Property.IS_INSTANCE_OF.getUri()), new NativeURI(revision, concept), false);
	}
	public void findMemberProperties(String property) throws Exception{
		con = repository.getConnection();
		res = con.getStatements(null, new NativeURI(revision, property), null, false);
	}
	
	public void findPropertyAndIndividual(String individual) throws Exception {
		con = repository.getConnection();
		res = con.getStatements(new NativeURI(revision, individual),null,null , false);
	}
	
	public static void main(String[] args) throws Exception {
		SesameDao se = new SesameDao("d:/semplore/wordnet");
		//se.insertNTFile("d:/good2.txt");
		//se.findTypes("http://www.w3.org/2006/03/wn/wn20/schema/Word");
		se.findProperties("1",null,"http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
		//System.out.println(se.hasNext());
		while(se.hasNext())
		{
			se.next();
			System.out.println(se.getSubject());
		}
	}
	
	/**
	 * @param args
	 * @throws RepositoryException 
	 */
//	public static void main(String[] args) throws RepositoryException {
//		// TODO Auto-generated method stub
//		String index;
//		Repository repository;
//		ValueStoreRevision revision = null;
//		 RepositoryConnection conn = null;
//		 RepositoryResult<Statement> res;
//		Statement stmt;
//		repository = new SailRepository(new NativeStore(new File(indexRoot)));
//		try {
//			revision = new ValueStoreRevision(new ValueStore(new File(indexRoot)));
//		} catch (IOException e1) {
//			System.err.println("Error in create revision");
//			e1.printStackTrace();
//		}
//		try {
//			repository.initialize();
//		} catch (RepositoryException e) {
//			System.err.println("Error in read index");
//			e.printStackTrace();
//		}
//		//sd.insertNTFile("D:/semplore/wordnet.nt");
//		res = conn.getStatements(new NativeURI(revision, "http://www.w3.org/2006/03/wn/wn20/schema/lexicalForm"),null,null, false);
//		
//		while(res.hasNext())
//		{
//			Statement stmt1 = res.next();
//			System.out.println(stmt1.getPredicate());
//		}
//	}
}
