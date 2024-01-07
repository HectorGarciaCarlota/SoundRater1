package com.example.soundrater1

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyService {
    @GET("search")
    fun searchTracks(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("type") type: String = "track"
    ): Call<SpotifySearchResponse> // Define SpotifySearchResponse according to the expected JSON structure
}

// In your activity or another part of your code

private fun searchSpotifySongs(query: String, token: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/v1/")
        .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter
        .build()

    val spotifyService = retrofit.create(SpotifyService::class.java)

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = spotifyService.searchTracks("Bearer $token", query).execute()
            if (response.isSuccessful) {
                // Handle successful response
                val tracksResponse = response.body()
                // Now you can use tracksResponse to get your data
                tracksResponse?.tracks?.items?.forEach { track ->
                    Log.d("Track Name", track.name) // Example of accessing track name
                }
                // Remember to switch to the Main thread if you're updating the UI
            } else {
                // Handle error
                Log.e("SpotifySearch", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle network error
            Log.e("SpotifySearch", "Exception", e)
        }
    }
}

