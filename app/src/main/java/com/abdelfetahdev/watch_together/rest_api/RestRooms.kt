package com.abdelfetahdev.watch_together.rest_api

import android.util.Log
import com.abdelfetahdev.watch_together.entities.Asset
import com.abdelfetahdev.watch_together.entities.ErrorOrData
import com.abdelfetahdev.watch_together.entities.Member
import com.abdelfetahdev.watch_together.entities.Message
import com.abdelfetahdev.watch_together.entities.RestAPI
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.entities.Video
import com.abdelfetahdev.watch_together.entities.YoutubeVideo
import com.abdelfetahdev.watch_together.utilities.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class RestRooms(private val httpClient: HttpClient) {
    companion object {
        const val TAG = "RestRooms"
        const val EXPLORE_API_URL = "${RestAPI.BASE_API_URL}explore"
        const val USER_ROOMS_API_URL = "${RestAPI.BASE_API_URL}user/rooms"
        const val ROOM_CHAT_API_URL = "${RestAPI.BASE_API_URL}room/chat"
        const val CREATE_ROOM_API_URL = "${RestAPI.BASE_API_URL}room/create"
        const val UPLOAD_ROOM_PROFILE_IMAGE_API_URL =
            "${RestAPI.BASE_API_URL}room/upload_profile_image"
        const val ROOM_MEMEBERS_API_URL = "${RestAPI.BASE_API_URL}room/members"
        const val YOUTUBE_SEARCH_API_URL = "${RestAPI.BASE_API_URL}room/youtube_search"
        const val GET_YOUTUBE_VIDEO_API_URL = "${RestAPI.BASE_API_URL}get_video"
    }

    suspend fun getExploreRooms(): ErrorOrData<ArrayList<Room>> = withContext(
        Dispatchers.IO
    ) {
        val request = httpClient.getRequestWithCredentials(EXPLORE_API_URL)

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

            val data = jsonResponse.getJSONArray("data")
            val rooms = Room.getListOfRooms(data)

            return@withContext ErrorOrData(rooms, "Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }


    suspend fun getUserRooms(): ErrorOrData<ArrayList<Room>> = withContext(
        Dispatchers.IO
    ) {
        val request = httpClient.getRequestWithCredentials(USER_ROOMS_API_URL)

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

            val data = jsonResponse.getJSONArray("data")
            val rooms = Room.getListOfRooms(data)

            return@withContext ErrorOrData(rooms, "Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }

    suspend fun getChatMessages(roomId: String): ErrorOrData<ArrayList<Message>> = withContext(
        Dispatchers.IO
    ) {
        val request = httpClient.getRequestWithCredentials("${ROOM_CHAT_API_URL}?room_id=$roomId")

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

            val data = jsonResponse.getJSONArray("data")
            val messages = Message.getListOfMessages(data)

            return@withContext ErrorOrData(messages, "Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }

    suspend fun createRoom(room: Room): ErrorOrData<Room> = withContext(
        Dispatchers.IO
    ) {
        val json = room.toJSON()

        val jsonString = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)

        val request = httpClient.postRequestWithCredentials(CREATE_ROOM_API_URL, requestBody)

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

            return@withContext ErrorOrData(Room.fromJSON(data), "Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }

    suspend fun uploadRoomProfileImage(roomId: String, imagePath: String): ErrorOrData<Asset> =
        withContext(
            Dispatchers.IO
        ) {

            val imageFile = File(imagePath)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("room_id", roomId)
                .addFormDataPart(
                    "profile_image",
                    imageFile.name,
                    imageFile.asRequestBody("image/*".toMediaType())
                )
                .build()

            val request = httpClient.postRequestWithCredentials(
                UPLOAD_ROOM_PROFILE_IMAGE_API_URL,
                requestBody
            )

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

                return@withContext ErrorOrData(Asset.fromJSON(data), "Success")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "e.message: ${e.message}")
                return@withContext ErrorOrData(null, "Something went wrong")
            }
        }

    suspend fun getRoomMembers(roomId: String): ErrorOrData<ArrayList<Member>> = withContext(
        Dispatchers.IO
    ) {
        val json = JSONObject(mapOf("room_id" to roomId))

        val jsonString = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)

        val request =
            httpClient.postRequestWithCredentials(ROOM_MEMEBERS_API_URL, requestBody)

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

            val data = jsonResponse.getJSONArray("data")
            val members = Member.getListOfMembers(data)

            return@withContext ErrorOrData(members, "Success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "e.message: ${e.message}")
            return@withContext ErrorOrData(null, "Something went wrong")
        }
    }

    suspend fun searchYoutubeVideos(query: String): ErrorOrData<ArrayList<YoutubeVideo>> =
        withContext(
            Dispatchers.IO
        ) {
            val request = httpClient.getRequestWithCredentials("${YOUTUBE_SEARCH_API_URL}?q=$query")

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
                val videosArray = data.getJSONArray("videos")
                val videos = YoutubeVideo.getListOfVideos(videosArray)

                return@withContext ErrorOrData(videos, "Success")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "e.message: ${e.message}")
                return@withContext ErrorOrData(null, "Something went wrong")
            }
        }

    suspend fun getYoutubeVideo(videoUrl: String): ErrorOrData<Video> =
        withContext(
            Dispatchers.IO
        ) {
            val request =
                httpClient.getRequestWithCredentials("${GET_YOUTUBE_VIDEO_API_URL}?video_url=$videoUrl")

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

                val data = jsonResponse.getJSONArray("data")
                if (data.length() == 0) {
                    return@withContext ErrorOrData(null, "No Video Found")
                }

                val videoObject = data.getJSONObject(0)
                val video = Video.fromJSON(videoObject)

                return@withContext ErrorOrData(video, "Success")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "e.message: ${e.message}")
                return@withContext ErrorOrData(null, "Something went wrong")
            }
        }
}