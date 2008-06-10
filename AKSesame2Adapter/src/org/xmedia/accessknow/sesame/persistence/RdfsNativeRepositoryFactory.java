package org.xmedia.accessknow.sesame.persistence;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;

public class RdfsNativeRepositoryFactory {

	public static RepositoryConfig createRepository(Repository ownerRepository, String theRepositoryId) throws RepositoryConfigException, RepositoryException {
		
		ValueFactory vf = ownerRepository.getValueFactory();
		Graph graph = new GraphImpl(vf);
		
		Resource theRepository = vf.createBNode();
		Resource repoImpl = vf.createBNode();
		Resource sailImpl = vf.createBNode();
		Resource delegate = vf.createBNode();
		
		graph.add(theRepository, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
		graph.add(theRepository, RepositoryConfigSchema.REPOSITORYID, vf.createLiteral(theRepositoryId));
		graph.add(theRepository, RDFS.LABEL, vf.createLiteral(theRepositoryId));
		graph.add(theRepository, RepositoryConfigSchema.REPOSITORYIMPL, repoImpl);
		
		graph.add(repoImpl, RepositoryConfigSchema.REPOSITORYTYPE, vf.createLiteral("openrdf:SailRepository"));
		graph.add(repoImpl, vf.createURI("http://www.openrdf.org/config/repository/sail#sailImpl"), sailImpl);
		
		graph.add(sailImpl, vf.createURI("http://www.openrdf.org/config/sail#sailType"), vf.createLiteral("openrdf:ForwardChainingRDFSInferencer"));
		graph.add(sailImpl, vf.createURI("http://www.openrdf.org/config/sail#delegate"), delegate);
		
		graph.add(delegate, vf.createURI("http://www.openrdf.org/config/sail#sailType"), vf.createLiteral("openrdf:NativeStore"));
		
		RepositoryConfig repConfig = RepositoryConfig.create(graph, theRepository);
		repConfig.validate();
		
		RepositoryConfigUtil.updateRepositoryConfigs(ownerRepository, repConfig);
		
		return repConfig;
	}
	
}
