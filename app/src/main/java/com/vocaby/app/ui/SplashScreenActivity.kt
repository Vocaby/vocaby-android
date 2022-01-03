package com.vocaby.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent

// TODO: setup required resources here (shared prefs, etc)
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}