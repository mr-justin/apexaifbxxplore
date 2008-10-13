/**
 * 
 */
package com.ibm.semplore.test;

import java.io.File;
import java.util.Properties;
import java.util.Map.Entry;

import com.ibm.semplore.btc.QuerySnippetDB;
import com.ibm.semplore.btc.mapping.MappingIndexReaderFactory;
import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.search.impl.XFacetedSearchableImpl;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.IndexReaderImpl;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author xrsun
 *
 */
public class TestSearchAll {

	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();
	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
	public static TermFactory termFactory = TermFactoryImpl.getInstance();
	private static IndexReaderImpl indexReader;
	private static String dataSource;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out
					.println("usage: java TestSearchAll [datasrc_config_file] [keyword]");
			System.exit(1);
		}
		Properties config = Config.readConfigFile(args[0]);
		QuerySnippetDB.init(config.getProperty("snippet"));
		MappingIndexReaderFactory.init(new File(config.getProperty("mapping")));
		
		System.out.println("=== Init config ===");

		for (Entry<Object, Object> i: config.entrySet()) {
			if (!"snippet".equals(i.getKey()) && !"mapping".equals(i.getKey())) 
				dataSource = (String)i.getKey();
			Properties indexconfig = new Properties();
			indexconfig.put(Config.INDEX_PATH, config.get(dataSource));
			indexconfig.setProperty(Config.THIS_DATA_SOURCE, dataSource);
			
			XFacetedSearchService searchService = searchFactory
					.getXFacetedSearchService(indexconfig);
			XFacetedSearchableImpl searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
			indexReader = (IndexReaderImpl)searcher.getInsIndexReader();
			TestSearch.schemaSearcher = searchService.getSchemaSearchable();
			TestSearch.searcher = searchService.getXFacetedSearchable();

			System.out.println("\n=== Searching " + dataSource + " ===");

			TestSearch.searchInstance(schemaFactory.createCatRelGraph()
					 .add(schemaFactory.createKeywordCategory(args[1]))
					 ,0);
		}
		

	}

}
