package com.example.hck.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NetworkClient {
    private val client = OkHttpClient()

    @Throws(IOException::class)
    fun run(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return response.body!!.string()
        }
    }
}