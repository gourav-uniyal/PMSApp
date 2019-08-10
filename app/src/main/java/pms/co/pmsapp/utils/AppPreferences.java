package pms.co.pmsapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private SharedPreferences mPreferences;
    private static String KEY_EMAIL = "email";
    private static String KEY_NAME = "name";
    private SharedPreferences.Editor editor;

    public AppPreferences(Context mContext) {
        String PREF_NAME = "PMSApp";
        mPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = mPreferences.edit();
        editor.apply();
    }

    public void setEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return mPreferences.getString(KEY_EMAIL, null);
    }

    public void setName(String name) {
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public String getName() {
        return mPreferences.getString(KEY_NAME, null);
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
