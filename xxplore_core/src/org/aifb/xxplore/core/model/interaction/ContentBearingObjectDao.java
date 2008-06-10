package org.aifb.xxplore.core.model.interaction;

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

public class ContentBearingObjectDao implements IContentBearingObjectDao{
	
	private static IContentBearingObjectDao m_instance = null;
	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";
	private final String DESCRIPTION = POLICY_ONTOLOGY_URI + "#description";

	public static IContentBearingObjectDao getInstance() {
		if (m_instance == null)
			m_instance = new ContentBearingObjectDao();
		return m_instance;
	}

	public Set<IContentBearingObject> findAllContentBearingObjects() {
		try {
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<IContentBearingObject> cboUris = new HashSet<IContentBearingObject>();
			
			INamedConcept cbo = cDao.findByUri(POLICY_ONTOLOGY_URI + "#content bearing object");
			for (IIndividual cboInd : cbo.getMemberIndividuals(true)) {
				cboUris.add(new ContentBearingObject(((INamedIndividual)cboInd).getUri()));
			}
			return cboUris;
			
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public IContentBearingObject findContentBearingObjectByUri(String cboUri) {
		// TODO Auto-generated method stub
		IContentBearingObject cbo = null;
		
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual cboIndividual = iDao.findByUri(cboUri);
			
			cbo = new ContentBearingObject(cboUri);
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(cboIndividual);
			for (IPropertyMember prop : props) {		
				if (prop.getProperty().getUri().equals(DESCRIPTION))
					cbo.setDes(((ILiteral)prop.getTarget()).getLiteral());
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return cbo;
	}

	public void saveContentBearingObject(IContentBearingObject cbo) {
		// TODO Auto-generated method stub
		
	}
}
