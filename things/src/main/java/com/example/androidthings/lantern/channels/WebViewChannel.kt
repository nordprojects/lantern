package com.example.androidthings.lantern.channels

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.*
import android.graphics.BitmapFactory



/**
 * Shows a web page using the Android WebView.
 *
 * Config parameters:
 *   - "url"
 *       The URL to be displayed.
 *
 * Created by dingxu on 2/5/18.
 */
class WebViewChannel: Channel() {
    private var webView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (webView == null) {
            webView = ATWebView(activity)
            loadURL(config.settings.optString("url"))
        }
        return webView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.clearFindViewByIdCache()
    }

    private fun loadURL(url: String) {
        webView?.loadUrl(url)
    }

    private inner class ATWebView constructor(context: Context) : WebView(context) {
        init {
            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // scroll to specified location if specified
                    if(config.settings.has("scrollTo")) {
                        scrollTo(0, config.settings.getInt("scrollTo"));
                    }
                }
            }

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
        }
    }
}