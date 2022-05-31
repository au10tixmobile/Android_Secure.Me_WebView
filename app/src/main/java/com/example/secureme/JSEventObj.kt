package com.example.secureme
import com.google.gson.annotations.SerializedName

data class JSEventObj(
    @SerializedName("authorizationToken")
    val authorizationToken: String,
    @SerializedName("customerInternalReference")
    val customerInternalReference: String,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("payload")
    val payload: Payload,
    @SerializedName("transactionReference")
    val transactionReference: String
) {
    data class Payload(
        @SerializedName("value")
        val value: String
    )
}