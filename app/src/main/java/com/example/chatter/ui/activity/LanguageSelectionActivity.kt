package com.example.chatter.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.adapters.LanguageAdapter
import com.example.chatter.extra.Preferences
import com.example.chatter.R
import com.example.chatter.interfaces.LanguageSelectedInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class LanguageSelectionActivity : BaseSelectionActivity(),
    LanguageSelectedInterface {
    private var nations = arrayListOf<String>(
        "Spanish",
        "French",
        "German",
        "Arabic",
        "Mandarin",
        "Hebrew",
        "Dutch",
        "Hindi",
        "Russian"
    )
    private var flagImages =
        arrayListOf<Int>(
            R.drawable.spanishflag,
            R.drawable.frenchflag,
            R.drawable.germanflag,
            R.drawable.spanishflag,
            R.drawable.spanishflag,
            R.drawable.frenchflag,
            R.drawable.germanflag,
            R.drawable.spanishflag,
            R.drawable.spanishflag
        )

    var selectedLang: String? = null

    private val languageMap = HashMap<String, String>()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        preferences = Preferences(this)
        sortLanguagesAlphabetically()
        setUpTopBar()
        setUpBottomNavBar()
        setUpLanguageMap()
        setUpDropdownRecycler()
        setUpScrollListener()
        setUpArrowClicks()
    }

    private fun setUpLanguageMap() {
        languageMap.put("English", "en")
        languageMap.put("Spanish", "es")
        languageMap.put("Russian", "ru")
        languageMap.put("French", "fr")
        languageMap.put("Hindi", "hi")
        languageMap.put("German", "de")
        languageMap.put("Arabic", "ar")
        languageMap.put("Mandarin", "zh")
        languageMap.put("Hebrew", "he")
        languageMap.put("Dutch", "nl")
    }

    private fun setUpBottomNavBar() {
        button_back.setOnClickListener {
            this.finish()
        }
        button_next.setOnClickListener {
            if (selectedLang != null && languageMap.containsKey(selectedLang as String)) {
                if (auth.currentUser != null) {
                    auth.currentUser?.uid?.let {
                        val uid = it
                        database.child("Users/${uid}").child("nativeLanguage")
                            .setValue(languageMap[selectedLang as String]).addOnSuccessListener {
                                Toast.makeText(this, "Selection Saved", Toast.LENGTH_LONG)
                                val intent = Intent(this, DashboardActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }.addOnFailureListener {
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                                    .show()
                            }
                    }
                } else {
                    languageMap[selectedLang as String]?.let {
                        preferences.storeNativeLanguageSelection(it)
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("GUEST_MODE", true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } ?: Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun setUpTopBar() {
        setUpTopAndBottomBars()
    }

    private fun setUpTopAndBottomBars() {
        if (intent.extras?.containsKey("ChangingDefaultLanguage") == true) {
            top_bar_title.text = "Language"
            top_bar_mic.visibility = View.GONE
            home.visibility = View.GONE
            back.visibility = View.VISIBLE
            top_bar_save_button.visibility = View.VISIBLE
            language_selection_bottom_bar.visibility = View.GONE
            back.setOnClickListener {
                finish()
            }
            top_bar_save_button.setOnClickListener {
                setUpSaveButton()
            }
        } else {
            top_bar_title.text = "Language"
            top_bar_mic.visibility = View.GONE
        }
    }

    private fun setUpSaveButton() {
        if (auth.currentUser != null) {
            //User signed in
            auth.currentUser?.let {
                val uid = it.uid
                if (selectedLang != null && languageMap.containsKey(selectedLang as String)) {
                    languageMap[selectedLang as String]?.let {
                        database.child("Users/${uid}").child("nativeLanguage").setValue(it)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Changes Saved", Toast.LENGTH_LONG).show()
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                                    .show()
                            }
                    }
                }
            }
        } else {
            //Guest Mode
            if (selectedLang != null && languageMap.containsKey(selectedLang as String)) {
                languageMap[selectedLang as String]?.let {
                    preferences.storeNativeLanguageSelection(it)
                    Toast.makeText(this, "Changes Saved", Toast.LENGTH_LONG).show()
                    finish()
                }
            } else if (selectedLang == null) {
                Toast.makeText(this, "Select a language", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun setUpDropdownRecycler() {
        language_recycler.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = LanguageAdapter(
                this@LanguageSelectionActivity,
                nations,
                flagImages,
                this@LanguageSelectionActivity
            )
        }
    }

    override fun setUpScrollListener() {
        val layoutManager = language_recycler.layoutManager as LinearLayoutManager
        language_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                if (firstVisibleItem == 0) {
                    language_selection_up_arrow.visibility = View.INVISIBLE
                } else {
                    language_selection_up_arrow.visibility = View.VISIBLE
                }
                if (lastVisibleItem == totalItemCount - 1) {
                    language_selection_down_arrow.visibility = View.INVISIBLE
                } else {
                    language_selection_down_arrow.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun setUpArrowClicks() {
        val layoutManager = language_recycler.layoutManager as LinearLayoutManager
        language_selection_up_arrow.setOnClickListener {
            val firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
            val targetPos = if (firstVisibleItem - 3 >= 0) firstVisibleItem - 3 else 0
            language_recycler.smoothScrollToPosition(targetPos)
        }
        language_selection_down_arrow.setOnClickListener {
            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            val targetPos =
                if (lastVisibleItem + 3 < totalItemCount) lastVisibleItem + 3 else totalItemCount - 1
            language_recycler.smoothScrollToPosition(targetPos)
        }
    }

    override fun onLanguageSelected(language: String) {
        selectedLang = language
    }

    private fun sortLanguagesAlphabetically() {
        for (x in 0..nations.size - 1) {
            for (y in (x + 1)..nations.size - 1) {
                if (nations[x].compareTo(nations[y]) > 0) {
                    val tempLang = nations[x]
                    nations[x] = nations[y]
                    nations[y] = tempLang

                    val tempFlag = flagImages[x]
                    flagImages[x] = flagImages[y]
                    flagImages[y] = tempFlag
                }
            }
        }
    }
}