package com.example.chatter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chatter.R
import com.example.chatter.ui.activity.BotStoryActivityLatest
import com.example.chatter.ui.activity.HomeNavActivityLatest
import kotlinx.android.synthetic.main.fragment_bot_story_individual.*

class BotStoryIndividualFragment : BaseFragment() {
    private var title: String = ""
    private var subtitle: String = ""
    private var image = ""
    private var order = 0
    private var isEnd = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_story_individual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTitle()
        setUpSubtitle()
        setUpImage()
    }

    override fun onResume() {
        super.onResume()
        //setUpPins()
        //setUpStartChattingButton()
        //Toast.makeText(context, "" + order + " " + isEnd,Toast.LENGTH_SHORT).show()
    }

    private fun setUpImagePath(){
        if(isEnd){
            (activity as? BotStoryActivityLatest)?.setUpImagePath(image)
        }
    }

    private fun setUpStartChattingButton() {
        if (isEnd) {
            (activity as? BotStoryActivityLatest)?.enableStartChattingButton()
        } else {
            (activity as? BotStoryActivityLatest)?.disableStartChattingButton()
        }
    }

    private fun setUpPins() {
        (activity as? BotStoryActivityLatest)?.setUpPinColors(order)
    }

    private fun setUpTitle() {
        bot_story_individual_title.text = title
    }

    private fun setUpSubtitle() {
        bot_story_individual_subtitle.text = subtitle
    }

    private fun setUpImage() {
        val mContext = context
        if (mContext != null) {
            bot_story_image.let {
                Glide.with(mContext)
                    .load(image)
                    .into(it)
            }
        }
        setUpImagePath()
    }

    companion object {
        fun newInstance(
            title: String,
            subtitle: String,
            image: String,
            order: Int,
            isEnd: Boolean
        ): BotStoryIndividualFragment {
            val fragment = BotStoryIndividualFragment()
            fragment.title = title
            fragment.subtitle = subtitle
            fragment.image = image
            fragment.order = order
            fragment.isEnd = isEnd
            return fragment
        }
    }

}