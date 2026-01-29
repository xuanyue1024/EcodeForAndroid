package com.github.ecode.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 工具类
 * 用于持久化存储简单的键值对数据，如服务器地址、用户 Token 等。
 */
public class PreferenceUtils {
    // SharedPreferences 文件名
    private static final String PREF_NAME = "ecode_prefs";
    // 服务器地址 Key
    private static final String KEY_SERVER_URL = "server_url";
    // 用户 Token Key
    private static final String KEY_TOKEN = "jwt_token";

    /**
     * 保存服务器地址
     *
     * @param context 上下文
     * @param url     服务器地址
     */
    public static void saveServerUrl(Context context, String url) {
        getPrefs(context).edit().putString(KEY_SERVER_URL, url).apply();
    }

    /**
     * 获取服务器地址
     *
     * @param context 上下文
     * @return 服务器地址，默认为空字符串
     */
    public static String getServerUrl(Context context) {
        return getPrefs(context).getString(KEY_SERVER_URL, "");
    }

    /**
     * 保存用户 Token
     * 登录成功后调用此方法持久化存储 Token
     *
     * @param context 上下文
     * @param token   用户 Token
     */
    public static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * 获取用户 Token
     * 用于接口请求时添加到 Header 中
     *
     * @param context 上下文
     * @return 用户 Token，默认为空字符串
     */
    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, "");
    }
    
    /**
     * 清除用户 Token
     * 退出登录或 Token 过期（401）时调用
     *
     * @param context 上下文
     */
    public static void clearToken(Context context) {
        getPrefs(context).edit().remove(KEY_TOKEN).apply();
    }

    /**
     * 获取 SharedPreferences 实例
     *
     * @param context 上下文
     * @return SharedPreferences 实例
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
