package org.xmedia.oms.persistence.dao;


public interface IDaoFactory {
    /**
     * Creates a new DAO instance that implements the specified interface.
     *
     * @param daoInterface A subinterface of {@link org.xmedia.oms.persistence.dao.IDao}
     * @return A new DAO instance that implements the specified interface
     * @precondition <code>!IDao.class.equals( daoInterface )</code>
     * @precondition <code>IDao.class.isAssignableFrom( daoInterface )</code>
     */
    IDao createDao(Class daoInterface);
    
}
