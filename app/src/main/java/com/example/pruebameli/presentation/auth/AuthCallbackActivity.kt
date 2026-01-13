package com.example.pruebameli.presentation.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.pruebameli.MainActivity

class AuthCallbackActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        val state = uri?.getQueryParameter("state")
        val error = uri?.getQueryParameter("error")

        Log.d("CALLBACK", "uri=$uri")
        Log.d("CALLBACK", "code=$code state=$state error=$error")

        val i = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("code", code)
            putExtra("state", state)
            putExtra("error", error)
        }

        startActivity(i)
        finish()
    }
}