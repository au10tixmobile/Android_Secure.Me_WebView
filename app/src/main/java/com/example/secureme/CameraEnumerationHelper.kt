package com.example.secureme

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object CameraEnumerationHelper {

    val FRONT_CAM = "Front Camera"
    val BACK_CAM = "Back Camera"
    val EXTERNAL_CAM = "External Camera"
    val UNKNOWN_CAM = "Unknown Camera"
    var devicesArray: JSONArray? = null

    fun enumerateDevices(context: Context): JSONArray {
        this.devicesArray = JSONArray()
        getCameras(context)
        return devicesArray as JSONArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun getCameras(context: Context) {
        // Video inputs
        val camera = context.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = camera.cameraIdList
            var characteristics: CameraCharacteristics
            var label: String? = ""
            for (i in cameraId.indices) {
                val device = JSONObject()
                characteristics = camera.getCameraCharacteristics(cameraId[i])
                label = getVideoType(characteristics)
                device.put("deviceId", cameraId[i])
                device.put("groupId", "")
                device.put("kind", "videoinput")
                device.put("label", label)
                this.devicesArray!!.put(device)
            }
        } catch (e: CameraAccessException) {
            println("ERROR IOException $e")
        } catch (e: JSONException) {
            println("ERROR IOException $e")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun getVideoType(input: CameraCharacteristics): String? {
        var deviceType: String? = ""
        var num = ""
        try {
            for (i in 0 until this.devicesArray!!.length()) {
                val obj: JSONObject = this.devicesArray!!.getJSONObject(i)
                val id = obj.getString("label")
                if (id.contains(EXTERNAL_CAM)) {
                    num = Integer.toString(num.toInt() + 1)
                }
            }
        } catch (e: JSONException) {
            println("ERROR JSONException $e")
        }
        when (input.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> deviceType = FRONT_CAM
            CameraCharacteristics.LENS_FACING_BACK -> deviceType = BACK_CAM
            CameraCharacteristics.LENS_FACING_EXTERNAL -> deviceType = EXTERNAL_CAM + " " + num
            else -> deviceType = UNKNOWN_CAM
        }
        return deviceType
    }
}