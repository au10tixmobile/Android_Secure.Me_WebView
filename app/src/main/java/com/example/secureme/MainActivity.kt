package com.example.secureme

// Added
//
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SecureMe"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webview.settings.javaScriptEnabled = true
        webview.settings.javaScriptCanOpenWindowsAutomatically = true
//        webview.settings.pluginState = WebSettings.PluginState.ON
//        webview.settings.domStorageEnabled = true
        webview.webViewClient = WebViewClient()
        webview.settings.mediaPlaybackRequiresUserGesture = false

        webview.webChromeClient = object : WebChromeClient() {
            // Grant permissions for cam
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d(TAG, "onPermissionRequest")
                runOnUiThread {
                    Log.d(TAG, request.origin.toString())
                    if (request.origin.toString() == "https://secure-me.au10tixservicesdev.com/") {
                        Log.d(TAG, "GRANTED")
                        request.grant(request.resources)
                    } else {
                        Log.d(TAG, "DENIED")
                        request.deny()
                    }
                }
            }

            override fun getDefaultVideoPoster(): Bitmap? {
                Log.d(TAG, "onPermissionRequest2")
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            }
        }
        webview.loadUrl("https://secure-me.au10tixservicesdev.com?token=l5vYUMiidfpqsx&api=aHR0cHM6Ly93ZXUtY20tYXBpbS1kZXYuYXp1cmUtYXBpLm5ldC9zZWN1cmUtbWUvdjE%3D")
    }
}