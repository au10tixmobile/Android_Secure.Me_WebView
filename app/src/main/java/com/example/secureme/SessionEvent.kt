package com.example.secureme

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SessionEvent(
    val authorizationToken: String,
    val customerInternalReference: String,
    val dateTime: String,
    val eventType: String,
    val payload: Payload?,
    val transactionReference: String
) : Parcelable {
    override fun toString(): String {
        return "authorizationToken : ${this.authorizationToken}\n" +
                "customerInternalReference : ${this.customerInternalReference}\n" +
                "dateTime : ${this.dateTime}\n" +
                "eventType : ${this.eventType}\n" +
                "payload : ${this.payload.toString()}\n" +
                "transactionReference : ${this.transactionReference}\n"
    }

    @Parcelize
    data class Payload(
        val metaInfo: Int,
        val value: String
    ) : Parcelable {
        override fun toString(): String {
            return "metaInfo : ${this.metaInfo}\n" +
                    "value : ${this.value}\n"

        }
    }

    companion object {
        fun createSessionEvent(body: String?): SessionEvent? {
            return try {
                val obj = JSONObject(body)
                SessionEvent(
                    obj.getString("authorizationToken"),
                    obj.getString("customerInternalReference"),
                    obj.getString("dateTime"),
                    obj.getString("eventType"),
                    if (obj.optJSONObject("payload") != null) {
                        Payload(
                            obj.getJSONObject("payload").getInt("metaInfo"),
                            obj.getJSONObject("payload").getString("value")
                        )
                    } else null,
                    obj.getString("transactionReference")
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }

    object EventType {
        const val SUCCESS = "Success"
        const val ERROR = "Error"
    }
}