package com.example.myapplication2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()  // 상단바 숨김
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // ✅ 자동 로그인 처리
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, ShopListActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val loginButton: Button = findViewById(R.id.button)
        val registerButton: Button = findViewById(R.id.button2)

        loginButton.setOnClickListener {
            loginUser()
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = findViewById<EditText>(R.id.EmailAddress).text.toString()
        val password = findViewById<EditText>(R.id.Password).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            showCustomToast("이메일과 비밀번호를 입력해주세요")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showCustomToast("로그인 성공")
                    val intent = Intent(this, ShopListActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showCustomToast("로그인 실패: ${task.exception?.message}")
                }
            }
    }

    // ✅ 글자색 검정으로 명확하게 보이는 Toast 함수
    private fun showCustomToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.view?.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        toast.show()
    }
}