package basic;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.law.warc.filters.Filter;
import it.unimi.dsi.law.warc.filters.Filters;
import it.unimi.dsi.law.warc.io.GZWarcRecord;
import it.unimi.dsi.law.warc.io.WarcFilteredIterator;
import it.unimi.dsi.law.warc.io.WarcRecord;
import it.unimi.dsi.law.warc.util.BURL;
import it.unimi.dsi.law.warc.util.WarcHttpResponse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Used to read a data source that is in the form of a single .warc file
 * @author Linyun Fu
 *
 */
public class WarcReader implements IDataSourceReader{

	private String fileName;
	private BufferedReader br;
	private WarcHttpResponse response;
	private WarcFilteredIterator it;
	private GZWarcRecord record;
	private WarcRecord nextRecord;
	
	public WarcReader(String fn) {
		fileName = fn;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init() throws Exception {
		FastBufferedInputStream in = new FastBufferedInputStream(new FileInputStream(fileName));
		record = new GZWarcRecord();
		Filter<WarcRecord> filter = Filters.adaptFilterBURL2WarcRecord(new Filter<BURL>() {
			public boolean accept(BURL x) {
				return true;
			}
			public String toExternalForm() {
				return "true";
			}
		});
		it = new WarcFilteredIterator(in, record, filter);
		response = new WarcHttpResponse();
		nextRecord = it.next();
		response.fromWarcRecord(nextRecord);
		br = new BufferedReader(new InputStreamReader(response.contentAsStream(), "UTF-8"));
	}
	
	public String readLine() throws Exception {
		String ret = br.readLine();
		while (ret == null) {
			if (!it.hasNext()) return null;
			nextRecord = it.next();
			response.fromWarcRecord(nextRecord);
			br = new BufferedReader(new InputStreamReader(response.contentAsStream()));
			ret = br.readLine();
		}
		return ret;
	}
	
	public void close() throws Exception {
		br.close();
	}
	
	public static void main(String[] args) throws Exception {
		String testFile = "\\\\poseidon\\team\\Semantic Search\\BillionTripleData\\geonames.warc";
		WarcReader wr = new WarcReader(testFile);
		int lineCount = 0;
		for (String line = wr.readLine(); line != null; line = wr.readLine()) {
			System.out.println(line);
			lineCount++;
			if (lineCount == 20) break;
		}
		wr.close();
//		System.out.println(lineCount + " lines in all");
	}
}
