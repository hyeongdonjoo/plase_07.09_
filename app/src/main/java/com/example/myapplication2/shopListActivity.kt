package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShopListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopAdapter
    private val shopList = mutableListOf<Shop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // ✅ 상단바 제거
        setContentView(R.layout.activity_shop_list)

        // ✅ 로그아웃 버튼 동작
        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Firebase 세션 종료
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ✅ 뒤로가기 버튼 동작
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewShops)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ShopAdapter(shopList) { selectedShop ->
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("shopName", selectedShop.name)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        loadShopsFromFirestore()
    }

    private fun loadShopsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        Log.d("ShopListActivity", "📢 Firestore 불러오기 시작")

        db.collection("shops")
            .get()
            .addOnSuccessListener { result ->
                Log.d("ShopListActivity", "✅ Firestore 불러오기 성공")
                shopList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: document.id
                    val address = document.getString("address") ?: "주소 없음"
                    Log.d("ShopListActivity", "불러온 가게: $name / $address")
                    shopList.add(Shop(name, address))
                }
                Log.d("ShopListActivity", "총 ${shopList.size}개 가게 추가됨")
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ShopListActivity", "🔥 Firestore 불러오기 실패", e)
            }
    }
}