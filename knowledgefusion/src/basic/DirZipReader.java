package basic;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipInputStream;

/**
 * Used to read a data source that is in the form of a single directory of zip files
 * @author Linyun Fu
 *
 */
public class DirZipReader implements IDataSourceReader {

	private String folderName;
	private BufferedReader br;
	private ZipInputStream zipinput;
	private File[] files;
	private int fileIndex;
	
	public DirZipReader(String fn) {
		folderName = fn;
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
		File dir = new File(folderName);
		files = dir.listFiles();
		fileIndex = 0;
		zipinput = new ZipInputStream(new FileInputStream(files[fileIndex]));
		br = new BufferedReader(new InputStreamReader(zipinput, "UTF-8"));
	}

	public String readLine() throws Exception {
		String ret = br.readLine();
		while (ret == null) {
			while (zipinput.getNextEntry() == null) {
				fileIndex++;
				if (fileIndex >= files.length) return null;
				zipinput = new ZipInputStream(new FileInputStream(files[fileIndex]));
			}
			br = new BufferedReader(new InputStreamReader(zipinput, "UTF-8"));
			ret = br.readLine();
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		String testFile = "E:\\billiontriples\\testDir\\";
		DirZipReader dzr = new DirZipReader(testFile);
		PrintWriter pw = new PrintWriter(new FileWriter("E:\\billiontriples\\testDir.contents"));
		for (String line = dzr.readLine(); line != null; line = dzr.readLine()) pw.println(line);
		dzr.close();
		pw.close();
	}
}
