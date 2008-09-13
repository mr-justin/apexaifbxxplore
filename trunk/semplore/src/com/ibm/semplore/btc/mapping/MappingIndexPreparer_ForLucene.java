package com.ibm.semplore.btc.mapping;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.search.impl.XFacetedSearchableImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.IndexReaderImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author xrsun
 *
 */
public class MappingIndexPreparer_ForLucene extends MappingIndexPreparer {
	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	public static TermFactory termFactory = TermFactoryImpl.getInstance();
	
	HashMap<Long, Integer> idmap1 = new HashMap<Long, Integer>();
	HashMap<Long, Integer> idmap2 = new HashMap<Long, Integer>();

	public MappingIndexPreparer_ForLucene(InputStream input, File path_of_ds1, File path_of_ds2,
			int strategy) {
		super(input, path_of_ds1, path_of_ds2, strategy);
	}

	public void build() throws Exception {
		Properties config = new Properties();
		config.put(Config.INDEX_PATH, path_of_ds1.toString());
		XFacetedSearchService searchService = searchFactory.getXFacetedSearchService(config);
		XFacetedSearchableImpl searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
		IndexReaderImpl indexReader1 = (IndexReaderImpl)searcher.getInsIndexReader();

		config.put(Config.INDEX_PATH, path_of_ds2.toString());
		searchService = searchFactory.getXFacetedSearchService(config);
		searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
		IndexReaderImpl indexReader2 = (IndexReaderImpl)searcher.getInsIndexReader();

		
		BufferedReader fin = new BufferedReader(new InputStreamReader(input));
		String line;
		int lineno=0;
		long time_a = System.currentTimeMillis();

		//TODO implement strategy 1&2
		if (strategy==3) {
			while ((line=fin.readLine())!=null) {
				lineno++;
				String[] split = line.split("\\t");
				long hashid1 = Long.parseLong(split[0]);
				long hashid2 = Long.parseLong(split[1]);
				
				DocStream docs = indexReader1.getDocStream(termFactory.createTerm(String.valueOf(hashid1), FieldType.ID));
				if (!docs.next()) continue;
				System.out.print(docs.doc());
				System.out.print("\t");
				docs = indexReader2.getDocStream(termFactory.createTerm(String.valueOf(hashid2), FieldType.ID));
				if (!docs.next()) continue;
				System.out.println(docs.doc());
			}
			
		}
		
		System.err.println("Time(ms): " + String.valueOf(System.currentTimeMillis()-time_a));
		System.err.println("Line: " + String.valueOf(lineno));
		
	}
}
