package org.team.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public class MappingIndexBuilder {
	private static final String SOURCE_FIELD = "source";
	private static final String TARGET_FIELD = "target";
	private static final String SOURCE_DS_FIELD = "sourceDS";
	private static final String TARGET_DS_FIELD = "targetDS";
	private static final String CONFIDENCE_FIELD = "conf";
	private static final String MAPPING_FIELD = "mapping";
		
	private IndexWriter m_writer;
	private String m_indexDir;
	private String m_filepath;
	
	public static void main(String[] args) {
		MappingIndexBuilder builder = new MappingIndexBuilder(args[1],args[0]);
		builder.createIndex();
	}
	
	public MappingIndexBuilder(String mappingIndexDir,String filepath) {
		m_indexDir = mappingIndexDir;
		m_filepath = filepath;
	}
	
	public void createIndex() {
		try {
			StandardAnalyzer analyzer = new StandardAnalyzer();
			File indexDir = new File(m_indexDir);
			if (!indexDir.exists()) {
				indexDir.mkdirs();
			}
			m_writer = new IndexWriter(indexDir, analyzer, true);
			
			BufferedReader br = new BufferedReader(new FileReader(m_filepath));
			
			String line = br.readLine();
			String[] tokens = line.split("\t");
			String ds1 = tokens[0];
			String ds2 = tokens[1];
			
			System.out.println("Data source 1 " + ds1);
			System.out.println("Data source 2 " + ds2);
			
			while((line = br.readLine()) != null) {
				tokens = line.split("\t");
				String concept1 = tokens[0];
				String concept2 = tokens[1];
				double confidence = Double.parseDouble(tokens[2]);
				Mapping mapping  = new SchemaMapping(concept1,concept2,ds1,ds2,confidence);
				this.indexMappings(mapping);
			}
			
			br.close();
			m_writer.optimize();
			m_writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void indexMappings(Mapping mapping){

		Document doc = new Document();
		doc.add(new Field(SOURCE_FIELD, mapping.getSource(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field(TARGET_FIELD, mapping.getTarget(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field(CONFIDENCE_FIELD, String.valueOf(mapping.getConfidence()), Field.Store.YES, Field.Index.NO));
		doc.add(new Field(SOURCE_DS_FIELD, mapping.getSourceDsURI(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		System.out.println(mapping.getSourceDsURI());
		doc.add(new Field(TARGET_DS_FIELD, mapping.getTargetDsURI(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		System.out.println(mapping.getTargetDsURI());
		
		if (mapping instanceof InstanceMapping){
			doc.add(new Field(MAPPING_FIELD, ((InstanceMapping)mapping).getMapping().toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		}
		try{
			m_writer.addDocument(doc);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
