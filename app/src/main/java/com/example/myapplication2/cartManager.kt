package com.example.myapplication2

object CartManager {
    val cartItems = mutableListOf<CartItem>()  // ✅ CartItem으로 바꿔야 함

    fun addItem(item: CartItem) {
        val existing = cartItems.find { it.name == item.name }
        if (existing != null) {
            existing.quantity += item.quantity
        } else {
            cartItems.add(item)
        }
    }

    fun removeItem(item: CartItem) {
        cartItems.removeIf { it.name == item.name }
    }

    fun clear() {
        cartItems.clear()
    }

    fun getTotalPrice(): Int {
        return cartItems.sumOf { it.price * it.quantity }
    }

    fun getItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }
}