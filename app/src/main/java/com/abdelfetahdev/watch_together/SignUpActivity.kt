package com.abdelfetahdev.watch_together

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.cardview.widget.CardView
import kotlinx.coroutines.runBlocking

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()

        val usernameInput = findViewById<EditText>(R.id.username)
        val emailInput = findViewById<EditText>(R.id.email)
        val passwordInput = findViewById<EditText>(R.id.password)

        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)
        val signInBtn = findViewById<Button>(R.id.sign_in_btn)

        signUpBtn.setOnClickListener {
            val loadingScreen = findViewById<CardView>(R.id.loading_screen)
            loadingScreen.visibility = View.VISIBLE

            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            runBlocking {
                val token = (application as MyApp).client.signUp(username,email,password)
                if(token != null){
                    store.saveToken(token)
                    (application as MyApp).initUser()
                    val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        signInBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}