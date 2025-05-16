package com.example.myapplication2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShopAdapter(
    private val shopList: List<Shop>,
    private val onItemClick: (Shop) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    inner class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopNameText: TextView = itemView.findViewById(R.id.shopName)
        val shopAddressText: TextView = itemView.findViewById(R.id.shopAddress)

        init {
            itemView.setOnClickListener {
                val shop = shopList[adapterPosition]
                onItemClick(shop)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shopList[position]
        holder.shopNameText.text = shop.name
        holder.shopAddressText.text = shop.address
    }

    override fun getItemCount(): Int = shopList.size
}