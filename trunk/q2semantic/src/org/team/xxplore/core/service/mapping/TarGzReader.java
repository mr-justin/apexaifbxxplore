package org.team.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

import com.ice.tar.TarInputStream;

/**
 * Used to read a data source that is in the form of a single (.nt).tar.gz file
 * @author Linyun Fu
 *
 */
public class TarGzReader implements IDataSourceReader {

	private String fileName;
	private BufferedReader br;
	private TarInputStream targzinput;
	
	
	public TarGzReader(String fn) {
		fileName = fn;
	}
	
	public void close() throws Exception {
		br.close();
	}

	public void init() throws Exception {
		targzinput = new TarInputStream(new GZIPInputStream(new FileInputStream(fileName)));
		br = new BufferedReader(new InputStreamReader(targzinput));
	}

	public String readLine() throws Exception {
		String ret = br.readLine();
		while (ret == null) {
			if (targzinput.getNextEntry() == null) return null;
			ret = br.readLine();
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		String testFile = "E:\\billiontriples\\law-1.4-src.tar.gz";
		TarGzReader tgr = new TarGzReader(testFile);
		tgr.init();
		PrintWriter pw = new PrintWriter(new FileWriter("E:\\billiontriples\\law-1.4-src.contents"));
		for (String line = tgr.readLine(); line != null; line = tgr.readLine()) pw.println(line);
		tgr.close();
		pw.close();
	}
}
