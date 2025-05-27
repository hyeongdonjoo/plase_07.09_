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

        // ✅ 위치 권한 체크 및 요청
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
                    val intent = Intent(this, ShopListActivity::class.java)
                    startActivity(intent)
                    finish()
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

    // ✅ 위치 권한 요청
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            scanNearbyWifi()
        }
    }

    // ✅ 권한 응답 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanNearbyWifi()
            } else {
                showCustomToast("위치 권한이 필요합니다.")
            }
        }
    }

    // ✅ RSSI로 Wi-Fi 감지
    private fun scanNearbyWifi() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val scanResults = wifiManager.scanResults

        for (result in scanResults) {
            val ssid = result.SSID
            val rssi = result.level

            Log.d("WiFiScan", "📶 SSID: $ssid, RSSI: $rssi dBm")

            if (ssid == "김밥천국_WiFi" && rssi > -60) {
                showCustomToast("김밥천국 접근 감지!")
                goToShopMenu("김밥천국")
                break
            }
        }
    }

    // ✅ 감지된 가게 메뉴로 이동
    private fun goToShopMenu(shopName: String) {
        val intent = Intent(this, ShopListActivity::class.java)
        intent.putExtra("shopName", shopName)
        startActivity(intent)
    }
}