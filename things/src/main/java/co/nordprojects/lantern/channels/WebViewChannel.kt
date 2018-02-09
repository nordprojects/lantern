package co.nordprojects.lantern.channels

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
import co.nordprojects.lantern.Channel
import co.nordprojects.lantern.R
import kotlinx.android.synthetic.*

/**
 * Created by dingxu on 2/5/18.
 */
class WebViewChannel: Channel() {
    private var webView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater?.inflate(R.layout.webview_fragment, container, false)
        if (webView == null) {
            webView = ATWebView(activity)
            loadURL(config.settings.getString("url"))
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

    /*
     * Custom WebView with WebChromeClient, WebViewClient and onKeyDown settings
     */
    private inner class ATWebView constructor(context: Context) : WebView(context) {

        init {
            webChromeClient = object: WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    // fullscreen
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    // fullscreen
                }
            }

            webViewClient = object: WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // scroll to specified location if specified
                    if(config.settings.has("scrollTo")) {
                        scrollTo(0, config.settings.getInt("scrollTo"));
                    }

                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }
            }

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
        }

        override fun onKeyDown(keyCode:Int, event: KeyEvent):Boolean {
            if (event.getAction() === KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        if (canGoBack()) {
                            goBack()
                            return true
                        }
                    }
                }
            }
            return super.onKeyDown(keyCode, event)
        }
    }

}