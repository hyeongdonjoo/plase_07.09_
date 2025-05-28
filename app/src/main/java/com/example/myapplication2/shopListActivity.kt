package com.example.myapplication2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShopListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noShopsTextView: TextView
    private lateinit var refreshButton: Button
    private lateinit var countdownTextView: TextView
    private lateinit var adapter: ShopAdapter
    private val shopList = mutableListOf<Shop>()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1002
    private lateinit var firestore: FirebaseFirestore
    private val detectedSsids = mutableSetOf<String>()

    private lateinit var wifiManager: WifiManager
    private var lastScanTime: Long = 0

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
            if (success) {
                handleScanResults()
            } else {
                Log.e("WiFiScan", "스캔 실패 또는 결과 없음")
                showNoWifiDetected()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_shop_list)

        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewShops)
        progressBar = findViewById(R.id.progressBar)
        noShopsTextView = findViewById(R.id.textViewNoShops)
        refreshButton = findViewById(R.id.buttonRefresh)
        countdownTextView = findViewById(R.id.textViewCountdown)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ShopAdapter(shopList) { selectedShop ->
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("shopName", selectedShop.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        refreshButton.setOnClickListener {
            refreshWifiScan()
            disableRefreshButtonWithCountdown()
        }

        refreshWifiScan()
    }

    private fun disableRefreshButtonWithCountdown() {
        refreshButton.isEnabled = false
        countdownTextView.visibility = View.VISIBLE

        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                countdownTextView.text = "${secondsRemaining}초 후 재시도 가능"
            }

            override fun onFinish() {
                refreshButton.isEnabled = true
                countdownTextView.visibility = View.GONE
            }
        }
        timer.start()
    }

    private fun refreshWifiScan() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        noShopsTextView.visibility = View.GONE
        checkLocationPermission()
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
            startWifiScan()
        }
    }

    private fun startWifiScan() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < 10000) {
            Toast.makeText(this, "⏱ 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        lastScanTime = currentTime

        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "📶 Wi-Fi가 꺼져 있습니다. 켜주세요.", Toast.LENGTH_SHORT).show()
            showNoWifiDetected()
            return
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        noShopsTextView.visibility = View.GONE

        val success = try {
            wifiManager.startScan()
        } catch (e: SecurityException) {
            Log.e("WiFiScan", "startScan() 권한 오류", e)
            false
        }

        Log.d("WiFiScan", "startScan() 호출됨 → 성공 여부: $success")

        if (!success) {
            showNoWifiDetected()
        }
    }

    private fun handleScanResults() {
        detectedSsids.clear()
        val scanResults = wifiManager.scanResults

        for (result in scanResults) {
            val ssid = result.SSID
            val rssi = result.level
            Log.d("WiFiScan", "📡 SSID: $ssid, RSSI: $rssi dBm")

            if (rssi > -60 && ssid.isNotBlank()) {
                detectedSsids.add(ssid)
            }
        }

        if (detectedSsids.isNotEmpty()) {
            loadShopsMatchingNearbyWifi()
        } else {
            showNoWifiDetected()
        }
    }

    private fun loadShopsMatchingNearbyWifi() {
        firestore.collection("shops")
            .get()
            .addOnSuccessListener { result ->
                shopList.clear()

                for (document in result) {
                    val name = document.getString("name") ?: continue
                    val address = document.getString("address") ?: "주소 없음"
                    val ssid = document.getString("ssid") ?: continue

                    if (detectedSsids.contains(ssid)) {
                        shopList.add(Shop(name, address))
                        Log.d("ShopListActivity", "📍감지된 가게: $name (SSID: $ssid)")
                    }
                }

                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE

                if (shopList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    noShopsTextView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noShopsTextView.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShopListActivity", "Firestore 불러오기 실패", e)
                progressBar.visibility = View.GONE
                noShopsTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
    }

    private fun showNoWifiDetected() {
        Toast.makeText(this, "📡 근처에 등록된 Wi-Fi가 없습니다.", Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        noShopsTextView.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startWifiScan()
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            noShopsTextView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiScanReceiver)
    }
}
