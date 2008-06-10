package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.ApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IComputerAidedProcess;
import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;

public class ApplicationInteractionDao extends AbstractDao implements IApplicationInteractionDao {
	
	
	private static IApplicationInteractionDao m_instance = null;
	private final String APPLICATION_INTERACTION = "Application_Interaction";
	private final String IS_PART_OF = "is_part_of";
	
	
	private ApplicationInteractionDao() {}
	
	public static IApplicationInteractionDao getInstance() {
		if(m_instance == null)
			m_instance = new ApplicationInteractionDao();
		return m_instance;
	}

	public void setComputerAidedProcess(IApplicationInteraction interaction, IComputerAidedProcess process) {
		INamedIndividual interactionInd = findIndividualByUri(interaction.getUri());
		if (interactionInd == null) {
			interaction.storeApplicationInteraction();
			interactionInd = findIndividualByUri(interaction.getUri());
		} 
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (process == null) {
			process.storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		} 
		
//		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
//				KAON2Manager.factory().objectProperty(getEntityUri(IS_PART_OF)),
//				(Individual)interactionInd.getDelegate(),
//				(Individual)processInd.getDelegate());
//		
//		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
//		try {
//			IOntology onto = getAdaptationOntology();
//			if (onto instanceof Kaon2Ontology)
//				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
//		} catch (KAON2Exception e) {
//			e.printStackTrace();
//		}
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(interactionInd.getUri());
			URI predicate = factory.createURI(getEntityUri(IS_PART_OF));
			URI object = factory.createURI(processInd.getUri());

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,predicate,object);
				}
				finally
				{
					con.close();
				}
			} 
			catch (RepositoryException e) 
			{

				e.printStackTrace();
			}
		}
	}
	
	public void insert(IApplicationInteraction interaction) {
		
		Emergency.checkPrecondition(interaction instanceof ApplicationInteraction,"interaction instanceof ApplicationInteraction");

//		Axiom axiom = KAON2Manager.factory().classMember(
//				KAON2Manager.factory().owlClass(getEntityUri(APPLICATION_INTERACTION)),
//				KAON2Manager.factory().individual(interaction.getUri()));
//
//		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
//		try {
//			IOntology onto = getAdaptationOntology();
//			if (onto instanceof Kaon2Ontology)
//				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
//		} catch (KAON2Exception e) {
//			e.printStackTrace();
//		}
		
		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(interaction.getUri());
			URI object = factory.createURI(getEntityUri(APPLICATION_INTERACTION));

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject, RDF.TYPE, object);
				}
				finally
				{
					con.close();
				}
			} 
			catch (RepositoryException e) 
			{

				e.printStackTrace();
			}
		}
	}
}
