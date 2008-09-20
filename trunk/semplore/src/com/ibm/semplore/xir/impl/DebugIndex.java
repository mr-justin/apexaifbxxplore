package com.ibm.semplore.xir.impl;

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
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.TermFactory;

public class DebugIndex {

	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();

	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
	
	public static TermFactory termFactory = TermFactoryImpl.getInstance();

	public static void printDocPositionStream(IndexReaderImpl indexReader, DocPositionStream stream) throws Exception {
		stream.init();
		System.out.println("length: "+stream.getLen());
		for (int i=0; i<stream.getLen(); stream.next(),i++) {
//			if (i>=200)
//				break;
			String id = indexReader.reader.document(stream.doc()).getField(FieldType.URI.toString()).stringValue();
			String hash = indexReader.reader.document(stream.doc()).getField(FieldType.ID.toString()).stringValue();
			System.out.print(stream.doc()+"("+id+","+hash + "): ");
			while (stream.hasNextPosition()) {
				System.out.print(stream.nextPosition()+" ");
			}
			System.out.println();
		}
		System.out.println();		
	}

	public static void printDocStream(IndexReaderImpl indexReader, DocStream stream) throws Exception {
		stream.init();
		System.out.println("length: "+stream.getLen());
		for (int i=0; i<stream.getLen(); stream.next(),i++) {
//			if (i>=10)
//				break;
			String id = indexReader.reader.document(stream.doc()).getField(FieldType.URI.toString()).stringValue();
			System.out.println(stream.doc()+"("+id+"): ");
		}
		System.out.println();		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Properties config = Config.readConfigFile(args[0]);
			XFacetedSearchService searchService = searchFactory
					.getXFacetedSearchService(config);
			XFacetedSearchableImpl searcher = (XFacetedSearchableImpl)searchService.getXFacetedSearchable();
			IndexReaderImpl indexReader = (IndexReaderImpl)searcher.getInsIndexReader();
			
			System.out.println(String.format("relations: %d, categories: %d",
					indexReader.getDocStream(termFactory.createTermForRootRelations()).getLen(),
					indexReader.getDocStream(termFactory.createTermForRootCategories()).getLen()));

			if (args.length==2) {
				int doc = Integer.parseInt(args[1]);
				System.out.println(indexReader.reader.document(doc));
			} else
			for (int i=0; i<5; i++) {
				System.out.println("\nDocument #" + i);
				System.out.println(indexReader.reader.document(i));
			}
			
//			printDocPositionStream(indexReader, indexReader.getDocPositionStream(termFactory.createTerm(FieldType.CATEGORIES.toString(),FieldType.CATEGORIES)));
//			printDocStream(indexReader, indexReader.getDocStream(termFactory.createTermForRootRelations()));
//			printDocPositionStream(indexReader, indexReader.getDocPositionStream(termFactory.createTermForSubjects(schemaFactory.createRelation(Md5_BloomFilter_64bit.URItoID("<http://dbpedia.org/property/workInstitutions>")))));
//			printDocStream(indexReader, indexReader.getDocStream(termFactory.createTermForInstances(schemaFactory.createKeywordCategory("princeton"))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
