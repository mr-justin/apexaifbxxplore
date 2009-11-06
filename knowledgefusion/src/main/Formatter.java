package main;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Date;

import basic.IDataSourceReader;
import basic.IOFactory;


public class Formatter {
	
	// read everything out from input, and write them back to gz file
	public static void clean(String input, String gzfile) throws Exception {
		System.out.println("converting " + input + " into " + gzfile);
		PrintWriter pw = IOFactory.getGzPrintWriter(gzfile);
		IDataSourceReader dsr = IOFactory.getReader(input);
		int count = 0;
		try {
			for (String line = dsr.readLine(); line != null; line = dsr.readLine()) {
				pw.println(line);
				count++;
				if (count%3000000 == 0) System.out.println(new Date().toString() + " : " + count);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		pw.close();
		dsr.close();
		System.out.println(count + " lines in all");
	}

	// read everything out from input, and write them back to gz file, read at most maxLine lines
	public static void clean(String input, String gzfile, int maxLine) throws Exception {
		System.out.println("converting " + input + " into " + gzfile);
		PrintWriter pw = IOFactory.getGzPrintWriter(gzfile);
		IDataSourceReader dsr = IOFactory.getReader(input);
		int count = 0;
		try {
			for (String line = dsr.readLine(); line != null; line = dsr.readLine()) {
				pw.println(line);
				count++;
				if (count%3000000 == 0) System.out.println(new Date().toString() + " : " + count);
				if (count == maxLine) break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		pw.close();
		dsr.close();
		System.out.println(count + " lines in all");
	}
	
	/**
	 * remove empty lines in input and write the normal lines to output, both input and output are in gz format
	 * @param input
	 * @param output
	 * @throws Exception
	 */
	public static void removeEmptyLines(String input, String output) throws Exception {
		System.out.println("removing empty lines from " + input);
		BufferedReader br = IOFactory.getGzBufferedReader(input);
		PrintWriter pw = IOFactory.getGzPrintWriter(output);
		int count = 0;
		for (String line = br.readLine(); line != null; line = br.readLine()) { 
			if (!line.equals("")) pw.println(line);
			count++;
			if (count%3000000 == 0) System.out.println(count);
		}
		pw.close();
		br.close();
	}
	
	public static void main(String[] args) throws Exception {
//		removeEmptyLines(Common.dbpedia, Common.gzFolder + "dbpediaNoEmptyLines.gz"); // done
//		removeEmptyLines(Common.geonames, Common.gzFolder + "geonamesNoEmptyLines.gz"); // done
//		removeEmptyLines(Common.uscensus, Common.gzFolder + "uscensusNoEmptyLines.gz"); // done
//		removeEmptyLines(Common.foaf, Common.gzFolder + "foafNoEmptyLines.gz"); // done
		
//		String workingDir = "smb://poseidon/team/semantic search/BillionTripleData/";
//		String dbpedia = "dbpedia-v3.nt.tar.gz";
//		String dblp = "swetodblp_noblank.gz";
//		String uscensus = "uscensus.nt.tar.gz";
//		String geonames = "geonames.warc";
//		clean(workingDir+dbpedia, workingDir+"gz/dbpedia.gz");
//		clean(workingDir+dblp, workingDir+"gz/dblp.gz");
//		clean(workingDir+uscensus, workingDir+"gz/uscensus.gz");
//		clean(workingDir+geonames, workingDir+"gz/geonames.gz");
		
//		String wordnet = "wordnet.nt.tar.gz";
//		clean(workingDir+wordnet, workingDir+"gz/wordnet.gz");
		
//		String foaf = "smb://poseidon/team/semantic search/data/foaf/foafNTRIPLE.gz";
//		clean(foaf, "smb://poseidon/team/semantic search/data/foaf/foaf.gz", 54000000);
	}
}
