/*
 * 
 */
package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

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
	
	public static String checkFileType(String filename) {
		String[] allowed = new String[] { ".nt", ".warc", ".nt.tar.gz" };
		for (int i = 0; i < allowed.length; i++) {
			if (filename.contains(allowed[i]) && allowed[i].equals(filename.substring(filename.length()
					- allowed[i].length(), filename.length())))
				return allowed[i];
		}
		return null;
	}

	public static String strip(String s) {
		return s.substring(s.lastIndexOf("/")+1);
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
		if (tripletype == Util4NT.ATTRIBUTE) {
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
					output.write(String.format("\"%s\" -> %d [label=\"%s\"]\n", strip(s),attSize,strip(triple[1])));
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
					output.write(String.format("\"%s\" -> \"%s\" [label=\"%s\"]\n", strip(s),strip(o),strip(triple[1])));
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
		output.write("digraph {\n");
		
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
			if (filetype.equals(".nt"))
				(new NTReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			System.out.println("Stage 2: "+fin);
			if (filetype.equals(".warc"))
				(new WarcReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt.tar.gz"))
				(new TarGzReader(OntologySummary.class)).main(new String[]{fin.getPath()});
			if (filetype.equals(".nt"))
				(new NTReader(OntologySummary.class)).main(new String[]{fin.getPath()});
		}
		output.write("}");
		output.close();
	}

	public static void main(String[] args) throws Exception {
		if (args.length==1)
			summary(args[0], null);
		else
			summary(args[0], new File(args[1]));
	}

}
