/**
 * 
 */
package sjtu.apex.q2semantic.search;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.xmedia.oms.query.OWLPredicate;

import sjtu.apex.q2semantic.index.IndexEnvironment;

/**
 * @author whfcarter
 *
 */
public class SearchService {

	/**
	 * @param args
	 * @throws RepositoryException 
	 * @throws MalformedQueryException 
	 * @throws QueryEvaluationException 
	 */
	public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		// TODO Auto-generated method stub
		LuceneQueryService qs = new LuceneQueryService(IndexEnvironment.TAP_FLAG);
		QueryIntepretationService qis = new QueryIntepretationService(IndexEnvironment.TAP_FLAG);
//		try {
//			IndexSearcher searcher = new IndexSearcher("tap_kb_index");
//			QueryParser parser = new QueryParser("literal", new StandardAnalyzer());
//			String query = "\"The Hungry Stones And Other Stories\"";
//			Query q = parser.parse(query);
//			Hits hits = searcher.search(q);
//			System.out.println(hits.length());
//		} catch (CorruptIndexException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Map<String, Collection<KbElement>> ress = qs.searchKb("label SVG");
		Collection<Collection<OWLPredicate>> queries = qis.computeQueries(ress, 0, 0);
//		System.out.println(queries.size());
		if (queries == null)
			System.exit(0);
		Iterator<Collection<OWLPredicate>> iter = queries.iterator();
		if (iter.hasNext()) {
			Collection<OWLPredicate> query = iter.next();
			String sparql = qis.generateSPARQL(query);

			Repository repository = new SailRepository(new NativeStore(
					new File(IndexEnvironment.TAP_REPO), IndexEnvironment.INDICES));
			repository.initialize();
			RepositoryConnection conn = repository.getConnection();
			System.out.println(sparql);
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
			TupleQueryResult result = tupleQuery.evaluate();
			int i = 1;
			while (result.hasNext()) {
				if (i == 10)
					break;
				System.out.println(i++);
			}
			result.close();
			conn.close();
		}
// catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
