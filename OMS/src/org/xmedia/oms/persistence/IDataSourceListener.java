package org.xmedia.oms.persistence;

public interface IDataSourceListener extends IMessageListener{
	
	void datasourceOpened(IDataSource ds);
	
	void datasourceImported(IDataSource ds, IDataSource importedds);
	
	void datasourceClosed(IDataSource ds);
	
	

}
