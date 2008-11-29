package org.team.xxplore.core.service.q2semantic;

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

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Building KeywordIndex from graph and nt File
 * @author kaifengxu
 *
 */
public class KeywordIndexBuilder{
	/* Constant */
	private static final String TYPE_FIELD = "type";
	private static final String CONCEPT = "concept";
	private static final String LITERAL = "literal";
	private static final String OBJECTPROPERTY = "objectproperty";
	private static final String DATAPROPERTY = "dataproperty";
	
	private static final String LABEL_FIELD =  "label";
	private static final String URI_FIELD =  "uri";
	private static final String DS_FIELD =  "ds";
	
	private static final String CONCEPT_FIELD = "concept_field";
	private static final String ATTRIBUTE_FIELD = "attribute_field";
	private static final String LITERAL_FIELD = "literal_field";
	
	
	public final String rootPath = "testbuilder";
	public final String conceptFile = "concept.txt";
	public final String relationFile = "relation.txt";
	public final String attributeFile = "attrbute.txt";
	public final String literalOut = "literal.txt";
	public final String litAttrout = "statement.txt";
	public final String keywordIndexDir = "keywordIndex";
	
	
	public static void main(String[] args) {
		KeywordIndexBuilder builder = new KeywordIndexBuilder();
		builder.indexKeywords("testbuilder/freebase.nt","test");
		
	}

	/**
	 * index concept property literal individual
	 * @param ntFn
	 * @param datasourceURI
	 * @param schemaGraph
	 * @param synIndexdir
	 */
	public void indexKeywords(String ntFn,String ds) {
		File indexDir = new File(this.rootPath + "/" + keywordIndexDir);
		if (!indexDir.exists()) {
			indexDir.mkdirs();
		}
		try {
			StandardAnalyzer analyzer = new StandardAnalyzer();
			IndexWriter indexWriter = new IndexWriter(indexDir, analyzer,true);
//			indexSchema(indexWriter, ds, this.rootPath + "/" + this.conceptFile, CONCEPT);
//			indexSchema(indexWriter, ds, this.rootPath + "/" + this.attributeFile, DATAPROPERTY);
//			indexSchema(indexWriter, ds, this.rootPath + "/" + this.relationFile, OBJECTPROPERTY);
			
			indexLiteral(indexWriter, ntFn, ds, this.rootPath + "/" + this.literalOut, this.rootPath + "/" + this.litAttrout);
			indexWriter.optimize();
			indexWriter.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	protected  void indexSchema(IndexWriter indexWriter, String ds,String file,String type){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String uri = line;
				String label = SummaryGraphUtil.getLocalName(uri);
				label = label.toLowerCase();
				/* Write Index */
				Document doc = new Document();
				doc.add(new Field(TYPE_FIELD, type,	Field.Store.YES, Field.Index.NO));
				doc.add(new Field(LABEL_FIELD, label, Field.Store.YES,Field.Index.TOKENIZED));
				doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
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
				doc.add(new Field(TYPE_FIELD, LITERAL,	Field.Store.YES, Field.Index.NO));
				System.out.println(literal);
				doc.add(new Field(LABEL_FIELD, literal, Field.Store.YES,Field.Index.TOKENIZED));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
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
				doc.add(new Field(LITERAL_FIELD, literal, Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(CONCEPT_FIELD, concept,	Field.Store.YES, Field.Index.NO));
				doc.add(new Field(ATTRIBUTE_FIELD, attribute, Field.Store.YES,Field.Index.UN_TOKENIZED));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
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
				if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2) continue;
				cur = part[0];
				String predicate = part[1];
				String object = part[2];
				
				if(object.length() == 0) {
					System.out.println(line);
					continue;
				}
		
				if(predicate.equals("<" + RDF.type.getURI() + ">")) {
					if(!object.equals("<" + RDFS.Class.getURI() + ">") &&
							!object.equals("<" + OWL.Class.getURI() + ">") &&
							!object.equals("<" + OWL.DatatypeProperty.getURI() + ">") &&
							!object.equals("<" + OWL.ObjectProperty.getURI() + ">") &&
							!object.equals("<" + RDFS.Datatype.getURI() + ">") &&
							!object.equals("<" + RDFS.Literal.getURI() + ">")) {
						type = INSTANCE;
						concept.add(object.substring(1, object.length() -1));
					}
					
				}
				
				
				if(!predicate.equals("<" + RDFS.subClassOf.getURI() + ">") &&
						!predicate.equals("<" + RDFS.subPropertyOf.getURI() + ">") && 
						!predicate.equals("<" + RDFS.domain.getURI() + ">") &&
						!predicate.equals("<" + RDFS.range.getURI() + ">") ) {
					if(object.charAt(0) != '<') {
						literal.add(new LitAttr(object,predicate));
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
