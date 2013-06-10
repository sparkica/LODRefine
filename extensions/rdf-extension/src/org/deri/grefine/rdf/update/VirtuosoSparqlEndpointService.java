package org.deri.grefine.rdf.update;

import java.net.MalformedURLException;
import java.net.URL;

import org.deri.grefine.rdf.RdfSchema;
import org.openrdf.repository.Repository;


public class VirtuosoSparqlEndpointService extends SparqlUpdateEndpointServiceFactory {
	
	public VirtuosoSparqlEndpointService() throws MalformedURLException {
		super();
	}
	
	public VirtuosoSparqlEndpointService(final URL serviceUrl) throws MalformedURLException{
		super(serviceUrl);
	}
	
	public VirtuosoSparqlEndpointService(final URL serviceUrl, final String[] propNames) throws MalformedURLException {
		super(serviceUrl, propNames);
		
	}
	

	@Override
	public String buildQuery(String graph, Repository model, RdfSchema schema) {
	
		StringBuilder query = new StringBuilder();
		String triples = generateTriples(model);

		//at this point we don't add prefixes to the query
		String defaultGraph = graph != null ? graph : getProperty("default-graph");

		query.append(VIRTUOSO_SPARQL_INSERT_TEMPLATE
				.replace("[[GRAPH]]", defaultGraph)
				.replace("[[DATA_TRIPLES]]", triples));

		return query.toString();
	}
	
	private static final String VIRTUOSO_SPARQL_INSERT_TEMPLATE = "INSERT DATA IN <[[GRAPH]]> {" +
																	"[[DATA_TRIPLES]]" + 
																	"}";
}
