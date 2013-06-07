package org.deri.grefine.rdf.update;

import java.net.MalformedURLException;
import java.net.URL;


public class SparqlUpdateEndpointGeneric extends SparqlUpdateEndpointServiceFactory {

	public SparqlUpdateEndpointGeneric() throws MalformedURLException {
		super();
	}
	
	public SparqlUpdateEndpointGeneric(final URL endpoint) throws MalformedURLException {
		super(endpoint);
	}

}
