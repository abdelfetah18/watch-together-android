package com.abdelfetahdev.watch_together

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class Client (var accessToken: String){
    private val client = OkHttpClient()
    private fun getRequest(url: String): JSONObject? {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", accessToken)
            .get()
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            response.headers["Content-Type"]?.contains("application/json")
                ?: throw Throwable("response.body not Found")
            val responseBody: ResponseBody =
                response.body ?: throw Throwable("response.body not Found")
            val resBody = responseBody.string()

            Log.i("HTTP_REQUEST_INFO", "url: $url\naccessToken: $accessToken")
            return JSONObject(resBody)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("CLIENT", "e.message: ${e.message}")
            if (e.message == "timeout") {
                return getRequest(url)
            }
        }
        return null
    }

    private fun postRequest(url: String, json: JsonObject): JSONObject? {
        val jsonString = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", accessToken)
            .post(requestBody)
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            response.headers["Content-Type"]?.contains("application/json")
                ?: throw Throwable("response.body not Found")
            val responseBody: ResponseBody =
                response.body ?: throw Throwable("response.body not Found")
            val resBody = responseBody.string()

            return JSONObject(resBody)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("CLIENT", "e.message: ${e.message}")
            if (e.message == "timeout") {
                return postRequest(url, json)
            }
        }
        return null
    }

    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/user"

        try {
            val jsonBody = getRequest(url) ?: return@withContext null
            val data = jsonBody.getJSONObject("data")

            val user = User(
                data.getString("_id"),
                data.getString("username"),
                data.optString("profile_image","null")
            )
            user
        } catch (e: IOException) {
            println("Something went wrong")
            null
        }
    }

    suspend fun getNewAccessToken() : String? = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/auth/update_token"

        try {
            val jsonBody = getRequest(url) ?: return@withContext null
            val status = jsonBody.getString("status")
            Log.i("CLIENT", jsonBody.toString(2))
            if(status == "error"){
                return@withContext null
            }
            val data = jsonBody.getJSONObject("data")
            data.getString("token").toString()
        }catch(e : IOException){
            null
        }
    }

    suspend fun getExploreRooms(): MutableList<Room> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/explore"

        try {
            val jsonBody = getRequest(url) ?: return@withContext mutableListOf()
            val data = jsonBody.getJSONArray("data")

            val rooms : MutableList<Room> = mutableListOf()
            for(i in 0 until data.length()){
                val room = data.getJSONObject(i)
                val admin = room.getJSONObject("admin")
                val creator = room.getJSONObject("creator")

                rooms.add(
                    Room(
                        room.getString("_id"),
                        room.optString("profile_image","null"),
                        User(
                            admin.getString("_id"),
                            admin.getString("username"),
                            admin.optString("profile_image", "null")
                        ),
                        User(
                            creator.getString("_id"),
                            creator.getString("username"),
                            creator.optString("profile_image","null")
                        ),
                        room.getString("name"),
                        room.getString("description"),
                        room.getInt("total_members")
                    )
                )
            }
            rooms
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun getMyRooms(): MutableList<Room> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/user/rooms"

        try {
            val jsonBody = getRequest(url) ?: return@withContext mutableListOf()
            val data = jsonBody.getJSONArray("data")
            val rooms : MutableList<Room> = mutableListOf()
            for(i in 0 until data.length()){
                val room = data.getJSONObject(i)
                val admin = room.getJSONObject("admin")
                val creator = room.getJSONObject("creator")

                rooms.add(
                    Room(
                        room.getString("_id"),
                        room.optString("profile_image","null"),
                        User(
                            admin.getString("_id"),
                            admin.getString("username"),
                            admin.optString("profile_image", "null")
                        ),
                        User(
                            creator.getString("_id"),
                            creator.getString("username"),
                            creator.optString("profile_image","null")
                        ),
                        room.getString("name"),
                        room.getString("description"),
                        room.getInt("total_members")
                    )
                )
            }
            rooms
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun getVideoInfo(videoUrl : String): String? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/get_video?video_url=$videoUrl"
        try {
            val jsonBody = getRequest(url) ?: return@withContext null
            val data = jsonBody.getJSONArray("data")
            if(data.length() > 0){
                data.getJSONObject(0).getString("url")
            }else{
                null
            }
        }catch(e : IOException){
            null
        }
    }

    suspend fun getRoomById(roomId: String): Room? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/room/$roomId"

        try {
            val jsonBody = getRequest(url) ?: return@withContext null
            val data = jsonBody.getJSONObject("data")
            val admin = data.getJSONObject("admin")
            val creator = data.getJSONObject("creator")
            Room(
                data.getString("_id"),
                data.optString("profile_image","null"),
                User(
                    admin.getString("_id"),
                    admin.getString("username"),
                    admin.optString("profile_image","null")
                ),
                User(
                    creator.getString("_id"),
                    creator.getString("username"),
                    creator.optString("profile_image","null")
                ),
                data.getString("name"),
                data.getString("description"),
                data.getInt("total_members")
            )
        } catch (e: IOException) {
            println("Something went wrong")
            null
        }
    }

    suspend fun signIn(username: String, password: String): JSONObject? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/auth/sign_in"
        val json = JsonObject(
            mapOf(
                "username" to JsonPrimitive(username),
                "password" to JsonPrimitive(password)
            )
        )

        val jsonBody = postRequest(url, json)
        if(jsonBody != null){
            val status = jsonBody.getString("status")
            if(status == "success"){
                val data = jsonBody.getJSONObject("data")
                val token = data.getString("token")
                accessToken = token
            }
            return@withContext jsonBody
        }
        null
    }

    suspend fun signUp(username: String,email: String,password: String): String? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/auth/sign_up"
        val json = JsonObject(
            mapOf(
                "username" to JsonPrimitive(username),
                "email" to JsonPrimitive(email),
                "password" to JsonPrimitive(password)
            )
        )

        val jsonBody = postRequest(url, json)
        if(jsonBody != null){
            val data = jsonBody.getJSONObject("data")
            val token = data.getString("token")
            Log.i("HTTP_REQUEST_INFO", "TOKEN: $token")
            accessToken = token
            return@withContext token
        }
        null
    }

    suspend fun getChatMessages(roomId: String): MutableList<Message> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/room/chat?room_id=$roomId"

        try {
            val jsonBody = getRequest(url) ?: return@withContext mutableListOf()
            val data = jsonBody.getJSONArray("data")
            val chatMessages : MutableList<Message> = mutableListOf()
            for(i in 0 until data.length()){
                val message = data.getJSONObject(i)
                val user = message.getJSONObject("user")
                chatMessages.add(
                    Message(
                        User(
                            user.getString("_id"),
                            user.getString("username"),
                            user.optString("profile_image","null")
                        ),
                        message.getString("message"),
                        message.getString("type"),
                        message.getString("_createdAt")
                    )
                )
            }
            chatMessages
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun joinRoom(roomId: String): Boolean = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/room/join"
        val json = JsonObject(
            mapOf(
                "room_id" to JsonPrimitive(roomId)
            )
        )
        val jsonBody = postRequest(url, json) ?: return@withContext false
        return@withContext jsonBody.getString("status") == "success"
    }

    suspend fun searchYoutubeVideos(query: String): MutableList<Video> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/room/youtube_search?q=$query"

        try {
            val jsonBody = getRequest(url) ?: return@withContext mutableListOf()
            val isOfficialAPI = jsonBody.getBoolean("is_official_api")
            val data = jsonBody.getJSONArray("videos")
            val videos : MutableList<Video> = mutableListOf()
            for(i in 0 until data.length()){
                val video = data.getJSONObject(i)
                if(isOfficialAPI){
                    TODO("Check youtube official API properties.")
                }else{
                    videos.add(
                        Video(
                            video.getString("title"),
                            video.getString("url"),
                            video.getString("thumbnail")
                        )
                    )
                }
            }
            videos

        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun searchRooms(query: String): MutableList<Room> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/search?query=$query"

        try {
            val jsonBody = getRequest(url) ?: return@withContext mutableListOf()
            val data = jsonBody.getJSONArray("data")
            val rooms : MutableList<Room> = mutableListOf()
            for(i in 0 until data.length()){
                val room = data.getJSONObject(i)
                val admin = room.getJSONObject("admin")
                val creator = room.getJSONObject("creator")

                rooms.add(
                    Room(
                        room.getString("_id"),
                        room.optString("profile_image","null"),
                        User(
                            admin.getString("_id"),
                            admin.getString("username"),
                            admin.optString("profile_image", "null")
                        ),
                        User(
                            creator.getString("_id"),
                            creator.getString("username"),
                            creator.optString("profile_image","null")
                        ),
                        room.getString("name"),
                        room.getString("description"),
                        room.getInt("total_members")
                    )
                )
            }
            rooms
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun createRoom(name: String,description: String,privacy: String,password: String?): JSONObject? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/room/create"
        val json = JsonObject(
            mapOf(
                "name" to JsonPrimitive(name),
                "description" to JsonPrimitive(description),
                "privacy" to JsonPrimitive(privacy),
                "password" to JsonPrimitive(password)
            )
        )

        return@withContext postRequest(url, json)
    }

    suspend fun uploadRoomProfileImage(roomId: String,roomImage: String): JSONObject? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/room/upload_profile_image"
        val imageFile = File(roomImage)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("room_id", roomId)
            .addFormDataPart("profile_image", imageFile.name, imageFile.asRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", accessToken)
            .post(requestBody)
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            response.headers["Content-Type"]?.contains("application/json")
                ?: throw Throwable("response.body not Found")
            val responseBody: ResponseBody =
                response.body ?: throw Throwable("response.body not Found")
            val resBody = responseBody.string()

            return@withContext JSONObject(resBody)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("CLIENT", "e.message: ${e.message}")
        }
        null
    }
}