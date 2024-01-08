package com.example.soundrater1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("DEPRECATION")
class MainMenu : AppCompatActivity() {
    //late init for userprofile
    private var userProfile: UserProfile? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list
        trackAdapter = TrackAdapter(emptyList())
        recyclerView.adapter = trackAdapter

        // Retrieve UserProfile
        userProfile = intent.getParcelableExtra("USER_PROFILE")

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchSpotifySongs(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Implement search-as-you-type functionality
                return true
            }
        })
        trackAdapter.onItemClickListener = object : TrackAdapter.OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                navigateToRateSongActivity(track)
            }
        }
    }


    private fun searchSpotifySongs(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val spotifyService = retrofit.create(SpotifyService::class.java)

        // Access token
        val accessToken = userProfile?.Token

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = spotifyService.searchTracks("Bearer $accessToken", query).execute()
                if (response.isSuccessful) {
                    val tracks = response.body()?.tracks?.items ?: emptyList()
                    runOnUiThread {
                        trackAdapter.updateTracks(tracks) // Update the RecyclerView with new data
                    }
                } else {
                    Log.e("MainMenu", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MainMenu", "Exception", e)
            }
        }
    }

    private fun navigateToRateSongActivity(track: TrackItem) {
        val intent = Intent(this, RateSong::class.java)
        intent.putExtra("TRACK_NAME", track.name)
        intent.putExtra("ARTIST_NAME", track.artists.joinToString { it.name })
        intent.putExtra("IMAGE_URI", track.album.images.firstOrNull()?.url)
        intent.putExtra("USER_PROFILE", userProfile)
        startActivity(intent)
    }
}
