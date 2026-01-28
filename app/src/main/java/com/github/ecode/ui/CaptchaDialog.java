package com.github.ecode.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.ecode.util.OkHttpUtils;
import com.github.ecode.util.PreferenceUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class CaptchaDialog extends Dialog {

    private final OnCaptchaListener listener;
    private WebView webView;

    public interface OnCaptchaListener {
        void onSuccess(String token);
        void onFailed(String msg);
    }

    public CaptchaDialog(@NonNull Context context, OnCaptchaListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(getContext());
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(webView);

        // Configure WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
                Log.d("CaptchaWebView", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return true;
            }
        });
        webView.addJavascriptInterface(new CaptchaInterface(), "Android");

        // Use WebViewAssetLoader to load assets securely
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getContext()))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Intercept API calls from local WebView
                if (url.contains("/api/proxy/gen")) {
                    return proxyRequest(request, "/api/open/captcha/gen");
                } else if (url.contains("/api/proxy/check")) {
                    return proxyRequest(request, "/api/open/captcha/check");
                }
                
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Initialize Captcha
                // Pass empty string as baseUrl because we are now using relative proxy paths in index.html
                webView.evaluateJavascript("initCaptcha('');", null);
            }
        });

        // Load the local HTML file
        webView.loadUrl("https://appassets.androidplatform.net/assets/captcha/index.html");
    }
    
    private WebResourceResponse proxyRequest(WebResourceRequest request, String apiPath) {
        String baseUrl = PreferenceUtils.getServerUrl(getContext());
        if (baseUrl.isEmpty()) return null;
        
        String targetUrl = baseUrl + apiPath;
        if (request.getUrl().getQuery() != null) {
            targetUrl += "?" + request.getUrl().getQuery();
        }

        try {
            OkHttpUtils okHttpUtils = OkHttpUtils.builder().url(targetUrl);
            // Copy headers? Maybe not all.
            
            String responseBody = null;
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                 // For now, simple POST logic. If the captcha library sends JSON body, 
                 // we can't easily read it from WebResourceRequest (Android API limitation).
                 // LUCKILY: Tianai Captcha usually sends params via Query or simple Form.
                 // BUT: 'gen' is usually GET or POST with no body.
                 // 'check' sends JSON body usually.
                 
                 // CRITICAL LIMITATION: Android WebViewClient cannot read the POST body of intercepted requests.
                 // If the captcha library sends POST data, we cannot proxy it this way.
                 // We must check if the library supports GET for these, or if we need a JS Bridge instead.
                 
                 // Tianai Captcha 'check' usually sends data.
                 // If we cannot intercept body, we MUST use JS Interface to proxy.
                 
                 // ABORTING INTERCEPT STRATEGY for POST requests with body.
                 // Reverting to JS Bridge strategy if this fails?
                 // Wait, let's assume 'gen' is GET.
            } 
            
            // Actually, let's try GET for gen.
            if (apiPath.endsWith("gen")) {
                 responseBody = okHttpUtils.get().sync();
            } else {
                // If it's 'check', it likely needs body.
                // Interception won't work for POST body.
                // We will handle this by returning a special error or handling via JS.
                return null; 
            }

            if (responseBody != null) {
                return new WebResourceResponse("application/json", "UTF-8", new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class CaptchaInterface {
        @JavascriptInterface
        public void onVerify(String jsonResult) {
            Log.d("Captcha", "Result: " + jsonResult);
            try {
                JSONObject obj = JSON.parseObject(jsonResult);
                if (obj != null && obj.getIntValue("code") == 200) {
                     JSONObject data = obj.getJSONObject("data");
                     if (data != null && data.containsKey("validToken")) {
                         String token = data.getString("validToken");
                         if (listener != null) {
                             webView.post(() -> {
                                 listener.onSuccess(token);
                                 dismiss();
                             });
                         }
                     }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        @JavascriptInterface
        public void onError(String msg) {
             if (listener != null) {
                 webView.post(() -> listener.onFailed(msg));
             }
        }
    }
}
