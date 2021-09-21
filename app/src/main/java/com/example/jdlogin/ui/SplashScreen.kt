package com.example.jdlogin.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.jdlogin.R
import com.example.jdlogin.databinding.SplashScreenBinding

class SplashScreen: AppCompatActivity( ) {
    lateinit var binding: SplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        binding.intercorp1.startAnimation(topAnimation)
        binding.intercorp2.startAnimation(bottomAnimation)

        val splashScreenTimeOut = 4000
        val home = Intent(this@SplashScreen, MainActivity::class.java)
        Handler().postDelayed({
            startActivity(home)
            finish()
        }, splashScreenTimeOut.toLong())
    }
}
