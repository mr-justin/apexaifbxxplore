package org.ateam.xxplore.core.service.mappingA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.tools.bzip2.CBZip2InputStream;

public class Bz2Reader implements IDataSourceReader {

	private String fileName;
	private BufferedReader br;
	
	public Bz2Reader(String fn) {
		fileName = fn;
	}
	
	@Override
	public void close() throws Exception {
		br.close();
	}

	@Override
	public void init() throws Exception {
		br = new BufferedReader(new InputStreamReader(new CBZip2InputStream(new FileInputStream(fileName))));
		
		//read the initial "BZ" mark
		br.read();
		br.read();
	}

	@Override
	public String readLine() throws Exception {
		return br.readLine();
	}

	public static void main(String[] args) throws Exception {
		IDataSourceReader idsr = new Bz2Reader("\\\\poseidon\\team\\semantic search\\data\\musicbrainz\\Rdf data\\mball.bz2");
		idsr.init();
		for (int i = 0; i < 10; i++) System.out.println(idsr.readLine());
	}
}
