package org.ateam.xxplore.core.service.sampling;


import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.ateam.xxplore.core.service.q2semantic.SummaryGraphEdge;
import org.ateam.xxplore.core.service.q2semantic.SummaryGraphElement;
import org.jgrapht.graph.Pseudograph;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.nativerdf.ValueStore;
import org.openrdf.sail.nativerdf.ValueStoreRevision;
import org.openrdf.sail.nativerdf.model.NativeURI;
import org.xmedia.oms.model.impl.NamedConcept;

public class NTriplesSampler {

	private String semsameIndex = "//192.168.3.237/kaifengxu/freebase";
	private String summaryGraph = "//192.168.3.237/kaifengxu/freebase-summary.obj";
	private String output_file = "c:/freebase.sample.nt";
	
	private SailRepositoryConnection con;
	private SailRepository repository;
	private ValueStoreRevision revision;
	private int maxInstance = 10;
	
	private HashSet<String> allinstance = new HashSet<String>();
	private LinkedList<Statement> allstmt = new LinkedList<Statement>();
	
	public NTriplesSampler(String sesameIndex,String summaryGraph,String output_file) {
		this.semsameIndex = sesameIndex;
		this.summaryGraph = summaryGraph;
		this.output_file = output_file;
	}
	
	public NTriplesSampler() {
		// TODO Auto-generated constructor stub
	}
	
	private String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	/**
	 * init semsame in order to read NTriple file.
	 * @throws Exception
	 */
	private void initSesame() throws Exception {
		File file = new File(semsameIndex);
		repository = new SailRepository(new NativeStore(file));
		System.out.println(new File("//hades/kaifengxu/dblpnoblank/values.dat").exists());
		revision = new ValueStoreRevision(new ValueStore(file));
		repository.initialize();
		con = repository.getConnection();
	}
	
	public void testtripple() throws Exception {
		this.initSesame();
		RepositoryResult<Statement> tmp = con.getStatements(null, new NativeURI(revision,"http://www.w3.org/2000/01/rdf-schema#label"), null, false);
		while(tmp.hasNext()) {
			System.out.println(tmp.next().getObject().stringValue());
		}
	}
	
	/**
	 * read the summary graph from file.
	 * @return
	 * @throws Exception
	 */
	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> getSummaryGraph() throws Exception {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(summaryGraph));
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> ret = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>) ois.readObject();
			ois.close();
			return ret;
	}
	
	private ArrayList<String> getAllInstance(String concept_uri) throws RepositoryException {
		ArrayList<String> instances = new ArrayList<String>();
		
		RepositoryResult<Statement> stmt = con.getStatements(null, new NativeURI(revision,RDF_TYPE), 
				new NativeURI(revision,concept_uri), false);
		int count = 0;
		while(stmt.hasNext()) {
			if(count > this.maxInstance) break;
			Statement tmp = stmt.next();
			if(!tmp.getSubject().stringValue().startsWith("node")) {
				instances.add( tmp.getSubject().stringValue() );
				count ++;
			}
		}
		allinstance.addAll(instances);
		return instances;
	}
	
	private void getAllTriple(String instance) throws RepositoryException {
		System.out.println("once");
		RepositoryResult<Statement> stmt = con.getStatements(new NativeURI(revision,instance), null, null, false);
		while(stmt.hasNext()) {
			allstmt.add(stmt.next());
		}
		stmt = con.getStatements(null, null, new NativeURI(revision,instance), false);
		while(stmt.hasNext()) {
			allstmt.add(stmt.next());
		}
	}
	
	private void cutStmt() {
		Iterator<Statement> iter = allstmt.iterator();
		while(iter.hasNext()) {
			if( !allinstance.contains(iter.next().getObject().stringValue()) ||
					!allinstance.contains(iter.next().getSubject().stringValue()) ) {
				iter.remove();
			}
		}
	}
	
	public void sample() throws Exception {
		
		this.initSesame();
		
		
		//get all the concept in the summary graph
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = this.getSummaryGraph();
		int count = 0;
		for(SummaryGraphElement ele : graph.vertexSet()) {

			if(ele.getType() == SummaryGraphElement.CONCEPT) {
				NamedConcept concept = (NamedConcept)ele.getResource();
				String concept_uri = concept.getUri();
				System.out.println("concept uri: " + concept_uri);
				
				ArrayList<String> instances = getAllInstance(concept_uri);
				int length = Math.min(instances.size(), maxInstance);
				System.out.println("begin");
				for(int i=0;i<length;i++) {
					String instance = instances.get(i);
					this.getAllTriple(instance);
				}
				System.out.println("end");
			}
		}
		this.cutStmt();
		PrintWriter pw = new PrintWriter(output_file);
		for(Statement stmt : allstmt) {
			if(!stmt.getSubject().stringValue().startsWith("node") &&
					!stmt.getObject().stringValue().startsWith("node")) {
				String t1 = stmt.getSubject().stringValue();
				String t2 = stmt.getPredicate().stringValue();
				String t3 = stmt.getObject().stringValue();
				
				if(t1.indexOf("@") != -1 || t2.indexOf("@") != -1 
					|| t3.indexOf("@") != -1) {
					System.err.println(t1 + " " + t2 + " " + t3);
					continue;
				}
				
				if(t1.startsWith("http")) {
					t1 = "<" + t1 + ">";
				}
				if(t2.startsWith("http")) {
					t2 = "<" + t2 + ">";
				}
				if(t3.startsWith("http")) {
					t3 = "<" + t2 + ">";
				}
				else {
					String tokens[] = t3.split("\\^\\^");
					if(tokens.length == 1) {
						t3 = "\"" + tokens[0] + "\"";
					}
					else {
						t3 = "\"" + tokens[0] + "\"" + "^^" + "<" + tokens[1] + ">";
					}
				}
				pw.println(t1 + " " + t2 + " " + t3);
			}
		}
		pw.close();
		con.close();
	}
	
	public void testPrint() {
		String t1 = "http://www.sjtu.edu.cn";
		String t2 = "http://chenjunquan.edu";
		String t3 = "chenjunquan@englist";
		
		if(t1.indexOf("@") != -1 || t2.indexOf("@") != -1 
				|| t3.indexOf("@") != -1) {
				System.err.println(t1 + " " + t2 + " " + t3);
			}
		
		if(t1.startsWith("http")) {
			t1 = "<" + t1 + ">";
		}
		if(t2.startsWith("http")) {
			t2 = "<" + t2 + ">";
		}
		if(t3.startsWith("http")) {
			t3 = "<" + t2 + ">";
		}
		else {
			String tokens[] = t3.split("\\^\\^");
			if(tokens.length == 1) {
				t3 = "\"" + tokens[0] + "\"";
			}
			else {
				t3 = "\"" + tokens[0] + "\"" + "^^" + "<" + tokens[1] + ">";
			}
		}
		System.out.println(t1 + " " + t2 + " " + t3);
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length == 3) {
			new NTriplesSampler(args[0],args[1],args[2]).sample();
		}
		else {
			new NTriplesSampler().sample();
		}
		
	}	
}
