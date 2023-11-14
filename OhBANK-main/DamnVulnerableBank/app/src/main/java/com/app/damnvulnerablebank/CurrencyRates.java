package com.app.damnvulnerablebank;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CurrencyRates extends AppCompatActivity {
    String getToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencyrates);
        Bundle extras = getIntent().getExtras();
        WebView webView = findViewById(R.id.loads);

        if(extras == null){
            loadDefaultUrl(webView);
        }else {
            getToken = getIntent().getData().getQueryParameter("url");

            if(isValidUrl(getToken)){
                loadUrlSafely(webView, getToken);
            }else {
                loadDefaultUrl(webView);
            }
        }
    }
    private void loadDefaultUrl(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl("https://www.xe.com/");
    }
    private void loadUrlSafely(WebView webView, String getToken) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(getToken);
    }
    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        //허용 목록
        String[] allowedDomains = {"google.com", "naver.com"};
        String[] allowedProtocols = {"http", "https"};

        Uri uri = Uri.parse(url);
        Log.d("url Log : ", uri.getHost().toString());
        // 도메인 검사
        boolean isDomainAllowed = false;
        for (String allowedDomain  : allowedDomains) {
            if (uri.getHost() != null && uri.getHost().endsWith(allowedDomain )) {
                isDomainAllowed = true;
                break;
            }
        }
        // 프로토콜 검사
        boolean isProtocolAllowed = false;
        for (String allowedProtocol : allowedProtocols) {
            if (uri.getScheme() != null && uri.getScheme().equals(allowedProtocol)) {
                isProtocolAllowed = true;
                break;
            }
        }
        // 도메인과 프로토콜이 모두 허용된 경우에만 true 반환
        return isDomainAllowed && isProtocolAllowed;
    }
}



class WebViewClientImpl extends WebViewClient {
    private Activity activity = null;
    public WebViewClientImpl(Activity activity) {
        this.activity = activity;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        return false;

    }

}