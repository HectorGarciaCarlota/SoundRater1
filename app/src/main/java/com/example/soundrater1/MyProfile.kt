package com.example.soundrater1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import jp.wasabeef.glide.transformations.BlurTransformation

class MyProfile : AppCompatActivity() {
    private var userProfile: UserProfile? = null
    @SuppressLint("SetTextI18n", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        // retrieve again data from sharedPreferences :)
        val sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE)
        val userProfileJson = sharedPreferences.getString("USER_PROFILE", "")
        userProfile = Gson().fromJson(userProfileJson, UserProfile::class.java)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val backGroundImage = findViewById<ImageView>(R.id.ivBlurredAlbumCover)
        val profileImageView = findViewById<CircleImageView>(R.id.profile_image)
        val nameTextView = findViewById<TextView>(R.id.name)
        val emailTextView = findViewById<TextView>(R.id.email)
        val ratedSongsCountTextView = findViewById<TextView>(R.id.rated_songs_count)
        val countryTextView = findViewById<TextView>(R.id.country)

        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            logout()
        }

        // Set the values of the views
        userProfile?.let {
            // Set the name and email
            nameTextView.text = it.Username // Set ups value for actual username
            emailTextView.text = getString(R.string.profile_email, it.Email) // Set ups value for actual email, made a string value :9
            // Set the rated songs count

            // Put diferent text depends on the lanscape mode
            var ratedSongsText = ""

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ratedSongsText = getString(R.string.rated_songs_count_land, it.ratedSongs.size) // Set ups value for ratedSongs number, made a string value :9
            } else {
                ratedSongsText = getString(R.string.rated_songs_count, it.ratedSongs.size) // Set ups value for ratedSongs number, made a string value :9
            }

            ratedSongsCountTextView.text = ratedSongsText

            val countryText = getString(R.string.profile_country, it.Country)
            countryTextView.text = countryText

            if (it.ratedSongs.size > 0) {
                val lastSongRated = it.ratedSongs.last()

                // Use Glide to load the album cover image into the ImageView.
                Glide.with(this).load(lastSongRated.imageUri).into(backGroundImage)

                // Use Glide with a blur transformation to load a blurred version of the album image as a background.
                Glide.with(this)
                    .load(lastSongRated.imageUri)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                    .into(backGroundImage)

                backGroundImage.visibility = View.VISIBLE
            }

            // Using Glide library to change images
            Glide.with(this).load(it.ImageUrl).into(profileImageView)
        }
        // Set the profile menu item as selected (we are highlighting the item ^^)
        bottomNavigationView.selectedItemId = R.id.navigation_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_main_menu -> {
                    // Navigate to the MainMenu activity
                    val intent = Intent(this, MainMenu::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    // Profile is already selected
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        // Clear the token from SharedPreferences
        val sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("SPOTIFY_ACCESS_TOKEN").apply()
        sharedPreferences.edit().remove("USER_PROFILE").apply()

        // Redirect to the login activity
        val loginIntent = Intent(this, MainActivity::class.java)
        // Clear all activities on top of the MainActivity in the back stack
        loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(loginIntent)
        finish() // Close the current activity
    }
}

