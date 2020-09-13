package com.example.secureme

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_end.*


class EndActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SecureMe"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        val event = intent.extras?.getParcelable<SessionEvent>("EVENT")
        if (event != null) {
            Log.d(TAG, event.eventType)
            titleTxt.text = event.eventType
            bodyTxt.text = event.toString()
        } else {
            Log.d(TAG, "EVENT NULL")
        }
    }
}