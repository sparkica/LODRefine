package org.deri.grefine.rdf.update;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;


/**
 * Service manager storing configuration data for SPARQL UPDATE endpoints.
 * @author: Mateja Verlic
 */

public class EndpointServiceManager {

    private final static Logger LOGGER = Logger.getLogger(EndpointServiceManager.class);
    
    private final TreeMap<String, EndpointService> services;
    private final File settingsFile;
	
    
    public EndpointServiceManager(final File fSettings) 
    		throws IOException, JSONException, ClassNotFoundException {
    	settingsFile = fSettings;
    	services = new TreeMap<String, EndpointService>();
    	
    	//load default settings
    	
    	loadServices(new InputStreamReader(this.getClass().getResourceAsStream("/files/default-endpoints")));
   
    	if (settingsFile.exists())
            loadServices(new FileReader(settingsFile));
    
    }

    private void loadServices(final Reader serviceReader) throws JSONException, ClassNotFoundException {
        try {
            final JSONTokener tokener = new JSONTokener(serviceReader);
            parseServices((JSONArray)tokener.nextValue());
            serviceReader.close();
        }
        catch (IOException e) {
            LOGGER.error("Error parsing default-endpoints file.");
        }
    }
    
    private void parseServices(JSONArray serviceList) 
    		throws JSONException{

    	for (int s_index = 0; s_index < serviceList.length(); s_index++) {
    		JSONObject o_service = serviceList.getJSONObject(s_index);
    		
    		try {
    		EndpointService service = getOrCreateService(o_service.getString("name"), o_service.getString("class"));
    		
    		if (o_service.has("settings")) {
    			final JSONObject settings = o_service.getJSONObject("settings");
    			final Iterator<?> settingNames = settings.keys();
                while (settingNames.hasNext()) {
                    String settingName = (String)settingNames.next();
                    service.setProperty(settingName, settings.getString(settingName));
                }
    		}
    		}
    		catch(ClassNotFoundException e) {
    			LOGGER.error(String.format("Could not load service with class %s.", 
    					o_service.getString("class")));
    		}
    		
    	}
    }
    
    public void addService(final String name, final EndpointService service) {
        services.put(name, service);
    }
    
    
    public EndpointService getService(final String name) {
        if (!services.containsKey(name))
            throw new IllegalArgumentException("No service named " + name + " exists.");
        return services.get(name);
    }
    
    public boolean hasService(final String serviceName) {
        return services.containsKey(serviceName);
    }
    
    public EndpointService getOrCreateEndpoint(final String endpointName, final String endpointType) {
    	
    	EndpointService service = null;
    	
    	try {
	    	if(endpointType.equals("virtuoso")) {
	    		service = getOrCreateService(endpointName, EndpointType.VIRTUOSO.getClassName());
	    	}
	    	else {
	    		service = getOrCreateService(endpointName, EndpointType.GENERIC.getClassName());
	    	}
    	} catch(ClassNotFoundException e) {
    		LOGGER.error("Class for this endpoint was not found. " + e.getLocalizedMessage());
    	}
    	return service;
    }
    
    
    protected EndpointService getOrCreateService(final String serviceName, final String className) throws ClassNotFoundException {
        EndpointService service;
        if (hasService(serviceName)) {
            service = getService(serviceName);
        }
        else {
            // Create the service through reflection
            final Class<?> serviceClass = getClass().getClassLoader().loadClass(className);
            try {
                service = (EndpointService)serviceClass.newInstance();
            }
            catch (InstantiationException error) { throw new RuntimeException(error); }
            catch (IllegalAccessException error) { throw new RuntimeException(error); }
            
            addService(serviceName, service);
        }
        return service;
    }
    
    public void save() throws IOException {
        final FileWriter writer = new FileWriter(settingsFile);
        writeTo(new JSONWriter(writer));
        writer.close();
    }
    
    
    public String[] getServiceNames() {
        return services.keySet().toArray(new String[services.size()]);
    }
    
    public void writeTo(final JSONWriter output) {
        try {
            /* Array of services */
            output.array();
            for (final String serviceName : getServiceNames()) {
                final EndpointService service = getService(serviceName);
                /* Service object */
                output.object();
                {
                    output.key("name");
                    output.value(serviceName);
                    output.key("class");
                    output.value(service.getClass().getName());
                    
                    /* Service settings object */
                    output.key("settings");
                    output.object();
                    for(final String propertyName : service.getPropertyNames()) {
                        output.key(propertyName);
                        output.value(service.getProperty(propertyName));
                    }
                    output.endObject();
                }
                output.endObject();
            }
            output.endArray();
        }
        catch (JSONException e) { /* does not happen */ }
    }
    
    
    public enum EndpointType {
	    VIRTUOSO ("org.deri.grefine.rdf.update.VirtuosoSparqlEndpointService"), 
	    GENERIC ("org.deri.grefine.rdf.update.SparqlUpdateEndpointGeneric");
	    private String className;
	    
	    private EndpointType(String name) {
	    	className = name;
	    }
	    
	    public String getClassName() {
	    	return className;
	    }
    }

    
}
