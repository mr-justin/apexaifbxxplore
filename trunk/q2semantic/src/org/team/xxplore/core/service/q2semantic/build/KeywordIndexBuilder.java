package org.team.xxplore.core.service.q2semantic.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.team.xxplore.core.service.q2semantic.LineSortFile;
import org.team.xxplore.core.service.q2semantic.SummaryGraphUtil;


/**
 * Building KeywordIndex from graph and nt File
 * @author kaifengxu
 *
 */
public class KeywordIndexBuilder{

	Parameters para;
	
	public static void main(String[] args) {
		KeywordIndexBuilder builder = new KeywordIndexBuilder();
		builder.indexKeywords("testbuilder/freebase.nt","test");
	}
	
	public KeywordIndexBuilder(){
		para = Parameters.getParameters();
	}

	/**
	 * index concept property literal individual
	 * @param ntFn
	 * @param datasourceURI
	 * @param schemaGraph
	 * @param synIndexdir
	 */
	public void indexKeywords(String ntFn,String ds) {
		File indexDir = new File(para.keywordIndex);
		if (!indexDir.exists()) {
			indexDir.mkdirs();
		}
		try {
			StandardAnalyzer analyzer = new StandardAnalyzer();
			IndexWriter indexWriter = new IndexWriter(indexDir, analyzer,true);
			indexSchema(indexWriter, ds, para.conceptFile, para.CONCEPT,para.concept_boost);
			indexSchema(indexWriter, ds, para.attributeFile, para.DATATYPEPROP,para.attribute_boost);
			indexSchema(indexWriter, ds, para.relationFile, para.OBJECTPROP,para.relation_boost);			
			indexLiteral(indexWriter, ntFn, ds, para.literalOut, para.litAttrOut);
			indexWriter.optimize();
			indexWriter.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	protected  void indexSchema(IndexWriter indexWriter, String ds,String file,String type,float boost){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String tokens [] = line.split("\t");
				String uri,label;
				if(tokens.length == 1) {
					uri = line;
					label = SummaryGraphUtil.getLocalName(uri); 
				}
				else {
					uri = tokens[0];
					label = tokens[1];
				}
				
				
				label = label.toLowerCase();
				/* Write Index */
				Document doc = new Document();
				doc.add(new Field(para.TYPE_FIELD, type,	Field.Store.YES, Field.Index.NO));
				doc.add(new Field(para.LABEL_FIELD, label.trim(), Field.Store.YES,Field.Index.TOKENIZED));
				doc.add(new Field(para.URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(para.DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				doc.setBoost(boost);
				indexWriter.addDocument(doc);
			}
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class LitAttr {
		public String lit;
		public String attr;
		public LitAttr(String lit, String attr) {
			this.lit = lit;
			this.attr = attr;
		}
	}
	
	private void handleBlock(Set<String> concept,Set<LitAttr> literal,String ds,PrintWriter w1,PrintWriter w2) {
		try {
			for(LitAttr lit : literal) {
				w1.println(lit.lit + "\t" + ds);
				for(String con : concept) {
					w2.println(lit.lit + "\t" + lit.attr + "\t" + con + "\t" + ds);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void indexLiteral(IndexWriter writer,String file1,String file2) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file1));
			String line;
			while((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String literal = tokens[0];
				String ds = tokens[1];
				Document doc = new Document();
				doc.add(new Field(para.TYPE_FIELD, para.LITERAL,	Field.Store.YES, Field.Index.NO));
//				System.out.println(literal);
				doc.add(new Field(para.LABEL_FIELD, literal.trim(), Field.Store.YES,Field.Index.TOKENIZED));
				doc.add(new Field(para.DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				writer.addDocument(doc);
			}
			br.close();
			
			br = new BufferedReader(new FileReader(file2));
			while((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String literal = tokens[0];
				String attribute = tokens[1];
				String concept = tokens[2];
				String ds = tokens[3];
				
				Document doc = new Document();
				doc.add(new Field(para.LITERAL_FIELD, literal, Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(para.CONCEPT_FIELD, concept,	Field.Store.YES, Field.Index.NO));
				doc.add(new Field(para.ATTRIBUTE_FIELD, SummaryGraphUtil.getLocalName(attribute).toLowerCase(), Field.Store.YES,Field.Index.UN_TOKENIZED));
				doc.add(new Field(para.ATTRIBUTE_FIELD_URI, attribute, Field.Store.YES,Field.Index.NO));
				doc.add(new Field(para.DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				writer.addDocument(doc);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void indexLiteral(IndexWriter writer,String ntFile,String ds,String file1,String file2){
		try {
			PrintWriter w1 = new PrintWriter(file1);
			PrintWriter w2 = new PrintWriter(file2);
	
			String cur=null, pre=null;
			
			final int INSTANCE = 0;
			final int CONCEPT = 1;
			
			BufferedReader br = new BufferedReader(new FileReader(ntFile));
			String line;
			
			int type = CONCEPT;
			Set<String> concept = new HashSet<String>();		
			Set<LitAttr> literal = new HashSet<LitAttr>();
			
			int count = 0;
			while(true)	{
			
				count ++;
				if(count % 10000 == 0) System.out.println(count);
				
				line = br.readLine();
				
				if(line == null) {
					if(type == INSTANCE) {
						this.handleBlock(concept,literal,ds,w1,w2);
					}
					break;
				}
				String[] part = Util4NT.processTripleLine(line);
				if(part==null || part[0].length()<2 || part[1].length()<2) continue;
				cur = part[0];
				String predicate = part[1];
				String object = part[2];
				
				if(object.length() == 0) {
					System.out.println(line);
					continue;
				}
		
				if(predicate.equals("<" + RDF.TYPE.stringValue() + ">")) {
					if(!object.equals("<" + RDFS.CLASS.stringValue() + ">") &&
							!object.equals("<" + OWL.CLASS.stringValue() + ">") &&
							!object.equals("<" + OWL.DATATYPEPROPERTY.stringValue() + ">") &&
							!object.equals("<" + OWL.DATATYPEPROPERTY.stringValue() + ">") &&
							!object.equals("<" + RDFS.DATATYPE.stringValue() + ">") &&
							!object.equals("<" + RDFS.LITERAL.stringValue() + ">")) {
						type = INSTANCE;
						concept.add(object.substring(1, object.length() -1));
					}
					
				}
				
				
				if(!predicate.equals("<" + RDFS.SUBCLASSOF.stringValue() + ">") &&
						!predicate.equals("<" + RDFS.SUBPROPERTYOF.stringValue() + ">") && 
						!predicate.equals("<" + RDFS.DOMAIN.stringValue() + ">") &&
						!predicate.equals("<" + RDFS.RANGE.stringValue() + ">") ) {
					if(object.charAt(0) != '<') {
						literal.add(new LitAttr(object,predicate.substring(1,predicate.length() - 1)));
					}
				}
				
				// end of the block
				if(pre != null && !pre.equals(cur)) {
					if(type == INSTANCE) {
						this.handleBlock(concept,literal,ds,w1,w2);
					}
					concept = new HashSet<String>();
					literal = new HashSet<LitAttr>();
					type = CONCEPT;
				}
				pre = cur;
			}
			
			br.close();
			w1.close();
			w2.close();
			
			LineSortFile sortFile = new LineSortFile(file1);
			sortFile.setDeleteWhenStringRepeated(true);
			sortFile.sortFile();
			sortFile = new LineSortFile(file2);
			sortFile.setDeleteWhenStringRepeated(true);
			sortFile.sortFile();
			
			this.indexLiteral(writer, file1, file2);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
