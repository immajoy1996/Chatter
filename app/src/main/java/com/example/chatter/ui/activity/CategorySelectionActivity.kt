package com.example.chatter.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatter.interfaces.CategorySelectionInterface
import com.example.chatter.ui.activity.DashboardActivity.Companion.CATEGORY_REQUEST_CODE
import com.example.chatter.adapters.LanguageAdapter
import com.example.chatter.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_category_selection.*
import kotlinx.android.synthetic.main.activity_language_selection.*
import kotlinx.android.synthetic.main.bottom_nav_bar.*
import kotlinx.android.synthetic.main.top_bar.*

class CategorySelectionActivity : BaseActivity(),
    CategorySelectionInterface {
    private var categories = arrayListOf<String>()
    private var categoryImages =
        arrayListOf<String>()

    private var selectedCategory: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setUpTopBar()
        setUpDropdownRecycler()
        fetchCategories()
    }

    override fun setUpTopBar() {
        setUpTopAndBottomBars()
    }

    private fun setUpTopAndBottomBars() {
        top_bar_title.text = "Categories"
        top_bar_mic.visibility = View.GONE
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        top_bar_save_button.visibility = View.VISIBLE
        top_bar_save_button.setOnClickListener {
            val intent = Intent()
            if (selectedCategory != null) {
                selectedCategory?.let {
                    intent.putExtra("SelectedCategory", it)
                    setResult(CATEGORY_REQUEST_CODE, intent)
                    Toast.makeText(this, "Selection saved", Toast.LENGTH_LONG).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Select a Category", Toast.LENGTH_LONG).show()
            }
        }
        back.setOnClickListener {
            finish()
        }
    }

    private fun setUpDropdownRecycler() {
        category_recycler.apply {
            layoutManager = GridLayoutManager(
                this@CategorySelectionActivity,
                2
            )
        }
    }

    private fun fetchCategories() {
        database.child("Categories").addChildEventListener(baseChildEventListener {
            val image = it.child("categoryImage").value.toString()
            val langName = it.child("categoryName").value.toString()
            placeCategoryAlphabetically(langName, image)
            category_recycler.adapter = LanguageAdapter(
                this,
                categories,
                categoryImages,
                null,
                this
            )
        })
    }

    private fun placeCategoryAlphabetically(category: String, categoryImg: String) {
        var added = false
        for (index in 0 until categories.size) {
            if (category.compareTo(categories[index]) < 0) {
                categories.add(index, category)
                categoryImages.add(index, categoryImg)
                added = true
                break
            }
        }
        if (!added) {
            categories.add(category)
            categoryImages.add(categoryImg)
        }
    }

    override fun onCategorySelected(category: String) {
        selectedCategory = category
    }
}
