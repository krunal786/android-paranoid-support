package com.codebase.paranoidsupport.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppStorage {

    public static final String KEY_COUNTRY_CODE = "key_countryCode";
    public static final String KEY_COUNTRY = "key_country";
    public static final String KEY_STATE = "key_state";
    public static final String KEY_CITY = "key_city";

    private static AppStorage instance = null;
    private final SharedPreferences pref;

    private AppStorage(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new AppStorage(context);
        }
    }

    public static AppStorage get() {
        return instance;
    }

    public void put(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    public String get(String key) {
        return pref.getString(key, "");
    }

    public boolean has(String key) {
        return !pref.getString(key, "").isEmpty();
    }
}
