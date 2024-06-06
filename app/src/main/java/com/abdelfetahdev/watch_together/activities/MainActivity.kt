package com.abdelfetahdev.watch_together.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.NavHostFragment
import com.abdelfetahdev.watch_together.R


class MainActivity : AppCompatActivity() {
    lateinit var homeButton: ImageView
    lateinit var roomButton: ImageView
    lateinit var settingsButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        homeButton = findViewById(R.id.home_button)
        roomButton = findViewById(R.id.room_button)
        settingsButton = findViewById(R.id.settings_button)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        homeButton.setOnClickListener {
            navController.navigate("Home")

            resetNavigationBarIcons()
            homeButton.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_home_fill,
                    null
                )
            )
        }

        roomButton.setOnClickListener {
            navController.navigate("Room")

            resetNavigationBarIcons()
            roomButton.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_circle_user_fill,
                    null
                )
            )
        }

        settingsButton.setOnClickListener {
            navController.navigate("Settings")

            resetNavigationBarIcons()
            settingsButton.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_cog_fill,
                    null
                )
            )
        }
    }

    fun resetNavigationBarIcons(){
        homeButton.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_home_outline,
                null
            )
        )

        roomButton.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_circle_user_outline,
                null
            )
        )

        settingsButton.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_cog_outline,
                null
            )
        )
    }
}


