package com.start3a.ishowyou.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.MainActivity
import com.start3a.ishowyou.R
import com.start3a.ishowyou.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_signin.*

class SigninActivity : AppCompatActivity() {

    private var viewModel: LoginViewModel? = null
    private val REQUEST_AUTH_SIGN_IN = 0

    private val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(LoginViewModel::class.java)
        }
    }

    override fun onStart() {
        super.onStart()
        if (shouldStartSignIn())
            startSignIn()
        else startNextActivity(MainActivity::class.java)
    }

    private fun shouldStartSignIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignIn() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.AppTheme_NoActionBar)
            .build()

        startActivityForResult(intent, REQUEST_AUTH_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_AUTH_SIGN_IN -> {
                if (resultCode != Activity.RESULT_OK && shouldStartSignIn())
                    startSignIn()
                else successSignIn()
            }
        }
    }

    private fun successSignIn() {
        viewModel!!.run {
            checkUserExist { startNextActivity(MainActivity::class.java) }
        }
    }

    private fun startNextActivity(desActivity: Class<*>) {
        val intent = Intent(applicationContext, desActivity)
        startActivity(intent)
        finish()
    }
}