package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.ComputerAidedProcess;
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



public class ComputerAidedProcessDao extends AbstractDao implements IComputerAidedProcessDao {

	private static IComputerAidedProcessDao m_instance = null;
	
	private final String COMPUTER_AIDED_PROCESS = "Computer-aided_Process";
	private final String HAS_PART = "has_part";

	
	private ComputerAidedProcessDao() {}

	
	public static IComputerAidedProcessDao getInstance() {
		if(m_instance == null)
			m_instance = new ComputerAidedProcessDao();
		return m_instance;
	}
	
	public void setApplicationInteraction(IComputerAidedProcess process,IApplicationInteraction interaction) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			process.storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual interactionInd = findIndividualByUri(interaction.getUri());
		if (interactionInd == null) {
			interaction.storeApplicationInteraction();
			interactionInd = findIndividualByUri(interaction.getUri());
		}

		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(processInd.getUri());
			URI predicate = factory.createURI(getEntityUri(HAS_PART));
			URI object = factory.createURI(interactionInd.getUri());

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,predicate, object);
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
	
	public void insert(IComputerAidedProcess process) {
		
		Emergency.checkPrecondition(process instanceof ComputerAidedProcess,"process instanceof ComputerAidedProcess");

		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(process.getUri());
			URI object = factory.createURI(getEntityUri(COMPUTER_AIDED_PROCESS));

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
