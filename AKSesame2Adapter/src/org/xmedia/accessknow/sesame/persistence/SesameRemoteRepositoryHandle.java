package org.xmedia.accessknow.sesame.persistence;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

public class SesameRemoteRepositoryHandle {

	private static final String SERVER_URL_PARAMETER = "server_url";
	private static final String REPOSITORIES_PATH = "repositories/";
	
	private URI ontologyUri;
	private URL serverUrl;
	
	private Repository httpRepository = null;
	private Repository remoteSystem = null;
	
	public static URI buildRemoteOntologyUri(String ontologyUri, String sesameServerUrl) {
		try {
			return new URI(ontologyUri + getUrlParameterFragment() + sesameServerUrl);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String getUrlParameterFragment() {
		return "?" + SERVER_URL_PARAMETER + "=";
	}
	
	protected static SesameRemoteRepositoryHandle buildRemoteHandle(URI remoteOntologyUri) throws URISyntaxException, MalformedURLException {
		
		SesameRemoteRepositoryHandle remoteHandle = null;
		
		String ontologyUriValue = remoteOntologyUri.toString();
		String serverUrlParamFragment = getUrlParameterFragment();
		int paramStart;
		if ( (paramStart = ontologyUriValue.indexOf(serverUrlParamFragment)) > 0)
			remoteHandle = new SesameRemoteRepositoryHandle(
					new URI(ontologyUriValue.substring(0, paramStart)),
					new URL(ontologyUriValue.substring(paramStart + serverUrlParamFragment.length())));
		
		return remoteHandle;
	}
	
	private SesameRemoteRepositoryHandle(URI ontolgoyUri, URL serverUrl) {
		this.ontologyUri = ontolgoyUri;
		setServerUrl(serverUrl.toString());
	}
	
	private void setServerUrl(String serverUrl) {
		
		try {
			if (serverUrl.toString().endsWith("/"))
				this.serverUrl = new URL(serverUrl);
			else
				this.serverUrl = new URL(serverUrl + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public URL getServerUrl() {
		return serverUrl;
	}
	
	public synchronized Repository loadHttpRepository() throws RepositoryException {
		
		if (httpRepository == null) {
			httpRepository = new HTTPRepository(serverUrl + REPOSITORIES_PATH + SesameRepositoryFactory.fsTransduceUri(ontologyUri));
			httpRepository.initialize();
		}
		
		return httpRepository;
		
	}
	
	public synchronized Repository getRemoteSystem() throws RepositoryException {
		
		if (remoteSystem == null) {
			remoteSystem = new HTTPRepository(serverUrl + REPOSITORIES_PATH + "SYSTEM");
			remoteSystem.initialize();
		}
		
		return remoteSystem;
	}

	public URI getOntologyUri() {
		return ontologyUri;
	}
	
}
