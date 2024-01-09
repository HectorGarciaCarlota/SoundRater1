package com.example.soundrater1

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import jp.wasabeef.glide.transformations.BlurTransformation
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.google.gson.Gson


// The RateSong activity allows users to rate a song and saves their rating.
class RateSong : AppCompatActivity() {
    // Variable to hold the UserProfile object that may be passed to this activity.
    private var userProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the activity_rate_song layout.
        setContentView(R.layout.activity_rate_song)

        // Extract the track name, artist name, and image URI from the intent that started this activity.
        val trackName = intent.getStringExtra("TRACK_NAME")
        val artistName = intent.getStringExtra("ARTIST_NAME")
        val imageUri = intent.getStringExtra("IMAGE_URI")
        // Try to retrieve a UserProfile object from the intent.
        userProfile = intent.getParcelableExtra("USER_PROFILE")

        // Get references to the TextViews and ImageViews in the layout.
        val trackNameTextView = findViewById<TextView>(R.id.textViewTrackName)
        val artistNameTextView = findViewById<TextView>(R.id.textViewArtistName)
        val albumImageView = findViewById<ImageView>(R.id.imageViewAlbum)
        val backgroundImageView = findViewById<ImageView>(R.id.imageViewAlbumBackground)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)

        // Set the text for the TextViews to the track and artist names.
        trackNameTextView.text = trackName
        artistNameTextView.text = artistName

        // Use Glide to load the album cover image into the ImageView.
        Glide.with(this).load(imageUri).into(albumImageView)

        // Use Glide with a blur transformation to load a blurred version of the album image as a background.
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            .into(backgroundImageView)

        // Set up a listener that reacts to changes in the rating bar's rating.
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            userProfile?.let {
                // Create a new RatedSong object with the track details and rating.
                val ratedSong = RatedSong(
                    trackName = trackName ?: "",
                    artistName = artistName ?: "",
                    imageUri = imageUri ?: "",
                    rating = rating
                )
                // Add the new RatedSong to the user's list of rated songs.
                it.ratedSongs.add(ratedSong)
                // Save the updated user profile.
                saveUserProfile(it)
                // Finish the activity and return to the previous one.
                finish()
            }
        }
    }

    // Function to save the updated user profile to SharedPreferences.
    private fun saveUserProfile(userProfile: UserProfile) {
        // Get the SharedPreferences editor to save data.
        val sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Get the existing user profile JSON string from SharedPreferences.
        val existingUserProfileJson = sharedPreferences.getString("USER_PROFILE", null)
        var existingUserProfile = userProfile

        // If there's an existing user profile, convert the JSON string back to a UserProfile object.
        if (!existingUserProfileJson.isNullOrEmpty()) {
            existingUserProfile = gson.fromJson(existingUserProfileJson, UserProfile::class.java)
        }

        // Check if the current song has already been rated by the user.
        val existingRatedSongIndex = existingUserProfile.ratedSongs.indexOfFirst {
            it.trackName == userProfile.ratedSongs.last().trackName &&
                    it.artistName == userProfile.ratedSongs.last().artistName &&
                    it.imageUri == userProfile.ratedSongs.last().imageUri
        }

        // If the song is already rated, update the rating; otherwise, add the new rating.
        if (existingRatedSongIndex != -1) {
            existingUserProfile.ratedSongs[existingRatedSongIndex].rating =
                userProfile.ratedSongs.last().rating
        } else {
            existingUserProfile.ratedSongs.add(userProfile.ratedSongs.last())
        }

        // Convert the updated UserProfile object back to a JSON string.
        val updatedUserProfileJson = gson.toJson(existingUserProfile)
        // Save the JSON string in SharedPreferences.
        editor.putString("USER_PROFILE", updatedUserProfileJson)
        editor.apply()

        // Log the updated user profile for debugging.
        Log.d("RateSong", "UserProfile saved: $updatedUserProfileJson")

        // Display a toast message confirming the rating was saved.
        runOnUiThread {
            Toast.makeText(
                this,
                "Rated '${userProfile.ratedSongs.last().trackName}' by ${userProfile.ratedSongs.last().artistName} with ${userProfile.ratedSongs.last().rating} stars",
                Toast.LENGTH_LONG
            ).show()
        }

        // Close the activity and return to the previous screen.
        finish()
    }
}



