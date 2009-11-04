package basic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.ice.tar.TarInputStream;


/**
 * read lines from a .tar file
 * @author fulinyun
 *
 */
public class TarReader implements IDataSourceReader {

	private BufferedReader bReader;
	private TarInputStream tarInput;

	public TarReader(String fileName) {
		try {
			tarInput = new TarInputStream(new FileInputStream(fileName));
			bReader = new BufferedReader(new InputStreamReader(tarInput, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() throws Exception {
		bReader.close();
	}

	@Override
	public String readLine() throws Exception {
		String ret = bReader.readLine();
		while (ret == null) {
			if (tarInput.getNextEntry() == null) return null;
			ret = bReader.readLine();
		}
		return ret;

	}
	
	public static void main(String[] args) throws Exception {
		String testFile = "\\\\poseidon\\team\\semantic search\\data\\wikipedia-en-html-08-june.tar";
		TarReader tReader = new TarReader(testFile);
		for (int i = 0; i < 10; i++) System.out.println(tReader.readLine());
	}
}
