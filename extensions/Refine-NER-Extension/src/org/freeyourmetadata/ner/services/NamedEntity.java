package org.freeyourmetadata.ner.services;

import java.lang.String;
import java.net.URI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.refine.model.Cell;
import com.google.refine.model.Recon;
import com.google.refine.model.ReconCandidate;
import com.google.refine.model.Recon.Judgment;

/**
 * A named entity with a label and URIs
 * @author Ruben Verborgh
 * @author Stefano Parmesan
 */
public class NamedEntity {
    private final static URI[] EMPTY_URI_SET = new URI[0];
    private final static String[] EMPTY_TYPE_SET = new String[0];
    
    private final String label;
    private final NamedEntityMatch[] matches;
    
    /**
     * Creates a new named entity without URIs
     * @param label The label of the entity
     */
    public NamedEntity(final String label) {
        this(label, new NamedEntityMatch[] {new NamedEntityMatch(label)});
    }
    
    /**
     * Creates a new named entity with a single URI
     * @param label The label of the entity
     * @param uri The URI of the entity
     */
    public NamedEntity(final String label, final URI uri) {
        this(label, new NamedEntityMatch[] {new NamedEntityMatch(label, uri)});
    }
    
    /**
     * Creates a new named entity
     * @param label The label of the entity
     * @param uris The URIs of the entity
     */
    public NamedEntity(final String label, final URI[] uris) {
        this.label = label;
        this.matches = new NamedEntityMatch[uris.length];
        for (int i=0; i<uris.length; i++)
            matches[i] = new NamedEntityMatch(label, uris[i]);
    }

    /**
     * Creates a new named entity
     * @param label The label matched in the original text
     * @param matches A list of NamedEntityMatch, each with label, URI and score
     */
    public NamedEntity(final String label, final NamedEntityMatch[] matches) {
        this.label = label;
        this.matches = matches;
    }
    
    /**
     * Creates a new named entity from a JSON representation
     * @param json The JSON representation of the named entity
     * @throws JSONException if the JSON is not correctly structured
     */
    public NamedEntity(final JSONObject json) throws JSONException {
        this.label = json.getString("label");
        final JSONArray jsonMatches = json.getJSONArray("matches");
        this.matches = new NamedEntityMatch[jsonMatches.length()];
        for (int i = 0; i < matches.length; i++) {
            matches[i] = new NamedEntityMatch(jsonMatches.getJSONObject(i));
        }
    }

    /**
     * Gets the entity's label
     * @return The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the entity's matches
     * @return The matches
     */
    public NamedEntityMatch[] getMatches() {
        return matches;
    }
    
    /**
     * Writes the named entity in a JSON representation
     * @param json The JSON writer
     * @throws JSONException if an error occurs during writing
     */
    public void writeTo(final JSONWriter json) throws JSONException {
        json.object();
        json.key("label"); json.value(getLabel());
        json.key("matches");
        json.array();
        for (final NamedEntityMatch match : getMatches())
            match.writeTo(json);
        json.endArray();
        json.endObject();
    }
    
    /**
     * Convert the named entity into a Refine worksheet cell
     * @return The cell
     */
    public Cell toCell() {
        Recon recon = new Recon(-1L, "", "");
        int bestMatchIndex = -1;
        double bestMatchScore = -1.0;

        // add all the candidates, and find the best one
        for (int i=0; i<matches.length; i++) {
            final NamedEntityMatch match = matches[i];
            final String uriString = match.getUri().toString();
            if (uriString.length() > 0) {
                recon.addCandidate(new ReconCandidate(uriString, match.getLabel(), EMPTY_TYPE_SET, match.getScore()));
                if (match.getScore() > bestMatchScore) {
                    bestMatchScore = match.getScore();
                    bestMatchIndex = i;
                }
            }
        }

        if (bestMatchIndex == -1) {
            // no matches, or with empty URI
            recon = null;
        } else {
            // set the match and judgment
            recon.match = recon.candidates.get(bestMatchIndex);
            recon.matchRank = bestMatchIndex;
            recon.judgment = Judgment.Matched;
            recon.judgmentAction = "auto";
            recon.service = "NamedEntity";
        }

        return new Cell(getLabel(), recon);
    }
}
