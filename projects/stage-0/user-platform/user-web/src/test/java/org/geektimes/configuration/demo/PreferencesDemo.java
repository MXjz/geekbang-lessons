package org.geektimes.configuration.demo;

import java.util.Iterator;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesDemo {

    public static void main(String[] args) throws BackingStoreException {
//        Preferences userPreferences = Preferences.userRoot();
//        userPreferences.put("my-key", "Hello,World");
//        userPreferences.flush();
//        System.out.println(userPreferences.get("my-key", null));
//        Preferences.systemRoot();

        Map<String, String> map = System.getenv();
        for(Iterator<String> itr = map.keySet().iterator(); itr.hasNext();){
            String key = itr.next();
            System.out.println(key + "=" + map.get(key));
        }
    }
}
