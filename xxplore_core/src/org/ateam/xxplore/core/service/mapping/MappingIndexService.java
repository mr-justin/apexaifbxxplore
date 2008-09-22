package org.ateam.xxplore.core.service.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class MappingIndexService {

	private static Logger s_log = Logger.getLogger(MappingIndexService.class);

	private static final String SOURCE_FIELD = "source";
	private static final String TARGET_FIELD = "target";
	private static final String SOURCE_DS_FIELD = "sourceDS";
	private static final String TARGET_DS_FIELD = "sourceDS";
	private static final String CONFIDENCE_FIELD = "conf";
	private static final String MAPPING_FIELD = "mapping";

	public static final String SEARCH_SOURCE_DS_ONLY  = "sourceOnly";
	public static final String SEARCH_TARGET_DS_ONLY  = "targetOnly";
	public static final String SEARCH_TARGET_AND_SOURCE_DS = "both";
	
	public static final int SEARCH_SOURCE = 0;
	public static final int SEARCH_TARGET = 1;

	private String m_indexDir = null; 

	private IndexWriter m_writer;
	private StandardAnalyzer m_analyzer;
	private IndexSearcher m_searcher;


	public MappingIndexService(String mappingIndexDir){
		try {
			m_analyzer = new StandardAnalyzer();
			m_indexDir = mappingIndexDir;
			File indexDir = new File(mappingIndexDir);
			if (!indexDir.exists())
				indexDir.mkdirs();
			m_writer = new IndexWriter(indexDir, m_analyzer, true);
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
		doc.add(new Field(SOURCE_FIELD, mapping.getSource(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field(TARGET_FIELD, mapping.getTarget(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field(CONFIDENCE_FIELD, String.valueOf(mapping.getConfidence()), Field.Store.YES, Field.Index.NO));
		doc.add(new Field(SOURCE_DS_FIELD, mapping.getSourceDsURI(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field(TARGET_DS_FIELD, mapping.getTargetDsURI(), Field.Store.YES, Field.Index.UN_TOKENIZED));

		if (mapping instanceof InstanceMapping){
			doc.add(new Field(MAPPING_FIELD, ((InstanceMapping)mapping).getMapping().toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		}
		try{
			m_writer.addDocument(doc);

			m_writer.optimize();
			m_writer.close();
		} catch (Exception e){
			e.printStackTrace();
		}

	}
	public Collection<Mapping> searchMappingsForSource(String URI, String dsURI, int type) throws Exception
	{
		ArrayList<Mapping> res = new ArrayList<Mapping>();
		Query query = new BooleanQuery();
		if(type == MappingIndexService.SEARCH_SOURCE)
		{
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(SOURCE_DS_FIELD, dsURI)), BooleanClause.Occur.MUST));
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(SOURCE_FIELD, URI)), BooleanClause.Occur.MUST));
		}
		else if(type == MappingIndexService.SEARCH_TARGET)
		{
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(TARGET_DS_FIELD, dsURI)), BooleanClause.Occur.MUST));
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(TARGET_FIELD, URI)), BooleanClause.Occur.MUST));
		}
		Hits hits = m_searcher.search(query);
		if((hits != null) && (hits.length() > 0)){
			for(int i = 0; i < hits.length(); i++){
				Document doc = hits.doc(i);
				String source = doc.get(SOURCE_FIELD);
				String target = doc.get(TARGET_FIELD);
				String targetDS  = doc.get(TARGET_DS_FIELD);
				String sourceDS = doc.get(SOURCE_DS_FIELD);
				double conf = Double.parseDouble(doc.get(CONFIDENCE_FIELD));
				res.add(new SchemaMapping(source, target, sourceDS, targetDS, conf));
			}
		}
		return res;
	}
	
	public Collection<Mapping> searchMappingsForDS(String dsURI, String type){
		Emergency.checkPrecondition(type == SEARCH_SOURCE_DS_ONLY || type == SEARCH_TARGET_DS_ONLY  || 
				type == SEARCH_TARGET_AND_SOURCE_DS, "type == SEARCH_SOURCE_DS_ONLY || type == SEARCH_TARGET_DS_ONLY  ||" + 
				"type == SEARCH_TARGET_AND_SOURCE_DS");
		ArrayList<Mapping> res = new ArrayList<Mapping>();
		Query q = null;
		try {
			if (m_searcher == null){
				s_log.debug("Open index " + m_indexDir + " and init kb searcher!");
				m_searcher = new IndexSearcher(m_indexDir);
			}
			if(type == SEARCH_SOURCE_DS_ONLY){
				q = new TermQuery(new Term(SOURCE_DS_FIELD, dsURI));
			}
			else if (type == SEARCH_TARGET_DS_ONLY){
				q = new TermQuery(new Term(TARGET_DS_FIELD, dsURI));
			}

			else if (type == SEARCH_TARGET_AND_SOURCE_DS){
				q = new BooleanQuery();
				((BooleanQuery)q).add(new BooleanClause(new TermQuery(new Term(SOURCE_DS_FIELD, dsURI)), BooleanClause.Occur.SHOULD));
				((BooleanQuery)q).add(new BooleanClause(new TermQuery(new Term(TARGET_DS_FIELD, dsURI)), BooleanClause.Occur.SHOULD));
			}

			//search

			Hits hits = m_searcher.search(q);

			if((hits != null) && (hits.length() > 0)){
				for(int i = 0; i < hits.length(); i++){
					Document doc = hits.doc(i);
					float score = hits.score(i);
					if(score >= 0.9){
						if(doc != null){
							String source = doc.get(SOURCE_FIELD);
							String target = doc.get(SOURCE_FIELD);
							String targetDS  = doc.get(TARGET_DS_FIELD);
							String sourceDS = doc.get(SOURCE_DS_FIELD);
							double conf = -1;
							if (doc.get(CONFIDENCE_FIELD) != null) 
								conf = Double.valueOf(doc.get(CONFIDENCE_FIELD));
							
							String mapping = doc.get(MAPPING_FIELD);
							
							//is an instance mapping
							if (mapping != null){
								res.add(new InstanceMapping(source, target, sourceDS, targetDS, 
										SchemaMapping.getMappingFromString(mapping), conf)); 
							}
							//is a schema mapping
							else {
								res.add(new SchemaMapping(source, target, sourceDS, targetDS, conf));

							}
							Emergency.checkPostcondition(source != null && target != null && sourceDS != null &&
									targetDS != null && conf != -1, "source != null && target != null, sourceDS != null &&" +
									"targetDS != null && conf != -1"); 
							
						}
					}
				}
			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return res;
	}


//	public MappingIndexService(String mappingFile, String mappingIndexDir, String keywordIndexDir, boolean create) {
//	try {
//	br = new BufferedReader(new FileReader(mappingFile));
//	String line = br.readLine();
//	String[] datasources = line.split(";");
//	if(datasources.length == 2){
//	datasource1 = datasources[0];
//	datasource2 = datasources[1];
//	} 
//	indexSearcher = new IndexSearcher(keywordIndexDir);
//	m_analyzer = new StandardAnalyzer();
//	File indexDir = new File(mappingIndexDir);
//	if (!indexDir.exists())
//	indexDir.mkdirs();
//	indexWriter = new IndexWriter(indexDir, m_analyzer, create);
//	} catch (FileNotFoundException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//	} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//	}
//	} 

//	public MappingIndexService(String mappingFile, String mappingIndexDir, String datasource1, String datasource2, String keywordIndexDir, boolean create) {
//	this.datasource1 = datasource1;
//	this.datasource2 = datasource2;
//	try {
//	br = new BufferedReader(new FileReader(mappingFile));
//	indexSearcher = new IndexSearcher(keywordIndexDir);
//	m_analyzer = new StandardAnalyzer();
//	File indexDir = new File(mappingIndexDir);
//	if (!indexDir.exists())
//	indexDir.mkdirs();
//	indexWriter = new IndexWriter(indexDir, m_analyzer, create);
//	} catch (FileNotFoundException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//	} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//	}
//	} 

//	public void indexMappings() {
//	String line;
//	int i = 0;
//	try {
//	while((line = br.readLine()) != null) {
//	String[] mapping = line.split(";");
//	System.out.println(++i);
//	if (mapping.length == 3) {

//	Set<String> concepts1 = new HashSet<String>();
//	Term term1 = new Term("label", mapping[0]);
//	TermQuery termQuery1 = new TermQuery(term1);
//	Hits results1 = indexSearcher.search(termQuery1);
//	if((results1 != null) && (results1.length() > 0)){
//	for(int j = 0; j < results1.length(); j++){
//	Document docu = results1.doc(j);
//	if(docu.get("type").equals("indiviudal"))
//	concepts1.addAll(Arrays.asList(docu.getValues("concept")));
//	}
//	}

//	Set<String> concepts2 = new HashSet<String>();
//	Term term2 = new Term("label", mapping[1]);
//	TermQuery termQuery2 = new TermQuery(term2);
//	Hits results2 = indexSearcher.search(termQuery2);
//	if((results2 != null) && (results2.length() > 0)){
//	for(int j = 0; j < results2.length(); j++){
//	Document docu = results2.doc(j);
//	if(docu.get("type").equals("indiviudal"))
//	concepts2.addAll(Arrays.asList(docu.getValues("concept")));
//	}
//	}

//	if(concepts1 != null && concepts2 != null && concepts1.size() != 0 && concepts2.size() != 0){
//	Document doc = new Document();
//	doc.add(new Field("uri1", mapping[0], Field.Store.YES, Field.Index.UN_TOKENIZED));
//	doc.add(new Field("uri2", mapping[1], Field.Store.YES, Field.Index.UN_TOKENIZED));
//	doc.add(new Field("confidence", mapping[2], Field.Store.YES, Field.Index.NO));
//	doc.add(new Field("datasource", datasource1, Field.Store.YES, Field.Index.UN_TOKENIZED));
//	doc.add(new Field("datasource", datasource2, Field.Store.YES, Field.Index.UN_TOKENIZED));
//	for(String con1 : concepts1){
//	doc.add(new Field("concept1", con1, Field.Store.YES, Field.Index.UN_TOKENIZED));
//	}
//	for(String con2 : concepts2){
//	doc.add(new Field("concept2", con2, Field.Store.YES, Field.Index.UN_TOKENIZED));
//	}
//	indexWriter.addDocument(doc);
//	}
//	else {
//	Document doc = new Document();
//	doc.add(new Field("uri1", mapping[0], Field.Store.YES, Field.Index.UN_TOKENIZED));
//	doc.add(new Field("uri2", mapping[1], Field.Store.YES, Field.Index.UN_TOKENIZED));
//	doc.add(new Field("confidence", mapping[2], Field.Store.YES, Field.Index.NO));
//	doc.add(new Field("datasource", datasource1, Field.Store.YES, Field.Index.UN_TOKENIZED));
//	doc.add(new Field("datasource", datasource2, Field.Store.YES, Field.Index.UN_TOKENIZED));
//	indexWriter.addDocument(doc);
//	}

//	}
//	}

//	indexWriter.optimize();
//	indexWriter.close();
//	indexSearcher.close();
//	br.close();
//	m_analyzer = null;
//	} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//	}
//	}

	public void disposeService() {
		try {
			m_searcher.close();
			m_writer = null;
			m_analyzer = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
