package com.example.chatter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.gem_grid_item_view.view.*

class GemsGridAdapter(
    val context: Context,
    var gemImages: ArrayList<Int>,
    var haveGems: ArrayList<Boolean>,
    var gemNames: ArrayList<String>,
    var gemPrices: ArrayList<Int>,
    var gemClickInterface: GemClickInterface
) :
    RecyclerView.Adapter<GemsGridAdapter.GemsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GemsViewHolder {
        return GemsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.gem_grid_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GemsViewHolder, position: Int) {
        val gemImage = gemImages[position]
        val doIhaveIt = haveGems[position]
        holder.bind(gemImage, doIhaveIt)
        holder.itemView.setOnClickListener {
            gemClickInterface.onGemClicked(gemNames[position],gemPrices[position])
        }
    }

    override fun getItemCount(): Int = gemImages.size

    inner class GemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(gemImage: Int, doIHaveIt: Boolean) {
            itemView.gemstone.setImageResource(gemImage)
            if (doIHaveIt) {
                itemView.gemstone_layout.setBackgroundResource(R.drawable.have_gem_background)
            } else {
                itemView.gemstone_layout.setBackgroundResource(R.drawable.dont_have_gem_background)
            }
        }
    }
}