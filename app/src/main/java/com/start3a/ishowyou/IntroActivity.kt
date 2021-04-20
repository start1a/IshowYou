package com.start3a.ishowyou

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.start3a.ishowyou.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    var runnable = Runnable {
        val intent = Intent(applicationContext, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        setContentView(R.layout.activity_intro)

    }

    override fun onResume() {
        super.onResume()
        intro_image.postDelayed(runnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        intro_image.removeCallbacks(runnable)
    }
}