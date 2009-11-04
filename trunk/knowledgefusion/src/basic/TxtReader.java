package basic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TxtReader implements IDataSourceReader {

	private String filename;
	private BufferedReader br;
	
	public TxtReader(String fn) {
		filename = fn;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() throws Exception {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
	}
	
	@Override
	public void close() throws Exception {
		br.close();
	}

	@Override
	public String readLine() throws Exception {
		return br.readLine();
	}
	
	public static void main(String[] args) throws Exception {
		TxtReader tr = new TxtReader("e:\\user\\fulinyun\\btc\\searchsession.txt");
		for (int i = 0; i < 10; i++) System.out.println(tr.readLine());
		tr.close();
	}

}
