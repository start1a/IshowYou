package com.start3a.ishowyou.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.MainActivity
import kotlinx.android.synthetic.main.activity_signin.*

class SigninActivity : AppCompatActivity() {

    private var viewModel: SignInViewModel? = null
    private val REQUEST_GOOGLE_AUTH_SIGN_IN = 0

    private val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(SignInViewModel::class.java)
        }

        btnSignWithGoogle.setOnClickListener {
            startSignInWithGoogle()
        }

        btnSignWithPhone.setOnClickListener {
            val intent = Intent(applicationContext, PhoneSignInActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!shouldStartSignIn()) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun shouldStartSignIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignInWithGoogle() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.AppTheme_NoActionBar)
            .build()

        startActivityForResult(intent, REQUEST_GOOGLE_AUTH_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_GOOGLE_AUTH_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}