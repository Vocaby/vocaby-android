package com.vocaby.app.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("URL")
        url?.let {
            webView = WebView(this)
            setContentView(webView)
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
        } ?: finish()
    }
}