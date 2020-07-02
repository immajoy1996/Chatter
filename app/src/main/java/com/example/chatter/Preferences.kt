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
        "Banging your head against the wall for one hour burns 150 calories",
        "In Switzerland it is illegal to own just one guinea pig",
        "Pteronophobia is the fear of being tickled by feathers.",
        "Snakes can sense a coming earthquake from 121 kilometers away, up to five days before it happens.",
        "A flock of crows is known as a murder.",
        "Cherophobia is an irrational fear of fun or happiness.",
        "Seven percent of American adults believe that chocolate milk comes from brown cows."
    )

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

    private fun incrementQuoteIndex() {
        var quoteIndex = getQuoteIndex()
        quoteIndex = (quoteIndex + 1) % quotesArray.size
        sharedPreferences.edit().putInt(QUOTE_INDEX, quoteIndex).apply()
    }

    fun getCurrentQuote(): String {
        val quoteIndex = getQuoteIndex()
        val result = quotesArray[quoteIndex]
        incrementQuoteIndex()
        return result
    }

    private fun getQuoteIndex(): Int {
        return sharedPreferences.getInt(QUOTE_INDEX, 0)
    }

    fun storeTranslations(message: String, translationEn: String) {
        sharedPreferences.edit()
            .putString(message, "EN - Setup the google translate api from translations").apply()
        sharedPreferences.edit()
            .putString("EN - Setup the google translate api from translations", message).apply()
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
        const val QUOTE_INDEX = "QuoteIndex"
    }
}