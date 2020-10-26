package com.example.chatter.ui.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.chatter.R
import com.example.chatter.adapters.UserProfileDashboardAdapter
import com.example.chatter.extra.NOTIFICATION_TYPE
import com.example.chatter.ui.fragment.BaseFragment
import com.example.chatter.ui.fragment.StoryModeFragment
import com.example.chatter.ui.fragment.StudyModeFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_chatter.*
import kotlinx.android.synthetic.main.activity_home_nav_latest.*
import kotlinx.android.synthetic.main.fragment_user_profile.*

class HomeNavActivityLatest : BaseActivity() {

    var notificationType = arrayListOf(
        NOTIFICATION_TYPE.QUIZ,
        NOTIFICATION_TYPE.STUDYMODE,
        NOTIFICATION_TYPE.LEVEL_REACHED
    )
    var imageList = arrayListOf(R.drawable.quiz_icon, R.drawable.hotel, R.drawable.homeless)
    var subtitleList = arrayListOf("Doctor Hum-Vee", "Doctor Hum-Vee", "Language Level : Homeless")
    var locationNameList = arrayListOf("Test your understanding", "Edward's Hospital, NYC", "")
    private var studyModeFragment = StudyModeFragment()

    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_nav_latest)
        setUpViewPager()
        setUpSignOutButton()
    }

    private fun setUpViewPager() {
        viewPager = pager
        viewPager.adapter = PagerAdapter(supportFragmentManager)
    }

    private fun setUpSignOutButton() {
        signout_bar.setOnClickListener {
            finish()
        }
    }

    public fun setUpTabLayout() {
        tab_layout.setupWithViewPager(getViewPager())
    }

    private fun setUpBottomBar() {
        bottom_bar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_drawer_study_mode -> {
                    if (!studyModeFragment.isVisible) {
                        loadFragment(studyModeFragment)
                    }
                }
                R.id.bottom_drawer_story_mode -> {
                    if (studyModeFragment.isVisible) {
                        supportFragmentManager.popBackStack()
                    }
                }
            }
            true
        }
    }

    private fun loadFragment(fragment: BaseFragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(story_mode_fragment_container.id, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    override fun setUpTopBar() {
        //TODO("Not yet implemented")
    }

    public fun getViewPager(): ViewPager {
        return viewPager
    }

    class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        var studyModeFragment = StudyModeFragment()
        var storyModeFragment = StoryModeFragment()

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> {
                    return "Story Mode"
                }
                else -> {
                    return "Study Mode"
                }
            }
        }

        override fun getItem(position: Int): BaseFragment {
            when (position) {
                0 -> {
                    return storyModeFragment
                }
                else -> {
                    return studyModeFragment
                }
            }
        }
    }
}