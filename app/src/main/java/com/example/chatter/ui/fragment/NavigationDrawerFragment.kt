package com.example.chatter.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.chatter.extra.Preferences
import com.example.chatter.ui.activity.ProfileActivity
import com.example.chatter.R
import com.example.chatter.ui.activity.SignInActivity
import com.example.chatter.ui.activity.DashboardActivity
import com.example.chatter.ui.activity.MyStashActivity
import com.example.chatter.ui.activity.SignInActivity.Companion.BEAR_PROFILE_PATH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*

class NavigationDrawerFragment : BaseFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var preferences: Preferences
    private var isGuestMode = false
    private var targetLanguage: String? = null
    private var languageMap = HashMap<String, String>()
    private var targetBotCategory: String? = null
    private var profileImage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isGuestMode = (activity as? DashboardActivity)?.isGuestMode() ?: false
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        context?.let {
            preferences = Preferences(it)
        }
        setUpLanguageMap()
        setUpUserInfo()
        setUpButtonImages()
        setUpButtons()
        setUpProfileButtons()
    }

    private fun setUpLanguageMap() {
        languageMap.put("fr", "French")
        languageMap.put("en", "English")
        languageMap.put("ru", "Russian")
        languageMap.put("hi", "Hindi")
        languageMap.put("es", "Spanish")
        languageMap.put("de", "German")
        languageMap.put("ar", "Arabic")
        languageMap.put("zh", "Mandarin")
        languageMap.put("he", "Hebrew")
        languageMap.put("nl", "Dutch")
    }


    private fun setUpProfileButtons() {
        sound_effects_button.setOnClickListener {
            if (sound_effects_button.text == "ON") {
                sound_effects_button.setBackgroundResource(R.drawable.circle_disabled)
                sound_effects_button.text = "OFF"
                sound_effects_button.setTextColor(Color.parseColor("#696969"))
            } else {
                sound_effects_button.setBackgroundResource(R.drawable.circle_enabled)
                sound_effects_button.text = "ON"
                sound_effects_button.setTextColor(Color.parseColor("#ffffff"))
            }
        }
        camera_badge.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            startActivityForResult(
                intent,
                CHANGE_PROFILE_REQUEST_CODE
            )
        }
    }

    private fun setUpUserInfo() {
        if (auth.currentUser != null) {
            val curUid = auth.currentUser?.uid
            curUid?.let {
                val emailRef = databaseReference.child(USERS.plus(it)).child("email")
                val emailListener = baseValueEventListener { dataSnapshot ->
                    val email = dataSnapshot.value.toString()
                    navigation_drawer_username.text = email.removeSuffix("@gmail.com")
                }
                emailRef.addListenerForSingleValueEvent(emailListener)

                val pointsRef = databaseReference.child(USERS.plus(it)).child("points")
                val pointsListener = baseValueEventListener { dataSnapshot ->
                    val pointTotal = dataSnapshot.value.toString()
                    drawer_close_text.text = pointTotal
                    getNewGem(pointTotal.toInt())?.let {
                        drawer_settings_image.setImageResource(it)
                    }
                }
                pointsRef.addListenerForSingleValueEvent(pointsListener)
            }
        } else {
            navigation_drawer_username.text = "Guest"
            drawer_close_text.text = preferences.getCurrentScore().toString()
            getNewGem(preferences.getCurrentScore())?.let {
                drawer_settings_image.setImageResource(it)
            }
        }
    }

    private fun getNewGem(score: Int): Int? {
        if (score < preferences.gemPrices[0]) {
            return null
        }
        for (index in 1..preferences.gemImages.size - 1) {
            if (score < preferences.gemPrices[index]) {
                return preferences.gemImages[index - 1]
            }
        }
        return preferences.gemImages.last()
    }

    private fun setUpButtonImages() {
        drawer_categories_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_language_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_logout_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    }

    fun setUpLanguageTextField(targetLang: String) {
        val prefix = "Language: "
        var spannableStringLanguage: SpannableString? = null
        if (targetLang.isEmpty()) {
            val langText = "Language: N/A"
            spannableStringLanguage = SpannableString(langText)
            spannableStringLanguage.setSpan(
                StyleSpan(Typeface.BOLD), prefix.length, langText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            drawer_language_text.setText(spannableStringLanguage)
        } else if (languageMap.containsKey(targetLang)) {
            val langText = "Language: ${languageMap[targetLang]}"
            spannableStringLanguage = SpannableString(langText)
            spannableStringLanguage.setSpan(
                StyleSpan(Typeface.BOLD), prefix.length, langText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            drawer_language_text.setText(spannableStringLanguage)
        }
    }

    fun setUpProfileImage(profileImage: String) {
        if (profileImage.isNotEmpty()) {
            user_badge?.let {
                Glide.with(this)
                    .load(profileImage)
                    .into(it)
            }
        } else {
            user_badge?.let {
                Glide.with(this)
                    .load(BEAR_PROFILE_PATH)
                    .into(it)
            }
        }
    }

    fun setUpBotCategoryTextField(selectedCategory: String) {
        targetBotCategory = selectedCategory
        val prefix = "Category: "
        var spannableStringCategory: SpannableString? = null
        if (selectedCategory.isEmpty()) {
            val categoryText = "Category: All Bots"
            spannableStringCategory = SpannableString(categoryText)
            spannableStringCategory.setSpan(
                StyleSpan(Typeface.BOLD), prefix.length, categoryText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            drawer_caregories_text.setText(spannableStringCategory)
        } else {
            val categoryText = "Category: ${selectedCategory}"
            spannableStringCategory = SpannableString(categoryText)
            spannableStringCategory.setSpan(
                StyleSpan(Typeface.BOLD), prefix.length, categoryText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            drawer_caregories_text.setText(spannableStringCategory)
        }
    }

    private fun setUpButtons() {
        navigation_drawer_back.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        drawer_settings_layout.setOnClickListener {
            val intent = Intent(context, MyStashActivity::class.java)
            intent.putExtra("gem_image", drawer_settings_image.id)
            intent.putExtra("points", drawer_close_text.text.toString().toInt())
            context?.startActivity(intent)
        }
        drawer_my_logout_layout.setOnClickListener {
            activity?.finish()
            auth.signOut()
            val intent = Intent(context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        targetLanguage?.let {
            setUpLanguageTextField(it)
        }
        targetBotCategory?.let {
            setUpBotCategoryTextField(it)
        }
        drawer_language_layout.setOnClickListener {
            when (activity) {
                is DashboardActivity -> {
                    (activity as? DashboardActivity)?.loadLanguageSelectionScreen()
                }
            }
        }
        drawer_categories_layout.setOnClickListener {
            (activity as? DashboardActivity)?.loadCategoriesSelectionScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            auth.currentUser?.uid?.let {
                val uid = it
                val profilePathRef = databaseReference.child("Users/${uid}").child("profileImage")
                val profileListener = baseValueEventListener { dataSnapshot ->
                    val targetProfile = dataSnapshot.value.toString()
                    setUpProfileImage(targetProfile)
                }
                profilePathRef.addListenerForSingleValueEvent(profileListener)
            }
        } else {
            targetLanguage = preferences.getCurrentTargetLanguage()
            val targetProfileImage = preferences.getProfileImage()
            setUpProfileImage(targetProfileImage)
        }
    }

    companion object {
        const val USERS = "Users/"
        const val CHANGE_PROFILE_REQUEST_CODE = 20
        fun newInstance(
            targetLang: String,
            targetCategory: String
        ): NavigationDrawerFragment {
            val fragment =
                NavigationDrawerFragment()
            fragment.targetLanguage = targetLang
            fragment.targetBotCategory = targetCategory
            return fragment
        }
    }
}
