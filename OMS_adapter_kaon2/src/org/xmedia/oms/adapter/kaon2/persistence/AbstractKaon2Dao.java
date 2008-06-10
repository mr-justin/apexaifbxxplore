package org.xmedia.oms.adapter.kaon2.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.logic.Term;
import org.semanticweb.kaon2.api.reasoner.Query;
import org.semanticweb.kaon2.api.reasoner.Reasoner;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class AbstractKaon2Dao {
	private static Logger s_log = Logger.getLogger(AbstractKaon2Dao.class);
	
	public IEntity checkForDelegate(IEntity res){
		if (res.getDelegate() == null){
			if(res instanceof NamedConcept){
				IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
				return dao.findByUri(res.getUri());
			}
			if(res instanceof NamedIndividual){
				IIndividualDao dao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
				return dao.findByUri(res.getUri());
			}
			if(res instanceof IProperty){
				IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
				return dao.findByUri(res.getUri());
			}
			else return null;
		}

		else return res;
	}

	protected Kaon2Transaction getTransaction() {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		return (Kaon2Transaction)session.getTransaction();
	}

	protected List<String[]> runQuery(Ontology ontology, String queryText) throws KAON2Exception, InterruptedException {
		s_log.debug("query: " + queryText);
	    Reasoner reasoner=ontology.createReasoner();
	    List<String[]> terms = new ArrayList<String[]>();
	    try {
	        Query query=reasoner.createQuery(null,queryText);
	        try {
	            query.open();
	            Term[] tupleBuffer = query.tupleBuffer();
	            while (!query.afterLast()) {
	            	String[] results = new String [tupleBuffer.length];
	            	for (int i = 0; i < results.length; i++)
	            		results[i] = tupleBuffer[i].toString();
	            	terms.add(results);
	                query.next();
	            }
	            query.close();
	        }
	        finally {
	            query.dispose();
	        }
	    }
	    finally {
	        reasoner.dispose();
	    }
	    
	    s_log.debug("results: ");
	    for (String[] result : terms) {
	    	StringBuilder sb = new StringBuilder();
	    	for (String res : result)
	    		sb.append(res + " ");
	    	s_log.debug(sb);
	    }
	    
	    return terms;
	}
}
