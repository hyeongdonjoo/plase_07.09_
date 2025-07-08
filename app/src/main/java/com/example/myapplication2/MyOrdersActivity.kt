package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var layoutOrders: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_my_orders)

        layoutOrders = findViewById(R.id.layoutOrders)

        findViewById<Button>(R.id.buttonGoHome).setOnClickListener {
            val intent = Intent(this, ShopListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        loadLatestOrder()
    }

    private fun loadLatestOrder() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val lang = LocaleHelper.getSavedLanguage(this)

        db.collection("users")
            .document(userId)
            .collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                layoutOrders.removeAllViews()

                if (result.isEmpty) {
                    val emptyView = TextView(this)
                    emptyView.text = "주문 내역이 없습니다."
                    layoutOrders.addView(emptyView)
                    return@addOnSuccessListener
                }

                for (document in result) {
                    val orderView = layoutInflater.inflate(R.layout.item_order, null)

                    val shopText = orderView.findViewById<TextView>(R.id.textShopName)
                    val priceText = orderView.findViewById<TextView>(R.id.textTotalPrice)
                    val itemsText = orderView.findViewById<TextView>(R.id.textOrderItems)
                    val orderNumberText = orderView.findViewById<TextView>(R.id.textOrderNumber)

                    val orderNumber = document.get("orderNumber")?.toString() ?: "주문번호 없음"
                    orderNumberText.text = "주문번호: $orderNumber"

                    shopText.text = "가게: ${document.getString("shopName")}"
                    priceText.text = "총액: ${document.getLong("totalPrice")}원"

                    val items = document["items"] as? List<Map<String, Any>>
                    val itemDescriptions = items?.joinToString(", ") { item ->
                        val qty = (item["quantity"] as? Long ?: 1).toInt()

                        // 외국어 번역된 이름 우선 사용, 없으면 name
                        val translatedName = item["translatedName"] as? Map<*, *>
                        val name = translatedName?.get(lang)?.toString()
                            ?: translatedName?.get("ko")?.toString()
                            ?: item["name"]?.toString()
                            ?: "알 수 없음"

                        "$name x$qty"
                    } ?: "메뉴 정보 없음"

                    itemsText.text = "메뉴: $itemDescriptions"

                    layoutOrders.addView(orderView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "주문 내역 불러오기 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
