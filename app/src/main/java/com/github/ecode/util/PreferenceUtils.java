package com.github.ecode.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {
    private static final String PREF_NAME = "ecode_prefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_TOKEN = "jwt_token";

    public static void saveServerUrl(Context context, String url) {
        getPrefs(context).edit().putString(KEY_SERVER_URL, url).apply();
    }

    public static String getServerUrl(Context context) {
        return getPrefs(context).getString(KEY_SERVER_URL, "");
    }

    public static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, "");
    }
    
    public static void clearToken(Context context) {
        getPrefs(context).edit().remove(KEY_TOKEN).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
