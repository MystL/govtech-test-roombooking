package com.vin.booking.activities

import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.vin.booking.R
import kotlinx.android.synthetic.main.fragment_webview.backButton
import kotlinx.android.synthetic.main.fragment_webview.webView

class WebViewActivity : AppCompatActivity() {

    private var webUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_webview)

        intent?.extras?.let {
            webUrl = it.getString(WEB_VIEW_URL, "")
        }

        webView.clearCache(true) // Always clear the cache for a new instance
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        // Scale the webView to fit the screen
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                var loadUrl = url
                if (url.isNotEmpty()) {
                    val uri = Uri.parse(url)
                    if (uri.scheme == INTENT_IDENTIFIER
                        && uri.authority.equals(INTENT_AUTHORITY)
                    ) {
                        loadUrl = extractFromIntentScheme(uri)
                    }
                }
                view.loadUrl(loadUrl)
                return false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed()
            }

            override fun onPageFinished(view: WebView, url: String) {

            }
        }
        webView.loadUrl(webUrl)

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun extractFromIntentScheme(uri: Uri): String =
        uri.fragment?.split(";")
            ?.let {
                it.find { str -> str.startsWith(FALLBACK_URL_KEY) }
                    ?.let { fullStr ->
                        fullStr.split("=")?.get(1) ?: ""
                    } ?: ""
            } ?: ""


    companion object {
        const val WEB_VIEW_URL = "web_view_url"
        private const val FALLBACK_URL_KEY = "S.browser_fallback_url" // really??
        private const val INTENT_IDENTIFIER = "intent"
        private const val INTENT_AUTHORITY = "qrgo.page.link"
    }
}