package org.xmedia.oms.persistence.dao;

import java.util.Set;

import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.DatasourceException;

public interface IDatatypeDao extends IDao {
	
	public Set<IDatatype> findDatatypes(ILiteral literal) throws DatasourceException;
	
	public Set<IDatatype> findDatatypeRanges(IProperty property) throws DatasourceException;



}
