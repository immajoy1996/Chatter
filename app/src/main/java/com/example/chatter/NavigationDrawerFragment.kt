package com.example.chatter

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun setUpUserInfo() {
        if (!isGuestMode) {
            val curUid = auth.currentUser?.uid
            curUid?.let {
                val emailRef = databaseReference.child(USERS.plus(it)).child("email")
                val pointsRemainingRef =
                    databaseReference.child(USERS.plus(it)).child("pointsRemaining")
                val emailListener = baseValueEventListener { dataSnapshot ->
                    val email = dataSnapshot.value.toString()
                    navigation_drawer_username.text = email.removeSuffix("@gmail.com")
                }
                emailRef.addListenerForSingleValueEvent(emailListener)
                //pointsRemainingRef.addListenerForSingleValueEvent(pointsRemainingListener)
                user_level.setText(preferences.getUserLevel())
                //setUpUserLevelImage()
            }
        } else {
            //setUserBadge(R.drawable.pawn)
            navigation_drawer_username.text = "Guest"
            navigation_drawer_user_score.text = "1000"
            user_level.setText("Pawn")
        }
    }

    private fun setUpUserLevelImage() {
        var curUserLevel = preferences.getUserLevel()
        when (curUserLevel) {
            "Pawn" -> {
                setUserBadge(R.drawable.pawn)
            }
            "Knight" -> {
                setUserBadge(R.drawable.knight)
            }
        }
    }

    private fun setUserBadge(drawableResource: Int) {
        context?.let {
            user_badge.setImageDrawable(ContextCompat.getDrawable(it, drawableResource))
        }
    }

    private fun setUpButtonImages() {
        drawer_categories_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_language_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        drawer_logout_image.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    }

    fun setUpLanguageTextField(targetLang: String) {
        if (targetLang.isEmpty() == true) {
            drawer_language_text.text = "Language: N/A"
        } else if (targetLang != null) {
            if (languageMap.containsKey(targetLang)) {
                drawer_language_text.text = "Language: ${languageMap[targetLang]}"
            }
        }
    }

    private fun setUpButtons() {
        navigation_drawer_back.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        drawer_settings_layout.setOnClickListener {
            context?.startActivity(Intent(context, MyStashActivity::class.java))
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

    companion object {
        const val USERS = "Users/"
        fun newInstance(targetLang: String): NavigationDrawerFragment {
            val fragment = NavigationDrawerFragment()
            fragment.targetLanguage = targetLang
            return fragment
        }
    }
}
