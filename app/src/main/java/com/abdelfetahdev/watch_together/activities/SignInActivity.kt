package com.abdelfetahdev.watch_together.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.UserCredentials
import com.abdelfetahdev.watch_together.entities.UserSession
import com.abdelfetahdev.watch_together.rest_api.RestAuth
import com.abdelfetahdev.watch_together.utilities.LoadingDialogue
import kotlinx.coroutines.launch


class SignInActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var passwordInput: EditText

    lateinit var signInButton: Button
    lateinit var goToSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val myApp = this@SignInActivity.application as MyApp
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)

        signInButton = findViewById(R.id.sign_in_button)
        goToSignUp = findViewById(R.id.go_to_sign_up)

        signInButton.setOnClickListener {
            val loadingDialogue = LoadingDialogue(this@SignInActivity)
            loadingDialogue.show()

            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            lifecycle.coroutineScope.launch {
                val restAuth = RestAuth(myApp.httpClient)

                val response = restAuth.signIn(UserCredentials(username, password))
                if (response.isError) {
                    Toast.makeText(this@SignInActivity, response.message, Toast.LENGTH_LONG).show()
                } else {
                    val prefs: SharedPreferences = this@SignInActivity.getSharedPreferences(
                        MyApp.SHARED_PREFS,
                        Context.MODE_PRIVATE
                    )

                    with(prefs.edit()) {
                        putString(UserSession.ACCESS_TOKEN, response.data!!.access_token)
                        apply()
                    }

                    myApp.loadAccessToken()
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                }
            }
        }

        goToSignUp.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            finish()
        }
    }
}