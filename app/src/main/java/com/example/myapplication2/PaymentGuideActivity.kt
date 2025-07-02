package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_guide)

        val shopName = intent.getStringExtra("shopName") ?: ""
        val totalPrice = intent.getIntExtra("totalPrice", 0)
        val menuSummary = intent.getStringExtra("menuSummary") ?: ""

        findViewById<Button>(R.id.buttonStartPayment).setOnClickListener {
            sendOrderToFirebase(shopName, totalPrice, menuSummary)
        }
    }

    private fun sendOrderToFirebase(shopName: String, totalPrice: Int, menuSummary: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 주문번호 생성
        val orderNumber = Timestamp.now().toDate().time.toString()

        // menuSummary를 아이템 리스트로 변환 (예시)
        val items = menuSummary.split(",").map {
            val parts = it.trim().split(" x ")
            mapOf(
                "name" to (parts.getOrNull(0) ?: ""),
                "quantity" to (parts.getOrNull(1)?.toIntOrNull() ?: 1)
            )
        }

        // 주문 데이터 생성 (orderNumber 포함)
        val orderData = hashMapOf(
            "orderNumber" to orderNumber,
            "shopName" to shopName,
            "items" to items,
            "totalPrice" to totalPrice,
            "timestamp" to Timestamp.now()
        )

        // Firestore에 저장
        db.collection("users")
            .document(userId)
            .collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                // 저장 성공 → 주문완료 화면 이동
                val intent = Intent(this, OrderCompleteActivity::class.java).apply {
                    putExtra("shopName", shopName)
                    putExtra("totalPrice", totalPrice)
                    putExtra("menuSummary", menuSummary)
                    putExtra("orderNumber", orderNumber) // 주문번호도 넘김
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "주문 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
