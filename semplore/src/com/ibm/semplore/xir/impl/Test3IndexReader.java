/**
 * 
 */
package com.ibm.semplore.xir.impl;

import java.io.File;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.impl.SearchFactoryImpl;
import com.ibm.semplore.search.impl.XFacetedSearchableImpl;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;
import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.IndexService.IndexType;

/**
 * @author linna
 * 
 */
public class Test3IndexReader {

	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();

	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	public static TermFactory termFactory = TermFactoryImpl.getInstance();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File path = new File("D:\\semplore\\index\\wordnet");
			File path1 = new File("D:\\semplore\\index\\wordnet\\instance");
			File path2 = new File("D:\\semplore\\index\\wordnet2\\instance");
			File path3 = new File("D:\\semplore\\index\\wordnet3\\instance");
			IndexReaderImpl indexReader1 = new IndexReaderImpl(path1
					.getAbsolutePath(), IndexType.Instance);
			IndexReaderImpl indexReader2 = new IndexReaderImpl(path2
					.getAbsolutePath(), IndexType.Instance);
			IndexReaderImpl indexReader3 = new IndexReaderImpl(path3
					.getAbsolutePath(), IndexType.Instance);

			String rel = "<http://www.w3.org/2006/03/wn/wn20/schema/containsWordSense>";
			String c_subj = "<http://www.w3.org/2006/03/wn/wn20/schema/AdverbSynset>";
			String c_obj = "<http://www.w3.org/2006/03/wn/wn20/schema/AdverbWordSense>";

			Properties config = new Properties();
			config.setProperty(Config.INDEX_PATH, path.getAbsolutePath());
			XFacetedSearchService searchService = searchFactory
					.getXFacetedSearchService(config);
			XFacetedSearchableImpl searcher = (XFacetedSearchableImpl) searchService
					.getXFacetedSearchable();

			DocPositionStream relationStream = indexReader1
					.getDocPositionStream(termFactory
							.createTermForObjects(schemaFactory
									.createRelation(Md5_BloomFilter_64bit
											.URItoID(rel))));
			DocStream subjectStream = indexReader2.getDocStream(termFactory
					.createTermForInstances(schemaFactory
							.createCategory(Md5_BloomFilter_64bit
									.URItoID(c_obj))));
			DocStream CobjStream = indexReader3
					.getDocStream(termFactory
							.createTermForSubjects(schemaFactory
									.createRelation(Md5_BloomFilter_64bit
											.URItoID(rel))));
			DocStream objectStream = indexReader3.getDocStream(termFactory
					.createTermForInstances(schemaFactory
							.createCategory(Md5_BloomFilter_64bit
									.URItoID(c_subj))));
			DocStream resultStream = searcher.getAUManager().massUnion_Score(
					relationStream, subjectStream, CobjStream, true, -1);
			resultStream = searcher.getAUManager().binaryInter_Score(resultStream,
					objectStream, true, -1);
			DebugIndex.printDocStream(indexReader3, resultStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
