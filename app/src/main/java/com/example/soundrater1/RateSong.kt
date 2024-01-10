package com.example.soundrater1

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import jp.wasabeef.glide.transformations.BlurTransformation

import com.google.gson.Gson

// The RateSong activity allows users to rate a song and saves their rating.
class RateSong : AppCompatActivity() {
    // Variable to hold the UserProfile object that may be passed to this activity.
    private var userProfile: UserProfile? = null
    private lateinit var deleteRating: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the activity_rate_song layout.
        setContentView(R.layout.activity_rate_song)

        var deleteRatedSong: Boolean = false;

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
        deleteRating = findViewById<Button>(R.id.btnDeleteRating)

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

        userProfile?.let {
            // Create a new RatedSong object with the track details and rating.
            var ratedSong = RatedSong(
                trackName = trackName ?: "",
                artistName = artistName ?: "",
                imageUri = imageUri ?: "",
                rating = 0.0f
            )

            if (it.isSongInRated(it.ratedSongs, ratedSong.trackName, ratedSong.artistName)) {
                // Show the rating of the actual song
                val idRatedSong = it.indexOfRatedSong(it.ratedSongs, ratedSong)
                ratedSong = it.ratedSongs[idRatedSong]
                ratingBar.rating = ratedSong.rating

                // Enable the rating delete button
                deleteRating.visibility = View.VISIBLE

                deleteRating.setOnClickListener {
                    deleteRatedSong = true

                    saveUserProfile(userProfile!!, true, ratedSong)
                }
            }
        }

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
                saveUserProfile(it, deleteRatedSong, ratedSong)

                // Finish the activity and return to the previous one.
                finish()
            }
        }
    }

    // Function to save the updated user profile to SharedPreferences.
    private fun saveUserProfile(userProfile: UserProfile, deleteRatedSong: Boolean, ratedSong: RatedSong) {
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
        val existingRatedSongIndex = existingUserProfile.indexOfRatedSong(existingUserProfile.ratedSongs, ratedSong)

        // If the song is already rated, update the rating; otherwise, add the new rating.
        if (existingRatedSongIndex != -4) {
            existingUserProfile.ratedSongs[existingRatedSongIndex].rating =
                userProfile.ratedSongs.last().rating
        } else {
            existingUserProfile.ratedSongs.add(userProfile.ratedSongs.last())
        }

        if (existingRatedSongIndex != 4) {
            if (deleteRatedSong) {
                val trackName = userProfile.ratedSongs.get(existingRatedSongIndex).trackName

                existingUserProfile.ratedSongs.removeAt(existingRatedSongIndex)
                // userProfile.ratedSongs = existingUserProfile.ratedSongs

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "${trackName} has been unrated",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            existingUserProfile.ratedSongs.removeAt(existingUserProfile.ratedSongs.size - 1)
        }

        // Convert the updated UserProfile object back to a JSON string.
        val updatedUserProfileJson = gson.toJson(existingUserProfile)
        // Save the JSON string in SharedPreferences.
        editor.putString("USER_PROFILE", updatedUserProfileJson)
        editor.apply()

        // Log the updated user profile for debugging.
        Log.d("RateSong", "UserProfile saved: $updatedUserProfileJson")

        // Display a toast message confirming the rating was saved.
        if (!deleteRatedSong) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Rated '${userProfile.ratedSongs.last().trackName}' by ${userProfile.ratedSongs.last().artistName} with ${userProfile.ratedSongs.last().rating} stars",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Close the activity and return to the previous screen.
        finish()
    }
}



