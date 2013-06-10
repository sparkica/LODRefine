package org.deri.grefine.rdf.update;

import java.net.URL;

import org.deri.grefine.rdf.RdfSchema;
import com.google.refine.browsing.Engine;
import com.google.refine.model.Project;

/**
 * Interface for SPARQ UPDATE Endpoints
 * @author mateja
 *
 */

public interface EndpointService {

	public void setEndpointUrl(URL endpoint);
	public URL getEndpointUrl();
	public String[] getPropertyNames();	
	public String getProperty(String name);
	public void setProperty(String name, String value);
	//public String executeSparqlUpdateQueryDigestAuth(String graph, Repository model, RdfSchema schema);
	public String executeQuery(String graph, RdfSchema schema, Project project, Engine engine) throws Exception;
}
