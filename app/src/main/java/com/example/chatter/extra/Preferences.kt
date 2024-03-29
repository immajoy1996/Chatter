package com.example.chatter.extra

import android.content.Context
import android.util.Log
import com.example.chatter.R
import com.example.chatter.data.*
import com.google.gson.Gson

class Preferences(val context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    private val levelHashMap = HashMap<String, Int>()
    private val levelNames = arrayListOf<String>("Pawn", "Knight", "Bishop", "Rook")
    private val pointsRemaining = arrayListOf<Long>(2000, 3000, 4000, 5000)
    private val pointsForLevel = arrayListOf<Int>(2000, 5000, 9000)
    
    private var botStoriesInOrder =
        arrayListOf<BotStoryInfo>(
            BotStoryInfo("Taxi Pete", R.drawable.taxi1, "", "", -1, 4),
            BotStoryInfo("Manager Marge", R.drawable.hotel, "", "", 0, 4),
            BotStoryInfo("Manager Marge", R.drawable.hotel, "", "", 0, 4)
        )
    private var botsInOrder = arrayListOf<String>("Taxi Pete", "Doctor Hum-Vee")
    private val quotesArray = arrayListOf<String>(
        "What do you call a dinosaur who gets into an accident?",
        "Why is a leopard so bad at hide an seek?",
        "How does Moses make his tea?",
        "How did the rocket lose his job?",
        "What do you call a ship that sits at the bottom of the ocean and twitches?"
    )

    private val storyModeMap = HashMap<String, StoryModeDataItem>()
    private val newLevelsMap = HashMap<String, NewLevelDataItem>()
    private val quizMap = HashMap<String, QuizDataItem>()

    fun getStoryModeDataItems(): HashMap<String, StoryModeDataItem> {
        return storyModeMap
    }

    fun getNewLevelsDataItems(): HashMap<String, NewLevelDataItem> {
        return newLevelsMap
    }

    fun getCurrentLevelFromMap(): String {
        val curBot = getCurrentBotStory()
        return newLevelsMap[curBot]?.levelName ?: "something went wrong"
    }

    fun storeCurrentLevel(levelName: String) {
        sharedPreferences.edit().putString("currentLevel", levelName)
            .apply()
    }

    fun getStoredLevel(): String {
        return sharedPreferences.getString("currentLevel", "") ?: ""
    }

    private fun setUpQuizMap() {
        quizMap.put("Taxi Pete", QuizDataItem("Taxi Pete", "SpeechGame"))
        quizMap.put("Doctor Hum-Vee", QuizDataItem("Doctor Hum-Vee", "MultipleChoice"))
    }

    private fun setUpNewLevelsMap() {
        newLevelsMap.put("Taxi Pete", NewLevelDataItem("Taxi Pete", "Homeless", ""))
        newLevelsMap.put("Doctor Hum-Vee", NewLevelDataItem("Doctor Hum-Vee", "Scrub", ""))
    }

    private fun setUpStoryModeMap() {
        storyModeMap.put(
            "Doctor Hum-Vee",
            StoryModeDataItem(
                "Doctor Hum-Vee",
                R.drawable.hotel,
                "Story",
                "Doctor Hum-Vee",
                "Saint Jude's Hospital, NY"
            )
        )
        storyModeMap.put(
            "Taxi Pete",
            StoryModeDataItem(
                "Taxi Pete",
                R.drawable.taxi1,
                "Story",
                "Taxi Pete",
                "La Guardia Airport, NY"
            )
        )
    }

    init {
        setUpStoryModeMap()
        setUpNewLevelsMap()
        setUpQuizMap()
    }

    fun getStoredQuizMap(): HashMap<String, QuizDataItem> {
        return quizMap
    }

    fun getMyCurrentLevel(myPoints: Int): String {
        if (myPoints < pointsForLevel[0]) {
            return "Easy"
        } else if (myPoints < pointsForLevel[1]) {
            return "Medium"
        } else {
            return "Hard"
        }
    }

    fun getTotalPointsForMyLevel(myPoints: Int): Int {
        if (myPoints < pointsForLevel[0]) {
            return pointsForLevel[0]
        } else if (myPoints < pointsForLevel[1]) {
            return pointsForLevel[1]
        } else {
            return pointsForLevel[2]
        }
    }

    fun getLevelCompletionPercentage(myPoints: Int): Int {
        if (myPoints < pointsForLevel[0]) {
            return 100 * myPoints / pointsForLevel[0]
        } else if (myPoints < pointsForLevel[1]) {
            return 100 * (myPoints - pointsForLevel[0]) / (pointsForLevel[1] - pointsForLevel[0])
        } else {
            return 100 * (myPoints - pointsForLevel[1]) / (pointsForLevel[2] - pointsForLevel[1])
        }
    }

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

    fun storeNativeLanguageFlagSelection(flag: String) {
        sharedPreferences.edit().putString("native_language_flag", flag).apply()
    }

    fun storeProfileImageSelection(imagePath: String) {
        sharedPreferences.edit().putString("profile_image", imagePath).apply()
    }

    fun getProfileImage(): String {
        return sharedPreferences.getString("profile_image", "") ?: ""
    }

    private fun getTranslationKey(word: String, targetLanguage: String): String {
        return "${targetLanguage}/${word}"
    }

    fun storeWordAndDefinition(word: String, definition: String, targetLanguage: String) {
        sharedPreferences.edit().putString(getTranslationKey(word, targetLanguage), definition)
            .apply()
    }

    fun getDefinition(word: String, targetLanguage: String): String {
        return sharedPreferences.getString(getTranslationKey(word, targetLanguage), "") ?: ""
    }

    fun getCurrentBotIndex(): Int {
        return sharedPreferences.getInt("botStoryIndex", 0)
    }

    fun storeCurrentBotStoryIndex(index: Int) {
        sharedPreferences.edit().putInt("botStoryIndex", index)
            .apply()
    }

    fun incrementCurrentBotStoryIndex() {
        var index = getCurrentBotIndex()
        if (index == -1) {
            index = 0
        }
        storeCurrentBotStoryIndex(index + 1)
    }

    fun getMyCurrentBotStory(): BotStoryInfo {
        var index = getCurrentBotIndex()
        if (index == -1) {
            index = 0
        }
        if (index < botStoriesInOrder.size) {
            return botStoriesInOrder[index]
        } else {
            return botStoriesInOrder[0]
        }
    }

    fun haveSeenThisBot(
        botTitle: String
    ): Boolean {
        val index = getCurrentBotIndex()
        for (p in 0 until (index + 1)) {
            if (botsInOrder[p] == botTitle) {
                return true
            }
        }
        return false
    }

    fun getBotIndexValue(botTitle: String): Int {
        if (botsInOrder.contains(botTitle)) {
            return botsInOrder.indexOf(botTitle)
        }
        return -1000
    }

    fun getCurrentBotStory(): String {
        var index = getCurrentBotIndex()
        if (index == -1) {
            index = 0
        }
        if (index < botsInOrder.size) {
            return botsInOrder[index]
        } else {
            return ""
        }
    }

    fun getCurrentTargetLanguage(): String {
        return sharedPreferences.getString("native_language", "") ?: ""
    }

    fun getCurrentTargetLanguageFlag(): String {
        return sharedPreferences.getString("native_language_flag", "") ?: ""
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

    fun getNextJoke(): String {
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
        val key = getCurrentPathKey(userUid, botTitle)
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
        val botMessages = arrayListOf<String>()
        val userMessages = arrayListOf<String>()
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

    fun getMyFavoritesArray(): ArrayList<Vocab>? {
        val jsonStringObject = getMyFavoritesArrayAsJson()
        var favsList = arrayListOf<Vocab>()
        jsonStringObject.let {
            val vocabObject = Gson().fromJson(it, MyFavoritesList::class.java)
            vocabObject?.let {
                favsList = it.favsList
            }
        }
        return favsList
    }

    fun storeMyFavoritesJsonString(myFavs: ArrayList<Vocab>) {
        sharedPreferences.edit().putString(MY_FAVORITES, Gson().toJson(MyFavoritesList(myFavs)))
            .apply()
    }

    private fun getMyFavoritesArrayAsJson(): String {
        return sharedPreferences.getString(MY_FAVORITES, "") ?: ""
    }

    fun flashcardAlreadySeen(card: Vocab): Boolean {
        val botTitle = card.whichBot
        val flashcardHashmap = getFlashcardHashmap()
        return (flashcardHashmap.get(botTitle)?.seenFlashcards?.contains(card) == true)
    }

    fun addNewFlashcardToList(newCard: Vocab) {
        val botTitle = newCard.whichBot
        val flashcardHashmap = getFlashcardHashmap()
        val botVocab = flashcardHashmap.get(botTitle)
        if (botVocab == null) {
            botTitle?.let {
                flashcardHashmap.put(it, BotVocab(HashSet<Vocab>(), HashSet<Vocab>()))
            }
        }
        (botVocab?.flashcardList as? HashSet<Vocab>)?.add(newCard)
        botVocab?.let {
            if (botTitle != null) {
                flashcardHashmap.put(botTitle, it)
            }
        }
        storeFlashcardHashmapAsJsonString(flashcardHashmap)
    }

    fun storeCurrentNotificationList(notificationList: ArrayList<NotificationItem>) {
        sharedPreferences.edit()
            .putString("notification_list", Gson().toJson(NotificationListModel(notificationList)))
            .apply()
    }

    fun getCurrentNotificationList(): ArrayList<NotificationItem> {
        val jsonStringObject = getNotificationListAsJsonString()
        var notificationList = arrayListOf<NotificationItem>()
        jsonStringObject.let {
            val mapObject = Gson().fromJson(it, NotificationListModel::class.java)
            mapObject?.let {
                notificationList = it.notificationList
            }
        }
        return notificationList
    }

    fun storeNewFlashcard(newCard: Vocab) {
        val botTitle = newCard.whichBot
        val flashcardHashmap = getFlashcardHashmap()

        val botVocab = flashcardHashmap.get(botTitle)
        Log.d("BotVocab", botVocab.toString())
        (botVocab?.seenFlashcards as? HashSet<Vocab>)?.add(newCard)
        botVocab?.let {
            if (botTitle != null) {
                flashcardHashmap.put(botTitle, it)
            }
        }
        storeFlashcardHashmapAsJsonString(flashcardHashmap)
    }

    fun getCompletionRate(botTitle: String): Int {
        val hashmap = getFlashcardHashmap()
        val wholeDeckSize = hashmap.get(botTitle)?.flashcardList?.size
        val seenSize = hashmap.get(botTitle)?.seenFlashcards?.size
        Log.d("TotalSize", seenSize.toString() + " " + wholeDeckSize.toString())
        if (wholeDeckSize == null || seenSize == null) return 0
        return (100.0 * seenSize / wholeDeckSize).toInt()
    }

    fun storeFlashcardDeckForParticularBot(
        botTitle: String,
        flashcardList: Set<Vocab>,
        seenFlashcards: Set<Vocab>
    ) {
        val flashcardHashmap = getFlashcardHashmap()
        flashcardHashmap.put(botTitle, BotVocab(flashcardList, seenFlashcards))
        storeFlashcardHashmapAsJsonString(flashcardHashmap)
    }

    fun getFlashcardHashmap(): HashMap<String, BotVocab> {
        val jsonStringObject = getFlashcardHashmapAsJsonString()
        var hashmap = HashMap<String, BotVocab>()
        jsonStringObject.let {
            val mapObject = Gson().fromJson(it, AllFlashcards::class.java)
            mapObject?.let {
                hashmap = it.flashcardMap
            }
        }
        return hashmap
    }

    fun storeMultipleChoiceDeckComplete(botTitle: String) {
        sharedPreferences.edit()
            .putString("$MULTIPLE_CHOICE/$botTitle", "Complete")
            .apply()
    }

    fun getIsMultipleChoiceDeckComplete(botTitle: String): Boolean {
        val result = sharedPreferences.getString("$MULTIPLE_CHOICE/$botTitle", "") ?: ""
        return result.isNotEmpty()
    }

    private fun getNotificationListAsJsonString(): String {
        return sharedPreferences.getString("notification_list", "") ?: ""
    }

    private fun getFlashcardHashmapAsJsonString(): String {
        return sharedPreferences.getString("flashcard_hashmap", "") ?: ""
    }

    private fun storeFlashcardHashmapAsJsonString(hashMap: HashMap<String, BotVocab>) {
        sharedPreferences.edit()
            .putString("flashcard_hashmap", Gson().toJson(AllFlashcards(hashMap)))
            .apply()
    }

    fun storeMessage(id: Int, msg: String) {
        sharedPreferences.edit().putString(id.toString(), msg)
            .apply()
    }

    fun getMessage(id: Int): String {
        return sharedPreferences.getString(id.toString(), "") ?: ""
    }

    fun storeMessageTranslation(id: Int, msg: String) {
        sharedPreferences.edit().putString("Translate ${id}", msg)
            .apply()
    }

    fun getMessageTranslation(id: Int): String {
        return sharedPreferences.getString("Translate ${id}", "") ?: ""
    }

    fun storePath(id: Int, path: String) {
        sharedPreferences.edit().putString("$CURRENT_PATH/$id", path)
            .apply()
    }

    fun getPath(id: Int): String {
        return sharedPreferences.getString("$CURRENT_PATH/$id", "") ?: ""
    }

    fun storeCurrentPath(path: String) {
        sharedPreferences.edit().putString(path, "seen")
            .apply()
    }

    fun haveSeenCurrentPath(path: String): String {
        return sharedPreferences.getString(path, "") ?: ""
    }

    fun getCurrentStateId(): Int {
        return sharedPreferences.getInt("stateId", 0)
    }

    fun shouldShowNewStoryNotifcation(showStory: Boolean) {
        val showStoryInt = if (showStory) 1 else 0
        val stateId = getCurrentStateId()

        sharedPreferences.edit()
            .putInt("stateId", stateId + NotificationManager.Constants.STORY * showStoryInt)
            .apply()
    }

    private fun alreadyShowedThisNotification(
        stateId: Int,
        notificationType: NOTIFICATION_TYPE
    ): Boolean {
        when (notificationType) {
            NOTIFICATION_TYPE.QUIZ -> {
                return ((stateId / NotificationManager.Constants.QUIZ) % 10 == 1)
            }
            NOTIFICATION_TYPE.LEVEL_REACHED -> {
                return ((stateId / NotificationManager.Constants.LEVEL) % 10 == 1)
            }
            else -> {
                return false
            }
        }
    }

    fun shouldShowQuizNotification(showQuiz: Boolean) {
        val showQuizInt = if (showQuiz) 1 else 0
        val stateId = getCurrentStateId()
        if (alreadyShowedThisNotification(stateId, NOTIFICATION_TYPE.QUIZ)) {
            return
        }
        sharedPreferences.edit()
            .putInt("stateId", stateId + NotificationManager.Constants.QUIZ * showQuizInt)
            .apply()
    }

    fun shouldShowLevelUpNotification(showLevelUp: Boolean) {
        val showLevelUpInt = if (showLevelUp) 1 else 0
        val stateId = getCurrentStateId()
        if (alreadyShowedThisNotification(stateId, NOTIFICATION_TYPE.LEVEL_REACHED)) {
            return
        }
        sharedPreferences.edit()
            .putInt("stateId", stateId + NotificationManager.Constants.LEVEL * showLevelUpInt)
            .apply()
    }

    fun getCurrentStoryInfo(): StoryPathInfo? {
        val jsonStringObject = getStoryInfoAsJsonString()
        if (jsonStringObject.isEmpty()) {
            return null
        }
        var storyPathInfo: StoryPathInfo? = null
        jsonStringObject.let {
            val storyObject = Gson().fromJson(it, StoryPathInfo::class.java)
            storyObject?.let {
                storyPathInfo = it
            }
        }
        return storyPathInfo
    }

    private fun getStoryInfoAsJsonString(): String {
        return sharedPreferences.getString("story_path_info", "") ?: ""
    }

    fun storeStoryInfo(storyPathInfo: StoryPathInfo) {
        sharedPreferences.edit()
            .putString("story_path_info", Gson().toJson(storyPathInfo))
            .apply()
    }

    fun setCurrentStateId(
        showQuiz: Boolean?,
        leveledUp: Boolean?,
        showPracticeNotification: Boolean? = null
    ) {
        var currentBotIndex = getCurrentBotIndex()
        if (currentBotIndex == -1) {
            currentBotIndex = 0
        }
        val showQuizInt = if (showQuiz == true) 1 else 0
        val leveledUpInt = if (leveledUp == true) 1 else 0
        val showPracticeInt = if (showPracticeNotification == true) 1 else 0
        sharedPreferences.edit()
            .putInt(
                "stateId",
                NotificationManager.Constants.STORY * currentBotIndex + NotificationManager.Constants.QUIZ * showQuizInt + NotificationManager.Constants.LEVEL * leveledUpInt + NotificationManager.Constants.PRACTICE * showPracticeInt
            )
            .apply()
    }

    fun getCurrentStoryPath(): String {
        val userState = getUserState()
        val botIndex = userState.botIndex
        val currentBotStoryTitle = botStoriesInOrder[botIndex].botTitle
        val currentBotStoryItem = userState.currentPart + 1
        val path = "${BOT_STORIES}/${currentBotStoryTitle}/Part${currentBotStoryItem}"
        return path
    }

    fun getCurrentConversationPath(): String {
        val userState = getUserState()
        val botIndex = userState.botIndex
        val currentBotStoryTitle = botStoriesInOrder[botIndex].botTitle
        val currentBotStoryItem = userState.currentPart + 1
        val path = "${BOT_CONVERSATIONS}/${currentBotStoryTitle}/Conversation${currentBotStoryItem}"
        return path
    }

    fun initializeUserState() {
        storeUserState(UserState(0, 0))
    }

    fun incrementUserState() {
        val userState = getUserState()
        val currentBotIndex = getCurrentBotIndex()
        val botStory = botStoriesInOrder[currentBotIndex]
        if (userState.currentPart + 1 <= botStory.totalParts) {
            userState.currentPart++
        } else {
            userState.botIndex++
            userState.currentPart = 0
            incrementCurrentBotStoryIndex()
        }
        storeUserState(userState)
    }

    fun getUserState(): UserState {
        val jsonString = getUserStateAsJsonString()
        var userState = UserState(-1, -1)
        jsonString.let {
            val storyObject = Gson().fromJson(it, UserState::class.java)
            storyObject?.let {
                userState = it
            }
        }
        return userState
    }

    fun storeUserState(userState: UserState) {
        storeUserStateAsJsonString(userState)
    }

    private fun storeUserStateAsJsonString(userState: UserState) {
        sharedPreferences.edit().putString(USER_STATE, Gson().toJson(userState))
            .apply()
    }

    private fun getUserStateAsJsonString(): String {
        return sharedPreferences.getString(USER_STATE, "") ?: ""
    }

    fun getCurrentBotStoryTitle(): String {
        val currentBotIndex = getCurrentBotIndex()
        return botStoriesInOrder[currentBotIndex].botTitle
    }

    fun getCurrentBotStoryImage(): Int {
        val currentBotIndex = getCurrentBotIndex()
        return botStoriesInOrder[currentBotIndex].botImage
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
        const val MY_FAVORITES = "My favorites"
        const val MULTIPLE_CHOICE = "Multiple Choice"
        const val USER_STATE = "User State"
        const val BOT_CONVERSATIONS = "BotConversations"
        const val BOT_STORIES = "BotStories"
    }
}