package org.ateam.xxplore.core.service.mappingA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.ice.tar.TarInputStream;
import java.util.*;

import java.net.URLDecoder;

/**
 * Mapping the class and property of NTriple.
 * @author jqchen
 *
 */
public class NTripleMapping {	
	
	private String inputFile1 = "\\\\Poseidon\\team\\Semantic Search\\BillionTripleData\\dbpedia-v3.nt.tar.gz";
	private String inputFile2 = "\\\\Poseidon\\team\\Semantic Search\\BillionTripleData\\swetodblp_noblank.gz";
	private String inputFile3 = "\\\\poseidon\\Team\\Semantic Search\\BillionTripleData\\yago_typeOf.nt";
	
	private HashSet<String> sprep = null;
	
	public void initPrep() throws IOException {
		sprep = new HashSet<String>();
		String src = "Triple/sprep.txt";
		BufferedReader br = new BufferedReader(new FileReader(src));
		String line;
		while((line = br.readLine()) != null) {
			line = trim(line,' ');
			sprep.add(line);
		}
		br.close();
	}
	
	/**
	 * Remove the prep of a term.
	 * @param line
	 * @return
	 */
	public String removePrep(String line) {
		if( sprep == null ) {
			System.err.println("Please init prep!");
			return null;
		}
		String [] tokens = line.split("_");
		String ret = "";
		for(int i=0;i<tokens.length;i++) {
			String token = tokens[i];
			if(!sprep.contains(token)) {
				if(ret.length() == 0) {
					ret += token;
				}
				else {
					ret += "_" + token;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Get the local name of a term.
	 * @param name
	 * @return
	 */
	public String getLocalName(String name) {
		String tmp = null;
		
		int pos = name.lastIndexOf("#");
		if(pos == -1) {
			pos = name.lastIndexOf("/");
			tmp = name.substring(pos + 1,name.length());
		}
		else {
			tmp = name.substring(pos + 1,name.length());
		}
		
		tmp = URLDecoder.decode( tmp.toLowerCase().substring(0,tmp.length() - 1) );
		
		return Stemmer.stemWord(tmp);
	}
	
	/**
	 * split a line to tree part (subject,predicate,object)
	 * @param line
	 * @return
	 */
	public Statement getStmt(String line) {
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
	 * check whether the term is a literal.
	 * @param node
	 * @return
	 */
	public boolean isLiteral(String node) {
		return node.startsWith("\"");
	}
	
	/**
	 * check the predicate whether it's a predefined rdf syntax term.
	 * @param node
	 * @return
	 */
	public boolean isNonUseRDFString(String node) {
		return node.startsWith(RDFS.rdf_ns) || 
			   node.startsWith(RDFS.rdfs_ns) || 
			   node.startsWith(RDFS.owl_ns);
	}
	
	/**
	 * check the predicate whether it's a predefined rdf syntax term.
	 * @param stmt
	 * @param concept
	 * @param relation
	 * @param attribute
	 * @throws IOException
	 */
	public void handleLine(Statement stmt,HashMap<String,String> concept,
			HashMap<String,String> relation,HashMap<String,String> attribute) throws IOException {
		
		if(stmt.predicate.equals(RDFS.type)) {
			if( stmt.object.equals(RDFS.rdf_class) ) {
				concept.put(getLocalName(stmt.subject),stmt.subject);
			}
			else if(stmt.object.equals(RDFS.rdf_datatype)) {
				attribute.put(getLocalName(stmt.subject),stmt.subject);
			}
			else if(stmt.object.equals(RDFS.rdf_object)) {
				relation.put(getLocalName(stmt.subject), stmt.subject);
			}
			else {
				concept.put(getLocalName(stmt.object),stmt.object);
			}
		}
		else if(this.isNonUseRDFString(stmt.predicate)) {
			
		}
		else {
			if(isLiteral(stmt.object)) {
				attribute.put(getLocalName(stmt.predicate),stmt.predicate);
			}
			else {
				relation.put(getLocalName(stmt.predicate),stmt.predicate);
			}
		}
	}
	
	/**
	 * read the gzip file and handle every line.
	 * @param gzipFile
	 * @param concept
	 * @param relation
	 * @param attribute
	 * @throws IOException
	 */
	public void scanGZipFile(String gzipFile, HashMap<String, String> concept,
			HashMap<String, String> relation,
			HashMap<String, String> attribute)
			throws IOException {

		GZIPInputStream is = new GZIPInputStream(new FileInputStream(gzipFile));

		int count = 0;

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		while ((line = br.readLine()) != null) {
			count++;
			if(count % 1000 == 0) System.out.println(inputFile2 + " " + count);
			Statement stmt = getStmt(line);
			if (stmt == null)
				continue;
			handleLine(stmt, concept, relation, attribute);
		}
		is.close();
	}
	
	/**
	 * read tar.gz file and handle every line
	 * @param targzfile
	 * @param concept
	 * @param relation
	 * @param attribute
	 * @throws IOException
	 */
	public void scanTGZFile(String targzfile,HashMap<String,String> concept,
			HashMap<String,String> relation,HashMap<String,String> attribute) throws IOException {
		
		TarInputStream is = new TarInputStream(new GZIPInputStream(new FileInputStream(targzfile)));
		
		int count = 0;
		while (is.getNextEntry() != null) {

			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line;
			while ( (line = br.readLine()) != null ) {
				count ++;
				if(count % 1000 == 0) System.out.println(targzfile + " " + count);
				//System.out.println(line);
				
				Statement stmt = getStmt(line);
				if(stmt == null) continue;
				handleLine(stmt,concept,relation,attribute);
				
			}
		}
		is.close();
	}
	
	/**
	 * read nt file and handle every line
	 * @param targzfile
	 * @param concept
	 * @param relation
	 * @param attribute
	 * @throws IOException
	 */
	public void scanDumpFile(String targzfile,HashMap<String,String> concept,
			HashMap<String,String> relation,HashMap<String,String> attribute) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(targzfile));

		int count = 0;

		String line;
		while ((line = br.readLine()) != null) {
			count++;
			if (count % 1000 == 0)
				System.out.println(targzfile + " " + count);
			Statement stmt = getStmt(line);
			if (stmt == null)
				continue;
			handleLine(stmt, concept, relation, attribute);

		}
		br.close();
	}
	
	/**
	 * get the mapping of two schema.
	 * @param hm1
	 * @param hm2
	 * @param outputFile
	 * @throws IOException
	 */
	public void mapping(HashMap<String,String> hm1,HashMap<String,String> hm2,String outputFile) throws IOException {
		Set<Entry<String, String>> hs = hm1.entrySet();
		
		PrintWriter pw = new PrintWriter(outputFile);
		for(Entry<String,String> en : hs) {
			String fullname = hm2.get(en.getKey());
			if(fullname != null) {
				String output = en.getValue() + "\t" + fullname;
				System.out.println(output);
				pw.println(output);
			}
		}
		pw.close();
	}
	
	public void output(HashMap<String,String> hm,String filename) throws IOException {
		PrintWriter pw = new PrintWriter(filename);
		Set<Entry<String, String>> es = hm.entrySet();
		for(Entry<String,String> en : es) {
			String line = en.getKey() + "\t" + en.getValue();
			System.out.println(line);
			pw.println(line);
		}
		pw.close();
	}

	/**
	 * delete the _ which in the beginning of the String line.
	 * @param line
	 * @param c
	 * @return
	 */
	public String trim(String line, char c) {
		int pos = -1;
		for(int i=0;i<line.length();i++) {
			if(line.charAt(i) != c) {
				pos = i;
				break;
			}
		}
		
		if(pos == -1) return null;
		
		
		int pos2 = -1;
		for(int i=line.length()-1;i>=0;i--) {
			if(line.charAt(i) != c) {
				pos2 = i;
				break;
			}
		}
		
		if(pos2 == -1) return null;
		
		return line.substring(pos,pos2+1);
	}
	
	public void getConcept(HashMap<String,String> concepts) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inputFile3));
		String line;
		int count = 0;
		while( (line = br.readLine()) != null) {
			count ++;
			if(count % 1000 == 0) System.out.println(inputFile3 + " " + count);
			String tokens [] = line.split("\t");
			if(tokens.length >= 3) {
				String tmp = tokens[2];
				tmp = tmp.substring(1,tmp.length());
				int pos = tmp.indexOf("_");
				
				//If the token is begin with word
				if(tmp.toLowerCase().startsWith("wordnet")) {
					int pos2 = tmp.lastIndexOf("_");
					tmp = tmp.substring(pos + 1,pos2);
				}
				else {
					tmp = tmp.substring(pos + 1,tmp.length());
				}
				tmp = this.trim(tmp,'_');
				tmp = Stemmer.stemWord(tmp);
				concepts.put(tmp, tokens[2]);
				
			}
		}
		br.close();
		
	}	
	
	public void handleToken(String node,HashMap<String,String> concepts,HashMap<String,String> properties) {
		String prefix_property = "<http://www.freebase.com/property/";
		String prefix_concept = "<http://www.freebase.com/class/";
		if( node.startsWith(prefix_property) ) {
			String tmp = node.substring(prefix_property.length(),node.length() - 1);
			tmp = Stemmer.stemWord(tmp);
			properties.put(tmp,node);
		}
		else if(node.startsWith(prefix_concept)) {
			String tmp = node.substring(prefix_concept.length(),node.length() - 1);
			tmp = Stemmer.stemWord(tmp);
			concepts.put(tmp,node);
		}
		
	}
	
	
	public void scanFile(String filename,HashMap<String,String> concepts,HashMap<String,String> properties) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line = br.readLine()) != null) {
			String [] tokens = line.split(" ");
			if(tokens.length >= 3) {
				for(int i=0;i<3;i++) {
					this.handleToken(tokens[i],concepts, properties);
				}
			}
		}
	}
	
	
	public HashMap<String, String> readHashMap(String filename) throws IOException {
		HashMap<String,String> hs = new HashMap<String,String>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		String line;
		while((line = br.readLine()) != null) {
			String [] tokens = line.split("\t");
			hs.put(tokens[0], tokens[1]);
		}
		
		return hs;
	}
	
	public static void main(String args[]) throws IOException {
		NTripleMapping t = new NTripleMapping();
		t.mappingAll();
	}
	
	/*
	 * the list of dataset which is need to mapping.
	 */
	private String [][] dataset = new String [][] {
			{ "dbpedia","freebase" },
			{ "dbpedia","dblp" },
			{ "dbpedia","geonames" },
			{ "dbpedia","uscensus" },
			{ "freebase","dblp" },
			{ "freebase","geonames" },
			{ "freebase","uscensus" },
			{ "dblp","geonames" },
			{ "dblp","uscensus" },
			{ "geonames","uscensus" },
			
	};
	
	/**
	 * 
	 * @param data1 - The first concept or attribute or relation file path.
	 * @param data2 - The second concept or attribute or relation file path.
	 * @throws IOException
	 * 
	 */
	private void mappingTwo(String data1,String data2) throws IOException {
		String filetype [] = new String[] { "concept","attribute","relation" };
		
		for(int i=0;i<filetype.length;i++) {
			String path = "Triple2/" + data1 + "_" + data2 + "/";
			File t = new File(path);
			t.mkdir();
			String outputFile = path + data1 + "_" + data2 + "_" + filetype[i];
			HashMap<String,String> hm1 = readHashMap( "Triple/" + data1 + "/" + filetype[i]);
			HashMap<String,String> hm2 = readHashMap( "Triple/" + data2 + "/" + filetype[i]);
			this.mapping(hm1, hm2, outputFile);
		}		
	}
	
	/**
	 * @throws IOException
	 */
	public void mappingAll() throws IOException {
		for(int i=0;i<dataset.length;i++) {
			System.out.println(dataset[i][0] + " " + dataset[i][1]);
			this.mappingTwo(dataset[i][0],dataset[i][1]);
		}
	}
	
	/**
	 * @author jqchen
	 * @throws IOException
	 * 
	 * This method used to read the triple file to find each concept , relation , attribute. And output them to 
	 * three file.
	 */
	public void getSchema() throws IOException {
		HashMap<String,String> concept = new HashMap<String,String>();
		HashMap<String,String> relation = new HashMap<String,String>();
		HashMap<String,String> attribute = new HashMap<String,String>();
		
		String ntfile ="\\\\poseidon\\Team\\Semantic Search\\BillionTripleData\\geonames.dump";
				
		this.scanDumpFile(ntfile, concept, relation, attribute);
		
		String f1 = "Triple/geonames/concept";
		String f2 = "Triple/geonames/relation";
		String f3 = "Triple/geonames/attribute";
		
		this.output(concept,f1);
		this.output(relation,f2);
		this.output(concept,f3);
		
	}
}
