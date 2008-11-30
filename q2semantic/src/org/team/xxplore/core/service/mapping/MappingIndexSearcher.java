package org.team.xxplore.core.service.mapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

public class MappingIndexSearcher {
	private static final String SOURCE_FIELD = "source";
	private static final String TARGET_FIELD = "target";
	private static final String SOURCE_DS_FIELD = "sourceDS";
	private static final String TARGET_DS_FIELD = "targetDS";
	private static final String CONFIDENCE_FIELD = "conf";
	private static final String MAPPING_FIELD = "mapping";

	public static final String SEARCH_SOURCE_DS_ONLY  = "sourceOnly";
	public static final String SEARCH_TARGET_DS_ONLY  = "targetOnly";
	public static final String SEARCH_TARGET_AND_SOURCE_DS = "both";
	
	public static final int SEARCH_SOURCE = 0;
	public static final int SEARCH_TARGET = 1;
	
	private boolean dirExist = false;
	
	private IndexSearcher m_searcher;
	
	public MappingIndexSearcher(String mappingIndexDir) {
		dirExist = new File(mappingIndexDir).exists();
		if(dirExist) {
			try {
				m_searcher = new IndexSearcher(mappingIndexDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Collection<Mapping> searchMappings(String URI, String dsURI, int type) throws Exception {		
		ArrayList<Mapping> res = new ArrayList<Mapping>();
		if(!dirExist) return res;

		Query query = new BooleanQuery();
		if(type == MappingIndexSearcher.SEARCH_SOURCE)
		{
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(SOURCE_DS_FIELD, dsURI)), BooleanClause.Occur.MUST));
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(SOURCE_FIELD, URI)), BooleanClause.Occur.MUST));
		}
		else if(type == MappingIndexSearcher.SEARCH_TARGET)
		{
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(TARGET_DS_FIELD, dsURI)), BooleanClause.Occur.MUST));
			((BooleanQuery)query).add(new BooleanClause(new TermQuery(new Term(TARGET_FIELD, URI)), BooleanClause.Occur.MUST));
		}

		Hits hits = m_searcher.search(query);
		System.out.println(hits.length());
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
		ArrayList<Mapping> res = new ArrayList<Mapping>();
		if(!dirExist) {
			return res;
		}
		Query q = null;
		try {
			if(type.equals(SEARCH_SOURCE_DS_ONLY)){
				q = new TermQuery(new Term(SOURCE_DS_FIELD, dsURI));
			}
			else if (type.equals(SEARCH_TARGET_DS_ONLY)){
				q = new TermQuery(new Term(TARGET_DS_FIELD, dsURI));
			}

			else if (type.equals(SEARCH_TARGET_AND_SOURCE_DS)){
				q = new BooleanQuery();
				((BooleanQuery)q).add(new BooleanClause(new TermQuery(new Term(SOURCE_DS_FIELD, dsURI)), BooleanClause.Occur.SHOULD));
				((BooleanQuery)q).add(new BooleanClause(new TermQuery(new Term(TARGET_DS_FIELD, dsURI)), BooleanClause.Occur.SHOULD));
			}

			//search

			Hits hits = m_searcher.search(q);

			if((hits != null) && (hits.length() > 0)){
				for(int i = 0; i < hits.length(); i++){
					Document doc = hits.doc(i);
					if(doc != null){
						String source = doc.get(SOURCE_FIELD);

						String target = doc.get(TARGET_FIELD);
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
					}
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return res;
	}
}
