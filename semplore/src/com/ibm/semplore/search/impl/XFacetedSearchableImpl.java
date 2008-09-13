/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: XFacetedSearchableImpl.java,v 1.19 2008/09/05 02:11:38 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import com.ibm.semplore.model.CatRelConstraint;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CategoryRelationExp;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.CacheHint;
import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.Query;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.XFacetedSearchable;
import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;

/**
 * @author liu Qiaoling
 * 
 */
public class XFacetedSearchableImpl extends SearchableImpl implements
		XFacetedSearchable {
	/**
	 * The index reader for accessing instance index.
	 */
	protected IndexReader insIndexReader;

	protected Properties config;

	/**
	 * The manager of all kinds of arithmetic units and the execution of their
	 * computation.
	 */
	protected AUManager AUManager;

	/**
	 * @param insIR
	 *            the index reader for accessing instance index.
	 */

	protected XFacetedSearchableImpl(IndexReader insIR, Properties config)
			throws Exception {
		super();
		insIndexReader = insIR;
		AUManager = new AUManager(config);
		this.config = config;
		// File file = new File(AUconfig.getProperty(Config.RESULT_PATH));
		// if (file.exists())
		// file.mkdirs();
	}

	/**
	 * Evaluate results of the search target in the query constraint.
	 * 
	 * @param cons
	 * @param target
	 * @param searchHelper
	 * @param threshold
	 * @return
	 * @throws IOException
	 */
	protected DocStream evaluateTarget(CatRelConstraint cons, int target,
			SearchHelper searchHelper) throws IOException {
		if (cons instanceof CategoryRelationExp)
			return new ExpEvaluator((CategoryRelationExp) cons, target,
					searchHelper).evaluateTarget();
		else if (cons instanceof CatRelGraph)
			return new GraphEvaluator((CatRelGraph) cons, target, searchHelper)
					.evaluateTarget();
		return null;
	}

	// /**
	// * Compute facets information of the evaluated results of the target,
	// given
	// * some specified category facets.
	// *
	// * @param resultStream
	// * @param categories
	// * @param exactCount
	// * whether to exactly count the results that match with the facet
	// * @return
	// * @throws IOException
	// */
	// protected Facet[] suggestCategoryFacets(DocStream resultStream,
	// SchemaObjectInfo[] categories, boolean exactCount)
	// throws IOException {
	// Facet[] catFacets = null;
	// if (categories != null) {
	// catFacets = new FacetImpl[categories.length];
	// for (int i = 0; i < categories.length; i++) {
	// DocStream catStream = insIndexReader.getDocStream(termFactory
	// .createTermForInstances(schemaFactory
	// .createCategory(categories[i].getURI())));
	// DocStream countStream;
	// if (resultStream != null)
	// countStream = AUManager.binaryInter(
	// (DocStream) resultStream.clone(), catStream,
	// exactCount, -1);
	// else
	// countStream = catStream;
	// int count;
	// if (exactCount)
	// count = countStream.getLen();
	// else
	// count = countStream.getEstimatedNumberOfCompleteResults();
	// catFacets[i] = new FacetImpl(categories[i], count);
	// }
	// Arrays.sort(catFacets, new FacetComparator());
	// }
	// return catFacets;
	// }
	//
	// /**
	// * Compute facets information of the evaluated results of the target,
	// given
	// * some specified relation facets.
	// *
	// * @param resultStream
	// * @param relations
	// * @param exactCount
	// * whether to exactly count the results that match with the facet
	// * @return
	// * @throws IOException
	// */
	// protected Facet[] suggestRelationFacetsGivenObject(DocStream
	// resultStream,
	// SchemaObjectInfo[] relations, boolean exactCount)
	// throws IOException {
	// Facet[] relFacets = null;
	// if (relations != null) {
	// relFacets = new FacetImpl[relations.length];
	// for (int i = 0; i < relations.length; i++) {
	// int count;
	// DocStream relStream = insIndexReader.getDocStream(termFactory
	// .createTermForObjects(schemaFactory
	// .createRelation(relations[i].getURI())));
	// DocStream countStream;
	// if (resultStream != null)
	// countStream = AUManager.binaryInter(
	// (DocStream) resultStream.clone(), relStream,
	// exactCount, -1);
	// else
	// countStream = relStream;
	// if (exactCount)
	// count = countStream.getLen();
	// else
	// count = countStream.getEstimatedNumberOfCompleteResults();
	// relFacets[i] = new FacetImpl(relations[i], count);
	// }
	// Arrays.sort(relFacets, new FacetComparator());
	// }
	// return relFacets;
	// }
	//
	// protected Facet[] suggestRelationFacetsGivenSubject(DocStream
	// resultStream,
	// SchemaObjectInfo[] relations, boolean exactCount)
	// throws IOException {
	// Facet[] relFacets = null;
	// if (relations != null) {
	// relFacets = new FacetImpl[relations.length];
	// for (int i = 0; i < relations.length; i++) {
	// int count;
	// DocStream relStream = insIndexReader.getDocStream(termFactory
	// .createTermForSubjects(schemaFactory
	// .createRelation(relations[i].getURI())));
	// DocStream countStream;
	// if (resultStream != null)
	// countStream = AUManager.binaryInter(
	// (DocStream) resultStream.clone(), relStream,
	// exactCount, -1);
	// else
	// countStream = relStream;
	// if (exactCount)
	// count = countStream.getLen();
	// else
	// count = countStream.getEstimatedNumberOfCompleteResults();
	// relFacets[i] = new FacetImpl(relations[i], count);
	// }
	// Arrays.sort(relFacets, new FacetComparator());
	// }
	// return relFacets;
	// }

	static SchemaFactory factory = SchemaFactoryImpl.getInstance();

	Facet[] resultSet2Facet(ResultSetImpl_TopDocs result) throws Exception {
		int length = result.getLength();
		if (length > result.topcount)
			length = result.topcount;
		Facet[] ans = new Facet[length];
		for (int i = 0; i < length; i++) {
			ans[i] = new FacetImpl(result.getResult(i), (int) (result
					.getScore(i) + 0.01));
		}
		return ans;
	}

	protected int getCount(String filename, int id) {
		try {
			RandomAccessFile reader = new RandomAccessFile(filename, "r");
			reader.seek(id * 8);
			int rid = reader.readInt();
			int count = reader.readInt();
			reader.close();
			if (rid != id) {
				System.out.println("error of RandomAccessfile: id=" + id);
				return -1;
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	protected float strategy(float score, int count, int max) {
		double ans = 0;
		double c = 0.5;
		try {
			ans = c * score + (1 - c) * 1 / (1 + Math.exp(-count));
			// ans = c*score + (1-c)*Math.log(count+1)/Math.log(max+1);
			return (float) ans;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.search.XFacetedSearchable#search(com.ibm.semplore.search.XFacetedQuery,
	 *      com.ibm.semplore.search.SearchHelper)
	 */

	protected static final String TERM_STR_FOR_ROOT = "root";

	public XFacetedResultSet search(XFacetedQuery facetedQuery,
			SearchHelper searchHelper) throws Exception {
		System.out.println("begin searching...");
		long time_begin = System.currentTimeMillis();

		// evaluate results of the search target in the query expression
		DocStream resultStream = evaluateTarget(facetedQuery
				.getQueryConstraint(), facetedQuery.getSearchTarget(),
				searchHelper);
		if (resultStream == null)
			resultStream = new MEMDocStream_Score(new int[0], new float[0], 0);
		else {
			/*
			 * Compute static score of each result.
			 */
			// String filename = config.getProperty(Config.STATIC_SCORE_FILE);
			// if (filename!=null) {
			// DocStream newresult = (DocStream) resultStream.clone();
			// int[] result_ids = new int[resultStream.getLen()];
			// float[] result_scores = new float[resultStream.getLen()];
			// newresult.init();
			// int max = getMax(filename);
			// for (int i = 0; i < newresult.getLen(); i++,newresult.next()) {
			// result_ids[i] = newresult.doc();
			// int count = 0;
			// if (result_ids[i]<Config.MaxLanOfInstance)
			// count = getCount(filename, result_ids[i]);
			// result_scores[i] = strategy(newresult.score(), count, max);
			// }
			// resultStream = new MEMDocStream_Score(result_ids, result_scores,
			// resultStream.getLen());
			// }
		}
		long resultTime = System.currentTimeMillis() - time_begin;

		DocPositionStream cat, rel_obj, rel_sbj;
		// all the categories and relations(Cobjects)
		DocStream catDoc, relDoc;
		relDoc = insIndexReader.getDocStream(termFactory
				.createTermForRootRelations());
		catDoc = insIndexReader.getDocStream(termFactory
				.createTermForRootCategories());
		// positionstream(relations)
		cat = insIndexReader.getDocPositionStream(termFactory.createTerm(
				FieldType.CATEGORIES.toString(), FieldType.CATEGORIES));
		rel_obj = insIndexReader.getDocPositionStream(termFactory.createTerm(
				FieldType.INVERSERELATIONS.toString(),
				FieldType.INVERSERELATIONS));
		rel_sbj = insIndexReader.getDocPositionStream(termFactory.createTerm(
				FieldType.RELATIONS.toString(), FieldType.RELATIONS));
		// mass-union
		DocStream catStream = AUManager.massUnion_BV_Facet(cat,
				(DocStream) resultStream.clone(), catDoc);
		DocStream rel_objStream = AUManager.massUnion_BV_Facet(rel_obj,
				(DocStream) resultStream.clone(), (DocStream) relDoc.clone());
		DocStream rel_subStream = AUManager.massUnion_BV_Facet(rel_sbj,
				(DocStream) resultStream.clone(), relDoc);
		long facetTime = System.currentTimeMillis() - time_begin - resultTime;

		// facets
		ResultSetImpl_TopDocs catResult = new ResultSetImpl_TopDocs(catStream,
				insIndexReader);
		Facet[] catFacet = resultSet2Facet(catResult);
		/**
		 * ************************combine relation and inverse relation
		 * facets*********************
		 */
		ResultSetImpl_TopDocs rel_objResult = new ResultSetImpl_TopDocs(
				rel_objStream, insIndexReader);
		ResultSetImpl_TopDocs rel_sbjResult = new ResultSetImpl_TopDocs(
				rel_subStream, insIndexReader);
		Facet[] relFacet_obj = resultSet2Facet(rel_objResult);
		Facet[] relFacet_sub = resultSet2Facet(rel_sbjResult);
		Facet[] relFacet = new Facet[relFacet_sub.length + relFacet_obj.length];
		for (int i = 0, j = 0, k = 0; i < relFacet_sub.length
				|| j < relFacet_obj.length;) {
			if (i >= relFacet_sub.length || j < relFacet_obj.length
					&& relFacet_sub[i].getCount() < relFacet_obj[j].getCount()) {
				relFacet[k++] = new FacetImpl(relFacet_obj[j].getInfo(),
						relFacet_obj[j].getCount(), true);
				j++;
			} else {
				relFacet[k++] = relFacet_sub[i];
				i++;
			}
		}
		/**
		 * ************************combine relation and inverse relation
		 * facets*********************
		 */

		// traverse the result stream in vain for time count
		// DocStream traverse = (DocStream) resultStream.clone();
		// traverse.init();
		// for (int i = 0; i < traverse.getLen(); i++)
		// traverse.next();
		long time_end = System.currentTimeMillis();
		System.out.println("searching finished in " + (time_end - time_begin)
				+ " ms");

		XFacetedResultSet result = new XFacetedResultSetImpl(resultStream,
				insIndexReader, catFacet, relFacet, catStream.getLen(),
				rel_objStream.getLen() + rel_subStream.getLen(), resultTime,
				facetTime).setSnippetKeyword(facetedQuery.getSnippetKeyword());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.search.XFacetedSearchable#search(com.ibm.semplore.search.XFacetedQuery)
	 */
	public XFacetedResultSet search(XFacetedQuery facetedQuery)
			throws Exception {
		return search(facetedQuery, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.search.Searchable#search(com.ibm.semplore.search.Query)
	 */
	public ResultSet search(Query query) throws Exception {
		if (query instanceof XFacetedQuery)
			return search((XFacetedQuery) query);
		DocStream resultStream = insIndexReader.getDocStream(termFactory
				.createTerm(query.getText(), FieldType.TEXT));
		return new ResultSetImpl_TopDocs(resultStream, insIndexReader)
				.setSnippetKeyword(query.getText());
	}

	public IndexReader getInsIndexReader() {
		return insIndexReader;
	}

	abstract class Evaluator {
		SearchHelper searchHelper = null;

		int target = 0;

		public Evaluator(int target, SearchHelper searchHelper) {
			this.target = target;
			this.searchHelper = searchHelper;
		}

		abstract public DocStream evaluateTarget() throws IOException;

		/**
		 * Attempt to get start cache hint of the query expression from search
		 * helper.
		 * 
		 * @param searchHelper
		 * @param catRelExp
		 * @return
		 */
		protected DocStream getStartCacheHint(SearchHelper searchHelper,
				int nodeIndex) {
			if (searchHelper != null) {
				CacheHint cache = (CacheHint) (searchHelper.getHint(
						SearchHelper.START_CACHE_HINT, new Integer(nodeIndex)));
				if (cache != null) {// get start cache hint
					System.out.println("get start cache hint");
					return cache.getStream();
				}
			}
			return null;
		}

		/**
		 * Attempt to get cache hint of the category from search helper.
		 * 
		 * @param searchHelper
		 * @param cat
		 * @return
		 */
		protected DocStream getCategoryCacheHint(SearchHelper searchHelper,
				GeneralCategory cat) {
			if (searchHelper != null) {
				CacheHint cache = (CacheHint) (searchHelper.getHint(
						SearchHelper.CATEGORY_CACHE_HINT, cat));
				if (cache != null) {// get category cache hint
					System.out.println("get category cache hint: "
							+ cat.toString());
					return cache.getStream();
				}
			}
			return null;
		}

	}

	class GraphEvaluator extends Evaluator {
		CatRelGraph graph = null;

		public GraphEvaluator(CatRelGraph graph, int target,
				SearchHelper searchHelper) {
			super(target, searchHelper);
			this.graph = graph;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.semplore.search.impl.XFacetedSearchableImpl.Evaluator#evaluateTarget()
		 */
		public DocStream evaluateTarget() throws IOException {
			HashSet<Edge> checkedEdges = new HashSet<Edge>();
			Hashtable<Integer, DocStream> nodeResults = new Hashtable<Integer, DocStream>();
			bottomUpByDepthFirst(target, checkedEdges, nodeResults);
			return nodeResults.get(target);
		}

		public boolean isUniversalCategory(GeneralCategory cat) {
			if (cat instanceof Category)
				return ((Category) cat).isUniversal();
			if (cat instanceof CompoundCategory) {
				CompoundCategory comcat = (CompoundCategory) cat;
				if (comcat.size() == 0)
					return true;
				else {
					if (comcat.size() == 1) {
						return isUniversalCategory(comcat
								.getComponentCategories()[0]);
					} else {
						return false;
					}
				}
			}
			return false;
		}

		protected DocStream evaluateGeneralCategory(GeneralCategory cat)
				throws IOException {
			// Keyword, URI, Instance, Compound
			if (cat instanceof CompoundCategory
					&& ((CompoundCategory) cat).getCompoundType() == CompoundCategory.TYPE_AND) {
				GeneralCategory[] tmp = ((CompoundCategory) cat)
						.getComponentCategories();
				DocStream res = evaluateGeneralCategory(tmp[0]);
				for (int i = 1; i < tmp.length; i++) {
					DocStream tmpres = evaluateGeneralCategory(tmp[i]);
					res = AUManager.binaryInter_Score(res, tmpres, true, -1);
				}
				return res;
			} else {
				return insIndexReader.getDocStream(termFactory
						.createTermForInstances(cat));
			}
		}

		public void bottomUpByDepthFirst(int node, HashSet<Edge> checkedEdges,
				Hashtable<Integer, DocStream> nodeResults) throws IOException {

			if (nodeResults.get(node) != null) // the node is checked before
				return;

			// cal the instances of the category of this node
			GeneralCategory cat = graph.getNode(node);
			if (isUniversalCategory(cat)) {
				// when processing UC, nothing put into nodeResults
			} else {
				DocStream startStream = getStartCacheHint(searchHelper, node);
				DocStream catStream = evaluateGeneralCategory(cat);
				if (startStream != null)
					catStream = AUManager.binaryInter_Score(catStream,
							startStream, true, -1);
				nodeResults.put(node, catStream);
			}

			// check each edge of the node
			Iterator it = graph.getEdges(node);
			while (it.hasNext()) {
				Edge ed = (Edge) it.next();
				if (checkedEdges.contains(ed)) {
					continue;
				} else {
					checkedEdges.add(ed);
					int nextNode;
					boolean employInverseRelations;
					if (ed.getFromNode() == node) {
						nextNode = ed.getToNode();
						employInverseRelations = true;
					} else {
						nextNode = ed.getFromNode();
						employInverseRelations = false;
					}

					bottomUpByDepthFirst(nextNode, checkedEdges, nodeResults);

					// cal the relation expansion from nextNode to this node
					DocStream result = null;
					if (nodeResults.get(nextNode) != null)
						result = (DocStream) nodeResults.get(nextNode).clone();
					Relation rel = ed.getRelation();
					if (employInverseRelations && !rel.isInverse()
							|| !employInverseRelations && rel.isInverse()) {
						DocPositionStream inverseRelationStream = insIndexReader
								.getDocPositionStream(termFactory
										.createTermForObjects(rel));
						DocStream CsubjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForSubjects(rel));
						DocStream CobjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForObjects(rel));
						DocStream objectStream = AUManager.binaryInter(result,
								CobjStream, true, -1);
						result = AUManager.massUnion_Score(
								inverseRelationStream, objectStream,
								CsubjStream, true, -1);

					} else {
						DocPositionStream relationStream = insIndexReader
								.getDocPositionStream(termFactory
										.createTermForSubjects(rel));
						DocStream CobjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForObjects(rel));
						DocStream CsubjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForSubjects(rel));
						DocStream subjectStream = AUManager.binaryInter(result,
								CsubjStream, true, -1);
						result = AUManager.massUnion_Score(relationStream,
								subjectStream, CobjStream, true, -1);
					}

					// integrate the result of relation expansion into the node
					DocStream catStream = nodeResults.get(node);
					result = AUManager.binaryInter_Score(result, catStream,
							true, -1);
					nodeResults.put(node, result);
				}
			}
		}
	}

	class ExpEvaluator extends Evaluator {
		CategoryRelationExp catRelExp = null;

		public ExpEvaluator(CategoryRelationExp catRelExp, int target,
				SearchHelper searchHelper) throws IOException {
			super(target, searchHelper);
			this.catRelExp = catRelExp;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.semplore.search.impl.XFacetedSearchableImpl.Evaluator#evaluateTarget()
		 */
		public DocStream evaluateTarget() throws IOException {
			CategoryRelationExp prefixExp = schemaFactory
					.createCategoryRelationExp();
			for (int i = 0; i <= target; i++) {
				Object tmp = catRelExp.get(i);
				if (tmp instanceof Relation)
					prefixExp.append((Relation) tmp);
				else if (tmp instanceof GeneralCategory)
					prefixExp.append((GeneralCategory) tmp);
			}
			DocStream prefixResults = evaluateQueryExpression(prefixExp, false,
					searchHelper);

			DocStream suffixResults = null;
			if (target + 1 < catRelExp.size()) {
				CategoryRelationExp suffixExp = schemaFactory
						.createCategoryRelationExp();
				for (int i = catRelExp.size() - 1; i > target; i--) {
					Object tmp = catRelExp.get(i);
					if (tmp instanceof Relation)
						suffixExp.append((Relation) tmp);
					else if (tmp instanceof GeneralCategory)
						suffixExp.append((GeneralCategory) tmp);
				}
				suffixResults = evaluateQueryExpression(suffixExp, true,
						searchHelper);
			}

			return AUManager.binaryInter_Score(prefixResults, suffixResults,
					true, -1);
		}

		/**
		 * Evaluate results of the query expression, and it can be specified
		 * whether to employ inverse relations for all relation computation.
		 * 
		 * @param catRelExp
		 * @param employInverseRelations
		 *            if it is set true, then all computations would employ the
		 *            inverse relation of each relation in the exp instead.
		 * @param searchHelper
		 * @return null iff catRelExp has size of 0.
		 * @throws IOException
		 */
		protected DocStream evaluateQueryExpression(
				CategoryRelationExp catRelExp, boolean employInverseRelations,
				SearchHelper searchHelper) throws IOException {
			DocStream result = getStartCacheHint(searchHelper, -1);
			for (int i = 0; i < catRelExp.size(); i++) {
				Object tmp = catRelExp.get(i);
				if (tmp instanceof Relation) {
					Relation rel = (Relation) tmp;
					if (employInverseRelations && !rel.isInverse()
							|| !employInverseRelations && rel.isInverse()) {
						DocPositionStream inverseRelationStream = insIndexReader
								.getDocPositionStream(termFactory
										.createTermForObjects(rel));
						DocStream CsubjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForSubjects(rel));
						DocStream CobjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForObjects(rel));
						DocStream objectStream = AUManager.binaryInter(result,
								CobjStream, true, -1);
						result = AUManager.massUnion_Score(
								inverseRelationStream, objectStream,
								CsubjStream, true, -1);

					} else {
						DocPositionStream relationStream = insIndexReader
								.getDocPositionStream(termFactory
										.createTermForSubjects(rel));
						DocStream CobjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForObjects(rel));
						DocStream CsubjStream = insIndexReader
								.getDocStream(termFactory
										.createTermForSubjects(rel));
						DocStream subjectStream = AUManager.binaryInter(result,
								CsubjStream, true, -1);
						result = AUManager.massUnion_Score(relationStream,
								subjectStream, CobjStream, true, -1);
					}

				} else if (tmp instanceof GeneralCategory) {
					if (tmp instanceof Category
							&& ((Category) tmp).isUniversal()) // when
						// processing
						// UC, the
						// content of
						// result does
						// not change
						continue;

					GeneralCategory cat = (GeneralCategory) tmp;
					DocStream catStream = getCategoryCacheHint(searchHelper,
							cat);
					if (catStream == null)
						catStream = insIndexReader.getDocStream(termFactory
								.createTermForInstances(cat));
					;
					result = AUManager.binaryInter_Score(result, catStream,
							true, -1);
				}
			}
			return result;
		}

	}

	public DocStream evaluate(XFacetedQuery facetedQuery) throws Exception {
		return evaluate(facetedQuery, null);
	}

	public DocStream evaluate(XFacetedQuery facetedQuery,
			SearchHelper searchHelper) throws Exception {
		DocStream resultStream = evaluateTarget(facetedQuery
				.getQueryConstraint(), facetedQuery.getSearchTarget(),
				searchHelper);
		if (resultStream == null)
			resultStream = new MEMDocStream_Score(new int[0], new float[0], 0);
		return resultStream;
	}

	public com.ibm.semplore.search.impl.AUManager getAUManager() {
		return AUManager;
	}
}

class FacetComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		Facet a = (Facet) arg0;
		Facet b = (Facet) arg1;
		if (a.getCount() < b.getCount())
			return 1;
		else if (a.getCount() > b.getCount())
			return -1;
		else
			return 0;
	}
}