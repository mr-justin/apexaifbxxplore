/**
 * 
 */
package com.ibm.semplore.test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.index.CorruptIndexException;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.search.impl.XFacetedSearchableImpl;
import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.IndexReaderImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author xrsun
 *
 */
public class OutputFacetMapping {
	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();

	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
	
	public static TermFactory termFactory = TermFactoryImpl.getInstance();

	static Properties config ;
	
	public static void main(String[] args) throws Exception {
		config = Config.readConfigFile(args[0]);
		XFacetedSearchService searchService = searchFactory
				.getXFacetedSearchService(config);
		XFacetedSearchableImpl searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
		IndexReaderImpl indexReader = (IndexReaderImpl)searcher.getInsIndexReader();

//		long[] category = load("category");
//		long[] relation = load("relation");
		printDocPositionStream(indexReader, indexReader.getDocPositionStream(termFactory.createTerm(FieldType.CATEGORIES.toString(),FieldType.CATEGORIES)), null, "category.mapping");
		printDocPositionStream(indexReader, indexReader.getDocPositionStream(termFactory.createTerm(FieldType.RELATIONS.toString(),FieldType.RELATIONS)), null, "relation.mapping");
		printDocPositionStream(indexReader, indexReader.getDocPositionStream(termFactory.createTerm(FieldType.INVERSERELATIONS.toString(),FieldType.INVERSERELATIONS)), null, "irelation.mapping");
	}

	private static long[] load(String filename) throws IOException {
		DataInputStream fin = new DataInputStream(new BufferedInputStream(new FileInputStream(
				new File((String) config.get("data_for_index_dir")).getAbsolutePath()+File.separatorChar+filename)));
		int len = fin.available()/8;
		long[] table = new long[len];
		for (int i=0; i<len; i++) {
			table[i] = fin.readLong();
		}
		return table;
	}
	
	private static void printDocPositionStream(IndexReaderImpl indexReader,
			DocPositionStream stream, long[] table, String outname) throws CorruptIndexException, IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(outname));
		int docid;
		stream.init();
		for (int i=0; i<stream.getLen(); stream.next(),i++) {
			docid = stream.doc();
            while(stream.hasNextPosition()){
                int inner = stream.nextPosition();
                out.write(String.format("%d\t%d\n", docid, inner));
            }
		}
		out.close();
	}

}
