package com.tabling.homework.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.tabling.homework.R
import kotlinx.android.synthetic.main.activity_webview.*

@SuppressLint("Registered", "SetJavaScriptEnabled")
class WebViewActivity : Activity() {
    private val webView by lazy {
        web_view.apply {
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setAppCacheEnabled(true)
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        intent.getStringExtra("url")?.replace(" ", "_")?.let { webView.loadUrl(it) }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}