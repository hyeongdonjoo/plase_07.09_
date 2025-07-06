package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderCompleteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 액션바 숨기기
        supportActionBar?.hide()

        setContentView(R.layout.activity_order_complete)

        // 인텐트에서 데이터 받기
        val shopName = intent.getStringExtra("shopName") ?: "알 수 없는 가게"
        val totalPrice = intent.getIntExtra("totalPrice", 0)
        val menuSummary = intent.getStringExtra("menuSummary") ?: "메뉴 정보 없음"
        val orderNumber = intent.getStringExtra("orderNumber") ?: "주문번호 없음"

        // 주문 완료 메시지
        findViewById<TextView>(R.id.textShopNameComplete).text = "$shopName 주문이 완료되었습니다!"

        // 주문번호 표시
        findViewById<TextView>(R.id.textOrderNumber).text = "주문번호: $orderNumber"

        // 총액 표시
        findViewById<TextView>(R.id.textViewTotalPrice).text = "총액: ${String.format("%,d원", totalPrice)}"

        // 메뉴 요약을 세로로 한 줄씩 출력
        val menuListString = menuSummary.split(",").joinToString(separator = "\n") { it.trim() }
        findViewById<TextView>(R.id.textViewMenuList).text = menuListString

        // 홈으로 가기 버튼
        findViewById<Button>(R.id.buttonGoHome).setOnClickListener {
            val intent = Intent(this, ShopListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // 주문 내역 보기 버튼
        findViewById<Button>(R.id.buttonMyOrders).setOnClickListener {
            val intent = Intent(this, MyOrdersActivity::class.java)
            startActivity(intent)
        }
    }
}
