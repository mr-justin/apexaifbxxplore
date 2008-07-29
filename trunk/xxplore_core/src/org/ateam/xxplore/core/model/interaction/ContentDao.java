package org.ateam.xxplore.core.model.interaction;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class ContentDao implements IContentDao{
	private static IContentDao m_instance = null;
	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";
	private final String DESCRIPTION = POLICY_ONTOLOGY_URI + "#description";

	public static IContentDao getInstance() {
		if (m_instance == null)
			m_instance = new ContentDao();
		return m_instance;
	}

	public Set<IContent> findAllContents() {
		try {
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<IContent> contentUris = new HashSet<IContent>();
			
			INamedConcept content = cDao.findByUri(POLICY_ONTOLOGY_URI + "#content");
			for (IIndividual contentInd : content.getMemberIndividuals(true)) {
				contentUris.add(new Content(((INamedIndividual)contentInd).getUri()));
			}
			return contentUris;
			
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public IContent findContentByUri(String contentUri) {
		// TODO Auto-generated method stub
		IContent content = null;
		
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual contentIndividual = iDao.findByUri(contentUri);
			
			content = new Content(contentUri);
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(contentIndividual);
			for (IPropertyMember prop : props) {		
				if (prop.getProperty().getUri().equals(DESCRIPTION))
					content.setDes(((ILiteral)prop.getTarget()).getLiteral());
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return content;
	}

	public void saveContent(IContent agent) {
		// TODO Auto-generated method stub
		
	}
}
