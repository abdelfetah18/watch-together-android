package com.abdelfetahdev.watch_together.utilities

import android.app.Dialog
import android.content.Context
import com.abdelfetahdev.watch_together.R

class LoadingDialogue(val context: Context) {
    private val dialog: Dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

    fun show() {
        dialog.setContentView(R.layout.loading)
        dialog.show()
    }

    fun hide() {
        dialog.hide()
    }
}