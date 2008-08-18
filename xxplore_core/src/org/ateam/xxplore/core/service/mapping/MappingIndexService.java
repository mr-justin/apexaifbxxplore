package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public class MappingIndexService {
	
	private static final String MAPPING_FILE = "D:/BTC/sampling/mapping/opus_august2007.rdf+swrc_v0.7.owl.mapping";
	private static final String MAPPING_INDEX_DIR = "D:/BTC/sampling/mapping/index";
	
	private IndexWriter indexWriter;
	private StandardAnalyzer m_analyzer;
	
	private BufferedReader br;
	
	private String datasource1; 
	private String datasource2;
	
	public static void main(String[] args) {
		MappingIndexService service = new MappingIndexService(MAPPING_FILE, MAPPING_INDEX_DIR, true);
		service.indexMappings();
	}
	
	public MappingIndexService(String mappingFile, String mappingIndexDir, boolean create) {
		try {
			br = new BufferedReader(new FileReader(mappingFile));
			String line = br.readLine();
			String[] datasources = line.split(";");
			if(datasources.length == 2){
				datasource1 = datasources[0];
				datasource2 = datasources[1];
			} 
			m_analyzer = new StandardAnalyzer();
			File indexDir = new File(mappingIndexDir);
			if (!indexDir.exists())
				indexDir.mkdirs();
			indexWriter = new IndexWriter(indexDir, m_analyzer, create);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	public MappingIndexService(String mappingFile, String mappingIndexDir, String datasource1, String datasource2, boolean create) {
		this.datasource1 = datasource1;
		this.datasource2 = datasource2;
		try {
			br = new BufferedReader(new FileReader(mappingFile));
			m_analyzer = new StandardAnalyzer();
			File indexDir = new File(mappingIndexDir);
			if (!indexDir.exists())
				indexDir.mkdirs();
			indexWriter = new IndexWriter(indexDir, m_analyzer, create);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

	public void indexMappings() {
		String line;
		int i = 0;
		try {
			while((line = br.readLine()) != null) {
				String[] mapping = line.split(";");
				System.out.println(++i);
				if (mapping.length == 3) {
					Document doc = new Document();
					doc.add(new Field("uri1", mapping[0], Field.Store.YES, Field.Index.UN_TOKENIZED));
					doc.add(new Field("uri2", mapping[1], Field.Store.YES, Field.Index.UN_TOKENIZED));
					doc.add(new Field("confidence", mapping[2], Field.Store.YES, Field.Index.NO));
					doc.add(new Field("datasource", datasource1, Field.Store.YES, Field.Index.UN_TOKENIZED));
					doc.add(new Field("datasource", datasource2, Field.Store.YES, Field.Index.UN_TOKENIZED));
					indexWriter.addDocument(doc);
				}
			}
			
			indexWriter.optimize();
	        indexWriter.close();
			br.close();
			m_analyzer = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
