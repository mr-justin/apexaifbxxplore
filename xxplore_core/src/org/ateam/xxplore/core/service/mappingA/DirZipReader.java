package org.ateam.xxplore.core.service.mappingA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
	}
	
	public void close() throws Exception {
		br.close();
	}

	public void init() throws Exception {
		File dir = new File(folderName);
		files = dir.listFiles();
		fileIndex = 0;
		zipinput = new ZipInputStream(new FileInputStream(files[fileIndex]));
		br = new BufferedReader(new InputStreamReader(zipinput));
	}

	public String readLine() throws Exception {
		String ret = br.readLine();
		while (ret == null) {
			br.close();
			while (zipinput.getNextEntry() == null) {
				fileIndex++;
				if (fileIndex >= files.length) return null;
				zipinput = new ZipInputStream(new FileInputStream(files[fileIndex]));
			}
			br = new BufferedReader(new InputStreamReader(zipinput));
			ret = br.readLine();
		}
		return ret;
	}

}
