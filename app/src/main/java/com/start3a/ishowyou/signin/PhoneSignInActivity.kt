package com.start3a.ishowyou.signin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.MainActivity
import kotlinx.android.synthetic.main.activity_phone_sign_in.*

class PhoneSignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_sign_in)

        btnVerify.setOnClickListener {
            val phoneNumber = editPhoneNumber.text.toString().trim()

            if (phoneNumber.isNotBlank()) {
                val intent = Intent(applicationContext, PhoneVerifyActivity::class.java).apply {
                    putExtra("phoneNumber", phoneNumber)
                }
                startActivity(intent)
            }
            else Toast.makeText(applicationContext, "번호를 입력하세요", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK ; Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}