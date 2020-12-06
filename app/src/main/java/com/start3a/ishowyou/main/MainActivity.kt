package com.start3a.ishowyou.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.start3a.ishowyou.R
import com.start3a.ishowyou.signin.SigninActivity

class MainActivity : AppCompatActivity() {

    private var viewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            supportFragmentManager.beginTransaction().run {
                add(R.id.contentViewFrame, YoutubePlayerFragment()).commit()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AuthUI.getInstance().signOut(applicationContext).addOnSuccessListener {
            val intent = Intent(applicationContext, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
