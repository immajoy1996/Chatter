package com.example.chatter

import android.os.Bundle

abstract class BaseSelectionActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_selection)
    }

    override fun setUpTopBar() {
        //Not implemented
    }

    abstract fun setUpDropdownRecycler()
    abstract fun setUpScrollListener()
    abstract fun setUpArrowClicks()
}