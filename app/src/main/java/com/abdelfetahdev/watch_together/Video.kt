package com.abdelfetahdev.watch_together

class Video(video_title: String, video_url: String, thumb_url: String){
    private val title = video_title
    private val thumb = thumb_url
    private val url = video_url

    fun getTitle(): String { return title }
    fun getUrl(): String { return url }
    fun getThumb(): String { return thumb }
}