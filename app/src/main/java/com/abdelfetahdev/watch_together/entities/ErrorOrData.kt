package com.abdelfetahdev.watch_together.entities

data class ErrorOrData<T>(val data: T?, val message: String,val invalidAuth: Boolean = false) {
    var isError: Boolean = data == null
}
