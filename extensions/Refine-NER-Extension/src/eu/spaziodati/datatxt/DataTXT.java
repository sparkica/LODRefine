package eu.spaziodati.datatxt.services;

import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.freeyourmetadata.ner.services.NERServiceBase;
import org.freeyourmetadata.ner.services.NamedEntity;

/**
 * dataTXT service connector
 * @author Stefano Parmesan
 */
public class DataTXT extends NERServiceBase {
    private final static URI SERVICEBASEURL = createUri("http://spaziodati.eu/datatxt/v3/");
    private final static URI DOCUMENTATIONURI = createUri("http://spaziodati.3scale.net/");
    private final static String[] PROPERTYNAMES = { "APP ID", "APP key" };

    /**
     * Creates a new Alchemy service connector
     */
    public DataTXT() {
        super(SERVICEBASEURL, PROPERTYNAMES, DOCUMENTATIONURI);
    }

    /** {@inheritDoc} */
    public boolean isConfigured() {
        return getProperty("APP ID").length()>0 && getProperty("APP key").length()> 0;
    }

    /** {@inheritDoc} */
    protected HttpEntity createExtractionRequestBody(final String text) throws UnsupportedEncodingException {
        final ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(5);
        // TODO[sp] permettere di impostare tutto a parte service
        parameters.add(new BasicNameValuePair("service", "tag"));
        parameters.add(new BasicNameValuePair("lang", "it"));
        parameters.add(new BasicNameValuePair("text", text));
        parameters.add(new BasicNameValuePair("rho", "0.1"));
        parameters.add(new BasicNameValuePair("epsilon", "0.3"));
        parameters.add(new BasicNameValuePair("long_text", ""));
        parameters.add(new BasicNameValuePair("dbpedia", "true"));
        parameters.add(new BasicNameValuePair("include_abstract", "false"));
        parameters.add(new BasicNameValuePair("app_id", getProperty("APP ID")));
        parameters.add(new BasicNameValuePair("app_key", getProperty("APP key")));
        return new UrlEncodedFormEntity(parameters);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    protected NamedEntity[] parseExtractionResponseEntity(final JSONTokener tokener) throws JSONException {
        final JSONObject response = (JSONObject)tokener.nextValue();

        if (!response.isNull("error"))
            throw new IllegalArgumentException("dataTXT request failed.");

        final JSONArray annotations = response.getJSONArray("annotations");
        final NamedEntity[] results = new NamedEntity[annotations.length()];
        for (int i = 0; i < results.length; i++) {
            final JSONObject annotation = annotations.getJSONObject(i);
            final String label = annotation.getString("title");

            annotation.getJSONArray("ref");
            final JSONArray refList = annotation.getJSONArray("ref");
            final URI[] uris = new URI[refList.length()];
            for (int j=0; j<refList.length(); j++) {
                final JSONObject ref = refList.getJSONObject(j);
                for (Iterator<String> key = ref.keys(); key.hasNext(); ) {
                    uris[j] = createUri(ref.getString(key.next()));
                }
            }
            results[i] = new NamedEntity(label, uris);
        }
        return results;
    }
}
