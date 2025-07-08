package com.example.myapplication2

data class CartItem(
    val name: String,  // 사용자에게 보여질 이름 (외국어)
    val price: Int,
    var quantity: Int,
    val translatedName: Map<String, String>  // 포스기에서 참조할 번역들
)

