package org.ateam.xxplore.core.service.mapping;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.law.warc.filters.Filter;
import it.unimi.dsi.law.warc.filters.Filters;
import it.unimi.dsi.law.warc.io.GZWarcRecord;
import it.unimi.dsi.law.warc.io.WarcFilteredIterator;
import it.unimi.dsi.law.warc.io.WarcRecord;
import it.unimi.dsi.law.warc.util.BURL;
import it.unimi.dsi.law.warc.util.WarcHttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

import SameAsCollector.TrueFilter;

import com.ice.tar.TarInputStream;

public class CollectSpecifiedMappingService {
	public static String dir = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\";
	public static String[] equStrings = {"equivalentClass", "equivalentProperty", "sameAs", "differentFrom", "AllDifferent"};
	
	/**
	 * select lines containing equality strings from .tar.gz file
	 * @param fn is the name of the input .tar.gz file
	 * @param outputfn is the name of the output .equ file, this file contains the lines containing the following equality strings:
	 * "equivalentClass", "equivalentProperty", "sameAs", "differentFrom", "AllDifferent" 
	 */
	public static void processTarGZ(String fn, String outputfn) throws Exception {
		System.out.println(new Date().toString() + " Begin processing " + fn);
		TarInputStream tis = new TarInputStream(new GZIPInputStream(new FileInputStream(fn)));
		PrintWriter pw = new PrintWriter(new FileWriter(outputfn));
		int lineCount = 0;
		while (tis.getNextEntry() != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(tis));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				for (String s : equStrings) if (line.contains(s)) {
					pw.println(line);
					pw.flush();
					break;
				}
				lineCount++;
				if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
			}
		}
		tis.close();
		pw.close();
		System.out.println(new Date().toString() + " Finish processing " + fn);
		System.out.println("Number of lines: " + lineCount);
	}

	/**
	 * select lines containing equality strings from .warc file
	 * @param f is the input .warc file
	 * @param output is the output .equ file, which contains the lines containing the following equality strings:
	 * "equivalentClass", "equivalentProperty", "sameAs", "differentFrom", "AllDifferent" 
	 */
	public static void processWarc(File f, File output) throws Exception {
		System.out.println(new Date().toString() + " Begin processing " + f.getName());
		final FastBufferedInputStream in = new FastBufferedInputStream(new FileInputStream(f));
		GZWarcRecord record = new GZWarcRecord();
		Filter<WarcRecord> filter = Filters.adaptFilterBURL2WarcRecord(new TrueFilter());
		WarcFilteredIterator it = new WarcFilteredIterator(in, record, filter);
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		int lineCount = 0;
		WarcHttpResponse response = new WarcHttpResponse();
		while (it.hasNext()) {
			WarcRecord nextRecord = it.next();
			try {
				response.fromWarcRecord(nextRecord);
				BufferedReader br = new BufferedReader(new InputStreamReader(response.contentAsStream()));
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					for (String s : equStrings) if (line.contains(s)) {
						pw.println(line);
						pw.flush();
						break;
					}
					lineCount++;
					if (lineCount%1000000 == 0) System.out.println(new Date().toString() + " : " + lineCount);
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		pw.close();
		System.out.println(new Date().toString() + " Finish processing " + f.getName());
		System.out.println("Number of lines: " + lineCount);
	}
	
	public static class TrueFilter extends Filter<BURL> {

		public boolean accept(BURL x) {
			return true;
		}

		public String toExternalForm() {
			return "true";
		}
	}

	/**
	 * select real equality statements from .equ file
	 */
	public static void refineEqu(File input, File output) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(input));
		PrintWriter pw = new PrintWriter(new FileWriter(output));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			try {
				String[] part = line.split(" ");
				boolean contain = false;
				for (String s : equStrings) if (part[1].contains(s)) {
					contain = true;
					break;
				}
				if (contain) pw.println(line);
			} catch (Exception e) {
				System.out.println(line);
				continue;
			}
		}
		br.close();
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		try {
			processTarGZ(dir+"freebase.nt.tar.gz", "C:\\freebase.equ");
		} catch (Exception e) {
			
		}
		processTarGZ(dir+"uscensus.nt.tar.gz", "C:\\uscensus.equ");	
		processTarGZ(dir+"dbpedia-v3.nt.tar.gz", "C:\\dbpedia-v3.equ");
		processTarGZ(dir+"swetodblp.nt.tar.gz", "C:\\swetodblp.equ");
		processTarGZ(dir+"wordnet.nt.tar.gz", "C:\\wordnet.equ");
	
		processWarc(new File(dir+"watson.warc"), new File("C:\\watson.equ"));
		
		try {
			processWarc(new File(dir+"geonames.warc"), new File("C:\\geonames.equ"));
		} catch (Exception e) {
			
		}
		try {
			processWarc(new File(dir+"yars-2.warc"), new File("C:\\yars-2.equ"));
		} catch (Exception e) {
			
		}
		try {
			processWarc(new File(dir+"yars-1.warc"), new File("C:\\yars-1.equ"));
		} catch (Exception e) {
			
		}
		try {
			processWarc(new File(dir+"falcon-crawl.warc"), new File("C:\\falcon-crawl.equ"));
		} catch (Exception e) {
			
		}
		try {
			processWarc(new File(dir+"swoogle-crawl.warc"), new File("C:\\swoogle-crawl.equ"));
		} catch (Exception e) {
			
		}
		refineEqu(new File("c:\\dbpedia-v3.equ"), new File("c:\\train\\dbpedia-v3.equ"));
		refineEqu(new File("c:\\falcon-crawl.equ"), new File("c:\\train\\falcon-crawl.equ"));
		refineEqu(new File("c:\\swetodblp.equ"), new File("c:\\train\\swetodblp.equ"));
		refineEqu(new File("c:\\swoogle-crawl.equ"), new File("c:\\train\\swoogle-crawl.equ"));
		refineEqu(new File("c:\\watson.equ"), new File("c:\\train\\watson.equ"));
		refineEqu(new File("c:\\yars-1.equ"), new File("c:\\train\\yars-1.equ"));
		refineEqu(new File("c:\\yars-2.equ"), new File("c:\\train\\yars-2.equ"));
		refineEqu(new File("c:\\geonames.equ"), new File("c:\\train\\geonames.equ"));
	}
}
