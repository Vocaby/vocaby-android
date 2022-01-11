package com.vocaby.application.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// TODO: setup required resources here (shared prefs, etc)
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}