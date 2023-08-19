package com.abdelfetahdev.watch_together

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class Client (private val accessToken: String){
    private val client = OkHttpClient()

    private fun getDataJsonArray(url: String): JSONArray? {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", accessToken)
            .get()
            .build()
        val response: Response = client.newCall(request).execute()
        response.headers["Content-Type"]?.contains("application/json")
            ?: throw Throwable("response.body not Found")
        val responseBody: ResponseBody =
            response.body ?: throw Throwable("response.body not Found")
        val resBody = responseBody.string()

        val jsonBody = JSONObject(resBody)
        if (jsonBody.getString("status") == "success") {
            return jsonBody.getJSONArray("data")
        }
        return null
    }

    private fun getDataJsonObject(url: String, token: String): JSONObject? {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", token)
            .get()
            .build()
        val response: Response = client.newCall(request).execute()
        response.headers["Content-Type"]?.contains("application/json")
            ?: return null
        val responseBody: ResponseBody =
            response.body ?: return null
        val resBody = responseBody.string()
        println("url: $url")
        println("resBody: $resBody")
        val jsonBody = JSONObject(resBody)
        if (jsonBody.getString("status") == "success") {
            return jsonBody.getJSONObject("data")
        }
        return null
    }

    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/user"

        try {
            val data = getDataJsonObject(url, accessToken)
            if(data != null){
                val user = User(
                    data.getString("_id"),
                    data.getString("username"),
                    data.optString("profile_image","null")
                )
                user
            }else{
                null
            }
        } catch (e: IOException) {
            println("Something went wrong")
            null
        }
    }

    suspend fun getNewAccessToken(token : String) : String? = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/auth/update_token"

        try {
            val data = getDataJsonObject(url, token)
            data?.getString("token").toString()
        }catch(e : IOException){
            null
        }
    }

    suspend fun getExploreRooms(): MutableList<Room> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/explore"

        try {
            val data = getDataJsonArray(url)
            if(data != null){
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
            }else{
                mutableListOf()
            }
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun getMyRooms(): MutableList<Room> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/user/rooms"

        try {
            val data = getDataJsonArray(url)
            if(data != null){
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
            }else{
                mutableListOf()
            }
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun getVideoInfo(videoUrl : String): String? = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/get_video?video_url=$videoUrl"
        try {
            val data = getDataJsonArray(url)
            if(data != null){
                if(data.length() > 0){
                    data.getJSONObject(0).getString("url")
                }else{
                    null
                }
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
            val data = getDataJsonObject(url, accessToken)
            if(data != null){
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
            }else{
                null
            }
        } catch (e: IOException) {
            println("Something went wrong")
            null
        }
    }

    suspend fun signIn(username: String, password: String): String? = withContext(Dispatchers.IO) {
        val json = JsonObject(
            mapOf(
                "username" to JsonPrimitive(username),
                "password" to JsonPrimitive(password)
            )
        )

        val jsonString = json.toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://watch-together-uvdn.onrender.com/api/auth/sign_in")
            .post(requestBody)
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            response.headers["Content-Type"]?.contains("application/json")
                ?: throw Throwable("response.body not Found")
            val responseBody: ResponseBody =
                response.body ?: throw Throwable("response.body not Found")
            val resBody = responseBody.string()

            val jsonBody = JSONObject(resBody)
            if (jsonBody.getString("status") == "success") {
                val data = jsonBody.getJSONObject("data")
                data.getString("token").toString()
            }else{
                null
            }
        } catch (e: IOException) {
            println("Something went wrong")
            null
        }
    }

    suspend fun getChatMessages(roomId: String): MutableList<Message> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/room/chat?room_id=$roomId"

        try {
            val data = getDataJsonArray(url)
            if(data != null){
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
            }else{
                mutableListOf()
            }
        }catch(e : IOException){
            mutableListOf()
        }
    }

    suspend fun joinRoom(roomId: String): Boolean = withContext(Dispatchers.IO) {
        val url = "https://watch-together-uvdn.onrender.com/api/room/join"

        try {
            val json = JsonObject(
                mapOf(
                    "room_id" to JsonPrimitive(roomId)
                )
            )

            val jsonString = json.toString()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", accessToken)
                .post(requestBody)
                .build()

            val response: Response = client.newCall(request).execute()
            response.headers["Content-Type"]?.contains("application/json")
                ?: return@withContext false
            val responseBody: ResponseBody =
                response.body ?: return@withContext false
            val resBody = responseBody.string()
            val jsonBody = JSONObject(resBody)
            return@withContext jsonBody.getString("status") == "success"
        } catch (e: IOException) {
            println("Something went wrong")
            false
        }
    }

    suspend fun searchYoutubeVideos(query: String): MutableList<Video> = withContext(Dispatchers.IO) {
        val url ="https://watch-together-uvdn.onrender.com/api/room/youtube_search?q=$query"

        try {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", accessToken)
                .get()
                .build()
            val response: Response = client.newCall(request).execute()
            response.headers["Content-Type"]?.contains("application/json")
                ?: throw Throwable("response.body not Found")
            val responseBody: ResponseBody =
                response.body ?: throw Throwable("response.body not Found")
            val resBody = responseBody.string()

            val jsonBody = JSONObject(resBody)
            if (jsonBody.getString("status") != "success") {
                return@withContext mutableListOf()
            }
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
}