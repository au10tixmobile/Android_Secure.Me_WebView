package com.example.secureme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


@SuppressLint("SetJavaScriptEnabled")
class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SecureMe"
        const val MY_CAMERA_REQUEST_CODE = 100
    }

    private var fileUriCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), MY_CAMERA_REQUEST_CODE
            )
        } else {
            setupWebView(intent?.data)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.d(TAG, "onActivityResult")
        when (requestCode) {
            ActivityMain.REQUEST_CODE_GALLERY -> fileUriCallback = if (resultCode == RESULT_OK) {
                val selectedImageUri = intent!!.data!!
                val localIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, selectedImageUri)
                this.sendBroadcast(localIntent)
                // If we want to downsize check out the post
                //  http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
                fileUriCallback!!.onReceiveValue(arrayOf(selectedImageUri))
                null
            } else {
                fileUriCallback?.onReceiveValue(arrayOf())
                null
            }
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
        WebView.setWebContentsDebuggingEnabled(true)
        webview.apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            //those two lines seem necessary to keep data that were stored even if the app was killed.
            addJavascriptInterface(JsObject(), "JsObject")
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    Log.d(TAG, "onPermissionRequest")
                    runOnUiThread {
                        Log.d(TAG, request.origin.toString())
//                        if (request.origin.toString() == "https://secure-me") {
                        Log.d(TAG, "GRANTED")
                        request.grant(request.resources)
//                        } else {
//                            Log.d(TAG, "DENIED")
//                            request.deny()
//                        }
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    Log.d(TAG, "onShowFileChooser")
                    this@MainActivity.fileUriCallback = filePathCallback
                    showGallery()
                    return true
                }

                //Hides the default camera poster
                override fun getDefaultVideoPoster(): Bitmap? {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                }
            }
            loadUrl(data.toString())
        }
    }

    private fun showGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image From Gallery"
            ), ActivityMain.REQUEST_CODE_GALLERY
        )
    }

    internal class JsObject {
        @JavascriptInterface
        fun postMessage(json: String?, transferList: String?): Boolean {
            Log.d(TAG, "JsObject - $json")
            return false // here we return true if we handled the post.
        }
    }
}