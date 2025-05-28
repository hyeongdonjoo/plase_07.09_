package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
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

        // 가게 이름 받기 (null, 빈 문자열 체크)
        shopName = intent.getStringExtra("shopName") ?: ""
        if (shopName.isBlank()) {
            showToast("가게 이름이 올바르지 않습니다.")
            finish()
            return
        }

        findViewById<TextView>(R.id.textViewMenuTitle).text = "$shopName 의 메뉴입니다"

        // 장바구니 보기
        findViewById<Button>(R.id.buttonGoCart).setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("shopName", shopName)
            startActivity(intent)
        }

        menuContainer = findViewById(R.id.menuContainer)
        db = FirebaseFirestore.getInstance()

        // 메뉴 자동 로드
        checkShopExistsAndLoadMenus()
    }

    private fun checkShopExistsAndLoadMenus() {
        db.collection("shops").document(shopName).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    loadMenusFromFirestore()
                } else {
                    showToast("존재하지 않는 가게입니다.")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MenuActivity", "가게 조회 실패", e)
                showToast("가게 정보 로딩 실패")
                finish()
            }
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
                    menuContainer.removeAllViews()
                    return@addOnSuccessListener
                }

                menuContainer.removeAllViews()

                for (doc in documents) {
                    val menu = doc.toObject(MenuItem::class.java)
                    if (menu.name.isBlank()) {
                        Log.w("MenuActivity", "빈 이름 메뉴 스킵 문서ID: ${doc.id}")
                        continue
                    }
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

        if (menu.image.isNotBlank()) {
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
        } else {
            imageView.setImageResource(R.drawable.default_image) // 기본 이미지가 있다면 설정
        }

        // 2번: 눌림(Press) 애니메이션 효과 추가
        val menuRoot = view.findViewById<View>(R.id.menuRoot)
        menuRoot.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false // 클릭 이벤트는 계속 전달
        }

        menuRoot.setOnClickListener {
            it.isEnabled = false
            CartManager.addItem(CartItem(menu.name, menu.price, 1))
            showToast("${menu.name} 담았습니다")
            it.postDelayed({ it.isEnabled = true }, 500)
        }

        menuContainer.addView(view)
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.view?.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        toast.show()
    }
}
