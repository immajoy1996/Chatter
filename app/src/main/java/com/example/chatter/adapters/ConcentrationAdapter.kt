package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.interfaces.RevealItemInterface
import com.example.chatter.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.concentration_item_layout.view.*

class ConcentrationAdapter(
    val context: Context,
    var imageList: ArrayList<String>,
    var revealItemInterface: RevealItemInterface
) :
    RecyclerView.Adapter<ConcentrationAdapter.ConcentrationViewHolder>() {
    private var selectedPosOne = -1
    private var selectedPosTwo = -1
    private var imagesLeft = 25

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcentrationViewHolder {
        return ConcentrationViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.concentration_item_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConcentrationViewHolder, position: Int) {
        val image = imageList[position]
        holder.bind(image, position)
        holder.itemView.setOnClickListener {
            if (imagesLeft == 1) {
                //game end
                revealItemInterface.endGame()
            }
            if (selectedPosOne != -1 && selectedPosTwo != -1) {
                //do nothing
            } else if ((selectedPosOne == -1 || selectedPosTwo == -1) && selectedPosOne != position && selectedPosTwo != position) {
                if (holder.itemView.concentration_item_enabled_image.visibility == View.GONE) {
                    holder.itemView.concentration_item_enabled_image.visibility = View.VISIBLE
                    holder.itemView.concentration_item_disabled_image.visibility = View.GONE
                }
                if (selectedPosOne == -1) {
                    selectedPosOne = position
                } else if (selectedPosTwo == -1) {
                    selectedPosTwo = position
                    if (imageList[selectedPosOne] == imageList[selectedPosTwo]) {
                        imageList[selectedPosOne] = "-1"
                        imageList[selectedPosTwo] = "-1"
                        imagesLeft -= 2
                    }
                    revealItemInterface.revealItem()
                }
            }
        }
    }

    fun resetSelectedPositions() {
        selectedPosOne = -1
        selectedPosTwo = -1
    }

    override fun getItemCount(): Int = imageList.size

    inner class ConcentrationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var botImage: ImageView? = null

        init {
            botImage = view.concentration_item_enabled_image
        }

        fun bind(image: String, position: Int) {
            if (image == "-1") {
                itemView.concentration_item_inner_layout.visibility = View.GONE
            } else {
                itemView.concentration_item_inner_layout.visibility = View.VISIBLE
                itemView.concentration_item_enabled_image.visibility = View.GONE
                itemView.concentration_item_disabled_image.visibility = View.VISIBLE
                botImage?.let {
                    Glide.with(context)
                        .load(image)
                        .into(it)
                }
            }
        }
    }
}