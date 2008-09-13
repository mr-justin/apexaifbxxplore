package com.ibm.semplore.imports.impl.data.load;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.MeasurableInputStream;
import it.unimi.dsi.law.warc.filters.Filter;
import it.unimi.dsi.law.warc.filters.Filters;
import it.unimi.dsi.law.warc.io.GZWarcRecord;
import it.unimi.dsi.law.warc.io.WarcFilteredIterator;
import it.unimi.dsi.law.warc.io.WarcRecord;
import it.unimi.dsi.law.warc.util.BURL;
import it.unimi.dsi.law.warc.util.WarcHttpResponse;

import java.io.*;
import java.lang.reflect.Method;
import java.util.regex.*;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;


public class WarcReader {
	Class processer;
	public WarcReader(Class p) {
		processer = p;
	}
	
	public  FastBufferedInputStream in;
	public  WarcFilteredIterator it;
	public  WarcHttpResponse response;
	public  WarcRecord record;
	public  BufferedReader br;

	private NTriplesParser parser = new NTriplesParser();

	private int tripleCount = 0;
	private int lineCount = 0;
	private int docCount = 0;

	public static class TrueFilter extends Filter<BURL> {

		@Override
		public boolean accept(BURL x) {
			return true;
		}

		@Override
		public String toExternalForm() {

			return "true";
		}

	}

	public void countLines(MeasurableInputStream block) throws IOException {
		int c = 0;
		while ((c = block.read()) != -1) {
			if (c == '\n') {
				lineCount++;
			}
		}
	}

	public void dumpContent(MeasurableInputStream block) throws IOException {
		int c = 0;
		while ((c = block.read()) != -1) {
			System.out.write(c);
		}
	}

	public void init(String fileName) throws FileNotFoundException {
		in = new FastBufferedInputStream(
				new FileInputStream(new File(fileName)));
		GZWarcRecord gzRecord = new GZWarcRecord();
		Filter<WarcRecord> filter = Filters
				.adaptFilterBURL2WarcRecord(new TrueFilter());
		it = new WarcFilteredIterator(in, gzRecord, filter);
		response = new WarcHttpResponse();
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public void getDocument() {
		record = it.next();
		try {
			response.fromWarcRecord(record);
			if (br != null)
				br.close();
			br = new BufferedReader(new InputStreamReader(response
					.contentAsStream()));
			docCount++;
		} catch (IOException e) {
			System.out.println("document exception");
			e.printStackTrace();
		}
	}

	 public String[] triple() {
		String s;
		String[] triple = new String[3];
		try {
			if ((s = br.readLine()) == null)
				return null;
			else {
				int start = 0;
				for (int i = 0; i < 3; i++) {
					// extract URI
					Matcher uri = Pattern.compile("(<(.+?)>) ").matcher(s);
					// extract BlankNode
					Matcher blankNode = Pattern.compile("(_:node(.+?)) ")
							.matcher(s);

					if (uri.lookingAt()) {
						triple[i] = uri.group(1);
						start = uri.end();
						s = s.substring(start, s.length());
					} else if (blankNode.lookingAt()) {
						triple[i] = blankNode.group(1);
						start = blankNode.end();
						s = s.substring(start, s.length());
					}

				}

				// operates literal
				if (triple[2] == null) {
					triple[2] = "\"\"";
				}
				return triple;
			}
		} catch (IOException e) {
			System.out.println("triple exception");
			e.printStackTrace();
		}
		return triple;
	}

	public int docCount() {
		return docCount;
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public void main(String[] args) throws FileNotFoundException, SecurityException, NoSuchMethodException {
		String fileName = args[0];
		init(fileName);
		int cnt = 0;
		long sttime = System.currentTimeMillis();
		int triplecnt = 0;
		Method method = processer.getDeclaredMethod("processTripleLine", new Class[]{String.class});
		while (hasNext()) {
			long time = System.currentTimeMillis() - sttime;
			if (time > 50) {
//				System.err.println("out of time:" + (cnt - 1));
//				System.err.println("triplecnt: " + triplecnt);
			}
			triplecnt = 0;
			sttime = System.currentTimeMillis();
			String[] temp;
			getDocument();
//			System.err.println(cnt++);
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					method.invoke(processer, line);				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// String inputFile = "D:\\NTTest.txt";
		// br = new BufferedReader(new FileReader(inputFile));
		// String[] temp;
		// while((temp = triple()) != null){
		// System.err.println(temp[0]);
		// System.err.println(temp[1]);
		// System.err.println(temp[2]);
		// System.err.println();
		// }
	}
}
