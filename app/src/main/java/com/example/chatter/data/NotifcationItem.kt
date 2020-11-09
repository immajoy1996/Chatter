package com.example.chatter.data

import com.example.chatter.extra.NOTIFICATION_TYPE

data class NotificationItem(
    var notificationType: NOTIFICATION_TYPE,
    var image: Int? = null,
    var title: String? = null,
    var subtitle: String? = null,
    var locationName: String? = null,
    var quizType: String? = null
)
