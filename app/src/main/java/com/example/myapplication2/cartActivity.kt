package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
            sendOrderToFirebase()
        }
    }

    private fun displayCartItems() {
        layoutCartItems.removeAllViews()

        val cartItems = CartManager.cartItems
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
            itemView.findViewById<TextView>(R.id.textItemName).text = "${item.name} x ${item.quantity}"
            itemView.findViewById<TextView>(R.id.textItemPrice).text = "${item.price * item.quantity}원"
            layoutCartItems.addView(itemView)
        }

        textTotalPrice.text = "총합: ${CartManager.getTotalPrice()}원"
    }

    private fun sendOrderToFirebase() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val orderData = hashMapOf(
            "shopName" to shopName,
            "items" to CartManager.cartItems.map {
                mapOf(
                    "name" to it.name,
                    "price" to it.price,
                    "quantity" to it.quantity
                )
            },
            "totalPrice" to CartManager.getTotalPrice(),
            "timestamp" to Timestamp.now()
        )

        db.collection("users")
            .document(userId)
            .collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                CartManager.clear()
                val intent = Intent(this@CartActivity, OrderCompleteActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@CartActivity, "주문 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}