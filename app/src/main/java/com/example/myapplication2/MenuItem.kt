package com.example.myapplication2

data class MenuItem(
    val name: String = "",
    val desc: String = "",
    val price: Int = 0,
    val image: String = "",   // 또는 imageUrl 또는 photoId → 구조에 따라
    var quantity: Int = 1
)