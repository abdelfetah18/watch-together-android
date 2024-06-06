package com.abdelfetahdev.watch_together.entities

import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject

data class Room(
    val id: String?,
    val name: String,
    val description: String,
    val privacy: String,
    val totalMembers: Int?,
    val profileImage: Asset?,
    val admin: User?,
    val creator: User?
) {
    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()

        jsonObject.put("name", name)
        jsonObject.put("bio", description)
        jsonObject.put("privacy", privacy)
        jsonObject.put("profileImage", (profileImage?.toJSON() ?: JSONObject.NULL))
        jsonObject.put("categories", JSONArray())

        return jsonObject
    }

    companion object {
        fun getListOfRooms(json: JSONArray): ArrayList<Room> {
            val rooms = ArrayList<Room>()

            for (index in 0 until json.length()) {
                val profileImage = json.getJSONObject(index).optJSONObject("profile_image")
                rooms.add(
                    Room(
                        json.getJSONObject(index).getString("_id"),
                        json.getJSONObject(index).getString("name"),
                        json.getJSONObject(index).getString("bio"),
                        json.getJSONObject(index).getString("privacy"),
                        json.getJSONObject(index).getInt("total_members"),
                        if (profileImage != null) Asset(profileImage.getString("url")) else null,
                        User.fromJSON(json.getJSONObject(index).getJSONObject("admin")),
                        User.fromJSON(json.getJSONObject(index).getJSONObject("creator"))
                    )
                )
            }

            return rooms
        }

        fun fromJSON(json: JSONObject): Room {
            val profileImage = json.optJSONObject("profile_image")

            return Room(
                json.getString("_id"),
                json.getString("name"),
                json.getString("bio"),
                json.getString("privacy"),
                json.optInt("total_members"),
                if (profileImage != null) Asset(profileImage.getString("url")) else null,
                User.fromJSON(json.getJSONObject("admin")),
                User.fromJSON(json.getJSONObject("creator"))
            )
        }

        fun fromBundle(bundle: Bundle): Room {
            val profileImageUrl = bundle.getString("profileImageUrl")
            return Room(
                bundle.getString("id"),
                bundle.getString("name") ?: "",
                bundle.getString("description") ?: "",
                bundle.getString("privacy") ?: "",
                bundle.getInt("totalMembers"),
                if (profileImageUrl != null) Asset(profileImageUrl) else null,
                User(bundle.getString("adminId"), "", "", "", null),
                User(bundle.getString("creatorId"), "", "", "", null),
            )
        }
    }

    fun toBundle(): Bundle {
        val bundle = Bundle()

        bundle.putString("id", id)
        bundle.putString("name", name)
        bundle.putString("description", description)
        bundle.putString("privacy", privacy)
        bundle.putInt("totalMembers", totalMembers ?: 1)

        if (profileImage != null) {
            bundle.putString("profileImageUrl", profileImage.url)
        }

        if (admin != null) {
            bundle.putString("adminId", admin.id)
        }

        if (creator != null) {
            bundle.putString("creatorId", creator.id)
        }

        return bundle
    }
}
