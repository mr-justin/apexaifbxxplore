package org.aifb.xxplore.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;

import org.aifb.xxplore.misc.CommonSearchResultObservable;
import org.aifb.xxplore.misc.ScoredDocumentITupleContainer;
import org.aifb.xxplore.views.MergedResultsViewer;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.ITuple;

public class MergedViewContentProvider implements IStructuredContentProvider, Observer{
	private static Logger s_log = Logger.getLogger(MergedViewContentProvider.class);
	
	private boolean cleared;
	private CommonSearchResultObservable searchResultObservable = CommonSearchResultObservable.getInstance();
	private MergedResultsViewer viewer;
	
	private TreeMap<Float,ScoredDocumentITupleContainer> mergedResult;
	private float searchRelationRatio; 
	
	public MergedViewContentProvider(MergedResultsViewer viewer) {
		this.viewer = viewer;
		searchRelationRatio = 0.0f;
		searchResultObservable.addObserver(this);
	}

	public void dispose() {
		searchResultObservable.deleteObserver(this);
		viewer = null;
	}

	public Object[] getElements(Object o) {
		if (cleared) 
			return new Object[] {"Query Cleared."};
		
		if (mergedResult.isEmpty())
			return new Object[] {"No Result found!"};
		
		Object[] ret = new Object[mergedResult.size()+1];
		
		ret[0] = "Results for f(x) = " + (1-searchRelationRatio) + "*inFacts(x) + " + searchRelationRatio + "docScore(x):";

		for (int i=1; !mergedResult.isEmpty(); i++) {
			Object nextHighestKey = mergedResult.lastKey();
			ret[i] = mergedResult.get(nextHighestKey);
			mergedResult.remove(nextHighestKey);
		}
		
		return ret;
	}

	
	
	public float getSearchRelationRatio() {
		return searchRelationRatio;
	}

	public void setSearchRelationRatio(float searchRelationRatio) {
		this.searchRelationRatio = searchRelationRatio;
	}

	public void update(Observable obserable, Object event) {
		if ((Integer)event == ExploreEnvironment.CLEAR ) {
			cleared = true;
			viewer.refresh();
			return;
		}
		cleared = false;
		
		/* get factResult and hash ITuples by compareableURI */
		IQueryResult factQueryResult = searchResultObservable.getFactResult();
		
		HashMap<String, ITuple> inFactResultOnlyHM;
		if (factQueryResult != null) {
			Set<ITuple> factResult = factQueryResult.getResult();
			
			inFactResultOnlyHM = new HashMap<String, ITuple>(factResult.size()/2*3);
			
			Iterator factIter = factResult.iterator();
			while (factIter.hasNext()){
				ITuple tuple = (ITuple) factIter.next();
				inFactResultOnlyHM.put(getURI(tuple), tuple);
			}
		} else {
			inFactResultOnlyHM = new HashMap<String, ITuple>(1);
		}
		
		/* the following part implements the calculation for merging
		 * the provided funktion is
		 * f(x) = (1-c) * inFactSearchResult(x) + c * docScore(x)
		 * docScore(x) is the score provided by lucene / max{allScores from lucene}
		 * inFactSearchResult(x) = 1 if x is a fact, 0 otherwise 
		 * the document with the highest f(x) value is the best
		 * 
		 * after calculating the score put all elements into a SortedMap (TreeMap)
		 */

		mergedResult = new TreeMap<Float, ScoredDocumentITupleContainer>();
		try {
			/* get documentResult and use higest score as compareValue. */
			Hits hits = searchResultObservable.getDocumentResult();
			float compareValue = hits.score(0);
			
			/* iterate thru all docs from docsearch and calclate new scores, this is where the magic happens! ;) */
			Iterator<Hit> docIter = hits.iterator();
			while (docIter.hasNext()) {
				ScoredDocumentITupleContainer container;
				
				Hit hit = docIter.next();
				float score = hit.getScore() / compareValue;
				
				Document document = hit.getDocument();
				String uri = getURI(document);
				ITuple tuple = inFactResultOnlyHM.get( uri  );
				if (tuple != null) { /*this is where the document is refered in both searches*/
					score = (searchRelationRatio * score) /*score for docSearch*/ 
						+ ((1-searchRelationRatio) ); /*+ score for factSearch*/
					container = new ScoredDocumentITupleContainer(tuple, document, score);
					inFactResultOnlyHM.remove(uri);
				} else { /*this is where only documents in docsearch occour*/
					score = searchRelationRatio * score; /*only score for docSearch*/
					container = new ScoredDocumentITupleContainer(null, document, score);
				}
				mergedResult.put(container.getScore(), container);
			}
			
			/*after removing all facts found in both, score all left tuples*/
			Iterator<ITuple> tupleIter = inFactResultOnlyHM.values().iterator();
			while (tupleIter.hasNext()) {/*this is where only factsearch refered documents are contained*/
				ITuple tup = tupleIter.next();
				float score = searchRelationRatio * 1;
				ScoredDocumentITupleContainer container = new ScoredDocumentITupleContainer(tup, null, score);
				mergedResult.put(container.getScore(), container);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		s_log.debug("result merged with ratio " + searchRelationRatio);
		viewer.refresh();
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//not needed, viewer is set thru construktor and input is allways commonSearchResultObservable
	}

	private static String getURI(org.apache.lucene.document.Document doc) {
		return doc.get("path");
	}
	
	private static String getURI(ITuple ituple) {
		/*TODO get path*/
		return ituple.getElementAt(1).getLabel();
	}
}