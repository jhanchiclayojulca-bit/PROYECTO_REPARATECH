package com.cibertec.cibertecapp.core.service

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryService {

    private const val CLOUD_NAME = "dkibbzkfb"
    private const val UPLOAD_PRESET = "repair_images"

    suspend fun uploadImage(file: File): String = suspendCancellableCoroutine { continuation ->
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.Companion.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody())
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: ""
                    val url = JSONObject(body).getString("secure_url")
                    continuation.resume(url)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        })
    }
}