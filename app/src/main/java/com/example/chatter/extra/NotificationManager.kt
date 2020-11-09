package com.example.chatter.extra

import com.example.chatter.data.NotificationItem

class NotificationManager {
    class Constants {
        companion object {
            const val STORY = 1000
            const val QUIZ = 100
            const val LEVEL = 10
            const val PRACTICE = 1
        }
    }

    companion object {
        //currentId = 1000*botIndex+100*quizPassed+10*leveledUp
        fun getNotificationList(
            preferences: Preferences
        ): ArrayList<NotificationItem> {
            val currentId = preferences.getCurrentStateId()
            val storyModeMap = preferences.getStoryModeDataItems()
            val levelMap = preferences.getNewLevelsDataItems()
            val quizMap = preferences.getStoredQuizMap()

            val currentBot = preferences.getCurrentBotStory()
            val notifications = arrayListOf<NotificationItem>()
            val newId = currentId / Constants.QUIZ
            val showQuiz = newId % 10
            if (showQuiz == 1) {
                notifications.add(
                    NotificationItem(
                        NOTIFICATION_TYPE.QUIZ,
                        0,
                        currentBot,
                        "",
                        null,
                        quizMap[currentBot]?.gameType ?: ""

                    )
                )
            }
            val levelId = currentId / Constants.LEVEL
            val showLeveledUp = levelId % 10
            if (showLeveledUp == 1) {
                notifications.add(
                    NotificationItem(
                        NOTIFICATION_TYPE.LEVEL_REACHED,
                        0,
                        "New Language Level",
                        levelMap[currentBot]?.levelName ?: ""
                    )
                )
            }
            val showPracticeNotification = currentId / Constants.PRACTICE
            if (showPracticeNotification % 10 == 1) {
                if (Math.random() > .5) {
                    notifications.add(
                        NotificationItem(
                            NOTIFICATION_TYPE.PRACTICE_GAMES
                        )
                    )
                } else {
                    notifications.add(
                        NotificationItem(
                            NOTIFICATION_TYPE.PRACTICE_FLASHCARDS
                        )
                    )
                }
            }
            notifications.add(
                NotificationItem(
                    NOTIFICATION_TYPE.STUDYMODE,
                    storyModeMap[currentBot]?.image ?: 0,
                    "Story",
                    storyModeMap[currentBot]?.subtitle ?: "",
                    storyModeMap[currentBot]?.location
                )
            )
            //sort notifications array in a particular way?
            return notifications
        }
    }
}