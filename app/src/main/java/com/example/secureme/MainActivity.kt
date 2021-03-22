package com.example.secureme

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION), MY_CAMERA_REQUEST_CODE)
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
        WebView.setWebContentsDebuggingEnabled(true)
        webview.apply {
            addJavascriptInterface(
                LocalStorageJavaScriptInterface(applicationContext),
                "LocalStorage"
            )
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            //Enable and setup JS localStorage
            settings.domStorageEnabled = true
            //those two lines seem necessary to keep data that were stored even if the app was killed.
            settings.databaseEnabled = true
            settings.databasePath = filesDir.parentFile.path + "/databases/"
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

                //Hides the default camera poster
                override fun getDefaultVideoPoster(): Bitmap? {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                }
            }
            loadUrl(data.toString())
        }
    }

    private class LocalStorageJavaScriptInterface(c: Context) {
        private val mContext: Context
        private val localStorageDBHelper: LocalStorage
        private lateinit var database: SQLiteDatabase

        /**
         * This method allows to get an item for the given key
         * @param key : the key to look for in the local storage
         * @return the item having the given key
         */
        @JavascriptInterface
        fun getItem(key: String?): String? {
            var value: String? = null
            if (key != null) {
                database = localStorageDBHelper.readableDatabase
                val cursor: Cursor = database.query(
                    LocalStorage.LOCALSTORAGE_TABLE_NAME,
                    null,
                    LocalStorage.LOCALSTORAGE_ID + " = ?", arrayOf(key), null, null, null
                )
                if (cursor.moveToFirst()) {
                    value = cursor.getString(1)
                }
                cursor.close()
                database.close()
            }
            return value
        }

        /**
         * set the value for the given key, or create the set of datas if the key does not exist already.
         * @param key
         * @param value
         */
        @JavascriptInterface
        fun setItem(key: String?, value: String?) {
            if (key != null && value != null) {
                val oldValue = getItem(key)
                database = localStorageDBHelper.writableDatabase
                val values = ContentValues()
                values.put(LocalStorage.LOCALSTORAGE_ID, key)
                values.put(LocalStorage.LOCALSTORAGE_VALUE, value)
                if (oldValue != null) {
                    database.update(
                        LocalStorage.LOCALSTORAGE_TABLE_NAME,
                        values,
                        LocalStorage.LOCALSTORAGE_ID + "='" + key + "'",
                        null
                    )
                } else {
                    database.insert(LocalStorage.LOCALSTORAGE_TABLE_NAME, null, values)
                }
                database.close()
            }
        }

        /**
         * removes the item corresponding to the given key
         * @param key
         */
        @JavascriptInterface
        fun removeItem(key: String?) {
            if (key != null) {
                database = localStorageDBHelper.writableDatabase
                database.delete(
                    LocalStorage.LOCALSTORAGE_TABLE_NAME,
                    LocalStorage.LOCALSTORAGE_ID + "='" + key + "'",
                    null
                )
                database.close()
            }
        }

        /**
         * clears all the local storage.
         */
        @JavascriptInterface
        fun clear() {
            database = localStorageDBHelper.writableDatabase
            database.delete(LocalStorage.LOCALSTORAGE_TABLE_NAME, null, null)
            database.close()
        }

        init {
            mContext = c
            localStorageDBHelper = LocalStorage.getInstance(mContext)
        }
    }
}