package com.start3a.ishowyou.signin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.MainActivity
import kotlinx.android.synthetic.main.activity_phone_verify.*
import java.util.concurrent.TimeUnit

class PhoneVerifyActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var verificationId: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verify)
        
        mAuth = FirebaseAuth.getInstance()

        val phoneNumber = intent.getStringExtra("phoneNumber")!!
        sendVerificationCode(phoneNumber)

        btnVerifyDone.setOnClickListener {
            val code = editVerifCode.text.toString()
            if (code.isBlank() || code.length < 6) {
                editVerifCode.run {
                    error = "Enter Code.."
                    requestFocus()
                    return@setOnClickListener
                }
            }
            loading_verify.visibility = View.VISIBLE
            verifyCode(code)
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInwithCredential(credential)
    }

    private fun signInwithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                // 인증 성공
                if (task.isSuccessful) {
                    val intent = Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK ; Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                else {
                    Toast.makeText(applicationContext, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(code: String, p0: PhoneAuthProvider.ForceResendingToken) {
            verificationId = code
        }

        override fun onVerificationCompleted(pc: PhoneAuthCredential) {
            pc.smsCode?.let {
                loading_verify.visibility = View.VISIBLE
                verifyCode(it)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(applicationContext, p0.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}