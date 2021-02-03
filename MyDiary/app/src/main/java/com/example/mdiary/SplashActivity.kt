package com.example.mdiary

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val shader: Shader = LinearGradient(
            0.toFloat(),
            0.toFloat(),
            0.toFloat(),
            20.toFloat(),
            ContextCompat.getColor(this, R.color.turquoise),
            ContextCompat.getColor(this, R.color.light_blue),
            TileMode.CLAMP
        )
        tvSplashTitle.paint.shader = shader
    }

    override fun onStart() {
        super.onStart()

        val splashImageView = findViewById<ImageView>(R.id.splashImageView)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotateAnimation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                startActivity(Intent(this@SplashActivity,ScrollingActivity::class.java))
                finish()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        splashImageView.startAnimation(rotateAnimation);
    }
}
