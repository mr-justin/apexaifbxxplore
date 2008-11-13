package org.team.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

/**
 * Used to read a data source that is in the form of a single .gz file
 * @author Linyun Fu
 *
 */
public class GzReader implements IDataSourceReader {

	private String fileName;
	private BufferedReader br;
	
	public GzReader(String fn) {
		fileName = fn;
	}
	
	@Override
	public void close() throws Exception {
		br.close();
	}

	@Override
	public void init() throws Exception {
		br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
	}

	@Override
	public String readLine() throws Exception {
		return br.readLine();
	}

	public static void main(String[] args) throws Exception {
		String testFile = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\swetodblp_noblank.gz";
		GzReader gr = new GzReader(testFile);
		PrintWriter pw = new PrintWriter(new FileWriter("d:\\dblpContents.txt"));
		gr.init();
		int lineCount = 0;
		for (String line = gr.readLine(); line != null; line = gr.readLine()) {
			pw.println(line);
			lineCount++;
			if (lineCount%1000000 == 0) System.out.println(lineCount);
		}
		gr.close();
		pw.close();
		System.out.println(lineCount + "lines in all");
	}
}
