package com.abdelfetahdev.watch_together

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            val accessToken = (application as MyApp).client.getNewAccessToken(token)
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
        signInBtn.setOnClickListener {
            Toast.makeText(this, "Sign in", Toast.LENGTH_SHORT).show()
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
    }
}


