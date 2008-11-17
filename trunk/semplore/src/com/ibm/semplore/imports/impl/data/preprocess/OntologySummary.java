package com.ibm.semplore.imports.impl.data.preprocess;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import com.ibm.semplore.imports.impl.data.load.GZReader;
import com.ibm.semplore.imports.impl.data.load.NTReader;
import com.ibm.semplore.imports.impl.data.load.TarGzReader;
import com.ibm.semplore.imports.impl.data.load.Util4NT;
import com.ibm.semplore.imports.impl.data.load.WarcReader;

/**
 * @author xrsun
 *
 */
public class OntologySummary {
	//max instance stored for each category
	static final int maxCatSize = 100;
	//   Instance->Category
	static HashMap<String, String> store;
	//    Category-># instance stored
	static HashMap<String, Integer> catSize;
	//   Category Subj-> Relation -> Category Obj
	static HashMap<String, HashMap<String, String>> relGraph;
	//  Category -> Attribute
	static HashMap<String, HashSet<String>> attGraph;
	
	static int relSize;
	static int attSize;
	static BufferedWriter output;
	static Formatter format;
	
	public static String checkFileType(String filename) {
		String[] allowed = new String[] { ".nt", ".warc", ".nt.tar.gz", ".nt.gz" };
		for (int i = 0; i < allowed.length; i++) {
			if (filename.contains(allowed[i]) && allowed[i].equals(filename.substring(filename.length()
					- allowed[i].length(), filename.length())))
				return allowed[i];
		}
		return null;
	}

	public static String strip(String s) {
		int i = s.lastIndexOf("/");
		if (i<0) i=-1;
		if (i+1>s.length()-1) return "";
		return s.substring(i+1,s.length()-1);
	}
	
	public static String labelsnippet(String s) {
		if (s.length()<2) return "";
		s = s.replace('"', '_');
		if (s.length()<20) return s.substring(1, s.length()-1);
		else return s.substring(1,20);
	}
	
	public static void processTripleLine(String line) throws IOException {
		String[] triple = line.replaceAll("\t", " ").split(" ");
		int i = 3;
		while (triple.length > i) {
			if (triple[i].equals(".")) 
				break;
			triple[2] = triple[2] + " " + triple[i];
			i++;
		}
		if (triple.length < 3)
			return;
		
		String tripletype = Util4NT.checkTripleType(triple);
		if (false && tripletype == Util4NT.ATTRIBUTE) {
			String s = store.get(triple[0]);
			if (s!=null) {
				HashSet<String> edges = attGraph.get(s);
				if (edges == null) {
					edges = new HashSet<String>();
					attGraph.put(s, edges);
				}
				if (!edges.contains(triple[1])) {
					attSize++;
					edges.add(triple[1]);
					output.write(format.attribute(
							strip(s),strip(triple[1]),labelsnippet(triple[2])));
				}
			}
		}
		else if (tripletype == Util4NT.RELATION) {
			String s,o;
			s=store.get(triple[0]);
			o=store.get(triple[2]);
			if (s!=null && o!=null) {
				HashMap<String, String> edges = relGraph.get(s);
				if (edges == null) {
					edges = new HashMap<String, String>();
					relGraph.put(s, edges);
				}
				if (edges.get(triple[1])==null) {
					relSize ++;
					output.write(format.relation(strip(s),strip(triple[1]),strip(o)));
					edges.put(triple[1], o);
				}
			}
		}
		else if (tripletype == Util4NT.CATEGORY) {
			Integer s = catSize.get(triple[2]);
			if (s==null) s = 0;
			if (s<maxCatSize) {
				store.put(triple[0], triple[2]);
				catSize.put(triple[2], s+1);
			}
		}
	}
	
	public static void summary(String nt_dir, File outf) throws Exception {
		if (outf==null)
			output = new BufferedWriter(new OutputStreamWriter(System.out));
		else 
			output = new BufferedWriter(new FileWriter(outf));
		output.write(format.header());
		
		store = new HashMap<String, String>();
		catSize = new HashMap<String, Integer>();
		relGraph = new HashMap<String, HashMap<String,String>>();
		attGraph = new HashMap<String, HashSet<String>>();
		relSize = 0;
		attSize = 0;
		
		System.out.println("input dir: " + nt_dir);
		File dir = new File(nt_dir);
		File[] files = dir.listFiles();
		for (int j = 0; j < files.length; j++) {
			File fin = files[j];
			String filetype= checkFileType(fin.getName());
			if (filetype==null)
				continue;
			System.out.println("processing file: "+fin);
			if (filetype.equals(".warc"))
				(new WarcReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt.tar.gz"))
				(new TarGzReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt.gz"))
				(new GZReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt"))
				(new NTReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			System.out.println("Stage 2: "+fin);
			if (filetype.equals(".warc"))
				(new WarcReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt.tar.gz"))
				(new TarGzReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt.gz"))
				(new GZReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt"))
				(new NTReader(OntologySummary.class)).main(new String[]{fin.getPath()});
		}
		output.write(format.tailer());
		output.close();
	}

	public static void main(String[] args) throws Exception {
		if (args[0].equals("dot")) format = new DotFormatter();
		else if (args[0].equals("sif")) format = new SIFFormatter();
			
		if (args.length==2)
			summary(args[1], null);
		else
			summary(args[1], new File(args[2]));
	}

	private static interface Formatter {
		public String header();
		public String tailer();
		public String attribute(String cls, String label, String val);
		public String relation(String c1, String rel, String c2);
	}
	private static class DotFormatter implements Formatter{

		@Override
		public String attribute(String cls, String label, String val) {
			return String.format("\"%s\" -> \"%s\" [label=\"%s\"]\n", cls,val,label);
		}

		@Override
		public String header() {
			return "digraph {\n";
		}

		@Override
		public String relation(String c1, String rel, String c2) {
			return String.format("\"%s\" -> \"%s\" [label=\"%s\"]\n", c1,c2,rel);
		}

		@Override
		public String tailer() {
			return "}";
		}
	}
	
	private static class SIFFormatter implements Formatter{

		@Override
		public String attribute(String cls, String label, String val) {
			return String.format("%s\t%s\t\"%s\"\n", cls,label, val);
		}

		@Override
		public String header() {
			return "";
		}

		@Override
		public String relation(String c1, String rel, String c2) {
			return String.format("%s\t%s\t%s\n", c1,rel,c2);
		}

		@Override
		public String tailer() {
			return "";
		}
		
	}
}
