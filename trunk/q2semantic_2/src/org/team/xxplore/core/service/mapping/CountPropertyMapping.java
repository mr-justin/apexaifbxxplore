package org.team.xxplore.core.service.mapping;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import java.net.URLDecoder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import com.ice.tar.TarInputStream;

public class CountPropertyMapping {
	private String rootPath = "";
	private String instanceMappingPath = "";
	private String nTripleFilePath = "";
	
	private HashMap<MapToken,Integer> mt_hs = new HashMap<MapToken,Integer>();
	
	/**
	 * Init the lucene to create index
	 * @param indexpath
	 * @return
	 * @throws IOException
	 */
	public IndexWriter initLucene(String indexpath) throws IOException {
		File   indexDir = new File(indexpath);
        //dataDir is the directory that hosts the text files that to be indexed
        Analyzer luceneAnalyzer = new StandardAnalyzer();
        IndexWriter indexWriter = new IndexWriter(indexDir,luceneAnalyzer,true);
        return indexWriter;
	}
	
	/**
	 * finish create index.
	 * @param indexWriter
	 * @throws IOException
	 */
	public void finishLucene(IndexWriter indexWriter) throws IOException {
        indexWriter.optimize();
        indexWriter.close();  	
	}
	
	
	
	/**
	 * create index.
	 * @param filename
	 * @param iDir1 - index directory.
	 * @return
	 * @throws IOException
	 */
	public void createIndex(String filename,String iDir1) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
	
		IndexWriter indexWriter = initLucene(iDir1);
		
		int count = 0;
		String line;
		while(true) {
			line = br.readLine();
			if(line == null) break;
			String tokens [] = line.split("\t");
			if(tokens.length != 3) continue;
			count ++;
			if(count % 1000 == 0) System.out.println(count + "\t" + line);
			
			
			Document doc = new Document();
			doc.add(new Field("subject",tokens[0],Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc.add(new Field("predicate",tokens[1],Field.Store.YES,Field.Index.NO));
			doc.add(new Field("type",tokens[2],Field.Store.YES,Field.Index.NO));
			
			indexWriter.addDocument(doc);
		}
		br.close();
		
		this.finishLucene(indexWriter);
		
	}
		
	public CountPropertyMapping() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("c:"));
			this.rootPath = prop.getProperty("Root");
			this.instanceMappingPath = this.rootPath + "/" + prop.getProperty("instanceMappingPath");
			this.nTripleFilePath = this.rootPath + "/" + prop.getProperty("TripleFilePath");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * Put all the instances in instance mapping file to the HashSet which will be used to
	 * check whether the instance triple file is also in instance mapping file.
	 * @throws IOException
	 */
	public void putInstance2HashSet(String instanceMappingFile,HashSet<String> insSet1,
			HashSet<String> insSet2) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(instanceMappingFile));
		String line;
		while((line = br.readLine()) != null ) {
			String [] tokens = line.split("\t");
			insSet1.add(tokens[0]);
			insSet2.add(tokens[1]);
		}
		br.close();
	}
	
	/**
	 * Check whether the predicate is predefined in the RDF syntax.
	 * @param node
	 * @return
	 */
	public boolean isNonUseRDFString(String node) {
		return node.startsWith(RDFS.rdf_ns) || 
			   node.startsWith(RDFS.rdfs_ns) || 
			   node.startsWith(RDFS.owl_ns);
	}
	
	/**
	 * split a line into tree part subject,predicate,object.
	 * @param line
	 * @return
	 */
	public Statement getStatement(String line) {
		int t = line.indexOf(" ");
		if(t == -1) {
			System.err.println(" error : " + line);
			return null;
		}
		String s1 = line.substring(0, t);

		int t2 = line.indexOf(" ", t + 1);
		if( t2 == -1 ) {
			System.err.println(" error2 : " + line);
			return null;
		}
		String s2 = line.substring(t + 1, t2);
		String s3 = line.substring(t2 + 1, line.length() - 2);
		return new Statement(s1,s2,s3);
	}
	
	/**
	 * check whether the element is a literal.
	 * @param element
	 * @return
	 */
	private boolean isLiteral(String element) {
		return element.startsWith("\"");
	}
	
	/**
	 * Insert a new node into the HashMap.
	 * @param hm
	 * @param element
	 * @param node
	 */
	private void putNewNode(HashMap<String,ArrayList<Node>> hm,String element,Node node) {
		ArrayList<Node> tmp = hm.get(element);
		if(tmp == null) {
			tmp = new ArrayList<Node>();
		}
		tmp.add(node);
		hm.put(element, tmp);
	}
	
	/**
	 * The result of HashMap will be store in a file as Intermediate results, this method is used
	 * to read the HashMap from the file. 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public HashMap<String,ArrayList<Node>> getHashMap(String filename) throws IOException {
		HashMap<String,ArrayList<Node>> ret = new HashMap<String,ArrayList<Node>>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line = br.readLine()) != null) {
			String tokens[] = line.split("\t");
			Node node = new Node(tokens[1],this.getTypeEnum(tokens[2]));
			this.putNewNode(ret, tokens[0], node);
		}
		br.close();
		return ret;
	}
	
	/**
	 * Handle the one line of 
	 * @param line
	 */
	public void handleLine(String line,HashSet<String> insSet,PrintWriter pw) {
		Statement stmt = this.getStatement(line);
		if(stmt == null) return;		
		if( !this.isNonUseRDFString(stmt.predicate) ) {
			if(isLiteral(stmt.object)) {

				if( insSet.contains(stmt.subject) ) {
					pw.println(stmt.subject + "\t" + stmt.predicate + "\t" + Node.Type.attribute);
				}
			}
			else {
				if( insSet.contains(stmt.subject) ) {
					pw.println(stmt.subject + "\t" + stmt.predicate + "\t" + Node.Type.relation_subject);
				}

				if( insSet.contains(stmt.object) ) {
					pw.println(stmt.object + "\t" + stmt.predicate + "\t" + Node.Type.relation_object);
				}
			}
		}		
	}
	
	/**
	 * write the HashMap into a file as Intermediate results.
	 * @param hm
	 * @param filename
	 * @throws IOException
	 */
	public void outputHashMap(HashMap<MapToken,Integer> hm,String filename) throws IOException {
		PrintWriter pw = new PrintWriter(filename);
		Set<Entry<MapToken, Integer>> es = hm.entrySet();
		for(Entry<MapToken,Integer> en : es) {
			String output = en.getKey().token1 + "\t" +
			en.getKey().token2 + "\t" + en.getValue() + "\t" + en.getKey().type;
			pw.println(output);
			System.out.println(output);
		}
		pw.close();
	}
	
	/**
	 * read zip files
	 * @param zipdir - the father fold of zip files.
	 * @throws IOException
	 */
	public void scanZipFile(String zipdir,HashSet<String> insSet,PrintWriter pw) throws IOException {
		File zdir = new File(zipdir);
		for(File file  : zdir.listFiles()) {
			ZipInputStream br2 = new ZipInputStream(new FileInputStream(file));
			while(br2.getNextEntry() != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(br2));
				String line;
				int count = 0;
				
				while ((line = br.readLine()) != null) {
					count ++;
					if(count % 1000 == 0) System.out.println(count + "\t" + line);
					handleLine(line,insSet,pw);
				}
			}
			br2.close();	
		}
	}
	
	/**
	 * read the .gz file.
	 * @param filename
	 * @throws IOException
	 */
	public void scanGZipFile(String filename,HashSet<String> insSet,PrintWriter pw) throws IOException {

		GZIPInputStream br2 = new GZIPInputStream(new FileInputStream(filename));
		BufferedReader br = new BufferedReader(new InputStreamReader(br2));
		String line;
		int count = 0;
		
		while ((line = br.readLine()) != null) {
			count ++;
			if(count % 1000 == 0) System.out.println(count);
			handleLine(line,insSet,pw);
		}
		br.close();
	}
	
	/**
	 * read the N-Triple file.
	 * @param filename
	 * @throws IOException
	 */
	public void scanDumpFile(String filename,HashSet<String> insSet,PrintWriter pw) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		int count = 0;
		while((line = br.readLine()) != null) {
			count ++;
			if(count % 1000 == 0) System.out.println(count);
			handleLine(line,insSet,pw);
		}
		br.close();
	}
	
	/**
	 * read the .tar.gz file
	 * @param filename
	 * @throws IOException
	 */
	public void scanTGZFile(String filename,HashSet<String> insSet,PrintWriter pw) throws IOException {
		TarInputStream is = new TarInputStream(new GZIPInputStream(
				new FileInputStream(filename)));
		int count = 0;
		while (is.getNextEntry() != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = br.readLine()) != null ) {
				count ++;
				if(count % 1000 == 0) System.out.println(count);
				handleLine(line,insSet,pw);
			}
		}
		is.close();
	}
	
	/**
	 * Init search for lucene.
	 * @param indexpath
	 * @return
	 * @throws IOException
	 */
	public IndexSearcher initSearch(String indexpath) throws IOException {
		File indexDir = new File(indexpath);
        FSDirectory directory = FSDirectory.getDirectory(indexDir,false);
        IndexSearcher searcher = new IndexSearcher(directory);
        if(!indexDir.exists()){
        	System.out.println("The Lucene index is not exist");
        	return null;
        }
        return searcher;
	}
	
	/**
	 * search the index of lucene.
	 * @param searcher
	 * @param key
	 * @param queryStr
	 * @return
	 * @throws IOException
	 */
	public Hits search(IndexSearcher searcher,String key,String queryStr) throws IOException {
        Term term = new Term(key,queryStr);
        TermQuery luceneQuery = new TermQuery(term);
        Hits hits = searcher.search(luceneQuery);

        return hits;
	}
	
	/**
	 * run the create index.
	 * @throws IOException
	 */
	public void runCreateIndex() throws IOException {
		String f1 = "e:/user/jqchen/mapping_count/dbpedia_freebase/dbpedia.temp";
		String f2 = "e:/user/jqchen/mapping_count/dbpedia_freebase/freebase.temp";
		String idir1 = "e:/user/jqchen/index1";
		String idir2 = "e:/user/jqchen/index3";
		String output = "e:/user/jqchen/mapping_count/dbpedia_freebase/output";
		
		this.createIndex(f1, idir1);
		//this.createIndex(f2, idir2);
		
		
		String filename = "e:/user/jqchen/mapping_count/dbpedia_freebase/dbpediaFreebaseInstanceMap.txt";
		/*
		this.scanLinkFile2(filename, idir1, idir2,output);
		*/
	}
	
	/**
	 * @param filename - instance mapping result file path.
	 * @param iDir1 - index directory
	 * @param iDir2 - index directory
	 * @param output - where to store the result.
	 * @throws IOException
	 */
	public void getResult_Index(String filename,String iDir1,String iDir2,String output) throws IOException {
		IndexSearcher searcher1 = initSearch(iDir1);
		IndexSearcher searcher2 = initSearch(iDir2);
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		
		int count1 = 0;
		while((line = br.readLine()) != null) {
			String [] tokens = line.split("\t");
			String ins1 = tokens[1];
			String ins2 = tokens[3];
			
			count1 ++;
			if(count1 % 1000 == 0) System.out.println(ins1 + "\t" + ins2);
			
			if(count1 > 10000) break;
			
			Hits h1 = this.search(searcher1, "subject", ins1);
			Hits h2 = this.search(searcher2, "subject", ins2);
			
			for(int i=0;i<h1.length();i++) {
				Document doc1 = h1.doc(i);
				for(int j=0;j<h2.length();j++) {
					Document doc2 = h2.doc(j);
					
					if(doc1.get("type") == doc1.get("type")) {
						String element1 = doc1.get("predicate");
						String element2 = doc2.get("predicate");
						MapToken mt = new MapToken(element1,element2,this.getTypeEnum(doc1.get("type")));
						Integer count = mt_hs.get(mt);
						if(count == null) {
							mt_hs.put(mt, 1);
						}
						else mt_hs.put(mt, count + 1);
					}
				}
			}
		}
		
		this.outputHashMap(mt_hs, output);
	}
	
	/**
	 * 
	 * @param filename - instance mapping file path
	 * @param ins_node1 - HashMap.
	 * @param ins_node2 - HashMap
	 * @param output - where to store the result.
	 * @throws IOException
	 */
	public void getResult_HashMap(String filename,HashMap<String,ArrayList<Node>> ins_node1,HashMap<String,ArrayList<Node>> ins_node2,String output) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line = br.readLine()) != null) {
			String [] tokens = line.split("\t");
			String ins1 = tokens[1];
			String ins2 = tokens[3];
			
			ArrayList<Node> t1 = ins_node1.get(ins1);
			ArrayList<Node> t2 = ins_node2.get(ins2);
			
			if(t1 != null && t2 != null) {
				for(Node n1 : t1) {
					for(Node n2 : t2) {
						if(n1.type == n2.type) {
							MapToken mt = new MapToken(n1.element,n2.element,n1.type);
							Integer count = mt_hs.get(mt);
							if(count == null) {
								mt_hs.put(mt, 1);
							}
							else mt_hs.put(mt, count + 1);
						}
					}
				}
			}
		}
		br.close();
		
		this.outputHashMap(mt_hs,output);
	}
	
	/**
	 * convert the string to enum
	 * @param c
	 * @return
	 */
	private Node.Type getTypeEnum(String c) {
		if(c.equals("attribute")) {
			return Node.Type.attribute;
		}
		else if(c.equals("relation_object")) {
			return Node.Type.relation_object;
		}
		else {
			return Node.Type.relation_subject;
		}
	}
	
	
	public static void main(String args[]) throws IOException {
		new CountPropertyMapping().allRun();
	}
	
	public void allRun() throws IOException {
		for(File instMapping : new File(this.instanceMappingPath).listFiles()) {
			String [] ds = instMapping.getName().split("-");
			String instanceFile = instMapping.getPath() + "/" + "instance.txt";
			String tempFile1 = instMapping.getPath() + "/" + ds[0] + ".temp";
			String tempFile2 = instMapping.getPath() + "/" + ds[1] + ".temp";
			String outputFile = instMapping.getPath() + "/" + "count.txt";
			this.readFileAndGetResult(instanceFile, tempFile1, tempFile2, outputFile);
		}
	}
	
	/**
	 * First get the HashMap from file and calculate the count of property mapping.
	 * @param file - instance mapping file.
	 * @param file1 - HashMap file2
	 * @param file2 - HashMap file
	 * @param output - where the result be stored.
	 * @throws IOException
	 */
	public void readFileAndGetResult(String file,String file1,String file2,String output) throws IOException {
		HashMap<String,ArrayList<Node>> t1,t2;
		t1 = this.readfile(file1);
		t2 = this.readfile(file2);
		this.getResult_HashMap(file, t1, t2,output);
	}
	
	public HashMap<String,ArrayList<Node>> readfile(String filepath) throws IOException {
		HashMap<String,ArrayList<Node>> hm = new HashMap();
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line;
		
		int count = 0;
		while((line = br.readLine()) != null) {
			count ++;
			if(count % 1000 == 0) System.out.println(count + "\t" + filepath);
			String [] tokens = line.split("\t");
			
			if(tokens.length != 3) continue;
			ArrayList<Node> t = hm.get(tokens[0]);
			if(t == null) {
				t = new ArrayList();
				hm.put(tokens[0], t);
			}
			t.add(new Node(tokens[1],this.getTypeEnum(tokens[2])));		
		}
		
		return hm;
	}
	
	public void createHashMap() throws IOException {
		for(File instmapping : new File(instanceMappingPath).listFiles()) {
			String [] ds = instmapping.getName().split("-");
			HashSet<String> insSet[] = new HashSet[2];
			for(int i=0;i<2;i++) {
				insSet[i] = new HashSet<String>();
			}
			this.putInstance2HashSet(instmapping.getPath() + "/instanceMapping.txt", insSet[0], insSet[1]);
			
			
			for(int i=0;i<2;i++) {
				String theds = ds[i];
				HashSet<String> theInstSet = insSet[i];
				String outputfile = instmapping.getPath() + "/" + theds;
				
				
				for(File f : new File(this.nTripleFilePath ).listFiles()) {
					if( f.getName().startsWith(theds) ) {
						String ext = f.getName().substring(theds.length() + 1);
						PrintWriter pw = new PrintWriter(outputfile);
						if(ext.equals("dump")) {
							this.scanDumpFile(f.getName(), theInstSet, pw);
						}
						else if(ext.equals("tgz")) {
							this.scanTGZFile(f.getName(),theInstSet, pw);
						}
						else if(ext.equals("gz")) {
							this.scanGZipFile(f.getName(), theInstSet, pw);
						}
						else if(ext.equals("zip")) {
							this.scanZipFile(f.getName(), theInstSet, pw);
						}
						else {
							System.out.println("Error file type!");
						}
						pw.close();
					}
				}
			}
		}
	}	
}
