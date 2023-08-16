package com.abdelfetahdev.watch_together

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView.OnCloseListener
import android.widget.Toast

class NavigationController (
    private val homeBtn : ImageButton,
    private val profileBtn : ImageButton,
    private val settingsButton : ImageButton,
    private val currentActivity : Activity) {

    companion object {
        const val selectedColor = "#334155"
        const val defaultColor = "#1F2937"
    }
    fun initNavigation(path : String){
        when(path){
            "home" -> homeBtn.setBackgroundColor(Color.parseColor(selectedColor))
            "profile" -> profileBtn.setBackgroundColor(Color.parseColor(selectedColor))
            "settings" -> settingsButton.setBackgroundColor(Color.parseColor(selectedColor))
        }

        homeBtn.setOnClickListener {
            val intent = Intent(currentActivity.baseContext, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            currentActivity.startActivity(intent)
        }

        profileBtn.setOnClickListener {
            val intent = Intent(currentActivity.baseContext, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            currentActivity.startActivity(intent)
        }

        settingsButton.setOnClickListener{
            val intent = Intent(currentActivity.baseContext, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            currentActivity.startActivity(intent)
        }
    }
}

