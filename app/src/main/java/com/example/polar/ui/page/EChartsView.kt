package com.example.polar.ui.page

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// Shows an ECharts html file from the assets folder in a WebView.
// Every time "script" changes, it is run in the page to update the chart.
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EChartsView(fileName: String, script: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Fill the space Compose gives it, otherwise the chart height is 0
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                setBackgroundColor(Color.TRANSPARENT)
                // The first script runs before the page has loaded and gets lost.
                // So when loading finishes, run the latest script again.
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        val latestScript = view.tag as? String
                        if (latestScript != null) {
                            view.evaluateJavascript(latestScript, null)
                        }
                    }
                }
                loadUrl("file:///android_asset/$fileName")
            }
        },
        update = { webView ->
            // Keep the latest script on the WebView so onPageFinished can use it
            webView.tag = script
            webView.evaluateJavascript(script, null)
        },
        modifier = Modifier.fillMaxSize()
    )
}
