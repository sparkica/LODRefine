
package com.google.refine.crowdsourcing.crowdflower;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.browsing.Engine;
import com.google.refine.browsing.FilteredRows;
import com.google.refine.browsing.RowVisitor;
import com.google.refine.commands.Command;
import com.google.refine.crowdsourcing.CrowdsourcingUtil;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.util.ParsingUtilities;
import com.zemanta.crowdflower.client.CrowdFlowerClient;

public class ImageReconJobCommand extends Command {

        static final Logger logger = LoggerFactory.getLogger("crowdflower_image_recon");
        protected List<Integer> _cell_indeces;

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                        IOException {

                Project project = getProject(request);
                try {

                        JSONObject extension = new JSONObject(request.getParameter("extension"));
                        Engine engine = getEngine(request, project);

                        String apiKey = (String) CrowdsourcingUtil.getPreference("crowdflower.apikey");
                        Object defTimeout = CrowdsourcingUtil.getPreference("crowdflower.defaultTimeout");
                        String defaultTimeout = (defTimeout != null) ? (String) defTimeout : "1500";

                        CrowdFlowerClient cf_client = new CrowdFlowerClient(apiKey, Integer.valueOf(defaultTimeout));

                        response.setCharacterEncoding("UTF-8");
                        response.setHeader("Content-Type", "application/json");

                        if (extension.has("job_id") && !extension.isNull("job_id")) {
                                // do something

                                StringBuffer data = generateObjectsForUpload(extension, project, engine);
                                String msg = cf_client.bulkUploadJSONToExistingJob(extension.getString("job_id"),
                                                data.toString());

                                JSONObject result = ParsingUtilities.evaluateJsonStringToObject(msg);

                                if (result.has("response") && !result.getString("status").equals("ERROR")) {
                                        generateResponse(response, result);
                                } else {
                                        generateErrorResponse(response, result);
                                }

                        } else {

                                JSONObject obj = new JSONObject();
                                obj.put("status", "ERROR");
                                obj.put("message", "No job id was specified.");
                                generateErrorResponse(response, obj);
                        }

                } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

        }

        private void generateResponse(HttpServletResponse response, JSONObject data) throws IOException, JSONException {
                Writer w = response.getWriter();
                JSONWriter writer = new JSONWriter(w);
                try {
                        writer.object();
                        writer.key("status");
                        writer.value(data.get("status"));
                } catch (Exception e) {
                        logger.error("Generating response failed.");
                } finally {
                        writer.endObject();
                        w.flush();
                        w.close();
                }
        }

        private void generateErrorResponse(HttpServletResponse response, JSONObject data) throws IOException,
                        JSONException {
                Writer w = response.getWriter();
                JSONWriter writer = new JSONWriter(w);
                try {
                        writer.object();
                        writer.key("status");
                        writer.value(data.get("status"));
                        writer.key("message");
                        writer.value(data.get("message"));
                } catch (Exception e) {
                        logger.error("Generating ERROR response failed.");
                } finally {
                        writer.endObject();
                        w.flush();
                        w.close();
                }
        }

        private StringBuffer generateObjectsForUpload(JSONObject extension, Project project, Engine engine)
                        throws JSONException {
                _cell_indeces = new ArrayList<Integer>();

                Column golden_col = null;
                String golden_col_name = "";

                if (extension.has("golden_column")) {
                        golden_col_name = extension.getString("golden_column");
                }
                if (!golden_col_name.equals("")) {
                        golden_col = project.columnModel.getColumnByName(golden_col_name);
                }
                int gold_index = (golden_col != null) ? golden_col.getCellIndex() : -1;

                JSONArray column_names = extension.getJSONArray("column_names");

                for (int i = 0; i < column_names.length(); i++) {
                        Column col = project.columnModel.getColumnByName(column_names.getJSONObject(i)
                                        .getString("name"));
                        _cell_indeces.add(col.getCellIndex());
                }

                FilteredRows rows = engine.getAllFilteredRows();
                List<Integer> rows_indeces = new ArrayList<Integer>();

                // for each row create JSON object
                rows.accept(project, new RowVisitor() {

                        List<Integer> _rowIndices;

                        public RowVisitor init(List<Integer> rowIndices) {
                                _rowIndices = rowIndices;
                                return this;
                        }

                        @Override
                        public void start(Project project) {
                                // nothing to do
                        }

                        @Override
                        public void end(Project project) {
                                // nothing to do

                        }

                        @Override
                        public boolean visit(Project project, int rowIndex, Row row) {
                                // check cells in any of the selected columns

                                for (int k = 0; k < _cell_indeces.size(); k++) {

                                        Cell cell = row.getCell(_cell_indeces.get(k));
                                        if (cell != null) { // as soon a value
                                                            // is encountered in
                                                            // any of selected
                                                            // columns, add row
                                                            // index
                                                _rowIndices.add(rowIndex);
                                                break;
                                        }
                                }
                                return false;
                        }
                }.init(rows_indeces));

                StringBuffer bf = new StringBuffer();

                // TODO: what if there are records and not rows!
                for (Iterator<Integer> it = rows_indeces.iterator(); it.hasNext();) {
                        int row_index = it.next();

                        JSONObject obj = new JSONObject();
                        for (int c = 0; c < column_names.length(); c++) {
                                int cell_index = _cell_indeces.get(c);

                                String key = column_names.getJSONObject(c).getString("safe_name");
                                Object value = project.rows.get(row_index).getCellValue(cell_index);

                                if (value != null) {
                                        obj.put(key, value.toString());
                                } else {
                                        obj.put(key, "");
                                }
                        }

                        // TODO: generate objects for upload
                        // image url, anchor, site url
                        // we need wikipedia urls
                        // additional description, additional columns?

                        // TODO: add gold data if necessary

                        bf.append(obj.toString());

                }
                return bf;
        }

}
