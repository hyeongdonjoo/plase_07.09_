package com.example.myapplication2

data class MenuItem(
    val name: String = "",
    val desc: String = "",
    val price: Int = 0,
    val image: String = "",   // 이미지 ID 또는 URL
    val category: String = "", // 카테고리 필드 추가
    var quantity: Int = 1
)
