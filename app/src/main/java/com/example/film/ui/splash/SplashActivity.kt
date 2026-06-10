package com.example.film.ui.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.film.R
import com.example.film.base.BaseActivity
import com.example.film.databinding.ActivitySplashBinding
import com.example.film.ui.activity.login.LoginActivity

class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {


    override fun initializeComponent() {
        super.initializeComponent()

        binding.progressBar.post {
            startAutoProgress()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            nextActivity()
        }, 2500)

    }

    private fun startAutoProgress() {
        binding.progressBar.max = 100
        binding.progressBar.progress = 0

        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.duration = 5000L
        animator.interpolator = LinearInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                binding.progressBar.progress = 0
                animator.start()
            }
        })
        animator.start()
    }

    private fun nextActivity() {
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java)
            .putExtra("fromSplash", true))
        finish()
    }



}