package org.ateam.xxplore.core.service.datafiltering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.IService;
import org.ateam.xxplore.core.service.IServiceListener;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.Provenance;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class DataFilteringService implements IService {

	private final String TRUST = "trust";

	private String getUser() {
		return (String)((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection().getConfiguration().get(KbEnvironment.USER);
	}
	
	private String getBasePolicyOntologyUri() {
		return (String)((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection().getConfiguration().get(ExploreEnvironment.BASE_POLICY_ONTOLOGY_URI);
	}
	
	private IOntology retrieveOntology(String ontologyUri) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		for (IOntology onto : (Set<IOntology>)session.getConnection().findAllOntologies()) {
			if (onto.getUri().equals(ontologyUri))
				return onto;
		}
		
		return null;
	}

	private Set<INamedIndividual> getTrustedAgents(IOntology policyOntology, String userUri) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		String trust = getBasePolicyOntologyUri() + "#" + TRUST;


		Set<INamedIndividual> trustedAgents = new HashSet<INamedIndividual>();
		Queue<INamedIndividual> toProcess = new LinkedList<INamedIndividual>();
		Set<INamedIndividual> processed = new HashSet<INamedIndividual>();
		
		toProcess.offer(new NamedIndividual(userUri));
		
		try {
			IOntology activeOnto = session.getOntology();
			session.setOntology(policyOntology);

			IPropertyMemberAxiomDao propMemberDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();

			while (toProcess.peek() != null) {
				INamedIndividual individual = toProcess.poll();
				
				if (processed.contains(individual))
					continue;
				
				trustedAgents.add(individual);
				
				Set<IPropertyMember> props = propMemberDao.findBySourceIndividual((INamedIndividual)individual);
				for (IPropertyMember prop : props) {
					if (prop.getProperty().getUri().equals(trust)) {
						toProcess.offer((INamedIndividual)prop.getTarget());
					}
				}
				
				processed.add(individual);
			}

			session.setOntology(activeOnto);
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return trustedAgents;
	}
	
	public IOntology createOntologyFromAxioms(Set<IPropertyMember> axioms) throws Exception {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put(KbEnvironment.LOGICAL_ONTOLOGY_URI, "http://x-media.org/taskontologies/ontology1");
		params.put(KbEnvironment.PHYSICAL_ONTOLOGY_URI, "file:ontology1.xml");
		IOntology filteredOnto = session.getConnection().createOntology(params);
		session.setOntology(filteredOnto);
		IPropertyMemberAxiomDao propMemberDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
		
		for (IPropertyMember propMember : axioms) {
			if (propMember.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
				propMemberDao.insert(new NamedIndividual(((INamedIndividual)propMember.getSource()).getUri()),
						new DataProperty(propMember.getProperty().getUri()),
						new Literal(((ILiteral)propMember.getTarget()).getValue()));
			}
			else if (propMember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER){
				propMemberDao.insert(new NamedIndividual(((INamedIndividual)propMember.getSource()).getUri()),
						new ObjectProperty(propMember.getProperty().getUri()),
						new NamedIndividual(((INamedIndividual)propMember.getTarget()).getUri()));
			}
		}
		
		return filteredOnto;
	}
	
	private void copySchema(IOntology from, IOntology to) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
	}

	public void applyOrganizationalFilter(String ontologyUri, String policyOntologyUri) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();

		IOntology onto = retrieveOntology(ontologyUri);
		IOntology policyOnto = retrieveOntology(policyOntologyUri);
		String userUri = getUser();

		Emergency.checkPrecondition(userUri != null, "userUri != null");
		Emergency.checkPrecondition(onto != null, "ontology loaded");
		Emergency.checkPrecondition(policyOnto != null, "policy ontology loaded");
		
		Set<INamedIndividual> trustedAgents = getTrustedAgents(policyOnto, userUri);
		
		try {
			session.setOntology(onto);
			
			IPropertyMemberAxiomDao propMemberDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			
			Set<IPropertyMember> trustedAxioms = new HashSet<IPropertyMember>();
			
			for (INamedIndividual agent : trustedAgents) {
				trustedAxioms.addAll(propMemberDao.findByAgent(agent));
			}
			
			System.out.println(trustedAxioms);
			
			IOntology filteredOnto = createOntologyFromAxioms(trustedAxioms);
			session.setOntology(filteredOnto);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void applyTaskFilter(String ontologyUri, String policyOntologyUri, ITask task) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		IOntology onto = retrieveOntology(ontologyUri);
		IOntology policyOnto = retrieveOntology(policyOntologyUri);
		String userUri = getUser();

		Emergency.checkPrecondition(userUri != null, "userUri != null");
		Emergency.checkPrecondition(onto != null, "ontology loaded");
		Emergency.checkPrecondition(policyOnto != null, "policy ontology loaded");
		
		Set<INamedIndividual> agents = task.getAgents();
		Set<INamedIndividual> informationProviders = task.getInformationProviders();
		
		try {
			session.setOntology(onto);
			
			IPropertyMemberAxiomDao propMemberDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			
			Set<IPropertyMember> trustedAxioms = new HashSet<IPropertyMember>();
			
			for (IPropertyMember propMember : propMemberDao.findAll()) {
				Set<IProvenance> provenances = propMemberDao.getProvenances(propMember);
				
				// TODO add criteria
				for (IProvenance provenance : provenances) {
					if (agents.contains(provenance.getAgent())) {
						trustedAxioms.add(propMember);
						break;
					}
					
					if (informationProviders.contains(provenance.getSource())) {
						trustedAxioms.add(propMember);
						break;
					}
				}
			}
			
			System.out.println(trustedAxioms);

			IOntology filteredOnto = createOntologyFromAxioms(trustedAxioms);
			session.setOntology(filteredOnto);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub

	}

	public void disposeService() {
		// TODO Auto-generated method stub

	}

	public void init(Object... params) {
		// TODO Auto-generated method stub

	}

}
