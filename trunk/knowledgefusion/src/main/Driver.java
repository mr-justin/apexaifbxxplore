package main;

import java.util.HashSet;
import java.util.List;

import main.Indexer.IFeatureFilter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;

/**
 * Illustrate the whole workflow
 * @author Linyun Fu
 *
 */
public class Driver {

	public static String workFolder = "/usr/fulinyun/";
	public static String dbpedia = "/usr/fulinyun/dbpedia-v3.nt.tar.gz";
	public static String refIndexFolder = workFolder+"refIndex/";
	public static String basicFeatureIndexFolder = workFolder+"basicFeatureIndex/";
	
	public static void main(String[] args) throws Exception {
		// preprocessing
		Formatter.clean(dbpedia, workFolder+"temp.gz");
		Formatter.removeEmptyLines(workFolder+"temp.gz", workFolder+"dbpedia.gz");
		
		// indexing
		Indexer.preprocess(workFolder+"dbpedia.gz", workFolder+"dbpediaDump.txt");
		// run in command line: sort -S 512m -T . dbpediaDump.txt | gzip > dbpediaPreprocessed.gz
		Indexer.refIndexFromPreprocessed(KeyIndDealer.domainDBpedia, 
				workFolder+"dbpediaPreprocessed.gz", refIndexFolder);
		Indexer.basicFeatureIndex(refIndexFolder, basicFeatureIndexFolder);
		final IndexReader refIndexReader = IndexReader.open(basicFeatureIndexFolder);
		Indexer.dumpFeature(basicFeatureIndexFolder, "basic", new IFeatureFilter() {
			public boolean accept(int docNum, String feature) {
				return isIndividual(refIndexReader, docNum) && feature.length() != 0;
			}
			
			/**
			 * is ireader2.document(i) individual?
			 * @param ireader2
			 * @param i
			 * @return
			 */
			private boolean isIndividual(IndexReader ireader2, int i) {
				try {
					Document doc = ireader2.document(i);
					List fieldList = doc.getFields();
					for (Object o : fieldList) {
						Field f = (Field)o;
						String fn = f.name();
						if (fn.equals(Common.rdfType+"from") || fn.equals(Common.owlClass+"from") || 
								fn.equals(Common.dbpediaSubject+"from")) {
							return false;
						}
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

		}, workFolder+"dbpediaBasicFeatures.txt");
		
		// blocking
		Blocker.canonicalize(workFolder+"dbpediaBasicFeatures.txt", basicFeatureIndexFolder, 
				workFolder+"dbpediaBasicFeaturesCaned.txt");
		Blocker.prefixBlockingWithLucene(workFolder+"dbpediaBasicFeaturesCaned.txt", 0.2f, 100, 1000, 
				basicFeatureIndexFolder, workFolder+"dbpediaBlocks.txt", workFolder+"report.txt");
		
		// clustering
		Clusterer.cluster(workFolder+"dbpediaBlocks.txt", workFolder+"dbpediaClusters.txt", 2, 1.5f, 100, 
				new ISimCal() {
			public float distance(String[][] features, int i, int j) {
				return jaccard(features, i, j);
			}
		});
	}
	
	private static float jaccard(String[][] features, int i, int j) {
		String[] r1 = features[i];
		String[] r2 = features[j];
		HashSet<String> tokenSet = new HashSet<String>();
		for (String s : r1) tokenSet.add(s);
		for (String s : r2) tokenSet.add(s);
		int intersection = r1.length+r2.length-tokenSet.size();
		float ret = (float)(tokenSet.size()-intersection+0.0)/tokenSet.size();
		return ret;
	}

}
