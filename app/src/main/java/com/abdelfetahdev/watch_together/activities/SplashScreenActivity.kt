package com.abdelfetahdev.watch_together.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.entities.UserSession


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_splash_screen)

        val prefs: SharedPreferences = getSharedPreferences(
            MyApp.SHARED_PREFS,
            Context.MODE_PRIVATE
        )

        val accessToken = prefs.getString(UserSession.ACCESS_TOKEN, "")
        if (accessToken == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}