package com.example.chatter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.vocab_item_view.view.*


class VocabAdapter(
    val context: Context,
    var spanishWords: ArrayList<String>,
    var translations: ArrayList<String>,
    var transliterations: ArrayList<String>
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
        val spanishWord = spanishWords[position]
        val translation = translations[position]
        val transliteration = transliterations[position]
        holder.bind(spanishWord, translation, transliteration)
    }

    override fun getItemCount(): Int = spanishWords.size

    inner class VocabViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(spanishWord: String, translation: String, transliteration: String) {
            itemView.spanish_word.text = spanishWord
            itemView.translation.text = translation
            itemView.transliteration.text = transliteration
            //setOnClickListener()
        }

        fun setOnClickListener() {

        }

    }

    companion object {
    }
}