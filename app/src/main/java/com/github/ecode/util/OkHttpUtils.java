package com.github.ecode.util;


import com.alibaba.fastjson2.JSON;
import com.github.ecode.MainActivity;

import okhttp3.*;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 网络请求工具类
 * 封装了 GET/POST 请求、参数处理、SSL 证书忽略、以及全局 401 处理等功能。
 */
public class OkHttpUtils {
    // OkHttpClient 单例实例
    private static volatile OkHttpClient okHttpClient = null;
    // 信号量，用于同步控制并发数（目前主要用于 sync 方法的简单模拟，实际 async 方法未完全使用此逻辑控制并发）
    private static volatile Semaphore semaphore = null;
    // 请求头 Map
    private Map<String, String> headerMap;
    // 请求参数 Map
    private Map<String, String> paramMap;
    // 请求 URL
    private String url;
    // OkHttp 请求构建器
    private Request.Builder request;
    // 日志 TAG
    private static final String TAG = "OkHttpUtils";
    // 全局上下文，用于跳转登录页
    private static Context globalContext;

    /**
     * 初始化 OkHttpClient，并配置 SSL 信任所有证书（仅用于开发/测试环境）
     * 配置了连接、写入、读取超时时间，以及日志拦截器和 401 拦截器。
     */
    private OkHttpUtils() {
        if (okHttpClient == null) {
            synchronized (OkHttpUtils.class) {
                if (okHttpClient == null) {
                    TrustManager[] trustManagers = buildTrustManagers();
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                            .hostnameVerifier((hostName, session) -> true) // 忽略 Hostname 验证
                            .retryOnConnectionFailure(true)
                            .addInterceptor(chain -> {
                                Request request = chain.request();
                                Log.d(TAG, "Sending request to " + request.url());
                                Response response = chain.proceed(request);
                                
                                // 处理 401 未授权情况：清除 Token 并跳转至登录页
                                if (response.code() == 401) {
                                    handleUnAuthorized();
                                }
                                return response;
                            })
                            .build();
                    addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                }
            }
        }
    }

    /**
     * 初始化全局上下文，建议在 Application 或 MainActivity onCreate 中调用
     * 用于处理全局跳转（如 401 跳转登录页）
     * 
     * @param context 应用上下文
     */
    public static void init(Context context) {
        globalContext = context.getApplicationContext();
    }

    /**
     * 处理 401 未授权逻辑
     * 清除本地 Token，并跳转回 MainActivity（登录页）
     */
    private void handleUnAuthorized() {
        if (globalContext != null) {
            Log.w(TAG, "收到 401 响应，清除 Token 并跳转登录页");
            PreferenceUtils.clearToken(globalContext);
            
            // 使用 Handler 确保在主线程执行 UI 操作（如 Toast）或启动 Activity
            new Handler(Looper.getMainLooper()).post(() -> {
                // 跳转到 MainActivity，并清空任务栈，防止用户返回到旧页面
                Intent intent = new Intent(globalContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                globalContext.startActivity(intent);
            });
        } else {
             Log.e(TAG, "Global context is null, cannot redirect to login.");
        }
    }

    /**
     * 获取信号量实例，用于控制同步请求并发（示例用途）
     *
     * @return Semaphore 实例
     */
    private static Semaphore getSemaphoreInstance() {
        //只能1个线程同时访问
        synchronized (OkHttpUtils.class) {
            if (semaphore == null) {
                semaphore = new Semaphore(0);
            }
        }
        return semaphore;
    }

    /**
     * 创建 OkHttpUtils 构建器实例
     *
     * @return OkHttpUtils 实例
     */
    public static OkHttpUtils builder() {
        return new OkHttpUtils();
    }

    /**
     * 设置请求 URL
     *
     * @param url 请求地址
     * @return 当前 OkHttpUtils 实例
     */
    public OkHttpUtils url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 添加请求参数
     * 
     * @param key   参数名
     * @param value 参数值
     * @return 当前 OkHttpUtils 实例
     */
    public OkHttpUtils addParam(String key, String value) {
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.put(key, value);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key   Header 名
     * @param value Header 值
     * @return 当前 OkHttpUtils 实例
     */
    public OkHttpUtils addHeader(String key, String value) {
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>(16);
        }
        headerMap.put(key, value);
        return this;
    }

    /**
     * 初始化 GET 请求
     * 将参数拼接到 URL 后面
     *
     * @return 当前 OkHttpUtils 实例
     */
    public OkHttpUtils get() {
        request = new Request.Builder().get();
        StringBuilder urlBuilder = new StringBuilder(url);
        if (paramMap != null) {
            urlBuilder.append("?");
            try {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                            append("=").
                            append(URLEncoder.encode(entry.getValue(), "utf-8")).
                            append("&");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        request.url(urlBuilder.toString());
        return this;
    }

    /**
     * 初始化 POST 请求
     *
     * @param isJsonPost true 表示以 JSON 格式提交 (application/json)
     *                   false 表示以表单格式提交 (application/x-www-form-urlencoded)
     * @return 当前 OkHttpUtils 实例
     */
    public OkHttpUtils post(boolean isJsonPost) {
        RequestBody requestBody;
        if (isJsonPost) {
            String json = "";
            if (paramMap != null) {
                json = JSON.toJSONString(paramMap);
            } 
            // 使用新的参数顺序以避免已弃用的重载
            requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            if (paramMap != null) {
                paramMap.forEach(formBody::add);
            }
            requestBody = formBody.build();
        }
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }

    /**
     * 同步请求
     * 阻塞当前线程直到请求完成
     *
     * @return 响应体字符串，失败返回 null
     */
    public String sync() {
        setHeader(request);
        // 使用 try-with-resources 确保 Response 正确关闭
        try (Response response = okHttpClient.newCall(request.build()).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            return null;
        } catch (IOException e) {
            Log.e(TAG, "http请求失败：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 异步请求，有返回值（阻塞等待结果）
     * 注意：此方法虽然名为 async，但内部使用了 Semaphore 阻塞等待，表现类似于同步调用。
     *
     * @return 响应体字符串
     */
    public String async() {
        StringBuilder buffer = new StringBuilder();
         setHeader(request);
         okHttpClient.newCall(request.build()).enqueue(new Callback() {
             @Override
             public void onFailure(Call call, IOException e) {
                 buffer.append("请求出错：").append(e.getMessage());
             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {
                 // 注意：这里没有处理 null body 的情况，仅作示例
                 assert response.body() != null;
                 buffer.append(response.body().string());
                 getSemaphoreInstance().release();
             }
         });
         try {
             getSemaphoreInstance().acquire();
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
         return buffer.toString();
     }

    /**
     * 异步请求，带有接口回调
     *
     * @param callBack 成功或失败的回调接口
     */
    public void async(ICallBack callBack) {
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailure(call, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                         callBack.onSuccessful(call, response.body().string());
                    } else {
                         callBack.onSuccessful(call, "");
                    }
                } else {
                    // 非 200-299 响应（包括 401，虽然 401 已被拦截器处理，但这里仍会回调）
                    callBack.onFailure(call, "HTTP Error: " + response.code());
                }
            }
        });
    }

    /**
     * 为 request 添加请求头
     *
     * @param request Request.Builder 对象
     */
    private void setHeader(Request.Builder request) {
        if (headerMap != null) {
            try {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 生成安全套接字工厂，用于 HTTPS 请求的证书跳过
     *
     * @param trustAllCerts 信任所有证书的 TrustManager 数组
     * @return SSLSocketFactory 实例
     */
    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

    /**
     * 构建信任所有证书的 TrustManager
     * 警告：仅用于开发环境，生产环境不应信任所有证书
     *
     * @return TrustManager 数组
     */
    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * 自定义网络请求回调接口
     */
    public interface ICallBack {
        /**
         * 请求成功回调
         * @param call Call 对象
         * @param data 响应数据字符串
         */
        void onSuccessful(Call call, String data);

        /**
         * 请求失败回调
         * @param call Call 对象
         * @param errorMsg 错误信息
         */
        void onFailure(Call call, String errorMsg);

    }
    
    /**
     * Helper 类，支持简化的 Builder 模式调用
     */
    public static class Builder {
        private String url;
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        public OkHttpUtils get() {
            return OkHttpUtils.builder().url(url).get();
        }
    }
}
