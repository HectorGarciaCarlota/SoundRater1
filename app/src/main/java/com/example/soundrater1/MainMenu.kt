package com.example.soundrater1

import RatedSongAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
// estava pensat per utilitzar LinearLayout pero amb el menú hem canviat a ConstraintLayout
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("DEPRECATION")
class MainMenu : AppCompatActivity() {
    private var userProfile: UserProfile? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var ratedSongsRecyclerView: RecyclerView
    private var ratedSongAdapter: RatedSongAdapter? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onResume() {
        super.onResume()

        // Retrieve the updated UserProfile from SharedPreferences
        val sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE)
        val userProfileJson = sharedPreferences.getString("USER_PROFILE", "")
        userProfile = Gson().fromJson(userProfileJson, UserProfile::class.java)

        // Update the adapter with the latest UserProfile
        userProfile?.let {
            ratedSongAdapter?.updateUserProfile(it)
        }

        // If needed, refresh the RecyclerView to show the updated list of rated songs
        ratedSongsRecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE)
        retrieveUserProfile()
        checkSavedData()
        // Set up the RecyclerView for Spotify song search
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(emptyList())
        recyclerView.adapter = trackAdapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_main_menu -> {
                    // Nothing happens because we're here already :)
                    true // return true to show the item as selected
                }
                R.id.navigation_profile -> {
                    // Navigate to the MyProfile activity
                    val intent = Intent(this, MyProfile::class.java)
                    startActivity(intent)
                    true // return true to show the item as selected
                }
                else -> false
            }
        }



        // Retrieve UserProfile via getSharedPreferences, we retrieve the Json using Gson library since we did it on mianActivity
        val sharedPreferences = getSharedPreferences("SpotifyPreferences", MODE_PRIVATE)
        val userProfileJson =sharedPreferences.getString("USER_PROFILE", null)
        userProfile = Gson().fromJson(userProfileJson, UserProfile::class.java)
        Log.d("TOKEN", userProfile.toString())
        Log.d("MainMenu", "UserProfile received with rated songs: ${userProfile?.ratedSongs?.size} songs")

        ratedSongsRecyclerView = findViewById(R.id.ratedSongsRecyclerView)
        ratedSongsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Check if userProfile is not null
        userProfile?.let {
            // Initialize the adapter with non-null userProfile
            ratedSongAdapter = RatedSongAdapter(it) { ratedSong ->
                // Handle click on rated song
                val trackItem = TrackItem(
                    ratedSong.trackName,
                    listOf(Artist(ratedSong.artistName)),
                    Album(listOf(Image(ratedSong.imageUri, 300, 300))) //Les mides son random, realment no les utilitzo pero ho demanava spotify
                )
                navigateToRateSongActivity(trackItem, ratedSong)
            }
        } ?: run {
            // Handle the case where userProfile is null, we should never get to this case since it won't let you come to this activity if you can't login from MainActivty

        }
        ratedSongsRecyclerView.adapter = ratedSongAdapter
        // Set up the SearchView for Spotify song search
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchSpotifySongs(it)
                    updateRecyclerViewVisibility(true)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    updateRecyclerViewVisibility(false)
                }
                return true
            }
        })

        // Set the click listener for items in the search result RecyclerView
        trackAdapter.onItemClickListener = object : TrackAdapter.OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                navigateToRateSongActivity(track)
            }
        }
    }

    // Searches Spotify songs based on the user's query
    private fun searchSpotifySongs(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val spotifyService = retrofit.create(SpotifyService::class.java)
        val accessToken = userProfile?.Token

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = spotifyService.searchTracks("Bearer $accessToken", query).execute()
                if (response.isSuccessful) {
                    val tracks = response.body()?.tracks?.items ?: emptyList()
                    runOnUiThread {
                        trackAdapter.updateTracks(tracks)
                    }
                } else {
                    Log.e("MainMenu", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MainMenu", "Exception", e)
            }
        }
    }

    // Navigates to RateSong activity when a song is clicked
    private fun navigateToRateSongActivity(track: TrackItem, ratedSong: RatedSong? = null) {
        val intent = Intent(this, RateSong::class.java).apply {
            putExtra("TRACK_NAME", track.name)
            putExtra("ARTIST_NAME", track.artists.joinToString { it.name })
            putExtra("IMAGE_URI", track.album.images.firstOrNull()?.url)
            putExtra("USER_PROFILE", userProfile)

            if (ratedSong != null) {
                // Pass the entire RatedSong object
                putExtra("ALREADY_RATED", true)
                putExtra("RATED_SONG", ratedSong)
            }
        }
        startActivity(intent)
    }

    private fun checkSavedData() {
        // For access token
        val savedToken = sharedPreferences.getString("SPOTIFY_ACCESS_TOKEN", "defaultToken")
        Log.d("CheckSharedPreferences", "Saved Token: $savedToken")

        // For user profile
        val savedUserProfileJson = sharedPreferences.getString("USER_PROFILE", "defaultProfile")
        Log.d("CheckSharedPreferences", "Saved UserProfile: $savedUserProfileJson")
    }

    private fun retrieveUserProfile() {
        val userProfileJson = sharedPreferences.getString("USER_PROFILE", null)
        if (userProfileJson != null) {
            val gson = Gson()
            userProfile = gson.fromJson(userProfileJson, UserProfile::class.java)
            Log.d("MainMenu", "Retrieved UserProfile: $userProfile")
        } else {
            Log.d("MainMenu", "No UserProfile found")
        }
    }

    private fun updateRecyclerViewVisibility(isSearching: Boolean) {
        if (isSearching) {
            // When searching, show the search RecyclerView and hide the ratedSongsRecyclerView
            recyclerView.visibility = View.VISIBLE
            val searchParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams
            searchParams.height = 0
            searchParams.matchConstraintPercentHeight = 0.5f // 50% height
            recyclerView.layoutParams = searchParams

            ratedSongsRecyclerView.visibility = View.GONE
        } else {
            // When not searching, show only the ratedSongsRecyclerView
            recyclerView.visibility = View.GONE

            ratedSongsRecyclerView.visibility = View.VISIBLE
            val ratedParams = ratedSongsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            ratedParams.height = 0
            ratedParams.matchConstraintPercentHeight = 1f // 100% height
            ratedSongsRecyclerView.layoutParams = ratedParams
        }
    }
}
