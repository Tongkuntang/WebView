package com.azhamudev.kotlinproject

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.support.v4.app.ActivityCompat
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.Manifest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web_view.*
import android.webkit.DownloadListener
import android.content.pm.PackageManager
class WebViewActivity : AppCompatActivity(){

    private val URL = "http://bgfleet.loginto.me/Tracking/mobile/login.php"
    private var isAlreadyCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED);
        startLoaderAnimate()

        webView.settings.setJavaScriptEnabled(true);
        webView.settings.setSupportMultipleWindows(true);
        webView.settings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(false)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                endLoaderAnimate()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                endLoaderAnimate()
                showErrorDialog("Error",
                        "No internet connection. Please check your connection.",
                        this@WebViewActivity)
            }
        }

        // Expose Android methods to Javascript layer
        val javascriptInterface = JavascriptInterface(applicationContext)
        webView.addJavascriptInterface(javascriptInterface, "Android")
        webView.setDownloadListener(DownloadListener { url, _, _, _, _ ->
            if (url.startsWith("blob:")) {
                webView.evaluateJavascript(javascriptInterface.getBase64StringFromBlobUrl(url), null)
            }
        })


        webView.loadUrl(URL)
    }

    override fun onResume() {
        super.onResume()

        if (isAlreadyCreated && !isNetworkAvailable()) {
            isAlreadyCreated = false
            showErrorDialog("Error", "No internet connection. Please check your connection.",
                    this@WebViewActivity)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectionManager =
                this@WebViewActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectionManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showErrorDialog(title: String, message: String, context: Context) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setNegativeButton("Cancel", { _, _ ->
            this@WebViewActivity.finish()
        })
        dialog.setNeutralButton("Settings", {_, _ ->
            startActivity(Intent(Settings.ACTION_SETTINGS))
        })
        dialog.setPositiveButton("Retry", { _, _ ->
            this@WebViewActivity.recreate()
        })
        dialog.create().show()
    }

    private fun endLoaderAnimate() {
        loaderImage.clearAnimation()
        loaderImage.visibility = View.GONE
    }

    private fun startLoaderAnimate() {
        val objectAnimator = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val startHeight = 170
                val newHeight = (startHeight + (startHeight + 40) * interpolatedTime).toInt()
                loaderImage.layoutParams.height = newHeight
                loaderImage.requestLayout()
            }

            override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
                super.initialize(width, height, parentWidth, parentHeight)
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        objectAnimator.repeatCount = -1
        objectAnimator.repeatMode = ValueAnimator.REVERSE
        objectAnimator.duration = 1000
        loaderImage.startAnimation(objectAnimator)
    }


}