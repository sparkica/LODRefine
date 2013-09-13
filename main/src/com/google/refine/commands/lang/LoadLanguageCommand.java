
package com.google.refine.commands.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
<<<<<<< HEAD
=======
import java.io.Reader;
import java.util.Arrays;
>>>>>>> refine26

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

<<<<<<< HEAD
import com.google.refine.ProjectManager;
import com.google.refine.commands.Command;
=======
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.refine.ProjectManager;
import com.google.refine.commands.Command;
import com.google.refine.preference.PreferenceStore;

import edu.mit.simile.butterfly.ButterflyModule;

>>>>>>> refine26

public class LoadLanguageCommand extends Command {

    public LoadLanguageCommand() {
<<<<<<< HEAD
        // TODO Auto-generated constructor stub
=======
    	super();
>>>>>>> refine26
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

<<<<<<< HEAD
        String rawDirectoryFile = request.getSession().getServletContext().getRealPath("webapp/modules/langs/");
        String cleanedDirectory = rawDirectoryFile.replace("main" + File.separator + "webapp" + File.separator, "main"
                + File.separator);

        BufferedReader reader = null;
        String param = null;
        try {
            param = (String) ProjectManager.singleton.getPreferenceStore().get("userLang");
        } catch (NullPointerException e) {
        }
        if (param == null) param = request.getParameter("lng");

        String[] langs = param.split(" ");
        try {
            String file = cleanedDirectory + File.separator + "translation-" + langs[0] + ".json";
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (FileNotFoundException e1) {
            try {
                String file = cleanedDirectory + File.separator + "translation-default.json";
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            } catch (FileNotFoundException e3) {
                e3.printStackTrace();
            }
        }

        String line = null;
        String message = new String();
        if (reader != null) {
            while ((line = reader.readLine()) != null) {
                // buffer.append(line);
                message += line + System.getProperty("line.separator");
            }
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(message);
        response.getWriter().flush();
        response.getWriter().close();
=======
        String modname = request.getParameter("module");
        if (modname == null) {
            modname = "core";
        }
        ButterflyModule module = this.servlet.getModule(modname);

        String[] langs = request.getParameterValues("lang");
        if (langs == null || "".equals(langs[0])) {
            PreferenceStore ps = ProjectManager.singleton.getPreferenceStore();
            if (ps != null) {
                langs = new String[] {(String) ps.get("userLang")};
            }
        }
        langs = Arrays.copyOf(langs, langs.length+1);
        langs[langs.length-1] = "default";

        JSONObject json = null;
        boolean loaded = false;
        for (String lang : langs) {
            File langFile = new File(module.getPath(), "langs" + File.separator + "translation-" + lang + ".json");
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), "UTF-8"));
                json = new JSONObject(new JSONTokener(reader));
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                json.write(response.getWriter());
                response.getWriter().flush();
                response.getWriter().close();
                loaded = true;
                break;
            } catch (FileNotFoundException e1) {
                json = null;
                continue;
            } catch (JSONException e) {
                json = null;
                logger.error("JSON error reading/writing language file", e);
                continue;
            }
        }
        if (!loaded) {
        	logger.error("Failed to load any language files");
        }
>>>>>>> refine26
    }
}
