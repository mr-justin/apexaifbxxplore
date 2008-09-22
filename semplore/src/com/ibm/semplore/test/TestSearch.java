/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TestSearch.java,v 1.3 2008/09/12 15:37:02 xrsun Exp $
 */
package com.ibm.semplore.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;

import org.apache.xerces.parsers.DOMParser;

import com.ibm.semplore.model.CatRelConstraint;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.search.SchemaQuery;
import com.ibm.semplore.search.SchemaSearchable;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.search.XFacetedResultSpec;
import com.ibm.semplore.search.XFacetedSearchable;
import com.ibm.semplore.search.impl.SearchFactoryImpl;

/**
 * @author liu qiaoling
 * 
 */
public abstract class TestSearch {
	DOMParser parser = new DOMParser();

	public static int FACET_THRESHOLD = 10;

	public static int showResults = 20;

	public static boolean quit;

	public static SchemaSearchable schemaSearcher;

	public static XFacetedSearchable searcher;

	public static SearchFactory searchFactory = SearchFactoryImpl.getInstance();

	public static SearchHelper searchHelper;

	public static SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	public static void showChildrenFacets(PrintStream out, Category par,
			int depth) throws IOException {
		SchemaObjectInfo[] children = schemaSearcher.getSubCategories(par);
		for (int j = 0; j < children.length; j++) {
			for (int k = 0; k < depth; k++)
				System.out.print("\t");
			out.println("--> " + children[j].getLabel() + " ("
					+ children[j].getURI() + ")");
			showChildrenFacets(out, schemaFactory.createCategory(children[j]
					.getID()), depth + 1);
		}
	}

	public static void showChildrenFacets(PrintStream out, Relation par,
			int depth) throws IOException {
		SchemaObjectInfo[] children = schemaSearcher.getSubRelations(par);
		for (int j = 0; j < children.length; j++) {
			for (int k = 0; k < depth; k++)
				System.out.print("\t");
			out.println("--> " + children[j].getLabel() + " ("
					+ children[j].getURI() + ")");
			showChildrenFacets(out, schemaFactory.createRelation(children[j]
					.getID()), depth + 1);
		}
	}

	public static void showCatFacets(PrintStream out) throws Exception {
		SchemaObjectInfo[] cats = schemaSearcher.getRootCategories();
		out.println("category facets:");
		for (int i = 0; i < cats.length; i++) {
			out.println("catFacet " + i + ": " + cats[i].getLabel() + " ("
					+ cats[i].getURI() + ")");
			showChildrenFacets(out, schemaFactory.createCategory(cats[i]
					.getID()), 1);
		}
		out.println();
	}

	public static void showRelFacets(PrintStream out) throws Exception {
		SchemaObjectInfo[] rels = schemaSearcher.getRootRelations();
		out.println("relation facets:");
		for (int i = 0; i < rels.length; i++) {
			out.println("relFacet " + i + ": " + rels[i].getLabel() + " ("
					+ rels[i].getURI() + ")");
			showChildrenFacets(out, schemaFactory.createRelation(rels[i]
					.getID()), 1);
			// SchemaObjectInfo[] children =
			// schemaSearcher.getSubRelations(schemaFactory.createRelation(rels[i].getURI()));
			// for (int j=0; j<children.length; j++)
			// System.out.println("\t--> "+children[j].getLabel());
		}
		out.println();
	}

	// public static void showAttrFacets() throws Exception {
	// SchemaObjectInfo[] attrs = schemaSearcher.getAttributes();
	// System.out.println("attribute facets:");
	// for (int i=0; i<attrs.length; i++) {
	// System.out.println("attrFacet "+i+": "+attrs[i].getLabel());
	// }
	// System.out.println();
	// }

	// public static void searchInstance(GeneralCategory cat) throws Exception {
	// ResultSet resultSet =
	// searchInstance(schemaFactory.createCategoryRelationExp().append(cat), 0);
	// if (searchHelper != null) {
	// // searchHelper.setHint(SearchHelper.CATEGORY_CACHE_HINT, cat,
	// searchFactory.createCacheHint(resultSet));
	// // searchHelper.setHint(SearchHelper.START_CACHE_HINT, null,
	// searchFactory.createCacheHint(resultSet));
	// }
	// }

	public static ResultSet searchInstance(CatRelConstraint exp, int target)
			throws Exception {
		System.out.println("query expression: " + exp.toString());
		XFacetedQuery xfacetedQ = searchFactory.createXFacetedQuery();
		xfacetedQ.setQueryConstraint(exp);
		XFacetedResultSpec resultSpec = searchFactory
				.createXFacetedResultSpec();
		resultSpec.setCategoryFacetsInfo(false, null);
		resultSpec.setRelationFacetsInfo(false, null);
		// resultSpec.setCategoryFacetsInfo(false,
		// schemaSearcher.getRootCategories());
		resultSpec.setRelationFacetsInfo(false, schemaSearcher
				.getRootRelations());
		xfacetedQ.setResultSpec(resultSpec);
		xfacetedQ.setSearchTarget(target);
		// xfacetedQ.setRequestedNumberOfResults(requiredNumberOfResults);

		XFacetedResultSet resultSet = null;
		for (int i = 0; i < 1; i++) {
			resultSet = searcher.search(xfacetedQ, searchHelper);
		}
		showResultSet(resultSet, Instance.class);
		return resultSet;
	}

	// public static void searchInstance(Relation rel) throws Exception {
	// searchInstance(schemaFactory.createCategoryRelationExp().append(rel), 0);
	// }

	public static void showResultSet(ResultSet resultSet, Class cls)
			throws IOException {
		if (resultSet instanceof XFacetedResultSet) {
			XFacetedResultSet xr = (XFacetedResultSet) resultSet;
			System.out.println("query time(ms): " + xr.getResultTime());
			System.out.println("facet time(ms): " + xr.getFacetTime());
		}
		System.out.println("query results: " + resultSet.getLength());
		int tmp = showResults;
		if (tmp < 0 || tmp > resultSet.getLength())
			tmp = resultSet.getLength();
		try {
			for (int i = 0; i < tmp; i++) {
				resultSet.getResult(i).getLabel();
				System.out.println("result " + i + ": "
						+ URLDecoder.decode(resultSet.getResult(i).getLabel()));
				System.out.println("//score:" + resultSet.getScore(i));
				System.out.println("//uri:" + resultSet.getResult(i).getURI());
				System.out.println("//id:" + resultSet.getDocID(i));
//				System.out.println("//text:"
//						+ resultSet.getResult(i).getTextDescription());
//				System.out.println("//text:" + resultSet.getSnippet(i));
				SchemaObjectInfo[] path = null;
				if (cls == Category.class) {
					path = schemaSearcher.getSuperCategoryPath(schemaFactory
							.createCategory(resultSet.getResult(i).getID()));
				} else if (cls == Relation.class) {
					path = schemaSearcher.getSuperRelationPath(schemaFactory
							.createRelation(resultSet.getResult(i).getID()));
				}
				if (cls == Category.class || cls == Relation.class) {
					// System.out.print("//path:");
					// for (int j=0; j<path.length; j++) {
					// System.out.print(path[j].getLabel()+"-->");
					// }
					// System.out.println(resultSet.getResult(i).getLabel());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println();

		if (resultSet instanceof XFacetedResultSet) {
			// catFacets
			System.out.println("category facets count:");
			Facet[] catFacets = ((XFacetedResultSet) resultSet)
					.getCategoryFacets();
			if (catFacets != null) {
				int facetLen = catFacets.length;
				if (facetLen > FACET_THRESHOLD)
					facetLen = FACET_THRESHOLD;
				for (int i = 0, j = 0; i < facetLen; i++) {
					if (catFacets[i].getCount() > 0)
						System.out.println("catFacet " + (j++) + ": "
								+ catFacets[i].getInfo().getLabel() + "(~"
								+ catFacets[i].getCount() + ")");
				}
			}
			System.out.println();

			// relFacets
			System.out.println("relation facets count:");
			Facet[] relFacets = ((XFacetedResultSet) resultSet)
					.getRelationFacets();
			if (relFacets != null) {
				int facetLen = relFacets.length;
				if (facetLen > FACET_THRESHOLD)
					facetLen = FACET_THRESHOLD;
				for (int i = 0, j = 0; i < facetLen; i++) {
					if (relFacets[i].getCount() > 0) {
						if (relFacets[i].isInverseRelation())
							System.out.print("(inverse)");
						System.out.println("relFacet " + (j++) + ": "
								+ relFacets[i].getInfo().getLabel() + "(~"
								+ relFacets[i].getCount() + ")");
					}
				}
			}
			System.out.println();

			// relFacets_subj
			System.out.println("subject relation facets count:");
			relFacets = ((XFacetedResultSet) resultSet)
					.getRelationFacetsGivenSubject();
			if (relFacets != null) {
				int facetLen = relFacets.length;
				if (facetLen > FACET_THRESHOLD)
					facetLen = FACET_THRESHOLD;
				for (int i = 0, j = 0; i < facetLen; i++) {
					if (relFacets[i].getCount() > 0)
						System.out.println("subj_relFacet " + (j++) + ": "
								+ relFacets[i].getInfo().getLabel() + "(~"
								+ relFacets[i].getCount() + ")");
				}
			}
			System.out.println();

			// relFacets_obj
			System.out.println("object relation facets count:");
			relFacets = ((XFacetedResultSet) resultSet)
					.getRelationFacetsGivenObject();
			if (relFacets != null) {
				int facetLen = relFacets.length;
				if (facetLen > FACET_THRESHOLD)
					facetLen = FACET_THRESHOLD;
				for (int i = 0, j = 0; i < facetLen; i++) {
					if (relFacets[i].getCount() > 0)
						System.out.println("obj_relFacet " + (j++) + ": "
								+ relFacets[i].getInfo().getLabel() + "(~"
								+ relFacets[i].getCount() + ")");
				}
			}
			System.out.println();
		}
	}

	public static void searchSchema(SchemaQuery schemaQuery) throws Exception {
		System.out.println("search schema: " + schemaQuery.toString());
		ResultSet resultSet = null;
		for (int i = 0; i < 1; i++) {
			long time_b = System.currentTimeMillis();
			resultSet = schemaSearcher.search(schemaQuery);
			System.out.println("finish searching in "
					+ (System.currentTimeMillis() - time_b) + " ms");
		}
		if (schemaQuery.getType() == SchemaQuery.TYPE_CATEGORY)
			showResultSet(resultSet, Category.class);
		else if (schemaQuery.getType() == SchemaQuery.TYPE_RELATION)
			showResultSet(resultSet, Relation.class);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out
					.println("usage: java TestSearch [semplore_config_file] [xml_query_file]");
			System.exit(1);
		}
		try {
			ForServlet forServ = new ForServlet();
			String configFile = args[0];// "E:\\User\\AllisQM\\Semplore\\SSModel\\config\\semplore-dbpedia-yago.cfg";
			forServ.init(configFile);// config file
			String queryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><query>"
					+ "  <action type=\"all\" start=\"0\" count=\"10\"/>"
					+ "  <graph>"
					+ "    <queryNode insName=\"\" instanceConstraint=\"\" relName=\"\" relation=\"\" inverse=\"0\" conName=\"\" conceptConstraint=\"\" keyword=\"academy award\">"
					+ "      <queryNode relation=\"&lt;http://dbpedia.org/property/starring>\" inverse=\"0\" relName=\"starring\" insName=\"\" conceptConstraint=\"\" instanceConstraint=\"\" keyword=\"star wars\"/>"
					+ "      <queryNode relation=\"&lt;http://dbpedia.org/property/director>\" inverse=\"0\" relName=\"director\" insName=\"\" conceptConstraint=\"&lt;http://dbpedia.org/resource/Category:Jewish_American_film_directors>\" instanceConstraint=\"\" keyword=\"\" conName=\"TOP Category\"/>"
					+ "    </queryNode>"
					+ "  </graph>"
					+ "  <targetNode index=\"2\"/>" + "</query>";
			String uid = "127.0.0.1";
			String resultXML = forServ.query(new File(args[1]), "");// forServ.query(queryXML,uid);
			System.out.println("uid:" + uid);
			System.out.println("queryXML:" + args[1]);
			System.out.println("resultXML: write to result.xml");
			BufferedWriter file = new BufferedWriter(new FileWriter("result.xml"));
			file.write(resultXML);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
