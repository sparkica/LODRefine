package com.zemanta.dbpedia.util;

import com.google.refine.ProjectManager;
import com.google.refine.preference.PreferenceStore;


public class ZemantaUtil {

        public static Object getPreference(String prefName) {
                PreferenceStore ps = ProjectManager.singleton.getPreferenceStore();
                Object pref = ps.get(prefName);

                return pref;
        }



}
