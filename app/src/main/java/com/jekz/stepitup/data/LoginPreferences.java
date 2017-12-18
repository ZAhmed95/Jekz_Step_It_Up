package com.jekz.stepitup.data;

/**
 * Created by evanalmonte on 12/12/17.
 */

public interface LoginPreferences {
    void put(SharedPrefsManager.Key key, String value);

    void put(SharedPrefsManager.Key key, int value);

    void put(SharedPrefsManager.Key key, boolean value);

    String getString(SharedPrefsManager.Key key, String defaultValue);

    String getString(SharedPrefsManager.Key key);

    int getInt(SharedPrefsManager.Key key);

    int getInt(SharedPrefsManager.Key key, int defaultValue);

    boolean getBoolean(SharedPrefsManager.Key key, boolean defaultValue);


    void remove(SharedPrefsManager.Key... keys);
}
