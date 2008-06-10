package org.xmedia.oms.persistence.dao;

import java.util.Set;

import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.persistence.DatasourceException;

public interface ILiteralDao extends IDao {
	
	/***** find by datatype  **********************************************************/
	public Set<ILiteral> findMemberIndividuals(IDatatype datatype) throws DatasourceException;

}
