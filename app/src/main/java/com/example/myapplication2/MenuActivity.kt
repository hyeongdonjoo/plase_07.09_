package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Color

class MenuActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var menuContainer: LinearLayout
    private lateinit var shopName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_menu)

        // 뒤로가기
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }

        // 가게 이름 받기
        shopName = intent.getStringExtra("shopName") ?: "가게 없음"
        findViewById<TextView>(R.id.textViewMenuTitle).text = "$shopName 의 메뉴입니다"

        // 장바구니 보기 버튼
        findViewById<Button>(R.id.buttonGoCart).setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("shopName", shopName)
            startActivity(intent)
        }

        menuContainer = findViewById(R.id.menuContainer)
        db = FirebaseFirestore.getInstance()

        loadMenusFromFirestore()
    }

    private fun loadMenusFromFirestore() {
        db.collection("shops")
            .document(shopName)
            .collection("menus")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("MenuActivity", "✅ 메뉴 ${documents.size()}개 불러옴")
                if (documents.isEmpty) {
                    showToast("❗ 메뉴가 없습니다")
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val menu = doc.toObject(MenuItem::class.java)
                    addMenuCard(menu)
                }
            }
            .addOnFailureListener { e ->
                showToast("❗ 메뉴 불러오기 실패")
                Log.e("MenuActivity", "🔥 메뉴 로딩 실패", e)
            }
    }

    private fun addMenuCard(menu: MenuItem) {
        val view = LayoutInflater.from(this).inflate(R.layout.menu_item, menuContainer, false)
        view.findViewById<TextView>(R.id.textMenuName).text = menu.name
        view.findViewById<TextView>(R.id.textMenuDesc).text = menu.desc
        view.findViewById<TextView>(R.id.textMenuPrice).text = "${menu.price}원"

        val imageView = view.findViewById<ImageView>(R.id.imageMenu)

        db.collection("photo")
            .document(menu.image)
            .get()
            .addOnSuccessListener { snapshot ->
                val url = snapshot.getString("imageUrl")
                if (!url.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(url)
                        .into(imageView)
                }
            }
            .addOnFailureListener {
                Log.e("MenuActivity", "🔥 이미지 로딩 실패", it)
            }

        view.setOnClickListener {
            CartManager.addItem(CartItem(menu.name, menu.price, 1))
            showToast("${menu.name} 담았습니다")
        }

        menuContainer.addView(view)
    }

    // ✅ 글자색 적용된 Toast 메서드
    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.view?.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        toast.show()
    }
}
