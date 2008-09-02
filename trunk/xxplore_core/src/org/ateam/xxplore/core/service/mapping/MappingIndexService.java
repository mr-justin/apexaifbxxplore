package org.ateam.xxplore.core.service.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;

public class MappingIndexService {

	private static final String MAPPING_FILE = "res/BTC/sampling/mapping/mappingResult/schema.rdf+swrc.owl.mapping";
	private static final String MAPPING_INDEX_DIR = "res/BTC/sampling/mappingIndex";
	private static final String KEYWORD_INDEX_DIR = "res/BTC/sampling/keywordIndex";

	private IndexWriter indexWriter;
	private StandardAnalyzer m_analyzer;
	private IndexSearcher indexSearcher;

	private BufferedReader br;

	private String datasource1; 
	private String datasource2;

	public static void main(String[] args) {
		MappingIndexService service = new MappingIndexService(MAPPING_FILE, MAPPING_INDEX_DIR, KEYWORD_INDEX_DIR, true);
		service.indexMappings();
	}

	public MappingIndexService(String mappingIndexDir){
		try {
			m_analyzer = new StandardAnalyzer();
			File indexDir = new File(mappingIndexDir);
			if (!indexDir.exists())
				indexDir.mkdirs();
			indexWriter = new IndexWriter(indexDir, m_analyzer, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void indexMappings(Mapping mapping){

		Document doc = new Document();
		doc.add(new Field("source", mapping.getSource(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("target", mapping.getTarget(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("confidence", String.valueOf(mapping.getConfidence()), Field.Store.YES, Field.Index.NO));
		doc.add(new Field("sourceDS", mapping.getSourceDsURI(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field("targetDS", mapping.getTargetDsURI(), Field.Store.YES, Field.Index.UN_TOKENIZED));

		if (mapping instanceof InstanceMapping){
			doc.add(new Field("mapping", ((InstanceMapping)mapping).getMapping().toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		}
		try{
			indexWriter.addDocument(doc);

			indexWriter.optimize();
			indexWriter.close();
			indexSearcher.close();
			br.close();
			m_analyzer = null;
		} catch (Exception e){
			e.printStackTrace();
		}

	}


	public MappingIndexService(String mappingFile, String mappingIndexDir, String keywordIndexDir, boolean create) {
		try {
			br = new BufferedReader(new FileReader(mappingFile));
			String line = br.readLine();
			String[] datasources = line.split(";");
			if(datasources.length == 2){
				datasource1 = datasources[0];
				datasource2 = datasources[1];
			} 
			indexSearcher = new IndexSearcher(keywordIndexDir);
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

	public MappingIndexService(String mappingFile, String mappingIndexDir, String datasource1, String datasource2, String keywordIndexDir, boolean create) {
		this.datasource1 = datasource1;
		this.datasource2 = datasource2;
		try {
			br = new BufferedReader(new FileReader(mappingFile));
			indexSearcher = new IndexSearcher(keywordIndexDir);
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

					Set<String> concepts1 = new HashSet<String>();
					Term term1 = new Term("label", mapping[0]);
					TermQuery termQuery1 = new TermQuery(term1);
					Hits results1 = indexSearcher.search(termQuery1);
					if((results1 != null) && (results1.length() > 0)){
						for(int j = 0; j < results1.length(); j++){
							Document docu = results1.doc(j);
							if(docu.get("type").equals("indiviudal"))
								concepts1.addAll(Arrays.asList(docu.getValues("concept")));
						}
					}

					Set<String> concepts2 = new HashSet<String>();
					Term term2 = new Term("label", mapping[1]);
					TermQuery termQuery2 = new TermQuery(term2);
					Hits results2 = indexSearcher.search(termQuery2);
					if((results2 != null) && (results2.length() > 0)){
						for(int j = 0; j < results2.length(); j++){
							Document docu = results2.doc(j);
							if(docu.get("type").equals("indiviudal"))
								concepts2.addAll(Arrays.asList(docu.getValues("concept")));
						}
					}

					if(concepts1 != null && concepts2 != null && concepts1.size() != 0 && concepts2.size() != 0){
						Document doc = new Document();
						doc.add(new Field("uri1", mapping[0], Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("uri2", mapping[1], Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("confidence", mapping[2], Field.Store.YES, Field.Index.NO));
						doc.add(new Field("datasource", datasource1, Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("datasource", datasource2, Field.Store.YES, Field.Index.UN_TOKENIZED));
						for(String con1 : concepts1){
							doc.add(new Field("concept1", con1, Field.Store.YES, Field.Index.UN_TOKENIZED));
						}
						for(String con2 : concepts2){
							doc.add(new Field("concept2", con2, Field.Store.YES, Field.Index.UN_TOKENIZED));
						}
						indexWriter.addDocument(doc);
					}
					else {
						Document doc = new Document();
						doc.add(new Field("uri1", mapping[0], Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("uri2", mapping[1], Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("confidence", mapping[2], Field.Store.YES, Field.Index.NO));
						doc.add(new Field("datasource", datasource1, Field.Store.YES, Field.Index.UN_TOKENIZED));
						doc.add(new Field("datasource", datasource2, Field.Store.YES, Field.Index.UN_TOKENIZED));
						indexWriter.addDocument(doc);
					}

				}
			}

			indexWriter.optimize();
			indexWriter.close();
			indexSearcher.close();
			br.close();
			m_analyzer = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
