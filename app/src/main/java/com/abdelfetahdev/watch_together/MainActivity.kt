package com.abdelfetahdev.watch_together

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()
        val token = store.getToken()
        if(token == null || token == "null") {
            handleSignIn(store)
        }else{
            handleToken(token, store)
        }
    }

    private fun handleToken(token: String, store: UserStore){
        runBlocking {
            val accessToken = (application as MyApp).client.getNewAccessToken()
            println("token: $token")
            println("accessToken: $accessToken")
            if(accessToken != null){
                store.saveToken(accessToken)
                (application as MyApp).initUser()
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun handleSignIn(store : UserStore){
        setContentView(R.layout.sign_in)
        val signInBtn = findViewById<Button>(R.id.sign_in)
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)

        signInBtn.setOnClickListener {
            val loadingScreen = findViewById<CardView>(R.id.loading_screen)
            loadingScreen.visibility = View.VISIBLE
            val username = findViewById<EditText>(R.id.username).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()

            runBlocking {
                val token = (application as MyApp).client.signIn(username, password)
                if(token != null){
                    store.saveToken(token)
                    (application as MyApp).initUser()
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        signUpBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


