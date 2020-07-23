package com.example.chatter.extra

import android.content.Context
import android.util.Log
import com.example.chatter.R

class Preferences(val context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    private val levelHashMap = HashMap<String, Int>()
    private val levelNames = arrayListOf<String>("Pawn", "Knight", "Bishop", "Rook")
    private val pointsRemaining = arrayListOf<Long>(2000, 3000, 4000, 5000)

    private val quotesArray = arrayListOf<String>(
        "What do you call a dinosaur who gets into an accident?",
        "Why is a leopard so bad at hide an seek?",
        "How does Moses make his tea?",
        "How did the rocket lose his job?",
        "What do you call a ship that sits at the bottom of the ocean and twitches?"
    )

    private var answersArray = arrayListOf<String>(
        "A Tyrannosaurus Wrecks",
        "He always gets spotted",
        "He brews",
        "He got fired",
        "A nervous wreck"
    )

    var gemNames = arrayListOf<String>(
        "Amethyst",
        "Ruby",
        "Emerald",
        "Sapphire",
        "Orange Julius",
        "Diamond",
        "Yellow Fever",
        "Tropic of Cancer",
        "Emerald"
    )

    var gemPrices = arrayListOf<Int>(
        1000,
        2000,
        3000,
        4000,
        5000,
        6000,
        7000,
        8000,
        9000
    )

    var gemImages = arrayListOf<Int>(
        R.drawable.gem,
        R.drawable.ruby,
        R.drawable.emerald,
        R.drawable.sapphire,
        R.drawable.orange_gem,
        R.drawable.double_trouble,
        R.drawable.yellow_gem,
        R.drawable.pink_gem,
        R.drawable.trophy
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

    fun storeNativeLanguageSelection(lang: String) {
        sharedPreferences.edit().putString("native_language", lang).apply()
    }

    fun storeProfileImageSelection(imagePath: String) {
        sharedPreferences.edit().putString("profile_image", imagePath).apply()
    }

    fun getProfileImage(): String {
        return sharedPreferences.getString("profile_image", "") ?: ""
    }

    fun getCurrentTargetLanguage(): String {
        return sharedPreferences.getString("native_language", "") ?: ""
    }

    fun getCurrentScore(): Int {
        return sharedPreferences.getInt("points", 0)
    }

    fun storeCurrentScore(score: Int) {
        sharedPreferences.edit().putInt("points", score).apply()
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
        val quoteIndex = getQuoteIndex() % quotesArray.size
        val result = quotesArray[quoteIndex]
        incrementQuoteIndex()
        return result
    }

    fun getCurrentJokeAnswer(): String {
        val quoteIndex = getQuoteIndex() % quotesArray.size
        val oldIndex = (quoteIndex - 1 + quotesArray.size) % quotesArray.size
        return if (oldIndex >= 0 && oldIndex < answersArray.size) answersArray[oldIndex] else "Oops, something went wrong"
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

    fun storeUserLevel(level: String) {
        sharedPreferences.edit().putString(USER_LEVEL, level).apply()
    }

    fun getLastClickTime(buttonId: Int): Long {
        return sharedPreferences.getLong(buttonId.toString(), -1L)
    }

    fun storeLastClickTime(buttonId: Int, time: Long) {
        sharedPreferences.edit().putLong(buttonId.toString(), time).apply()
    }

    fun storeCountEnabledBots(count: Int) {
        sharedPreferences.edit().putInt(ENABLED_BOT_COUNT, count).apply()
    }

    fun getEnabledBotCount(): Int {
        return sharedPreferences.getInt(ENABLED_BOT_COUNT, -1)
    }

    companion object {
        const val SHARED_PREFERENCES_KEY = "Shared Preferences"
        const val USER_LEVEL = "User Level"
        const val BOT_STATES = "Bot States"
        const val MESSAGES = "Messages"
        const val CURRENT_PATH = "CurrentPath"
        const val TRANSLATIONS_ES_TO_EN = "TranslationsEsToEn"
        const val AUDIOS = "Audios/"
        const val QUOTE_INDEX = "QuoteIndex"
        const val ENABLED_BOT_COUNT = "Enabled_Bot_Count"
    }
}