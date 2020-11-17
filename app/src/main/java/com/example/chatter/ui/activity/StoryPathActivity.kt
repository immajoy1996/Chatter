package com.example.chatter.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.chatter.R
import com.example.chatter.ui.fragment.BaseFragment
import com.example.chatter.ui.fragment.StoryPathFragment
import kotlinx.android.synthetic.main.activity_bot_story_latest.*
import kotlinx.android.synthetic.main.activity_story_path.*
import kotlinx.android.synthetic.main.top_bar.*

class StoryPathActivity : BaseActivity() {

    val fragmentArray = arrayListOf<StoryPathFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_path)
        setUpTopBar()
        setUpViewPager()
    }

    override fun setUpTopBar() {
        home.visibility = View.GONE
        back.visibility = View.VISIBLE
        top_bar_title.text = "Story Mode"
    }

    private fun setUpViewPager() {
        val viewPager = createViewPager()
        viewPager?.let {
            setViewPager(it)
        }
    }

    private fun setViewPager(pagerAdapter: FragmentStatePagerAdapter) {
        story_path_view_pager.adapter = pagerAdapter
    }

    private fun createViewPager(): FragmentStatePagerAdapter? {
        class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

            override fun getCount(): Int {
                return fragmentArray.size
            }

            override fun getItem(position: Int): BaseFragment {
                return fragmentArray[position]
            }
        }
        if (fragmentArray.isNotEmpty()) {
            return PagerAdapter(supportFragmentManager)
        } else {
            return null
        }
    }

}