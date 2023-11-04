package com.abdelfetahdev.watch_together

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
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
        setContentView(R.layout.loading)
        println("Loading screen.")
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
            }else{
                store.clearToken()
                handleSignIn(store)
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
                val response = (application as MyApp).client.signIn(username, password)
                if(response != null){
                    val status = response.getString("status")
                    if(status == "success"){
                        val data = response.getJSONObject("data")
                        val token = data.getString("token")
                        store.saveToken(token)
                        (application as MyApp).initUser()
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        val message = response.optString("message")
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle("Alert")
                        alertDialog.setMessage(message ?: "Something went wrong")
                        alertDialog.setNeutralButton("OK", object: DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                loadingScreen.visibility = View.GONE
                            }
                        })
                        alertDialog.show()
                    }
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


