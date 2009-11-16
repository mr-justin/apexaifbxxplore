package main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import basic.IOFactory;


public class Indexer {

	public interface IFeatureFilter {
		public boolean accept(int docNum, String feature);
	}

	public static String indexFolder = "/usr/fulinyun/";
	public static String refIndex = indexFolder+"refIndex"; // local
	public static String basicFeatureIndex = indexFolder+"basicFeatureIndex"; // local
	public static int nonNullIndNum = 11677397;
	
	public static void main(String[] args) throws Exception {
//		lookBasicFeature();
//		preprocess(indexFolder+"geonames.gz", indexFolder+"geonames.dump"); // done
		// sort -S 512m -T . --compress-program=gzip geonames.dump | gzip > geonamesPreprocessed.gz // running
//		preprocess(indexFolder+"dblp.gz", indexFolder+"dblp.dump"); // done at gaea
		// sort -S 512m -T . --compress-program=gzip dblp.dump | gzip > dblpPreprocessed.gz // done at gaea
//		refIndexFromPreprocessed(KeyIndDealer.domainDBpedia, indexFolder+"dbpediaPreprocessed.gz", 
//				lap2indexParts+"/dbpedia"); // done
//		refIndexFromPreprocessed(KeyIndDealer.domainGeonames, indexFolder+"geonamesPreprocessed.gz", 
//				lap2indexParts+"/geonames"); // done
//		refIndexFromPreprocessed(KeyIndDealer.domainDblp, indexFolder+"dblpPreprocessed.gz", 
//				lap2index+"/dblp"); // done at gaea
//		mergeIndex(lap2indexParts, lap2index); // done
		// copy refIndex subfolders to poseidon
		// delete refIndex
		// rename refIndexAll to refIndex
//		lap3index(); // done
//		extractNonNullIndividuals(indexFolder+"nonNullInd.txt"); // done
//		extractNonNullClasses(indexFolder+"nonNullClass.txt"); // done
//		String lineList = indexFolder+"nonNullClass.txt";
//		dumpClassFeature(lineList, Analyzer.countLines(lineList), 
//				indexFolder+"classFeatureDump.txt"); // done
//		dumpFeatureRandom(indexFolder+"nonNullInd.txt", 1260000, indexFolder+"indFeature126w.txt");
		final IndexReader refIndexReader = IndexReader.open(refIndex);
		dumpFeature(basicFeatureIndex, "basic", new IFeatureFilter() {
			public boolean accept(int docNum, String feature) {
				return isIndividual(refIndexReader, docNum) && feature.length() != 0;
			}
			
			/**
			 * is ireader2.document(i) individual?
			 * @param ireader2
			 * @param i
			 * @return
			 */
			private boolean isIndividual(IndexReader ireader2, int i) {
				try {
					Document doc = ireader2.document(i);
					List fieldList = doc.getFields();
					for (Object o : fieldList) {
						Field f = (Field)o;
						String fn = f.name();
						if (fn.equals(Common.rdfType+"from") || fn.equals(Common.owlClass+"from") || 
								fn.equals(Common.dbpediaSubject+"from")) {
							return false;
						}
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

		}, indexFolder+"nonNullIndFeature.txt");
	}
	
	/**
	 * dump feature like {@link #dumpFeature(String, String, int, main.Indexer.IFeatureFilter, String)} but
	 * dump unlimited number of individuals
	 * @param index
	 * @param field
	 * @param filter
	 * @param output
	 * @throws Exception
	 */
	public static void dumpFeature(String index, String field, IFeatureFilter filter, 
			String output) throws Exception {
		dumpFeature(index, field, Integer.MAX_VALUE, filter, output);
	}
	
	/**
	 * dump feature from a field of an index, at most maxNum documents are read, filtered by filter, results
	 * written to output
	 * @param index
	 * @param field
	 * @param maxNum
	 * @param filter
	 * @param output
	 * @throws Exception
	 */
	public static void dumpFeature(String index, String field, int maxNum, IFeatureFilter filter, 
			String output) throws Exception {
		IndexReader ireader = IndexReader.open(index);
		PrintWriter pw = IOFactory.getPrintWriter(output);
		for (int i = 0; i < ireader.maxDoc() && i < maxNum; i++) {
			String feature = ireader.document(i).get(field);
			if (filter.accept(i, feature)) pw.println(i + " " + feature);
			if ((i+1)%1000000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
		}
		pw.close();
		ireader.close();
	}
	
	public static void lookBasicFeature() throws Exception {
		Scanner sc = new Scanner(System.in);
		IndexReader ireader = IndexReader.open(basicFeatureIndex);
		while (true) {
			int n = sc.nextInt();
			System.out.println(ireader.document(n).get("URI"));
			System.out.println(ireader.document(n).get("basic"));
		}
	}
	
	public static void lookRefIndex() throws Exception {
		Scanner sc = new Scanner(System.in);
		IndexReader ireader = IndexReader.open(refIndex);
		while (true) {
			int n = sc.nextInt();
			List fieldList = ireader.document(n).getFields();
			for (Object obj : fieldList) {
				Field field = (Field)obj;
				System.out.println(field.name() + " : " + field.stringValue());
			}
		}
	}
	
	/**
	 * index all information of individuals of a given domain
	 * @param domain
	 * @param input
	 * @param target
	 * @throws Exception
	 */
	public static void refIndexFromPreprocessed(String domain, String input, String target) throws Exception {
		System.out.println(new Date().toString() + " : start getting refIndex based on preprocessed file");
		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
		Directory directory = FSDirectory.getDirectory(target);
		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
				IndexWriter.MaxFieldLength.UNLIMITED);
//		iwriter.setRAMBufferSizeMB(1200);
		iwriter.setMergeFactor(2);
		BufferedReader breader = IOFactory.getGzBufferedReader(input);
		String currentURI = "";
		Document doc = null;
		int count = 0;
		for (String line = breader.readLine(); line != null; line = breader.readLine()) {
		    String[] parts = line.split(" ");
		    for (int i = 3; i < parts.length; i++) parts[2] += (" "+parts[i]); // get whole attribute value
			if (!parts[0].equals(currentURI)) {
				if (!currentURI.equals("") && currentURI.contains(domain) && 
						!currentURI.equals("<http://www.geonames.org/ontology#Feature>")) {
					iwriter.addDocument(doc);
					count++;
					if (count%100000 == 0) System.out.println(new Date().toString() + " : " + count);
				}
				currentURI = parts[0];
				doc = new Document();
				doc.add(new Field("URI", currentURI, Field.Store.YES, Field.Index.NOT_ANALYZED));
				if (!parts[1].equals(Common.sameAs+"to") && !parts[1].equals(Common.sameAs+"from") &&
						!currentURI.equals("<http://www.geonames.org/ontology#Feature>"))
					doc.add(new Field(parts[1], parts[2], Field.Store.YES, Field.Index.NO));
			} else {
				if (!parts[1].equals(Common.sameAs+"to") && !parts[1].equals(Common.sameAs+"from") &&
						!currentURI.equals("<http://www.geonames.org/ontology#Feature>"))
					doc.add(new Field(parts[1], parts[2], Field.Store.YES, Field.Index.NO));
			}
		}
		if (currentURI.contains(domain)) iwriter.addDocument(doc);
		count++;
		System.out.println(new Date().toString() + " : " + count);
		iwriter.optimize();
		iwriter.close();
		directory.close();
		breader.close();
		System.out.println(new Date().toString() + " : optimized");
	}
	
	public static void preprocess(String input, String output) throws Exception {
		BufferedReader breader = IOFactory.getGzBufferedReader(input);
		PrintWriter pwriter = IOFactory.getPrintWriter(output);
		for (String line = breader.readLine(); line != null; line = breader.readLine()) {
		    String[] parts = line.split(" ");
		    for (int i = 3; i < parts.length-1; i++) parts[2] += (" "+parts[i]); // get whole attribute value
		    // process this line
		    if (parts[2].startsWith("\"")) {
		        //attribute
		        pwriter.println(parts[0] + " " + parts[1] + " " + parts[2]);
		    } else {
		        //relation
		        pwriter.println(parts[0] + " " + parts[1] + "to " + parts[2]);
		        pwriter.println(parts[2] + " " + parts[1] + "from " + parts[0]);
		    }
		}
		pwriter.close();
		breader.close();
	}
	
	/**
	 * treat each attribute statement as one document with two fields named "URI" and "<predicateURI>", 
	 * and each relation statement as two documents (one for each direction) with two fields named "URI" 
	 * and "<predicateURI>to" or "<predicateURI>from", only the "URI" field is indexed and not tokenized, 
	 * relation targets/sources and attribute values are not indexed.
	 * time consumption: 97 sec for 1942887 lines, -> 36732sec (10 hr) to index 735737515 triples
	 * @throws Exception
	 */
	public static void lap1index(String[] toIndex, String targetFolder, boolean deleteOld) throws Exception {
		System.out.println(new Date().toString() + " : start lap 1 indexing");
		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();

				// Store the index in memory:
				// Directory directory = new RAMDirectory();
		// To store an index on disk, use this instead:
		Directory directory = FSDirectory.getDirectory(targetFolder);
		IndexWriter iwriter = new IndexWriter(directory, analyzer, deleteOld, 
				IndexWriter.MaxFieldLength.UNLIMITED);
//		iwriter.setRAMBufferSizeMB(800); // this could make the indexing process faster, 
		                                 // at the risk of OutOfMemory Exception when dealing 
		                                 // with a large data set
		iwriter.setMergeFactor(2); // hope this can help avoid "background merge hit exception"~~ failed!!!
		for (String s : toIndex) {
			System.out.println(new Date().toString() + " : start indexing " + s);
			BufferedReader br = IOFactory.getGzBufferedReader(s);
			int count = 0;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] parts = line.split(" ");
				for (int i = 3; i < parts.length-1; i++) parts[2] += (" "+parts[i]); // get whole attribute value
				Document doc = new Document();
				doc.add(new Field("URI", parts[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
				if (!parts[2].startsWith("\"")) { // relation
					doc.add(new Field(parts[1]+"to", parts[2], Field.Store.YES, Field.Index.NO));
				} else { // attribute
					doc.add(new Field(parts[1], parts[2], Field.Store.YES, Field.Index.NO));
				}
				iwriter.addDocument(doc);
				if (!parts[2].startsWith("\"")) { // relation
					Document docRev = new Document();
					docRev.add(new Field("URI", parts[2], Field.Store.YES, Field.Index.NOT_ANALYZED));
					docRev.add(new Field(parts[1]+"from", parts[0], Field.Store.YES, Field.Index.NO));
					iwriter.addDocument(docRev);
				}
				count++;
				if (count % 1000000 == 0) System.out.println(new Date().toString() + " : " + count);
			}
			br.close();
			System.out.println(new Date().toString() + " : " + count + " lines in all");
		}
		iwriter.optimize();
		iwriter.close();
		directory.close();
		System.out.println(new Date().toString() + " : optimized");
	}
	
	/**
	 * merge documents with the same "URI" field. The purpose of this lap is to collect all the 
	 * information of an individual in one posting list for further indexing and easy browsing 
	 * during clustering result analysis. Relation targets/sources and attribute values are still 
	 * not indexed. 
	 * speed: 24hr to index all the data sets
	 */
	public static void lap2index(String lap1indexPart, String target, int from, int to, 
			boolean deleteOld) throws Exception {
		System.out.println(new Date().toString() + " : start lap 2 indexing");
		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
		Directory directory = FSDirectory.getDirectory(target);
		IndexWriter iwriter = new IndexWriter(directory, analyzer, deleteOld, 
				IndexWriter.MaxFieldLength.UNLIMITED);
//		iwriter.setRAMBufferSizeMB(1200);
		iwriter.setMergeFactor(2);
		IndexReader ireader = IndexReader.open(lap1indexPart);
		
		// had a look at the terms in the "URI" field; they are in ascending lexical order 
		TermEnum te = ireader.terms();
		int fromCount = 0;
		while (te.next()) {
			if (fromCount < from) {
				fromCount++;
				continue;
			}
			if (fromCount == to) break;
			Term term = te.term();
			// this is the only class in Geonames, very large
			if (term.text().equals("<http://www.geonames.org/ontology#Feature>")) continue; 
			if (term.field().equals("URI")) { // always true
				Document d = new Document();
				boolean isClass = false;
				d.add(new Field("URI", term.text(), Field.Store.YES, Field.Index.NOT_ANALYZED));
				TermDocs td = ireader.termDocs(term);
				while (td.next()) {
					List fieldList = ireader.document(td.doc()).getFields();
					for (Object o : fieldList) {
						Field f = (Field)o;
						if (f.name().equals(Common.rdfType+"from") || 
								f.name().equals(Common.dbpediaSubject+"from")) 
							if (!isClass) {
								isClass = true;
								System.out.println(fromCount);
								System.out.println(term.text());
							}
						if (!f.name().equals("URI")) d.add(f);
					}
				}
				iwriter.addDocument(d);
				if (isClass) {
					iwriter.commit();
					System.out.println(fromCount);
				}
			}
			fromCount++;
			if (fromCount%10000 == 0) System.out.println(new Date().toString() + " : " + fromCount);
		}
		ireader.close();
		System.out.println(new Date().toString() + " : " + fromCount + " URIs aggregated");
		iwriter.optimize();
		iwriter.close();
		directory.close();
		System.out.println(new Date().toString() + " : optimized");
	}
	
	public static void checkIndex(String indexFolder) throws Exception {
		IndexReader ireader = IndexReader.open(indexFolder);
		int count = 0;
		for (int i = 0; i < ireader.maxDoc(); i++) {
			Document doc = ireader.document(i);
			List list = doc.getFields();
			for (Object o : list) {
				Field field = (Field)o;
				if (field.stringValue().contains("\"")) {
					System.out.println(field.stringValue());
					count++;
					if (count%40==0) {
						System.in.read();
						System.in.read();
					}
				}
			}
		}
		ireader.close();
	}
	
	public static void observeRefIndex() throws Exception {
		IndexReader ireader = IndexReader.open(refIndex);
		System.out.println(ireader.maxDoc());
		for (int i = 0; i < ireader.maxDoc(); i++) {
			String ab = ireader.document(i).get("<http://dbpedia.org/property/abstract>");
			if (ab != null) {
				System.out.println(ab);
				System.in.read();
			}
		}
		System.out.println();
		TermEnum te = ireader.terms();
		for (int i = 0; i < 40; i++) {
			te.next();
			Term term = te.term();
			System.out.println(term.toString());
		}
		ireader.close();
	}
	
	/**
	 * basic feature index
	 * based on the refIndex, merge each individual's attribute values into its "basic" field. 
	 * Attribute values are tokenized and indexed. URIs with "<rdf:type>from", "<owl:class>from" or 
	 * "<skos:subject>from" fields (i.e. classes) are also contained in the index.
	 */ 
	public static void basicFeatureIndex(String refFolder, String basicFolder) throws Exception {
		System.out.println(new Date().toString() + " : start lap 3 indexing");
		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
		Directory directory = FSDirectory.getDirectory(basicFolder);
		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
				IndexWriter.MaxFieldLength.UNLIMITED);
//		iwriter.setRAMBufferSizeMB(1200);
		iwriter.setMergeFactor(2);
		IndexReader ireader = IndexReader.open(refFolder);
		for (int i = 0; i < ireader.maxDoc(); i++) {
			Document doc = ireader.document(i);
			List fieldList = doc.getFields();
			String basic = "";
			for (Object o : fieldList) {
				Field field = (Field)o;
				String fn = field.name();
//				if (fn.equals(Common.rdfType+"from") || fn.equals(Common.owlClass+"from") || 
//						fn.equals(Common.dbpediaSubject+"from")) {
//					isIndividual = false;
//					break;
//				} else if (!fn.equals("URI") && !fn.endsWith("from") && !fn.endsWith("to")) {
//					basic += " " + field.stringValue();
//				}
				if (!fn.equals("URI") && !fn.endsWith("from") && !fn.endsWith("to")) {
					String value = field.stringValue();
					int end = value.lastIndexOf("\"");
					if (end == -1) end = value.length();
					basic += " " + value.substring(1, end);
				}
			}
			Document odoc = new Document();
			odoc.add(new Field("URI", doc.get("URI"), Field.Store.YES, Field.Index.NOT_ANALYZED));
			odoc.add(new Field("basic", basic, Field.Store.YES, Field.Index.ANALYZED));
			iwriter.addDocument(odoc);

			if ((i+1)%1000000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
		}
		ireader.close();
		iwriter.optimize();
		iwriter.close();
		directory.close();
		System.out.println(new Date().toString() + " : optimized");
	}
	
	public static void observeBasicFeatureIndex() throws Exception {
		IndexReader ireader = IndexReader.open(basicFeatureIndex);
		Random rand = new Random();
		for (int i = 0; i < 40; i++) {
			int n = rand.nextInt(ireader.maxDoc());
			Document doc = ireader.document(n);
			System.out.println(doc.get("URI") + " : " + doc.get("basic"));
		}
		ireader.close();
	}
	
	// the end of this source file
	
	
//	private static int[] getLineList(String nonNullIndNums, int numLines) throws Exception {
//		int[] ret = new int[numLines];
//		BufferedReader br = IOFactory.getBufferedReader(nonNullIndNums);
//		for (int i = 0; i < ret.length; i++) ret[i] = Integer.parseInt(br.readLine());
//		br.close();
//		return ret;
//	}

//	/**
//	 * record not null class lines in the basic feature index to nonNullNumbers, one doc# per line
//	 * @param nonNullNumbers
//	 */
//	public static void extractNonNullClasses(String nonNullNumbers) throws Exception {
//		IndexReader ireader3 = IndexReader.open(lap3index);
//		IndexReader ireader2 = IndexReader.open(lap2index);
//		PrintWriter pw = IOFactory.getPrintWriter(nonNullNumbers);
//		for (int i = 0; i < ireader3.maxDoc(); i++) if (!isIndividual(ireader2, i) 
//				&& !ireader3.document(i).get("basic").equals("")) {
//			pw.println(i);
//		}
//		pw.close();
//		ireader2.close();
//		ireader3.close();
//	}

//	/**
//	 * index class basic features
//	 * @throws Exception
//	 */
//	public static void classBasicIndex() throws Exception {
//		System.out.println(new Date().toString() + " : start class basic feature indexing");
//		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//		Directory directory = FSDirectory.getDirectory(classBasicIndex);
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
////		iwriter.setRAMBufferSizeMB(1200);
//		iwriter.setMergeFactor(2);
//		IndexReader ireader = IndexReader.open(lap2index);
//		for (int i = 0; i < ireader.maxDoc(); i++) {
//			Document doc = ireader.document(i);
//			List fieldList = doc.getFields();
//			String basic = "";
//			boolean isIndividual = true;
//			for (Object o : fieldList) {
//				Field f = (Field)o;
//				String fn = f.name();
//				if (fn.equals(Common.rdfType+"from") || fn.equals(Common.owlClass+"from") || 
//						fn.equals(Common.dbpediaSubject+"from")) {
//					isIndividual = false;
//				} else if (!fn.equals("URI") && !fn.endsWith("from") && !fn.endsWith("to")) {
//					basic += " " + f.stringValue();
//				}
//			}
//			if (!isIndividual) {
//				Document odoc = new Document();
//				odoc.add(new Field("URI", doc.get("URI"), Field.Store.YES, Field.Index.NOT_ANALYZED));
//				odoc.add(new Field("basic", basic, Field.Store.YES, Field.Index.ANALYZED));
//				iwriter.addDocument(odoc);
//			}
//			if ((i+1)%1000000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
//		}
//		ireader.close();
//		iwriter.optimize();
//		iwriter.close();
//		directory.close();
//		System.out.println(new Date().toString() + " : optimized");
//	}

//	/**
//	 * extended feature index
//	 * based on the 2nd- and 3rd- lap indexes, for each individual, merge its attribute values and 
//	 * its neighbors' attribute values into its "extended" field. This field is tokenized and indexed.
//	 * URIs with "<rdf:type>from", "<owl:class>from" or "<skos:subject>from" fields (i.e. classes) are 
//	 * not contained in the index.
//	 */
//	public static void lap4index(int from, int end, String target) throws Exception {
//		System.out.println(new Date().toString() + " : start lap 4 indexing");
//		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//		Directory directory = FSDirectory.getDirectory(target);
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
////		iwriter.setRAMBufferSizeMB(1200);
//		iwriter.setMergeFactor(2);
//		IndexReader ireader2 = IndexReader.open(lap2index);
//		IndexReader ireader3 = IndexReader.open(lap3index);
//		IndexSearcher isearcher3 = new IndexSearcher(ireader3);
//		for (int i = from; i < end && i < ireader3.maxDoc(); i++) {
//			Document doc = ireader3.document(i);
//			String uri = doc.get("URI");
//			String extended = doc.get("basic");
////			TopDocs td = isearcher2.search(new TermQuery(new Term("URI", uri)), 1);
////			Document toExtend = ireader2.document(td.scoreDocs[0].doc);
//			Document toExtend = ireader2.document(i);
//			if (!toExtend.get("URI").equals(doc.get("URI"))) {
//				System.out.println("URI not matched!!!");
//				System.exit(1);
//			}
//			List fieldList = toExtend.getFields();
//			for (Object o : fieldList) {
//				Field f = (Field)o;
//				if (f.name().endsWith("from") || f.name().endsWith("to")) {
//					TopDocs tdbasic = isearcher3.search(new TermQuery(new Term("URI", 
//							f.stringValue())), 1);
//					Document dbasic = ireader3.document(tdbasic.scoreDocs[0].doc);
//					extended += dbasic.get("basic");
//				}
//			}
//			Document odoc = new Document();
//			odoc.add(new Field("URI", uri, Field.Store.YES, Field.Index.NOT_ANALYZED));
//			odoc.add(new Field("extended", extended, Field.Store.YES, Field.Index.ANALYZED));
//			iwriter.addDocument(odoc);
//			if ((i+1)%10000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
//		}
//		iwriter.optimize();
//		iwriter.close();
//		directory.close();
//		isearcher3.close();
//		ireader2.close();
//		ireader3.close();
//		System.out.println(new Date().toString() + " : optimized");
//
//	}
	
//	/**
//	 * extended feature index
//	 * based on the 2nd- and 3rd- lap indexes, for each individual, merge its attribute values and 
//	 * its neighbors' attribute values into its "extended" field. This field is tokenized and indexed.
//	 * URIs with "<rdf:type>from", "<owl:class>from" or "<skos:subject>from" fields (i.e. classes) are 
//	 * not contained in the index.
//	 */
//	public static void lap4indexWithCache(int from, int end, String target) throws Exception {
//		System.out.println(new Date().toString() + " : start lap 4 indexing with cache");
//		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//		Directory directory = FSDirectory.getDirectory(target);
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
////		iwriter.setRAMBufferSizeMB(1200);
//		iwriter.setMergeFactor(2);
//		IndexReader ireader2 = IndexReader.open(lap2index);
//		IndexReader ireader3 = IndexReader.open(lap3index);
//		IndexSearcher isearcher3 = new IndexSearcher(ireader3);
//		HashMap<Integer, String> basicFeatureCache = new HashMap<Integer, String>();
//		int cacheSize = 0;
//		for (int i = from; i < end && i < ireader3.maxDoc(); i++) {
//			Document doc = ireader3.document(i);
//			String uri = doc.get("URI");
//			String extended = doc.get("basic");
////			TopDocs td = isearcher2.search(new TermQuery(new Term("URI", uri)), 1);
////			Document toExtend = ireader2.document(td.scoreDocs[0].doc);
//			Document toExtend = ireader2.document(i);
//			if (!toExtend.get("URI").equals(doc.get("URI"))) {
//				System.out.println("URI not matched!!!");
//				System.exit(1);
//			}
//			List fieldList = toExtend.getFields();
//			for (Object o : fieldList) {
//				Field f = (Field)o;
//				if (f.name().endsWith("from") || f.name().endsWith("to")) 
//				if (!f.name().equals(Common.rdfType+"from") && !f.name().equals(Common.owlClass+"from") 
//						&& !f.name().equals(Common.dbpediaSubject+"from")) {
//					TopDocs tdbasic = isearcher3.search(new TermQuery(new Term("URI", 
//							f.stringValue())), 1);
//					int idoc = tdbasic.scoreDocs[0].doc;
//					String dbasic = "";
//					if (!basicFeatureCache.containsKey(idoc)) {
//						if (cacheSize >= 1000000000) {
//							basicFeatureCache.clear();
//							cacheSize = 0;
//							System.out.println("cache cleared");
//						}
//						dbasic = ireader3.document(idoc).get("basic");
//						basicFeatureCache.put(idoc, dbasic);
//						cacheSize += dbasic.length();
//					} else {
//						dbasic = basicFeatureCache.get(idoc);
//					}
//					extended += dbasic;
//				}
//			}
//			Document odoc = new Document();
//			odoc.add(new Field("URI", uri, Field.Store.YES, Field.Index.NOT_ANALYZED));
//			odoc.add(new Field("extended", extended, Field.Store.YES, Field.Index.ANALYZED));
//			iwriter.addDocument(odoc);
//			if ((i+1)%10000 == 0) System.out.println(new Date().toString() + " : " + (i+1) + 
//					" ; cache size : " + cacheSize);
//		}
//		iwriter.optimize();
//		iwriter.close();
//		directory.close();
//		isearcher3.close();
//		ireader2.close();
//		ireader3.close();
//		System.out.println(new Date().toString() + " : optimized");
//
//	}
	
//	/**
//	 * unique extended feature index
//	 * for terms in the 4th-lap indexes ("extended" field) whose term frequencies are more than 1, split them 
//	 * into different terms such as "term", "term.1", "term.2" ... and sort all the terms according 
//	 * to the lexical order to meet the requirement of applying the C. Xiao et al. WWW09 positional 
//	 * prefix filtering technique.
//	 */
//	public static void lap5index() throws Exception {
//		System.out.println(new Date().toString() + " : start lap 5 indexing");
//		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//		Directory directory = FSDirectory.getDirectory(lap5index);
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
////		iwriter.setRAMBufferSizeMB(1200);
//		iwriter.setMergeFactor(2);
//		IndexReader ireader4 = IndexReader.open(lap4index);
//		for (int i = 0; i < ireader4.maxDoc(); i++) {
//			Document dextended = ireader4.document(i);
//			String uriex = dextended.get("URI");
//			String extended = dextended.get("extended");
//			String extendedsorted = sortUnique(extended);
//			Document odoc = new Document();
//			odoc.add(new Field("URI", uriex, Field.Store.YES, Field.Index.NOT_ANALYZED));
//			odoc.add(new Field("extended", extendedsorted, Field.Store.YES, Field.Index.ANALYZED));
//			iwriter.addDocument(odoc);
//			if ((i+1)%1000000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
//		}
//		iwriter.optimize();
//		iwriter.close();
//		directory.close();
//		ireader4.close();
//		System.out.println(new Date().toString() + " : optimized");
//		
//	}
	
//	/**
//	 * sort and unique tokens in str, duplicated tokens are assigned unique aliases
//	 * @param str
//	 * @return
//	 */
//	private static String sortUnique(String str) {
//		String[] tokens = str.split(" ");
//		Arrays.sort(tokens);
//		for (int i = 0; i < tokens.length; i++) 
//			for (int j = i+1; j < tokens.length && tokens[j].equals(tokens[i]); j++) tokens[j] += ("."+(j-i));
//		String ret = tokens[0];
//		for (int i = 1; i < tokens.length; i++) ret += " " + tokens[i];
//		return ret;
//	}

//	/**
//	 * sorted extended feature index
//	 * sort the terms in 5th-lap index according to document frequency ordering
//	 * @throws Exception
//	 */
//	public static void lap6index() throws Exception {
//		System.out.println(new Date().toString() + " : start lap 6 indexing");
//		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//		Directory directory = FSDirectory.getDirectory(lap6index);
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
////		iwriter.setRAMBufferSizeMB(1200);
//		iwriter.setMergeFactor(2);
//		final IndexReader ireader5 = IndexReader.open(lap5index);
//		for (int i = 0; i < ireader5.maxDoc(); i++) {
//			String ext = ireader5.document(i).get("extended");
//			String[] words = ext.split(" ");
//			Arrays.sort(words, new Comparator<String>() {
//
//				@Override
//				public int compare(String a, String b) {
//					try {
//						int dfa = ireader5.terms(new Term("extended", a)).docFreq();
//						int dfb = ireader5.terms(new Term("extended", b)).docFreq();
//						if (dfa > dfb) return 1;
//						else if (dfa == dfb) return 0;
//						return -1;
//					} catch (Exception e) {
//						e.printStackTrace();
//						return 0;
//					}
//					
//				}
//				
//			});
//			String extSorted = words[0];
//			for (int j = 1; j < words.length; j++) extSorted += " " + words[j];
//			Document doc = new Document();
//			doc.add(new Field("extended", extSorted, Field.Store.YES, Field.Index.ANALYZED));
//			iwriter.addDocument(doc);
//			if ((i+1)%1000000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
//		}
//		iwriter.optimize();
//		iwriter.close();
//		directory.close();
//		ireader5.close();
//		System.out.println(new Date().toString() + " : optimized");
//	}
	
//	/**
//	 * test similarity calculation
//	 */
//	public static void test() throws Exception {
//		org.apache.lucene.analysis.Analyzer analyzer = new StandardAnalyzer();
//		for (String s : StandardAnalyzer.STOP_WORDS) System.out.println(s); // show all the stop words
//		Directory directory = new RAMDirectory();
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
//		Document doc = new Document();
//		doc.add(new Field("text", "mary jane", Field.Store.YES, Field.Index.ANALYZED));
//		iwriter.addDocument(doc);
//		iwriter.optimize();
//		iwriter.close();
//		
//		// Now search the index:
//		IndexReader ireader = IndexReader.open(directory);
//		IndexSearcher isearcher = new IndexSearcher(directory);
//		// Parse a simple query that searches for "text":
//		QueryParser parser = new QueryParser("text", analyzer);
//		Query query = parser.parse("mary");
//		TopDocs results = isearcher.search(query, 1);
//		// Iterate through the results:
//		for (int i = 0; i < results.totalHits; i++) {
//			Document result = ireader.document(results.scoreDocs[i].doc);
//			float score = results.scoreDocs[i].score;
//			System.out.println(result.get("text") + " : " + score);
//		}
//		System.out.println("************");
//		
//		query = parser.parse("mary mary");
//		results = isearcher.search(query, 1);
//		for (int i = 0; i < results.totalHits; i++) {
//			Document result = ireader.document(results.scoreDocs[i].doc);
//			float score = results.scoreDocs[i].score;
//			System.out.println(result.get("text") + " : " + score);
//		}
//		
//		isearcher.close();
//		directory.close(); 
//	}
	
//	public static void lap2indexDebug(String lap1indexPart, String target, int from, int to, 
//			boolean deleteOld) throws Exception {
//		System.out.println(new Date().toString() + " : start lap 2 indexing");
//		org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//		Directory directory = FSDirectory.getDirectory(target);
//		IndexWriter iwriter = new IndexWriter(directory, analyzer, deleteOld, 
//				IndexWriter.MaxFieldLength.UNLIMITED);
////		iwriter.setRAMBufferSizeMB(1200);
//		iwriter.setMergeFactor(2);
//		IndexReader ireader = IndexReader.open(lap1indexPart);
//		
//		// had a look at the terms in the "URI" field; they are in ascending lexical order 
//		TermEnum te = ireader.terms();
//		int fromCount = 0;
//		while (te.next()) {
//			if (fromCount < from) {
//				fromCount++;
//				continue;
//			}
//			if (fromCount == to) break;
//			Term term = te.term();
//			if (term.field().equals("URI")) { // always true
//				Document d = new Document();
//				d.add(new Field("URI", term.text(), Field.Store.YES, Field.Index.NOT_ANALYZED));
//				TermDocs td = ireader.termDocs(term);
//				while (td.next()) {
//					List fieldList = ireader.document(td.doc()).getFields();
//					for (Object o : fieldList) {
//						Field f = (Field)o;
//						if (!f.name().equals("URI")) d.add(f);
//					}
//				}
//				iwriter.addDocument(d);
//				iwriter.commit();
//				System.out.println(fromCount);
//				System.out.println(term.text());
//			}
//			fromCount++;
//			if (fromCount%10000 == 0) System.out.println(new Date().toString() + " : " + fromCount);
//		}
//		ireader.close();
//		System.out.println(new Date().toString() + " : " + fromCount + " URIs aggregated");
//		iwriter.optimize();
//		iwriter.close();
//		directory.close();
//		System.out.println(new Date().toString() + " : optimized");
//	}

//	/**
//	 * dump basic or extended features of individuals or classes to output, one record per line
//	 */
//	public static void dumpFeature(String index, String field, String output) throws Exception {
//		IndexReader ireader = IndexReader.open(index);
//		PrintWriter pw = IOFactory.getPrintWriter(output);
//		for (int i = 0; i < ireader.maxDoc(); i++) {
//			pw.println(ireader.document(i).get(field));
//			if ((i+1)%1000000 == 0) System.out.println(new Date().toString() + " : " + (i+1));
//		}
//		pw.close();
//		ireader.close();
//	}

//	private static void mergeIndex(String source, String target) throws Exception {
//	System.out.println(new Date().toString() + " : start merging lap 2 indices");
//	org.apache.lucene.analysis.Analyzer analyzer = new WhitespaceAnalyzer();
//
//	Directory directory = FSDirectory.getDirectory(target);
//	IndexWriter iwriter = new IndexWriter(directory, analyzer, true, 
//			IndexWriter.MaxFieldLength.UNLIMITED);
//	iwriter.setMergeFactor(2);
//	File[] parts = new File(source).listFiles();
//	IndexReader[] indexParts = new IndexReader[parts.length];
//	for (int i = 0; i < parts.length; i++) indexParts[i] = IndexReader.open(parts[i]);
//	iwriter.addIndexes(indexParts);
//	iwriter.close();
//	directory.close();
//	for (int i = 0; i < parts.length; i++) indexParts[i].close();
//}

//	/**
//	 * dump class basic features, should be handled by tokenizer & ppjoin as a whole
//	 * @param target
//	 * @throws Exception
//	 */
//	public static void dumpClassFeature(String nonNullClasses, int numLines, 
//			String target) throws Exception {
//		int[] lineList = getLineList(nonNullClasses, numLines);
//		IndexReader ireader = IndexReader.open(lap3index);
//		PrintWriter pw = IOFactory.getPrintWriter(target);
//		for (int i = 0; i < lineList.length; i++) 
//			pw.println(ireader.document(lineList[i]).get("basic"));
//		pw.close();
//		ireader.close();
//	}
	
//	/**
//	 * dump basic features, should be handled by tokenizer & ppjoin as a whole
//	 * @param target
//	 * @throws Exception
//	 */
//	public static void dumpFeatureRandom(String indList, int numLines, 
//			String target) throws Exception {
//		int[] lineList = getLineList(indList, numLines);
//		Random r = new Random();
//		for (int i = 0; i < numLines; i++) {
//			int x = r.nextInt(1260000);
//			int y = r.nextInt(1260000);
//			int t = lineList[x];
//			lineList[x] = lineList[y];
//			lineList[y] = t;
//		}
//		IndexReader ireader = IndexReader.open(lap3index);
//		PrintWriter pw = IOFactory.getPrintWriter(target);
//		for (int i = 0; i < lineList.length; i++) 
//			pw.println(ireader.document(lineList[i]).get("basic"));
//		pw.close();
//		ireader.close();
//	}

	
//	/**
//	 * dump basic features of individuals in lineList to output, one record per line,
//	 * from start1 to end1, then from start2 to end2
//	 */
//	public static void dumpFeature(String index, String field, int[] lineList, 
//			int start1, int end1, int start2, int end2, String output) throws Exception {
//		IndexReader ireader = IndexReader.open(index);
//		PrintWriter pw = IOFactory.getPrintWriter(output);
//		for (int i = start1; i < end1; i++) 
//			pw.println(ireader.document(lineList[i]).get(field));
//		for (int i = start2; i < end2; i++)
//			pw.println(ireader.document(lineList[i]).get(field));
//		pw.close();
//		ireader.close();
//	}
	
//	/**
//	 * partition non null individual basic features, to a lot of .raw files in partitionFolder,
//	 * each with linesPerFile lines, 
//	 * @param nonNullIndNums
//	 * @param numLines
//	 * @param partitionFolder
//	 * @param linesPerFile
//	 * @throws Exception
//	 */
//	public static void partition4ppjoin(String nonNullIndNums, int numLines, String partitionFolder, 
//			int linesPerFile) throws Exception {
//		int[] lineList = getLineList(nonNullIndNums, numLines);
//		int partitionSize = linesPerFile/2;
//		int partitionNum = numLines/partitionSize;
//		int[] start = new int[partitionNum+1];
//		int[] end = new int[partitionNum+1];
//		start[0] = 0;
//		for (int i = 1; i < start.length; i++) start[i] = start[i-1]+partitionSize;
//		for (int i = 0; i < start.length; i++) end[i] = start[i]+partitionSize;
//		end[end.length-1] = numLines;
//		for (int i = 0; i < start.length; i++) for (int j = i+1; j < start.length; j++) {
//			dumpFeature(lap3index, "basic", lineList, start[i], end[i], start[j], end[j], 
//					partitionFolder+start[i]+"-"+end[i]+"."+start[j]+"-"+end[j]+".raw");
//		}
//	}
	
//	/**
//	 * have a look at whether the "<predicate>from" fields have been stored
//	 * already known they are not indexed
//	 * @throws Exception
//	 */
//	public static void observeLap1index() throws Exception {
//		IndexReader ireader = IndexReader.open(lap1index+"/dbpedia");
//		int count = 0;
////		System.out.println(ireader.maxDoc());
////		for (int i = 0; i < ireader.maxDoc(); i++) {
////			Document doc = ireader.document(i);
////			List fields = doc.getFields();
////			for (Object o : fields) {
////				Field f = (Field)o;
////				if (f.name().endsWith("from")) {
////					System.out.println(doc.toString());
////					count++;
////					break;
////				}
////			}
////			if (count > 0 && count%50 == 0) System.in.read();
////		}
//		TermEnum te = ireader.terms();
//		int countline = 0;
//		while (te.next()) {
//			TermDocs td = ireader.termDocs(te.term());
//			count = 0;
//			while (td.next()) count++;
//			if (count > 1) {
//				System.out.print(te.term().text() + " : ");
//				System.out.println(count);
//			}
//			countline++;
//			if (countline%1000 == 0) System.in.read();
//		}
//	}

//	/**
//	 * record not null individual lines in the basic feature index to nonNullNumbers, one doc# per line
//	 * @param nonNullNumbers
//	 */
//	public static void extractNonNullIndividuals(String nonNullNumbers) throws Exception {
//		IndexReader ireader3 = IndexReader.open(lap3index);
//		IndexReader ireader2 = IndexReader.open(lap2index);
//		PrintWriter pw = IOFactory.getPrintWriter(nonNullNumbers);
//		for (int i = 0; i < ireader3.maxDoc(); i++) if (isIndividual(ireader2, i) 
//				&& !ireader3.document(i).get("basic").equals("")) {
//			pw.println(i);
//		}
//		pw.close();
//		ireader2.close();
//		ireader3.close();
//	}


}
