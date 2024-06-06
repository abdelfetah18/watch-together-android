package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.coroutineScope
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.User
import com.abdelfetahdev.watch_together.rest_api.RestAuth
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var emailInput: EditText
    lateinit var passwordInput: EditText

    lateinit var signUpButton: Button
    lateinit var goToSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val myApp = this@SignUpActivity.application as MyApp

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)

        signUpButton = findViewById(R.id.sign_up_button)
        goToSignIn = findViewById(R.id.go_to_sign_in)

        signUpButton.setOnClickListener {
            val loadingScreen = findViewById<CardView>(R.id.loading_screen)
            loadingScreen.visibility = View.VISIBLE

            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            lifecycle.coroutineScope.launch {
                val restAuth = RestAuth(myApp.httpClient)

                val response = restAuth.signUp(User("",username, email, password, null))
                if (response.isError) {
                    Toast.makeText(this@SignUpActivity, response.message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SignUpActivity, response.message, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                    finish()
                }
            }
        }

        goToSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}