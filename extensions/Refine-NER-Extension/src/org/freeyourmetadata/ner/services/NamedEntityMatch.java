package org.freeyourmetadata.ner.services;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;


/**
 * A named entity match, composed by a label a URI and a score.
 * @author Stefano Parmesan
 */
public class NamedEntityMatch {
    private final String label;
    private final URI uri;
    private final double score;

    /**
     * Creates a new named entity match without URI
     * @param label The label of the entity
     */
    public NamedEntityMatch(final String label) {
        this(label, null, 1.0);
    }

    /**
     * Creates a new named entity match with URI
     * @param label The label of the entity
     * @param uri The URI of the entity
     */
    public NamedEntityMatch(final String label, final URI uri) {
        this(label, uri, 1.0);
    }

    /**
     * Creates a new named entity match
     * @param label The label of the entity
     * @param uri The URI of the entity
     * @param score The match score
     */
    public NamedEntityMatch(final String label, final URI uri, final double score) {
        URI tmpUri;
        try {
            tmpUri = (uri != null) ? uri : new URI("");
        } catch (URISyntaxException e) {
            tmpUri = null;
        }

        this.label = label;
        this.uri = tmpUri;
        this.score = score;
    }

    /**
     * Creates a new named entity match from a JSON representation
     * @param json The JSON representation of the named entity
     * @throws JSONException if the JSON is not correctly structured
     */
    public NamedEntityMatch(final JSONObject json) throws JSONException {
        URI uri;
        try {
            uri = new URI(json.getString("uri"));
        } catch (URISyntaxException e) {
            uri = null;
        }

        this.label = json.getString("label");
        this.uri = uri;
        this.score = json.getDouble("score");
    }

    /**
     * Gets the entity's label
     * @return The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the entity's URI
     * @return The URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Gets the entity's score
     * @return The score
     */
    public double getScore() {
        return score;
    }

    /**
     * Writes the named entity in a JSON representation
     * @param json The JSON writer
     * @throws JSONException if an error occurs during writing
     */
    public void writeTo(final JSONWriter json) throws JSONException {
        json.object();
        json.key("label"); json.value(getLabel());
        json.key("uri"); json.value(getUri().toString());
        json.key("score"); json.value(getScore());
        json.endObject();
    }
}
