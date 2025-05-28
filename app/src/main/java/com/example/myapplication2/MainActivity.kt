package com.example.myapplication2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // 🔥 핵심 추가: 이미 로그인된 사용자 체크
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, ShopListActivity::class.java))
            finish()
            return // 이후 코드 실행 방지
        }

        val loginButton: Button = findViewById(R.id.button)
        val registerButton: Button = findViewById(R.id.button2)

        loginButton.setOnClickListener { loginUser() }
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        checkLocationPermission()
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
                    startActivity(Intent(this, ShopListActivity::class.java))
                    finish() // 로그인 성공 시 현재 액티비티 종료
                } else {
                    showCustomToast("로그인 실패: ${task.exception?.message}")
                }
            }
    }

    private fun showCustomToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.view?.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        toast.show()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            checkWifiAndScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkWifiAndScan()
            } else {
                showCustomToast("위치 권한이 필요합니다.")
            }
        }
    }

    private fun checkWifiAndScan() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            showCustomToast("Wi-Fi를 켜주세요")
            return
        }
        scanNearbyWifi()
    }

    private fun scanNearbyWifi() {
        try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val success = wifiManager.startScan()
            if (!success) {
                Log.e("WiFiScan", "스캔 시작 실패")
                return
            }
            val scanResults = wifiManager.scanResults

            for (result in scanResults) {
                Log.d("WiFiScan", "📶 SSID: ${result.SSID}, RSSI: ${result.level} dBm")
                if (result.SSID == "김밥천국_WiFi" && result.level > -60) {
                    goToShopMenu("김밥천국")
                    return
                }
            }
        } catch (e: SecurityException) {
            Log.e("WiFiScan", "권한 오류", e)
            showCustomToast("권한 문제 발생")
        }
    }

    private fun goToShopMenu(shopName: String) {
        val intent = Intent(this, ShopListActivity::class.java).apply {
            putExtra("shopName", shopName)
        }
        startActivity(intent)
    }
}
