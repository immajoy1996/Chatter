package com.example.chatter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.vocab_item_view.view.*

class VocabAdapter(
    val context: Context,
    var expressions: ArrayList<String>,
    var definitions: ArrayList<String>,
    var expressionClickInterface: ExpressionClickInterface
) :
    RecyclerView.Adapter<VocabAdapter.VocabViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabViewHolder {
        return VocabViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.vocab_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VocabViewHolder, position: Int) {
        val expression = expressions[position]
        val definition = definitions[position]
        holder.bind(expression, definition)
    }

    override fun getItemCount(): Int = expressions.size

    inner class VocabViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(expression: String, definition: String) {
            itemView.spanish_word.text = expression
            itemView.translation.text = definition
            itemView.setOnClickListener {
                expressionClickInterface.onExpressionClicked(expression)
            }
        }
    }
}