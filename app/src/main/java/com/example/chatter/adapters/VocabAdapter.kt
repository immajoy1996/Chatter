package com.example.chatter.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import com.example.chatter.interfaces.ExpressionClickInterface
import com.example.chatter.interfaces.SubmitExpressionInterface
import kotlinx.android.synthetic.main.vocab_item_view.view.*

class VocabAdapter(
    val context: Context,
    var expressions: ArrayList<String>,
    var definitions: ArrayList<String>,
    var expressionClickInterface: ExpressionClickInterface,
    var submitExpressionInterface: SubmitExpressionInterface
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
            if (expression.isEmpty() && definition.isEmpty()) {
                itemView.vocab_real_layout.visibility = View.GONE
                itemView.vocab_editable_layout.visibility = View.VISIBLE
                itemView.editable_close_button.setOnClickListener {
                    closeEditableSection()
                }
                itemView.editable_submit.setOnClickListener {
                    val newExpression = itemView.editable_spanish_word.text.toString()
                    val newDefinition = itemView.editable_translation.text.toString()
                    if (newExpression.isNotEmpty() && newDefinition.isNotEmpty()) {
                        submitExpressionInterface.onSubmitExpressionClicked(
                            newExpression,
                            newDefinition
                        )
                        closeEditableSection()

                    } else {
                        Toast.makeText(context, "Enter an expression", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                itemView.vocab_real_layout.visibility = View.VISIBLE
                itemView.vocab_editable_layout.visibility = View.GONE
                itemView.spanish_word.text = expression
                itemView.translation.text = definition
                itemView.setOnClickListener {
                    expressionClickInterface.onExpressionClicked(expression)
                }
            }
        }

        private fun closeEditableSection() {
            expressions.removeAt(0)
            definitions.removeAt(0)
            notifyDataSetChanged()
        }
    }
}