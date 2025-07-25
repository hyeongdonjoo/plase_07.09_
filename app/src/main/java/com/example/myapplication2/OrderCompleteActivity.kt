package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class OrderCompleteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 액션바 숨기기
        supportActionBar?.hide()
        setContentView(R.layout.activity_order_complete)

        // 인텐트에서 데이터 받기
        val shopName = intent.getStringExtra("shopName") ?: "알 수 없는 가게"
        val totalPrice = intent.getIntExtra("totalPrice", 0)
        val orderNumber = intent.getStringExtra("orderNumber") ?: "주문번호 없음"

        // 번역된 메뉴명 리스트와 수량 리스트
        val translatedNames =
            intent.getSerializableExtra("translatedNames") as? ArrayList<HashMap<String, String>> // 안전하게 캐스팅
        val quantities = intent.getIntegerArrayListExtra("quantities") ?: arrayListOf()

        // 현재 언어 설정
        val lang = LocaleHelper.getSavedLanguage(this)

        // UI에 텍스트 반영
        val shopNameTextView = findViewById<TextView>(R.id.textShopNameComplete)
        val orderNumberTextView = findViewById<TextView>(R.id.textOrderNumber)
        val totalPriceTextView = findViewById<TextView>(R.id.textViewTotalPrice)
        val menuSummaryTextView = findViewById<TextView>(R.id.textViewMenuList)

        // 주문 정보 UI에 반영
        shopNameTextView.text = "$shopName 주문이 완료되었습니다!"
        orderNumberTextView.text = "주문번호: $orderNumber" // 주문번호 추가
        totalPriceTextView.text = "총액: ${String.format("%,d원", totalPrice)}"

        // 메뉴 요약 표시
        val summaryLines = mutableListOf<String>()
        if (translatedNames != null && quantities.isNotEmpty()) {
            for (i in translatedNames.indices) {
                val nameMap = translatedNames[i]
                val quantity = quantities.getOrNull(i) ?: 0 // 안전하게 값 가져오기
                val displayName = nameMap[lang] ?: nameMap["ko"] ?: "알 수 없음"
                summaryLines.add("$displayName x $quantity")
            }
        } else {
            summaryLines.add("메뉴 정보 없음")
        }

        menuSummaryTextView.text = summaryLines.joinToString("\n")

        // 홈으로 이동 버튼
        findViewById<Button>(R.id.buttonGoHome).setOnClickListener {
            val intent = Intent(this, ShopListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // 주문내역 보기 버튼
        findViewById<Button>(R.id.buttonMyOrders).setOnClickListener {
            startActivity(Intent(this, MyOrdersActivity::class.java))
        }
    }
}
