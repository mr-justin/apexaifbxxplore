package basic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 * Used to read a data source that is in the form of a single .gz file
 * @author Linyun Fu
 *
 */
public class GzReader implements IDataSourceReader {

	private BufferedReader br;
	
	public GzReader(String fn) {
		try {
			init(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() throws Exception {
		br.close();
	}

	private void init(String fileName) throws Exception {
		br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				new FileInputStream(fileName)), "UTF-8"));
	}

	@Override
	public String readLine() throws Exception {
		return br.readLine();
	}

	public static void main(String[] args) throws Exception {
		String testFile = 
			"\\\\poseidon\\team\\Semantic Search\\data\\musicbrainz\\Rdf data\\" +
			"mball.attribute.nt.gz";
		GzReader gr = new GzReader(testFile);
		Scanner sc = new Scanner(System.in);
		while (true) {
			int start = sc.nextInt();
			int end = sc.nextInt();
			for (int i = 0; i < start; i++) gr.readLine();
			for (int i = start; i < end; i++) System.out.println(gr.readLine());
			gr.close();
		}
	}
}
