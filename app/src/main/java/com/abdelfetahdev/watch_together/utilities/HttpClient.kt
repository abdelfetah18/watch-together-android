package com.abdelfetahdev.watch_together.utilities

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class HttpClient(val client: OkHttpClient, private val accessToken: String) {
    fun postRequest(url: String, requestBody: RequestBody): Request {
        return Request.Builder().url(url).post(requestBody).build()
    }

    fun getRequest(url: String): Request {
        return Request.Builder().url(url).get().build()
    }

    fun postRequestWithCredentials(url: String, requestBody: RequestBody): Request {
        return Request.Builder().url(url).header("Authorization", accessToken).post(requestBody)
            .build()
    }

    fun getRequestWithCredentials(url: String): Request {
        return Request.Builder().url(url).header("Authorization", accessToken).get().build()
    }
}