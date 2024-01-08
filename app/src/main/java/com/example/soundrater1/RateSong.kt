package com.example.soundrater1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class RateSong : AppCompatActivity() {
    private var userProfile: UserProfile? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_song)

        val trackName = intent.getStringExtra("TRACK_NAME")
        val artistName = intent.getStringExtra("ARTIST_NAME")
        val imageUri = intent.getStringExtra("IMAGE_URI")
        userProfile = intent.getParcelableExtra("USER_PROFILE")

        Log.d("TrackName", trackName.toString())
        Log.d("ArtistName", artistName.toString())
        Log.d("ImageUri", imageUri.toString())
    }
}