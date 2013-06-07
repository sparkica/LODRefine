package org.deri.grefine.rdf.update;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.deri.grefine.rdf.Node;
import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.exporters.RdfExporter.RdfRowVisitor;
import org.mortbay.jetty.HttpStatus;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.google.refine.browsing.Engine;
import com.google.refine.browsing.FilteredRows;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.util.ParsingUtilities;


public abstract class SparqlUpdateEndpointServiceFactory implements EndpointService {

	private URL endpoint;
	private final String[] propertyNames;
	private final HashMap<String, String> properties;

	private final static String[] PROPERTY_NAMES = { "auth-method","username","password","default-graph" };
	private final static String LOCAL_VIRTUOSO_ENDPOINT = "http://localhost:8890/sparql-auth";
	
	public SparqlUpdateEndpointServiceFactory() throws MalformedURLException {
		this(new URL(LOCAL_VIRTUOSO_ENDPOINT), PROPERTY_NAMES);
		properties.put("auth-method", "digest");
	}

	//default: locally installed virtuoso
	public SparqlUpdateEndpointServiceFactory(final URL endpoint) throws MalformedURLException {
		this(endpoint, PROPERTY_NAMES);
	}

	public SparqlUpdateEndpointServiceFactory(final URL serviceUrl, final String[] propNames) throws MalformedURLException {
		this.endpoint = serviceUrl;
		this.propertyNames = propNames;

		properties = new HashMap<String, String> (propNames.length);
		for (String propName : propNames) {
			this.properties.put(propName, "");
		}
	}

	public void setEndpointUrl(URL newEndpoint) {
		endpoint = newEndpoint;
	}
	
	public URL getEndpointUrl() {
		return endpoint;
	}

	public HashMap<String, String> getProperties() {
		return properties;
	}

	@Override
	public String[] getPropertyNames() {
		// TODO Auto-generated method stub
		return propertyNames;
	}

	@Override
	public String getProperty(String name) {
		// TODO Auto-generated method stub
		return properties.get(name);
	}

	@Override
	public void setProperty(String name, String value) {
		if(!properties.containsKey(name)) {
			String msg = "The property " + name + "is invalid for " + getClass().getName() + ".";
			throw new IllegalArgumentException(msg);
		}

		properties.put(name, value == null ? "" : value);

	}


	public final String generateTriples(Repository model) {
		StringWriter triples = new StringWriter();
		try{
			RepositoryConnection con = model.getConnection();
			try{
				RDFWriter w = Rio.createWriter(RDFFormat.TURTLE, triples);
				con.export(w);
			}finally{
				con.close();
			}
		}catch(RepositoryException ex){
			throw new RuntimeException(ex);
		}catch(RDFHandlerException ex){
			throw new RuntimeException(ex);
		}

		return triples.getBuffer().toString();
	}

	public String buildQuery(String graph, Repository model, RdfSchema schema) {

		StringBuilder query = new StringBuilder();
		String triples = generateTriples(model);

		//at this point we don't add prefixes to the query
		String defaultGraph = graph != null ? graph : properties.get("default-graph");

		query.append(INSERT_SPARQL_TEMPLATE
				.replace("[[GRAPH]]", defaultGraph)
				.replace("[[DATA_TRIPLES]]", triples));

		return query.toString();
	}

	@Override
	public String executeQuery(String graph, RdfSchema schema, Project project, Engine engine) throws Exception {
		
		String auth_type = getProperty("auth-method");
		String result = "";
		
		Repository model = buildModel(project, engine, schema);
		String query = buildQuery(graph, model, schema);
		
		//todo: add other authorization methods
		if (auth_type.equals("digest")) {
			result = executeSparqlUpdateQueryDigestAuth(query);
		}
		else {
			result = "{\"status\":\"error\", \"message\": \"This authentication method is not (yet) supported.\"}";
		}
		
		return result;
	}
	
	public String executeSparqlUpdateQueryDigestAuth(String query) throws URISyntaxException, ClientProtocolException, IOException {

		
		String result = "";

		DefaultHttpClient client = new DefaultHttpClient();
		client.getCredentialsProvider().setCredentials(
				new AuthScope(endpoint.getHost(),endpoint.getPort()),
				new UsernamePasswordCredentials(properties.get("username"), properties.get("password"))
				);
		client.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, AuthPolicy.DIGEST);

		HttpPost httpPost = new HttpPost(endpoint.toURI());

		try {
        		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        		nvps.add(new BasicNameValuePair("query", query));
        		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        		HttpResponse response = client.execute(httpPost);
        		
        
        		if(response.getStatusLine().getStatusCode()== HttpStatus.ORDINAL_200_OK) {
        			result =  "{\"status\": \"ok\"}";
        		}
        		else {
        			result =  "{\"status\": \"error\", \"message\": " + 
        						ParsingUtilities.encode(response.getStatusLine().getReasonPhrase()) + 
        						"}";
        		}
		} catch(Exception e) {
		    result =  "{\"status\": \"error\", \"message\": \"" + StringEscapeUtils.escapeJava(e.getLocalizedMessage())
                              + "\"}";
		}

		return result;
	}

	
	//TODO: can I build and upload nodes in batch?
	//there should be some limitations regarding amount of triples;
	public Repository buildModel(final Project project, Engine engine, RdfSchema schema) throws IOException{
		RdfRowVisitor visitor = new RdfRowVisitor(schema) {

			int _count = 0;
			@Override
			public boolean visit(Project project, int rowIndex, Row row) {
				if(_count >= MAX_LIMIT_TRIPLES){
					//todo: a warning that there is a huge number of triples to be uploaded?
					return true;
				}
				for(Node root:roots){
					root.createNode(baseUri, factory, con, project, row, rowIndex,blanks);
				}
				_count++;
				return false;
			}
		};
		Repository model = buildModel(project, engine,visitor);

		return model;
	}

	public static Repository buildModel(Project project, Engine engine, RdfRowVisitor visitor) {
		FilteredRows filteredRows = engine.getAllFilteredRows();
		filteredRows.accept(project, visitor);
		return visitor.getModel();

	}


	//TODO: figure out what is the maximal number before this gets too slow
	//TODO: it might be smarter to put this into preferences
	private static final long MAX_LIMIT_TRIPLES = 1000; 

	private static final String INSERT_SPARQL_TEMPLATE = "INSERT DATA INTO <[[GRAPH]]> " +
			"{ [[DATA_TRIPLES]]" + " }";



}
