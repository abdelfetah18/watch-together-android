package com.abdelfetahdev.watch_together.entities

import org.json.JSONArray

data class Member(val id: String, val user: User) {
    companion object {
        fun getListOfMembers(json: JSONArray): ArrayList<Member> {
            val members = ArrayList<Member>()

            for (index in 0 until json.length()) {
                members.add(
                    Member(
                        json.getJSONObject(index).getString("_id"),
                        User.fromJSON(json.getJSONObject(index).getJSONObject("user"))
                    )
                )
            }

            return members
        }

    }
}
