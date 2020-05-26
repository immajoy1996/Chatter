package com.example.chatter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.levels_item_view.view.*

class LevelsAdapter(
    val context: Context,
    var gemNames: ArrayList<String>,
    var gemImages: ArrayList<Int>,
    var prices: ArrayList<Int>
) :
    RecyclerView.Adapter<LevelsAdapter.LevelsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelsViewHolder {
        return LevelsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.levels_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LevelsViewHolder, position: Int) {
        val gemName = gemNames[position]
        val gemImage = gemImages[position]
        val gemPrice = prices[position]
        holder.bind(gemName, gemImage, gemPrice)
    }

    override fun getItemCount(): Int = gemNames.size

    inner class LevelsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(gemName: String, gemImage: Int, gemPrice: Int) {
            itemView.gem.setImageResource(gemImage)
            itemView.gem_name.text = gemName
            itemView.gem_price.text = gemPrice.toString()
        }
    }
}