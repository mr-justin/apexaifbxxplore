package org.xmedia.oms.persistence.dao;

import java.util.List;

import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.persistence.DatasourceException;


public interface IDao {

    /**
     * Returns the Class of the Business Objects associated with this DAO.
     *
     * @return The Class of the Business Objects associated with this DAO
     */
    public Class getBoClass();

    /**
     * Inserts the specified Business Objects into the datasource.
     *
     * @param newBo The new BO to be inserted into the database
     */
    public void insert(IBusinessObject newBo) throws DatasourceException;

    /**
     * Updates the specified Bo in the database.
     * @param existingBo The existing Bo to be updated in the database
     * @precondition <code>getBoClass().isInstance( existingBo )</code>
     */
    public void update(IBusinessObject existingBo) throws DatasourceException;

    /**
     * Deletes the Bo object with the specified ID.
     *
     * @param existingBo The existing Bo to be logically deleted
     * @precondition <code>getBoClass().isInstance( existingBo )</code>
     */
    public void delete(IBusinessObject existingBo) throws DatasourceException;

    /**
     * Returns the BO for the specified ID.
     *
     * @param id The ID of the requested BO object
     * @return The BO for the specified ID
     * @postcondition <code>result.getOid().longValue() == id</code>
     */
    public IBusinessObject findById(String id) throws DatasourceException;

    /**
     * Returns all active instances of the type returned by <code>getBoClass()</code>.
     *
     * @return a list of all active instances of the type returned by <code>getBoClass()</code>.
     */
    public List<? extends IBusinessObject> findAll() throws DatasourceException;


}
