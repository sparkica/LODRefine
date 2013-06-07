package org.deri.grefine.rdf.update.commands;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.update.EndpointService;
import org.deri.grefine.rdf.update.EndpointServiceManager;
import org.json.JSONObject;

import com.google.refine.browsing.Engine;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;

//Upload to Virtuoso
public class UploadRdfCommand extends Command {

    private final EndpointServiceManager serviceManager;

    public UploadRdfCommand(final EndpointServiceManager manager)
    {
        this.serviceManager = manager;
    }
    

    //TODO: what if there is a huge amount of triples? 
    //TODO: Can I provide some feedback how much data has been uploaded?
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Project project = getProject(request);
            Engine engine = getEngine(request, project);
            JSONObject o_param = ParsingUtilities.evaluateJsonStringToObject(request.getParameter("params")); 
            
            
            String graph = o_param.getString("graph");
            Boolean existingEndpoint = Boolean.parseBoolean(o_param.getString("existing"));
            
            JSONObject o_schema = ParsingUtilities.evaluateJsonStringToObject(request.getParameter("schema"));
            RdfSchema schema = RdfSchema.reconstruct(o_schema);
            EndpointService service;
            
            JSONObject service_details = o_param.getJSONObject("endpoint");
            String endpoint_name = service_details.getString("name");
            String type = service_details.getString("type");

           
           service = serviceManager.getOrCreateEndpoint(endpoint_name, type);
           
            if (!existingEndpoint)
            {
                URL endpoint_url = new URL(service_details.getString("url"));
                service.setEndpointUrl(endpoint_url);
                service.setProperty("auth-method",service_details.getString("auth"));
                service.setProperty("default-graph", service_details.getString("graph"));
                service.setProperty("username", service_details.getString("username"));
                service.setProperty("password", service_details.getString("password"));
            }

            String result = service.executeQuery(graph, schema, project, engine);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");
         
            respond(response, result);

        }catch (Exception e) {
            respondException(response, e);
        }
    }

}
