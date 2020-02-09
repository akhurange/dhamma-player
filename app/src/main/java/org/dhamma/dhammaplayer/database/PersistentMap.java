package org.dhamma.dhammaplayer.database;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistentMap {
    private SharedPreferences mSharedPreference;
    private static PersistentMap sSingletonInstance = null;

    private static final String sPreferenceName = "org.dhamma.dhammaplayer.PREFERENCE_FILE";

    public static PersistentMap getInstance(Context context) {
        if (null == sSingletonInstance) {
            sSingletonInstance = new PersistentMap(context);
        }
        return sSingletonInstance;
    }

    private PersistentMap(Context context) {
        mSharedPreference = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
    }

    public String getString(String key) {
        return mSharedPreference.getString(key, null);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void removeKey(String key) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.remove(key);
        editor.commit();
    }

    public void dropMap() {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.clear();
        editor.commit();
    }
}
