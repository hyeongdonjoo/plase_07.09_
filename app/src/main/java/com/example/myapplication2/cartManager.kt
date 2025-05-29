package com.example.myapplication2

object CartManager {
    // 가게별 장바구니 (가게 이름 -> 장바구니 리스트)
    private val carts = mutableMapOf<String, MutableList<CartItem>>()

    // 현재 선택된 가게명
    var currentShopName: String = ""

    fun addItem(item: CartItem) {
        if (currentShopName.isBlank()) return

        val cartItems = carts.getOrPut(currentShopName) { mutableListOf() }

        val existing = cartItems.find { it.name == item.name }
        if (existing != null) {
            existing.quantity += item.quantity
        } else {
            cartItems.add(item)
        }
    }

    fun removeOneItem(itemName: String) {
        if (currentShopName.isBlank()) return

        val cartItems = carts[currentShopName] ?: return

        val existing = cartItems.find { it.name == itemName }
        if (existing != null) {
            existing.quantity -= 1
            if (existing.quantity <= 0) {
                cartItems.remove(existing)
            }
        }
    }

    fun removeItem(item: CartItem) {
        if (currentShopName.isBlank()) return

        carts[currentShopName]?.removeIf { it.name == item.name }
    }

    fun clear() {
        if (currentShopName.isBlank()) return

        carts[currentShopName]?.clear()
    }

    fun getTotalPrice(): Int {
        return carts[currentShopName]?.sumOf { it.price * it.quantity } ?: 0
    }

    fun getItemCount(): Int {
        return carts[currentShopName]?.sumOf { it.quantity } ?: 0
    }

    fun getCartItems(): List<CartItem> {
        return carts[currentShopName]?.toList() ?: emptyList()
    }
}
