package com.example.chatter

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.util.logging.Logger

class Preferences(val context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    private val levelHashMap = HashMap<String, Int>()
    private val levelNames = arrayListOf<String>("Pawn", "Knight", "Bishop", "Rook")
    private val pointsRemaining = arrayListOf<Long>(2000, 3000, 4000, 5000)

    private val quotesArray = arrayListOf<String>(
        "",
        "Hi, I'm Bear Bot. I'm here to help because you really suck at Spanish",
        "Who ever thought talking to a bear was a good idea?",
        "You think you're amazing. Think again",
        "Unfortunately, they don't pay me enough to be positive",
        "Woah you're amazing. One new word a week. Simply spectacular!",
        "Sometimes I'm encouraging. Not today!",
        "I haven't had honey in so long. Can you get me some?"
    )

    private var quoteIndex = 0

    private var storiesHashmap = HashMap<String, ArrayList<String>>()

    fun setUpPreferences() {
        setUpStoriesHashmap()
    }

    private fun setUpStoriesHashmap() {
        storiesHashmap.put(
            "Hiker Harry",
            arrayListOf(
                "You will be talking to Harry. Harry loves to hike",
                "Be careful however. Harry may seem friendly but he's also quite the troublemaker."
            )
        )
    }

    fun getBotStories(botTitle: String): ArrayList<String> {
        Log.d("BotStories", botTitle)
        return storiesHashmap.get(botTitle) ?: arrayListOf("I have no idea about ".plus(botTitle))
    }

    private fun getQuotesList(): ArrayList<String> {
        return quotesArray
    }

    private fun incrementQuoteIndex() {
        quoteIndex = (quoteIndex + 1) % quotesArray.size
    }

    fun getCurrentQuote(): String {
        val result = quotesArray[quoteIndex]
        incrementQuoteIndex()
        return result
    }

    fun storeTranslations(message: String, translationEn: String) {
        sharedPreferences.edit().putString(message, "EN - Setup the google translate api from translations").apply()
        sharedPreferences.edit().putString("EN - Setup the google translate api from translations", message).apply()
    }

    fun getTranslation(message: String): String {
        return sharedPreferences.getString(message, "") ?: ""
    }

    fun getEnglishTranslation(message: String): String {
        return sharedPreferences.getString(TRANSLATIONS_ES_TO_EN.plus(message), "") ?: ""
    }

    fun getAudio(botMessage: String): String {
        return sharedPreferences.getString(AUDIOS.plus(botMessage), "") ?: ""
    }

    fun getLevelHashMap(): HashMap<String, Int> {
        var counter = 1
        for (item in levelNames) {
            levelHashMap.put(item, counter++)
        }
        return levelHashMap
    }

    fun getNextLevel(curLevel: String): String {
        val index = levelNames.indexOf(curLevel)
        if (index + 1 < levelNames.size) {
            return levelNames[index + 1]
        } else {
            return curLevel
        }
    }

    fun getCurrentLevelPoints(curLevel: String): Long {
        val index = levelNames.indexOf(curLevel)
        return pointsRemaining[index]
    }

    fun storeChatStatePath(userUid: String, botTitle: String, curPath: String) {
        var key = getCurrentPathKey(userUid, botTitle)
        sharedPreferences.edit().putString(key, curPath).apply()
    }

    fun storeChatMessages(
        userUid: String,
        botTitle: String,
        botMessages: ArrayList<String>,
        userMessages: ArrayList<String>
    ) {
        val key = getChatMessagesKey(userUid, botTitle)
        sharedPreferences.edit().putString(key, createMessagesString(botMessages, userMessages))
            .apply()
    }

    private fun createMessagesString(
        botMessages: ArrayList<String>,
        userMessages: ArrayList<String>
    ): String {
        Log.d("first array", "array1 " + botMessages)
        Log.d("second array", "array2 " + userMessages)
        var result = ""
        for (item in botMessages) {
            result = result.plus("$item").plus("*")
        }
        for (item in userMessages) {
            result = result.plus("$item ").plus("*")
        }
        Log.d("item", "item " + result)
        return result
    }

    fun getStoredPath(userUid: String, botTitle: String): String? {
        val key = getCurrentPathKey(userUid, botTitle)
        return sharedPreferences.getString(key, "")
    }

    fun getStoredMessages(
        userUid: String,
        botTitle: String
    ): Pair<ArrayList<String>, ArrayList<String>> {
        val key = getChatMessagesKey(userUid, botTitle)
        var result: String = ""
        if (sharedPreferences.contains(key)) {
            result = sharedPreferences.getString(key, "") ?: ""
        }
        return createMessagesArrayFromSerializedString(result)
    }

    private fun createMessagesArrayFromSerializedString(result: String): Pair<ArrayList<String>, ArrayList<String>> {
        val messageList = result.split("*")
        var botMessages = arrayListOf<String>()
        var userMessages = arrayListOf<String>()
        var botsTurn = true
        for (item in messageList) {
            if (botsTurn) {
                botMessages.add(item)
            } else {
                userMessages.add(item)
            }
            botsTurn = !botsTurn
        }
        return Pair(botMessages, userMessages)
    }

    private fun getCurrentPathKey(userUid: String, botTitle: String): String {
        return "$BOT_STATES $userUid $botTitle $CURRENT_PATH"
    }

    private fun getChatMessagesKey(userUid: String, botTitle: String): String {
        return "$BOT_STATES $userUid $botTitle $MESSAGES"
    }

    fun storeUserPoints(points: Long) {
        sharedPreferences.edit().putLong(USER_POINTS_REMAINING, points).apply()
    }

    fun getUserPoints(): Long {
        return sharedPreferences.getLong(USER_POINTS_REMAINING, 0L)
    }

    fun storeUserLevel(level: String) {
        sharedPreferences.edit().putString(USER_LEVEL, level).apply()
    }

    fun getUserLevel(): String {
        return sharedPreferences.getString(USER_LEVEL, "Novice") ?: "N/A"
    }

    companion object {
        const val SHARED_PREFERENCES_KEY = "Shared Preferences"
        const val USER_POINTS_REMAINING = "Points Remaining"
        const val USER_LEVEL = "User Level"
        const val BOT_STATES = "Bot States"
        const val MESSAGES = "Messages"
        const val CURRENT_PATH = "CurrentPath"
        const val TRANSLATIONS_EN_TO_ES = "TranslationsEnToEs/"
        const val TRANSLATIONS_ES_TO_EN = "TranslationsEsToEn"
        const val AUDIOS = "Audios/"
    }
}