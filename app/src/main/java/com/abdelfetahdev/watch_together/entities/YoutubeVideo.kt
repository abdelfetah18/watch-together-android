package com.abdelfetahdev.watch_together.entities

import org.json.JSONArray
import java.sql.Timestamp

data class YoutubeVideo(
    val thumbnailUrl: String,
    val videoTitle: String,
    val channelName: String,
    val views: String,
    val timestamp: String,
    val postedAt: String,
    val videoUrl: String
) {
    companion object {
        fun formatViews(views: Int): String {
            return when {
                views >= 1000000000 -> String.format("%.1fB", views.toFloat() / 1000000000)
                views >= 1000000 -> String.format("%.1fM", views.toFloat() / 1000000)
                views >= 1000 -> String.format("%.1fK", views.toFloat() / 1000)
                else -> views.toString()
            }
        }

        fun getListOfVideos(json: JSONArray): ArrayList<YoutubeVideo> {
            val videos = ArrayList<YoutubeVideo>()

            for (index in 0 until json.length()) {
                videos.add(
                    YoutubeVideo(
                        json.getJSONObject(index).getString("thumbnail"),
                        json.getJSONObject(index).getString("title"),
                        json.getJSONObject(index).getJSONObject("author").getString("name"),
                        formatViews(json.getJSONObject(index).getInt("views")),
                        json.getJSONObject(index).getString("timestamp"),
                        json.getJSONObject(index).getString("ago"),
                        json.getJSONObject(index).getString("url")

                    )
                )
            }

            return videos
        }

    }
}
