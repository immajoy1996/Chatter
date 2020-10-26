package com.example.chatter.data

import com.example.chatter.ui.BotStoryFragmentUsed
import com.example.chatter.ui.fragment.BotStoryIndividualFragment

data class BotStoryModel(
    var order: Int,
    var fragment: BotStoryIndividualFragment
)