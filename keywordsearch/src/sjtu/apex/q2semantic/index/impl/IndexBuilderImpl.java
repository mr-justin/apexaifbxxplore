/**
 * 
 */
package sjtu.apex.q2semantic.index.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import sjtu.apex.q2semantic.graph.Edge;
import sjtu.apex.q2semantic.graph.Graph;
import sjtu.apex.q2semantic.graph.Node;
import sjtu.apex.q2semantic.index.IndexBuilder;
import sjtu.apex.q2semantic.index.IndexEnvironment;

/**
 * @author whfcarter
 * 
 */
public class IndexBuilderImpl implements IndexBuilder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see sjtu.apex.q2semantic.index.IndexBuilder#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		try {
			conn.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sjtu.apex.q2semantic.index.IndexBuilder#init()
	 */
	public void init(int flag) {
		// TODO Auto-generated method stub
		ObjectInputStream ois = null;
		String obj_path = "";
		String datasource = "";
		String indexPath = "";
		baseURI = "";
		ns = "";
		
		if (flag == IndexEnvironment.TAP_FLAG) {
			obj_path = IndexEnvironment.TAP_OBJ_PATH;
			datasource = IndexEnvironment.TAP_REPO;
			indexPath = IndexEnvironment.TAP_KB_INDEX;
			baseURI = IndexEnvironment.TAP_BASEURI;
			ns = IndexEnvironment.TAP_NS;
		} else if (flag == IndexEnvironment.LUBM_FLAG) {
			obj_path = IndexEnvironment.LUBM_OBJ_PATH;
			datasource = IndexEnvironment.LUBM_REPO;
			indexPath = IndexEnvironment.LUBM_KB_INDEX;
			baseURI = IndexEnvironment.LUBM_BASEURI;
			ns = IndexEnvironment.LUBM_NS;
		} else if (flag == IndexEnvironment.DBLP_FLAG) {
			obj_path = IndexEnvironment.DBLP_OBJ_PATH;
			datasource = IndexEnvironment.DBLP_REPO;
			indexPath = IndexEnvironment.DBLP_KB_INDEX;
		} else {
			System.exit(0);
		}
		try {
			ois = new ObjectInputStream(new FileInputStream(obj_path));
			graph = (Graph) ois.readObject();
			System.out.println("loading graph successfully!");

			analyzer = new StandardAnalyzer();
			System.out
					.println("initialize the standard analyzer successfully!");
			File idx = new File(indexPath);
			indexWriter = new IndexWriter(idx, analyzer, true);
			indexWriter.setMaxBufferedDocs(10000);
			indexWriter.setMaxMergeDocs(10000);
			// indexWriter.setMaxFieldLength(10000);
			System.out.println("initialize the index writer successfully!");
			searcher = new IndexSearcher(IndexEnvironment.SYN_INDEX);
			System.out.println("initialize the index searcher successfully!");

			Repository repository = new SailRepository(new NativeStore(
					new File(datasource), IndexEnvironment.INDICES));
			repository.initialize();
			conn = repository.getConnection();
			System.out.println("initialize the repository successfully!");
			relSet = new HashSet<String>();
			attrSet = new HashSet<String>();
			indSet = new HashSet<Resource>();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void indexDataSourceByConcepts() throws CorruptIndexException,
			IOException {
		Map<Integer, Node> nodeMap = graph.nodes;
		Collection<Node> nodes = nodeMap.values();
		Iterator<Node> nodeIter = nodes.iterator();
		String lastURI = "", lastEdge = "", lastRangeURI = "";
		while (nodeIter.hasNext()) {
			Node node = nodeIter.next();
			if (node.type.equals(Graph.Typ)) {
				String label = node.name;
				String uri = baseURI + label;
//				System.out.println(uri);
				Document doc = new Document();
				doc.add(new Field("type", IndexEnvironment.CONCEPT,
						Field.Store.YES, Field.Index.NO));
				doc.add(new Field("label", label, Field.Store.NO,
						Field.Index.TOKENIZED));
				doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);

				List<Edge> edges = node.edges;
				for (int i = 0; i < edges.size(); i++) {
					Edge edge = edges.get(i);
					if (edge.type.equals(Graph.Attr)) {
						Document dpdoc = new Document();
//						System.out.println(edge.name + "\t" + uri);
						dpdoc.add(new Field("attribute", edge.name,
								Field.Store.YES, Field.Index.UN_TOKENIZED));
						dpdoc.add(new Field("domain", uri, Field.Store.YES,
								Field.Index.NO));
						indexWriter.addDocument(dpdoc);
					} else if (edge.type.equals(Graph.Rel)) {
						if (edge.originDirection) {
							Node range = nodeMap.get(Integer
									.valueOf(edge.to_id));
							String range_uri = baseURI + range.name;
							if (lastEdge.equals(edge.name) && lastURI.equals(uri) && lastRangeURI.equals(range_uri))
								continue;
//							System.out.println(edge.name + "\t" + uri + "\t" + range_uri);
							lastEdge = edge.name;
							lastURI = uri;
							lastRangeURI = range_uri;
							Document opdoc = new Document();
							opdoc.add(new Field("relation", edge.name,
									Field.Store.YES, Field.Index.UN_TOKENIZED));
							opdoc.add(new Field("domain", uri, Field.Store.YES,
									Field.Index.NO));
							opdoc.add(new Field("range", range_uri,
									Field.Store.YES, Field.Index.NO));
							indexWriter.addDocument(opdoc);
						}
					}
				}
				Set<String> values = new HashSet<String>();
				Term term = new Term("word", label.toLowerCase());
				TermQuery termQuery = new TermQuery(term);
				Hits results = searcher.search(termQuery);
				if (results != null && results.length() > 0) {
					for (int i = 0; i < results.length(); i++) {
						Document docu = results.doc(i);
						values.addAll(Arrays.asList(docu.getValues("syn")));
					}
				}
				// System.out.println(uri + ": " + values);
				for (String value : values) {
					Document docu = new Document();
					docu.add(new Field("type", IndexEnvironment.CONCEPT,
							Field.Store.YES, Field.Index.NO));
					docu.add(new Field("label", value, Field.Store.NO,
							Field.Index.TOKENIZED));
					docu.add(new Field("uri", uri, Field.Store.YES,
							Field.Index.NO));
					indexWriter.addDocument(docu);
				}
				values.clear();
			}
		}
	}

	private void indexDataSourceByProperties() throws CorruptIndexException,
			IOException {
		Collection<Edge> edges = graph.edges.values();
		Iterator<Edge> edgeIter = edges.iterator();
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			String label = edge.name;
			Document doc = new Document();
			doc.add(new Field("label", label, Field.Store.NO,
					Field.Index.TOKENIZED));
			String uri = ns + label;
			doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
			if (edge.type.equals(Graph.Rel)) {
				if (relSet.contains(label))
					continue;
				relSet.add(label);
				doc.add(new Field("type", IndexEnvironment.OBJECTPROPERTY,
						Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);
			} else if (edge.type.equals(Graph.Attr)) {
				if (attrSet.contains(label))
					continue;
				attrSet.add(label);
				doc.add(new Field("type", IndexEnvironment.DATAPROPERTY,
						Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);
			}
			System.out.println(label);
			Set<String> values = new HashSet<String>();
			Term term = new Term("word", label.toLowerCase());
			TermQuery termQuery = new TermQuery(term);
			Hits results = searcher.search(termQuery);
			if (results != null && results.length() > 0) {
				for (int i = 0; i < results.length(); i++) {
					Document docu = results.doc(i);
					values.addAll(Arrays.asList(docu.getValues("syn")));
				}
			}
			for (String value : values) {
				Document docu = new Document();
				docu.add(new Field("label", value, Field.Store.NO,
						Field.Index.TOKENIZED));
				docu
						.add(new Field("uri", uri, Field.Store.YES,
								Field.Index.NO));
				if (edge.type.equals(Graph.Rel))
					docu.add(new Field("type", IndexEnvironment.OBJECTPROPERTY,
							Field.Store.YES, Field.Index.NO));
				else
					docu.add(new Field("type", IndexEnvironment.DATAPROPERTY,
							Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(docu);
			}
			values.clear();
		}
	}

	private void indexDataSourceByLiterals() throws CorruptIndexException,
			IOException {
		try {
			HashSet<String> litSet = new HashSet<String>();
//			RepositoryConnection conn = repository.getConnection();
			RepositoryResult<Statement> res_results = conn.getStatements(null,
					null, null, false);
			int i = 1;
			boolean canAdd = true;
			while (res_results.hasNext()) {
				Statement stmt = res_results.next();
				Resource res = stmt.getSubject();
				Value obj = stmt.getObject();
				if (!indSet.contains(res)) {
					if (obj instanceof URI) {
						if (((URI) obj).getNamespace().equals(RDFS.NAMESPACE)
								||((URI) obj).getNamespace().equals(
										RDF.NAMESPACE)
								||((URI) obj).getNamespace().startsWith(
										"http://www.w3.org/2002/07/owl#")) {
							canAdd = false;
						}
					}
					if (canAdd)
						indSet.add(res);
					canAdd = true;
				}
				String predicate = stmt.getPredicate().getLocalName();
				if (!attrSet.contains(predicate))
					continue;
				String literal = stmt.getObject().stringValue();
				if (litSet.contains(literal))
					continue;
//				System.out.println(i++ + ": " + literal);
				Document doc = new Document();
				doc.add(new Field("type", IndexEnvironment.LITERAL,
						Field.Store.YES, Field.Index.NO));
				doc.add(new Field("label", literal, Field.Store.YES,
						Field.Index.TOKENIZED));

				indexWriter.addDocument(doc);
				litSet.add(literal);
			}
			if (res_results != null)
				res_results.close();
			litSet.clear();
			litSet = null;
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void indexDataSourcePerIndividual(Resource ind) throws CorruptIndexException, IOException {
		try {
//			RepositoryConnection conn = repository.getConnection();
			RepositoryResult<Statement> res_result = conn.getStatements(ind, null, null, false);
			Set<String> concepts = new HashSet<String>();
			Map<String, String> av_map = new HashMap<String, String>();
			while (res_result.hasNext()) {
				Statement stmt = res_result.next();
				String predicate = stmt.getPredicate().getLocalName();
				if (predicate.equals("type")) {
					concepts.add(stmt.getObject().stringValue());
//					System.out.println(stmt.getObject().stringValue());
				} else if (attrSet.contains(predicate)) {
					if (ns.equals(""))
						av_map.put(stmt.getPredicate().stringValue(), stmt.getObject().stringValue());
					else av_map.put(predicate, stmt.getObject().stringValue());
				}
			}
			Iterator<String> av_iter = av_map.keySet().iterator();
			
			while (av_iter.hasNext()) {
				String attr = av_iter.next();
				String lit = av_map.get(attr);
//				if (lit.equals("The Hungry Stones And Other Stories"))
//					System.out.println(lit); 
				Iterator<String> con_iter = concepts.iterator();
				while (con_iter.hasNext()) {
					String concept = con_iter.next();
					Document doc = new Document();
					doc.add(new Field("literal", lit, Field.Store.YES, Field.Index.UN_TOKENIZED));
					if (lit.equals("The Hungry Stones And Other Stories"))
						System.out.println(lit); 
					doc.add(new Field("dataproperty", ns + attr,Field.Store.YES,Field.Index.NO));
					doc.add(new Field("concept", baseURI + concept,Field.Store.YES, Field.Index.NO));
					
					indexWriter.addDocument(doc);
				}
			}
			concepts.clear();
			concepts = null;
			av_map.clear();
			av_map = null;
			
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	private void indexDataSourceByIndividuals() throws CorruptIndexException, IOException {
		Iterator<Resource> iter = indSet.iterator();
		int i = 1;
		while (iter.hasNext()) {
			Resource ind = iter.next();
//			System.out.println(i++ + ": " + ind);
			indexDataSourcePerIndividual(ind);
		}
		indSet.clear();
	}

	public void index() {
		// TODO Auto-generated method stub
		try {
			indexDataSourceByConcepts();
			indexDataSourceByProperties();
			searcher.close();
			indexDataSourceByLiterals();
			indexDataSourceByIndividuals();
			indexWriter.optimize();
			indexWriter.close();
		} catch (CorruptIndexException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public IndexBuilderImpl() {
	}

	private static IndexBuilderImpl ib;

	private Graph graph;

	private IndexWriter indexWriter;

	private IndexSearcher searcher;

	private StandardAnalyzer analyzer;

	private RepositoryConnection conn;

	private String baseURI;

	private String ns;

	private HashSet<String> relSet;

	private HashSet<String> attrSet;

	private HashSet<Resource> indSet;

}
