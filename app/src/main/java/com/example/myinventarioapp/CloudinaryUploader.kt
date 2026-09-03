package com.example.myinventarioapp

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

suspend fun uploadImageToCloudinary(
    imageBytes: ByteArray
): String {

    val url =
        "https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/image/upload"

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "upload_preset",
            CloudinaryConfig.UPLOAD_PRESET
        )
        .addFormDataPart(
            "file",
            "producto.webp",
            imageBytes.toRequestBody("image/webp".toMediaType())
        )
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    val client = OkHttpClient()

    return client.newCall(request).execute().use { response ->

        if (!response.isSuccessful) {
            throw IOException(
                "Error al subir imagen: HTTP ${response.code}"
            )
        }

        val responseBody = response.body?.string()
            ?: throw IOException("Respuesta vacía de Cloudinary")

        val json = JSONObject(responseBody)

        json.getString("secure_url")
    }
}
