package com.example.secureme

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetJavaScriptEnabled")
class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SecureMe"
        const val MY_CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
        } else {
            setupWebView(intent?.data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
                setupWebView(intent?.data)
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupWebView(data: Uri?) {
        webview.apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    Log.d(TAG, "onPermissionRequest")
                    runOnUiThread {
                        Log.d(TAG, request.origin.toString())
                        if (request.origin.toString() == "https://secure-me") {
                            Log.d(TAG, "GRANTED")
                            request.grant(request.resources)
                        } else {
                            Log.d(TAG, "DENIED")
                            request.deny()
                        }
                    }
                }

                //Hides the default camera poster
                override fun getDefaultVideoPoster(): Bitmap? {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                }
            }
            loadUrl(data.toString())
        }
    }
}