package com.abdelfetahdev.watch_together

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso


class SettingsActivity : AppCompatActivity() {
    lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()
        accessToken = store.getToken().toString()

        val usernameView = findViewById<TextView>(R.id.username)
        val profileImageView = findViewById<ImageView>(R.id.profile_image)

        val user = myApplication.user

        usernameView.text = user.username
        if(user.profile_image != "null"){
            Picasso.get().load(user.profile_image).placeholder(R.drawable.profile_1_1).into(profileImageView)
        }

        val navBar = NavigationController(findViewById(R.id.home_btn), findViewById(R.id.profile_btn), findViewById(R.id.settings_btn), this)
        navBar.initNavigation("settings")

        val logOutBtn = findViewById<Button>(R.id.log_out_btn)
        logOutBtn.setOnClickListener {
            store.clearToken()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}