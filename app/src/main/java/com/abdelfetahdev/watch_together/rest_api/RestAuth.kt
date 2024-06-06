package com.abdelfetahdev.watch_together.rest_api

import android.util.Log
import com.abdelfetahdev.watch_together.entities.ErrorOrData
import com.abdelfetahdev.watch_together.entities.RestAPI
import com.abdelfetahdev.watch_together.entities.User
import com.abdelfetahdev.watch_together.entities.UserCredentials
import com.abdelfetahdev.watch_together.entities.UserSession
import com.abdelfetahdev.watch_together.utilities.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RestAuth(private val httpClient: HttpClient) {
    companion object {
        const val TAG = "RestAuth"
        const val SIGN_IN_API_URL = "${RestAPI.BASE_API_URL}auth/sign_in"
        const val SIGN_UP_API_URL = "${RestAPI.BASE_API_URL}auth/sign_up"
    }

    suspend fun signIn(userCredentials: UserCredentials): ErrorOrData<UserSession> = withContext(
        Dispatchers.IO
    ) {
        val json = userCredentials.toJSON()

        val jsonString = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)
        val request = httpClient.postRequest(SIGN_IN_API_URL, requestBody)

        try {
            val response: Response = httpClient.client.newCall(request).execute()

            // Is Valid Response
            if (response.headers["Content-Type"]?.contains("application/json") != true) {
                return@withContext ErrorOrData(null, "Something went wrong")
            }

            if (response.body == null) {
                return@withContext ErrorOrData(null, "Something went wrong")
            }

            val responseBody = response.body!!.string()
            val jsonResponse = JSONObject(responseBody)

            if (jsonResponse.getString("status") == "error") {
                return@withContext ErrorOrData(null, jsonResponse.getString("message"))
            }

            val data = jsonResponse.getJSONObject("data")
            val userSession = UserSession(data.getString("token"))

            return@withContext ErrorOrData(userSession, "Sign In success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }

    suspend fun signUp(user: User): ErrorOrData<User> = withContext(
        Dispatchers.IO
    ) {
        val json = user.toJSON()

        val jsonString = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)
        val request = httpClient.postRequest(SIGN_UP_API_URL, requestBody)

        try {
            val response: Response = httpClient.client.newCall(request).execute()

            // Is Valid Response
            if (response.headers["Content-Type"]?.contains("application/json") != true) {
                return@withContext ErrorOrData(null, "Something went wrong")
            }

            if (response.body == null) {
                return@withContext ErrorOrData(null, "Something went wrong")
            }

            val responseBody = response.body!!.string()
            val jsonResponse = JSONObject(responseBody)

            if (jsonResponse.getString("status") == "error") {
                return@withContext ErrorOrData(null, jsonResponse.getString("message"))
            }

            return@withContext ErrorOrData(user, "Sign Up success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("CLIENT", "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }
}