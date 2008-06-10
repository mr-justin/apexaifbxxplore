package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IComputerAidedProcess;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IDevice;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IProcess;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;

public class ProcessDao extends AbstractDao implements IProcessDao {

	private static IProcessDao m_instance = null;

	private final String IS_POST = "is_post";
	private final String RESOURCE = "resource";
	private final String EXPERIENCER = "experiencer";
	private final String INSTRUMENT = "instrument";

	
	private ProcessDao() {}

	
	public static IProcessDao getInstance() {
		if (m_instance == null) m_instance = new ProcessDao();
		return m_instance;
	}

	public void setAgent(IProcess process, ICognitiveAgent agent) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction)
				((IApplicationInteraction) process)
						.storeApplicationInteraction();
			else if (process instanceof IComputerAidedProcess)
				((IComputerAidedProcess) process).storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual agentInd = findIndividualByUri(agent.getUri());
		if (agentInd == null) {
			agent.storeCognitiveAgent();
			agentInd = findIndividualByUri(agent.getUri());
		}

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(processInd.getUri());
			URI predicate = factory.createURI(getEntityUri(EXPERIENCER));
			URI object = factory.createURI(agentInd.getUri());

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

	public void setPostProcess(IProcess process, IProcess postProcess) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction)
				((IApplicationInteraction) process)
						.storeApplicationInteraction();
			else if (process instanceof IComputerAidedProcess)
				((IComputerAidedProcess) process).storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual postProcessInd = findIndividualByUri(postProcess
				.getUri());
		if (postProcessInd == null) {
			if (postProcess instanceof IApplicationInteraction)
				((IApplicationInteraction) postProcess)
						.storeApplicationInteraction();
			else if (postProcess instanceof IComputerAidedProcess)
				((IComputerAidedProcess) postProcess)
						.storeComputerAidedProcess();
			postProcessInd = findIndividualByUri(postProcess.getUri());
		}

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(processInd.getUri());
			URI predicate = factory.createURI(getEntityUri(IS_POST));
			URI object = factory.createURI(postProcessInd.getUri());

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

	public void setResource(IProcess process, IContent content) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction)
				((IApplicationInteraction) process)
						.storeApplicationInteraction();
			else if (process instanceof IComputerAidedProcess)
				((IComputerAidedProcess) process).storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(processInd.getUri());
			URI predicate = factory.createURI(getEntityUri(RESOURCE));
			URI object = factory.createURI(contentInd.getUri());

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

	public void setResource(IProcess process, IContentBearingObject cbo) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction)
				((IApplicationInteraction) process)
						.storeApplicationInteraction();
			else if (process instanceof IComputerAidedProcess)
				((IComputerAidedProcess) process).storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		}

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(processInd.getUri());
			URI predicate = factory.createURI(getEntityUri(RESOURCE));
			URI object = factory.createURI(cboInd.getUri());

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

	public void setInstrument(IProcess process, IDevice instrument) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction)
				((IApplicationInteraction) process)
						.storeApplicationInteraction();
			else if (process instanceof IComputerAidedProcess)
				((IComputerAidedProcess) process).storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual instrumentInd = findIndividualByUri(instrument
				.getUri());
		if (instrumentInd == null) {
			instrument.storeDevice();
			instrumentInd = findIndividualByUri(instrument.getUri());
		}

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(processInd.getUri());
			URI predicate = factory.createURI(getEntityUri(INSTRUMENT));
			URI object = factory.createURI(instrumentInd.getUri());

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
}
