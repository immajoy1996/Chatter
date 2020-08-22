package com.example.chatter.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.R
import com.example.chatter.adapters.BotGridItemDecoration
import com.example.chatter.adapters.WordByWordAdapter
import com.example.chatter.interfaces.WordByWordInterface
import com.example.chatter.ui.activity.BaseChatActivity
import com.example.chatter.ui.activity.ChatterActivity
import com.example.chatter.ui.activity.DashboardActivity
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_word_by_word_translate.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WordByWordTranslateFragment : BaseFragment(), WordByWordInterface {
    private lateinit var firebaseStorage: FirebaseStorage
    private var messageText: String = ""
    private var executorService:ExecutorService?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_word_by_word_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        executorService=Executors.newFixedThreadPool(5)
        setUpWordByWordRecycler()
        setUpCloseButton()
    }

    private fun setUpCloseButton() {
        word_by_word_close_button.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as? ChatterActivity)?.showOptionsMenuWithoutLoading()
        }
    }

    private fun setUpWordByWordRecycler() {
        word_by_word_recycler.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        context?.let {

            val wordArray = arrayListOf<String>()
            val translateArray = arrayListOf<String>()
            val shouldShowTranslationArray = arrayListOf<Boolean>()
            val splitArray = messageText.split(" ", "?", ".", ",", ":")
            for (word in splitArray) {
                if (word.isNotEmpty()) {
                    wordArray.add(word.toLowerCase())
                    translateArray.add("")
                    shouldShowTranslationArray.add(false)
                }
            }
            word_by_word_recycler.adapter = WordByWordAdapter(
                it, wordArray, translateArray, shouldShowTranslationArray, this
            )
        }
        word_by_word_recycler.addItemDecoration(BotGridItemDecoration(20, 0))
    }

    private fun translateWordAndUpdateRecycler(text: String, recyclerPos: Int) {
        val targetLang = (activity as? ChatterActivity)?.targetLanguage
        val runnable=Runnable {
            var result: String? = null
            targetLang?.let {
                result = (activity as? ChatterActivity)?.translate(text, it)
            }
            targetLang?.let {
                result?.let {
                    (activity as? ChatterActivity)?.runOnUiThread {
                        (word_by_word_recycler.adapter as? WordByWordAdapter)?.apply {
                            changeTranslation(
                                recyclerPos,
                                it
                            )
                            showTranslation(recyclerPos)
                            refreshRecycler()
                        }
                    }
                }
            }
        }
        Thread(runnable).start()
    }

    override fun onItemTapped(word: String, recyclerPos: Int) {
        translateWordAndUpdateRecycler(word, recyclerPos)
    }

    companion object {
        fun newInstance(text: String): WordByWordTranslateFragment {
            val fragment = WordByWordTranslateFragment()
            fragment.messageText = text
            return fragment
        }
    }
}