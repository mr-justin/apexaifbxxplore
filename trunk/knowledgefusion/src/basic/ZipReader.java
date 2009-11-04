package basic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipInputStream;

/**
 * Used to read a data source that is in the form of a single .zip file
 * @author Linyun Fu
 *
 */
public class ZipReader implements IDataSourceReader {

	private String fileName;
	private ZipInputStream zipinput;
	private BufferedReader br;
	
	public ZipReader(String fn) {
		fileName = fn;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws Exception {
		br.close();
	}

	private void init() throws Exception {
		zipinput = new ZipInputStream(new FileInputStream(fileName));
		br = new BufferedReader(new InputStreamReader(zipinput, "UTF-8"));
	}

	public String readLine() throws Exception {
		String ret = br.readLine();
		while (ret == null) {
			if (zipinput.getNextEntry() == null) return null;
			ret = br.readLine();
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		String testFile = "E:\\billiontriples\\test.zip";
		ZipReader zr = new ZipReader(testFile);
		PrintWriter pw = new PrintWriter(new FileWriter("E:\\billiontriples\\test.contents"));
		for (String line = zr.readLine(); line != null; line = zr.readLine()) pw.println(line);
		zr.close();
		pw.close();
	}
}
