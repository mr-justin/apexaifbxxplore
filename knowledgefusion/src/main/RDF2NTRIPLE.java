package main;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class RDF2NTRIPLE {

	public static int count = 0;
	public static String attributeName = null;
	public static String lastQname = null;
	public static String subjectName = null;
	public static String subjectType = null;
	public static int embedCount = 0;
	
//	public static String mball = 
//		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball.bz2";
//	public static String target = 
//		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball.relation.nt.gz";
//	public static String attention = 
//		"http://musicbrainz.org/track/ee56d352-d1f9-4f76-aef4-ef09f2dcaac7";
	public static String mballgz = 
		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mballparsable.rdf.gz";
//	public static String mball1gz = 
//		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball1.rdf.gz";
	
	public static String rdf = 
		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mballparsable.rdf.gz";
	public static String instance = 
		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball.instance.nt.gz";
	public static String attribute = 
		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball.attribute.nt.gz";
	public static String relation = 
		"\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball.relation.nt.gz";

	public static void main(String[] args) throws Exception {
//		convert(mball, target);
//		mainObserve(mballgz);
//		test(mballgz);
//		extractInstanceClass(mball1gz, target); // to run, to find more invalid char
//		extractAttribute(mballgz, target);
//		find(mballgz, "rdf:resource", "rdf:li", "dc:creator"); // rdf:type, mm:release
//		find(mballgz, new String(new byte[]{0x1a}));
//		findInvalidLines(mball);
//		clean(mball, mballgz);
//		cleanInvalidChar(mballgz, new String(new byte[]{0x1a}), mball1gz);
		extractRelation(mballgz, relation);
//		test();
	}
	
	// extract relation instances from RDF/XML file compressed as gz, write to N-TRIPLE 
	// file compressed as gz
	// pattern: "rdf:about" indicates subject, "rdf:resource" indicates object, corresponding 
	// attribute being predicate
	public static void extractRelation(String rdfgz, String ntgz) throws Exception {
		final PrintWriter pw = getGzPrintWriter(ntgz);
		DefaultHandler handler = new DefaultHandler() {
			public void startElement(String namespaceURI, String lname,
					String qname, Attributes attrs) {
				if (qname.equals("rdf:RDF")) return;
				int aboutIndex = attrs.getIndex("rdf:about");
				int resourceIndex = attrs.getIndex("rdf:resource");
				if (aboutIndex != -1) { // found a subject
					subjectType = qname;
					subjectName = attrs.getValue(aboutIndex);
					embedCount = 0;
					count++;
					if (count % 200000 == 0) 
						System.out.println(new Date().toString() + " : " + count);
				} else if (resourceIndex != -1) { // found a relation instance
					if (subjectName != null && embedCount == 0) {
						pw.println(subjectName + " " + qname + 
								" <" + attrs.getValue(resourceIndex) + "> .");
						embedCount++;
					}
				} else {
					embedCount++;
				}
			}
			
			public void endElement(String namespaceURI, String lname, String qname) {
				if (qname.equals(subjectType)) subjectName = null;
				else embedCount--;
			}
		};

		InputStream in = getGzInputStream(rdfgz);
		count = 0;
		embedCount = 0;
		parseXML(in, handler);
		System.out.println(count + " elements in all");
		pw.close();
		in.close();
		
	}
	
	// replace all invalidChar in gzfile with "?" and write to gzoutput
	public static void cleanInvalidChar(String gzfile, String invalidChar, String gzoutput) 
			throws Exception {
		BufferedReader br = getGzBufferedReader(gzfile);
		PrintWriter pw = getGzPrintWriter(gzoutput);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			pw.println(line.replaceAll(invalidChar, "?"));
			count++;
			if (count % 1000000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		pw.close();
		br.close();
		System.out.println(count + " lines in all");
	}
	
	// read everything out from bz2 file, and write them back to gz file, cleaning non-utf-8 chars
	public static void clean(String bz2file, String gzfile) throws Exception {
		BufferedReader br = getBz2bufferedReader(bz2file);
		PrintWriter pw = getGzPrintWriter(gzfile);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			pw.println(line);
			count++;
			if (count%1000000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		pw.close();
		br.close();
		System.out.println(count + " lines in all");
	}
	
	private static InputStream getBz2inputStream(String rdfbz2) throws Exception {
		FileInputStream fis = new FileInputStream(rdfbz2);
		//read the "BZ" mark
		fis.read();
		fis.read();
		return new CBZip2InputStream(fis);

	}
	
	private static void parseXML(InputStream in, DefaultHandler handler) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = factory.newSAXParser();
		try {
			saxParser.parse(in, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// extract all literal valued attributes from gz RDF/XML file to gz N-TRIPLE file
	// pattern: "rdf:about" attr indicates subject, non-empty characters indicates attribute 
	// values and the preceding element's qname being attribute name
	public static void extractAttribute(String rdfgz, String ntgz) throws Exception {
		final PrintWriter pw = getGzPrintWriter(ntgz);
		DefaultHandler handler = new DefaultHandler() {
			public void startElement(String namespaceURI, String lname,
					String qname, Attributes attrs) {
				try {
					if (qname.equals("rdf:RDF")) return;
					int aIndex = attrs.getIndex("rdf:about");
					if (aIndex != -1) { 
						subjectType = qname;
						subjectName = attrs.getValue(aIndex);
						count++;
						if (count % 200000 == 0) 
							System.out.println(new Date().toString() + " : " + count);
					} else {
						lastQname = qname;
						if (attributeName == null) attributeName = qname; // not an embedded tag
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			public void characters(char[] ch, int start, int length) {
				try {
					String content = new String(ch).substring(start, start+length);
					if (content.matches("\\s*")) return;
					if (subjectName != null && attributeName != null && attributeName.equals(lastQname)) {
						pw.println("<" + subjectName + "> " + attributeName + " \"" + content + "\" .");
						pw.flush();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
					
			}
			
			public void endElement(String namespaceURI, String lname, String qname) {
				try {
					if (qname.equals(attributeName)) attributeName = null;
					if (qname.equals(subjectType)) subjectName = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		InputStream in = getGzInputStream(rdfgz);
		count = 0;
		parseXML(in, handler);
		System.out.println(count + " elements in all");
		pw.close();
		in.close();
	}
	
	// extract all rdf:type statements from gz RDF/XML file to gz N-TRIPLE file
	public static void extractInstanceClass(String rdfgz, String ntgz) throws Exception {
		final PrintWriter pw = getGzPrintWriter(ntgz);
		count = 0;
		DefaultHandler handler = new DefaultHandler() {
			public void startElement(String namespaceURI, String lname,
					String qname, Attributes attrs) {
				int aIndex = attrs.getIndex("rdf:about");
				if (aIndex != -1) { 
					String avalue = attrs.getValue(aIndex);
					pw.println("<" + avalue + "> rdf:type " + qname + " .");
					pw.flush();
					count++;
					if (count % 200000 == 0) 
						System.out.println(new Date().toString() + " : " + count);
				}
			}
		};

		InputStream in = getGzInputStream(rdfgz);
		parseXML(in, handler);
		System.out.println(count + " elements in all");
		pw.close();
		in.close();
	}
	
	private static InputStream getGzInputStream(String rdfgz) throws Exception {
		return new BufferedInputStream(new GZIPInputStream(new FileInputStream(rdfgz)));
	}

	private static PrintWriter getGzPrintWriter(String ntgz) throws Exception {
		return new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(ntgz)), "UTF-8")));
	}

	public static void mainObserve(String target) throws Exception {
		Scanner sc = new Scanner(System.in);
		while (true) {
			int s = sc.nextInt();
			int e = s+40;
			observe(target, s, e);
		}
	}

	private static BufferedReader getBz2bufferedReader(String bz2file) throws Exception {
		FileInputStream fis = new FileInputStream(bz2file);
		//read the "BZ" mark
		fis.read();
		fis.read();
		return new BufferedReader(new InputStreamReader(new CBZip2InputStream(fis), "UTF-8"));
	}
	
	// observe part of a gz format file, from line startl to line endl-1
	public static void observe(String gzfile, int startl, int endl) throws Exception {
		BufferedReader br = getGzBufferedReader(gzfile);
		for (int i = 0; i < startl; i++) br.readLine();
		for (int i = startl; i < endl; i++) System.out.println(br.readLine());
		br.close();
	}
	
	public static BufferedReader getGzBufferedReader(String gzfile) throws Exception {
		return new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(gzfile)), "UTF-8"));
	}

	// test SAX parser
	public static void test(String gz) throws Exception {
		DefaultHandler handler = new DefaultHandler() {
			public void startElement(String namespaceURI, String lname,
					String qname, Attributes attrs) {
				System.out.println(qname);
				for (int i = 0; i < attrs.getLength(); i++) {
					String aname = attrs.getQName(i);
					String avalue = attrs.getValue(i);
					System.out.println(aname + "=" + avalue);
				}
				count++;
				if (count == 100) System.exit(0);
			}
			
			public void characters(char[] ch, int start, int length) {
				String content = new String(ch).substring(start, start+length);
				if (content.matches("\\s*")) return;
				System.out.println(content);
			}
			
			public void endElement(String namespaceURI, String lname, String qname) {
				System.out.println("/"+qname);
			}
		};

		InputStream in = getGzInputStream(gz);
		count = 0;
		parseXML(in, handler);
		in.close();
	}
	
	// find all occurences in a gz file, print the 5-line context
	public static void find(String gzfile, String attention, String not1, 
			String not2) throws Exception {
		BufferedReader br = getGzBufferedReader(gzfile);
		LinkedList<Integer> occlist = new LinkedList<Integer>();
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (line.contains(attention) && !line.contains(not1) && !line.contains(not2)) 
				occlist.add(count);
			count++;
			if (count%5000000 == 0) System.out.println(new Date().toString() + " : " + count);
		}
		br.close();
		System.out.println(count + " lines in all");
		System.out.println(occlist.size() + " occurences found");
		for (Integer i : occlist) {
			System.out.println("-------------------");
			observe(gzfile, i-2, i+3);
			System.out.println("-------------------");
		}
	}
	
	// find all lines with non-ascii chars, useless
	public static void findInvalidLines(String bz2file) throws Exception {
		BufferedReader br = getBz2bufferedReader(bz2file);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (!line.matches("\\p{ASCII}*")) System.out.println(count + " : " + line);
			count++;
			if (count%1000000 == 0) System.out.println(count);
		}
		br.close();
	}

	// convert an RDF/XML file compressed in bz2 format to a N-TRIPLE file compressed in gz format not completed
	public static void convert(String rdfbz2, String ntgz) throws Exception {
		OutputStream os = new GZIPOutputStream(new FileOutputStream(ntgz));
		count = 0;
		DefaultHandler handler = new DefaultHandler() {
			public void startElement(String namespaceURI, String lname,
					String qname, Attributes attrs) {
				System.out.println(qname);
				for (int i = 0; i < attrs.getLength(); i++) {
					String aname = attrs.getQName(i);
					String avalue = attrs.getValue(i);
					System.out.println(aname + "=" + avalue);
				}
				count++;
				if (count == 100) System.exit(0);
			}
			
			public void characters(char[] ch, int start, int length) {
				String content = new String(ch).substring(start, start+length);
				System.out.println(content);
			}
			
			public void endElement(String namespaceURI, String lname, String qname) {
				System.out.println(qname);
			}
		};

		InputStream in = getBz2inputStream(rdfbz2);
		parseXML(in, handler);
		
	}

	public static String embedTest = "e:\\user\\fulinyun\\embedTest.xml";
	
	public static void test() throws Exception {
		DefaultHandler handler = new DefaultHandler() {
			public void startElement(String namespaceURI, String lname,
					String qname, Attributes attrs) {
				if (qname.equals("rdf:RDF")) return;
				int aboutIndex = attrs.getIndex("rdf:about");
				int resourceIndex = attrs.getIndex("rdf:resource");
				if (aboutIndex != -1) { // found a subject
					subjectType = qname;
					subjectName = attrs.getValue(aboutIndex);
					embedCount = 0;
				} else if (resourceIndex != -1) { // found a relation instance
					if (subjectName != null && embedCount == 0) {
						System.out.println(subjectName + " " + qname + 
								" <" + attrs.getValue(resourceIndex) + "> .");
						embedCount++;
					}
				} else {
					embedCount++;
				}
			}
			
			public void endElement(String namespaceURI, String lname, String qname) {
				if (qname.equals(subjectType)) subjectName = null;
				else embedCount--;
			}
		};

		InputStream in = new FileInputStream(embedTest);
		embedCount = 0;
		parseXML(in, handler);
		in.close();

	}

}
