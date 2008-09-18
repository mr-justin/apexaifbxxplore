/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.xir.DocStream;

/**
 * @author xrsun
 *
 */
public class XFacetedResultSetForMultiDataSourcesImpl implements
		XFacetedResultSetForMultiDataSources {

	public XFacetedResultSetForMultiDataSourcesImpl(String ds, HashMap<String, Integer> dsfacet,
			XFacetedResultSet rs) {
		this.ds = ds;
		this.dsfacet = dsfacet;
		this.rs = rs;
	}

	private String ds;
	private XFacetedResultSet rs;
	private HashMap<String, Integer> dsfacet;
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.XFacetedResultSetForMultiDataSources#getDataSourceFacet()
	 */
	@Override
	public HashMap<String, Integer> getDataSourceFacets() {
		return dsfacet;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getCategoryFacets()
	 */
	@Override
	public Facet[] getCategoryFacets() {
		return rs.getCategoryFacets();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getCategoryFacetsLength()
	 */
	@Override
	public int getCategoryFacetsLength() {
		return rs.getCategoryFacetsLength();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getFacetTime()
	 */
	@Override
	public long getFacetTime() {
		return rs.getFacetTime();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getRelationFacets()
	 */
	@Override
	public Facet[] getRelationFacets() {
		return rs.getRelationFacets();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getRelationFacetsGivenObject()
	 */
	@Override
	public Facet[] getRelationFacetsGivenObject() {
		return rs.getRelationFacetsGivenObject();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getRelationFacetsGivenSubject()
	 */
	@Override
	public Facet[] getRelationFacetsGivenSubject() {
		return rs.getRelationFacetsGivenSubject();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getRelationFacetsLength()
	 */
	@Override
	public int getRelationFacetsLength() {
		return rs.getRelationFacetsLength();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#getResultTime()
	 */
	@Override
	public long getResultTime() {
		return rs.getResultTime();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.XFacetedResultSet#setSnippetKeyword(java.lang.String)
	 */
	@Override
	public XFacetedResultSet setSnippetKeyword(String keyword) {
		return rs.setSnippetKeyword(keyword);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.ResultSet#getDocID(int)
	 */
	@Override
	public int getDocID(int index) throws Exception {
		return rs.getDocID(index);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.ResultSet#getLength()
	 */
	@Override
	public int getLength() {
		return rs.getLength();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.ResultSet#getResult(int)
	 */
	@Override
	public SchemaObjectInfo getResult(int index) throws Exception {
		return rs.getResult(index);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.ResultSet#getResultStream()
	 */
	@Override
	public DocStream getResultStream() {
		return rs.getResultStream();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.ResultSet#getScore(int)
	 */
	@Override
	public double getScore(int index) throws Exception {
		return rs.getScore(index);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.ResultSet#getSnippet(int)
	 */
	@Override
	public String getSnippet(int index) throws Exception {
		return rs.getSnippet(index);
	}

	@Override
	public String getCurrentDataSource() {
		return ds;
	}

}
