package org.ateam.xxplore.core.service.mappingA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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

}
