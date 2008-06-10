package org.xmedia.accessknow.sesame.persistence;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class RdfMemoryRepositoryFactory {

	public static Repository createRepository() throws RepositoryException  {
		
		Repository theRepository = new SailRepository(new MemoryStore());
		theRepository.initialize();
		
		return theRepository;
	}
	
}
