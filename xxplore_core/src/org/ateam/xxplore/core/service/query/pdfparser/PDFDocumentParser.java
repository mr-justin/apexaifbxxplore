package org.ateam.xxplore.core.service.query.pdfparser;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.pdfbox.searchengine.lucene.LucenePDFDocument;


public class PDFDocumentParser {

	/**
	 * @param args
	 * @return 
	 */
	public static Document getLuceneDocument(File f){
		// TODO Auto-generated method stub

		Document doc = null;
		try {
			doc = LucenePDFDocument.getDocument(f);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;

	}

}
