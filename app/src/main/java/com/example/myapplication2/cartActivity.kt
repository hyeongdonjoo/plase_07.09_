package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var layoutCartItems: LinearLayout
    private lateinit var textTotalPrice: TextView
    private lateinit var textEmptyCart: TextView
    private lateinit var buttonOrder: Button
    private lateinit var buttonBack: Button
    private lateinit var shopName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_cart)

        layoutCartItems = findViewById(R.id.layoutCartItems)
        textTotalPrice = findViewById(R.id.textTotalPrice)
        textEmptyCart = findViewById(R.id.textEmptyCart)
        buttonOrder = findViewById(R.id.buttonOrder)
        buttonBack = findViewById(R.id.buttonBack)

        shopName = intent.getStringExtra("shopName") ?: "Unknown"

        buttonBack.setOnClickListener {
            finish()
        }

        displayCartItems()

        buttonOrder.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                Toast.makeText(this, "장바구니가 비어있습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cartItems = CartManager.getCartItems()
            val totalPrice = CartManager.getTotalPrice()
            val currentLang = LocaleHelper.getSavedLanguage(this)

            // 외국어 메뉴 요약 생성
            val menuSummary = cartItems.joinToString(", ") {
                val translated = it.translatedName[currentLang] ?: it.name
                "$translated x${it.quantity}"
            }

            sendOrderToFirebase(shopName, totalPrice, menuSummary)
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d원", price)
    }

    private fun displayCartItems() {
        layoutCartItems.removeAllViews()

        val cartItems = CartManager.getCartItems()
        val currentLang = LocaleHelper.getSavedLanguage(this)

        if (cartItems.isEmpty()) {
            textEmptyCart.visibility = View.VISIBLE
            buttonOrder.visibility = View.GONE
            textTotalPrice.text = ""
            return
        }

        textEmptyCart.visibility = View.GONE
        buttonOrder.visibility = View.VISIBLE

        for (item in cartItems) {
            val itemView = layoutInflater.inflate(R.layout.cart_item, layoutCartItems, false)
            val textItemName = itemView.findViewById<TextView>(R.id.textItemName)
            val textItemPrice = itemView.findViewById<TextView>(R.id.textItemPrice)
            val textQuantity = itemView.findViewById<TextView>(R.id.textQuantity)
            val buttonIncrease = itemView.findViewById<Button>(R.id.buttonIncrease)
            val buttonDecrease = itemView.findViewById<Button>(R.id.buttonDecrease)
            val buttonRemove = itemView.findViewById<Button>(R.id.buttonRemove)

            textItemName.text = item.translatedName[currentLang] ?: item.name
            textQuantity.text = item.quantity.toString()
            textItemPrice.text = formatPrice(item.price * item.quantity)

            buttonIncrease.setOnClickListener {
                item.quantity++
                displayCartItems()
            }

            buttonDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    displayCartItems()
                } else {
                    Toast.makeText(this, "최소 수량은 1개입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            buttonRemove.setOnClickListener {
                CartManager.removeItem(item)
                displayCartItems()
            }

            layoutCartItems.addView(itemView)
        }

        textTotalPrice.text = "총합: ${formatPrice(CartManager.getTotalPrice())}"
    }

    private fun sendOrderToFirebase(shopName: String, totalPrice: Int, menuSummary: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        val cartItems = CartManager.getCartItems()
        val currentLang = LocaleHelper.getSavedLanguage(this)

        // 메뉴 정보와 수량을 배열로 전달
        val translatedNames = cartItems.map { it.translatedName }
        val quantities = cartItems.map { it.quantity }

        val shopRef = db.collection("orders").document(shopName)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(shopRef)
            val currentOrderCount = snapshot.getLong("orderCount") ?: 0
            val newOrderNumber = currentOrderCount + 1  // 주문번호 계산

            val orderData = hashMapOf(
                "userId" to userId,
                "shopName" to shopName,
                "orderNumber" to newOrderNumber,
                "items" to cartItems.map {
                    mapOf(
                        "name" to it.name, // 포스기용 한국어
                        "translatedName" to it.translatedName,
                        "price" to it.price,
                        "quantity" to it.quantity
                    )
                },
                "totalPrice" to totalPrice,
                "timestamp" to Timestamp.now()
            )

            transaction.set(
                shopRef.collection("list").document("주문$newOrderNumber"),
                orderData
            )
            transaction.update(shopRef, "orderCount", newOrderNumber)

            // 트랜잭션 완료 후 Intent 전달
            val intent = Intent(this@CartActivity, OrderCompleteActivity::class.java).apply {
                putExtra("shopName", shopName)
                putExtra("totalPrice", totalPrice)
                putExtra("menuSummary", menuSummary)
                putExtra("orderNumber", "주문번호: $newOrderNumber")  // 주문번호 추가
                putExtra("translatedNames", ArrayList(translatedNames))  // 번역된 이름 리스트 전달
                putExtra("quantities", ArrayList(quantities))  // 수량 리스트 전달
            }

            CartManager.clear()  // 트랜잭션 완료 후 장바구니 비우기
            startActivity(intent)
            finish()

        }.addOnFailureListener { e ->
            Toast.makeText(this, "주문 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            buttonOrder.isEnabled = true
        }
    }
}
