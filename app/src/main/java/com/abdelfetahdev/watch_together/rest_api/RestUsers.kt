package com.abdelfetahdev.watch_together.rest_api

import android.util.Log
import com.abdelfetahdev.watch_together.entities.ErrorOrData
import com.abdelfetahdev.watch_together.entities.RestAPI
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.entities.User
import com.abdelfetahdev.watch_together.utilities.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RestUsers(private val httpClient: HttpClient) {
    companion object {
        const val TAG = "RestRooms"
        const val CURRENT_USER_API_URL = "${RestAPI.BASE_API_URL}user"
    }

    suspend fun getCurrentUser(): ErrorOrData<User> = withContext(
        Dispatchers.IO
    ) {
        val request = httpClient.getRequestWithCredentials(CURRENT_USER_API_URL)

        try {
            val response: Response = httpClient.client.newCall(request).execute()

            // Is Valid Response
            if (response.headers["Content-Type"]?.contains("application/json") != true) {
                return@withContext ErrorOrData(null, "The Response is not json", true)
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
            val user = User.fromJSON(data)

            return@withContext ErrorOrData(user, "Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }
}