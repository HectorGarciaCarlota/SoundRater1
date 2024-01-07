package com.example.soundrater1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

   /* override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val builder = AuthorizationRequest.Builder(SpotifyApi.CLIENT_ID, AuthorizationResponse.Type.TOKEN, SpotifyApi.REDIRECT_URI)
        builder.setScopes(arrayOf("streaming"))
        val request = builder.build()

        AuthorizationClient.openLoginInBrowser(this, request)
    }

    */

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var userProfile: UserProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE)

        val spotify_login_btn = findViewById<Button>(R.id.spotify_login_btn)
        spotify_login_btn.setOnClickListener {
            val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
            AuthorizationClient.openLoginActivity(
                this,
                SpotifyApi.AUTH_TOKEN_REQUEST_CODE,
                request
            )
        }
    }
    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest {
        return AuthorizationRequest.Builder(SpotifyApi.CLIENT_ID, type, SpotifyApi.REDIRECT_URI)
            .setShowDialog(false)
            .setScopes(arrayOf("user-read-email"))
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (SpotifyApi.AUTH_TOKEN_REQUEST_CODE == requestCode) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            val accessToken: String? = response.accessToken
            if (accessToken != null) {
                saveAccessToken(accessToken)
            }
            fetchSpotifyUserProfile(accessToken)
        }
    }
    /*
    private fun fetchSpotifyUserProfile(token: String?) {
        Log.d("Status: ", "Please Wait...")
        if (token == null) {
            Log.i("Status: ", "Something went wrong - No Access Token found")
            return
        }
        val getUserProfileURL = "https://api.spotify.com/v1/me"
        GlobalScope.launch(Dispatchers.Default) {
            val url = URL(getUserProfileURL)
            val httpsURLConnection = withContext(Dispatchers.IO) {url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            withContext(Dispatchers.Main) {
                val jsonObject = JSONObject(response)
                // Spotify Id
                val spotifyId = jsonObject.getString("id")
                Log.d("Spotify Id :", spotifyId)
                // Spotify Display Name
                val spotifyDisplayName = jsonObject.getString("display_name")
                Log.d("Spotify Display Name :", spotifyDisplayName)
                // Spotify Email
                val spotifyEmail = jsonObject.getString("email")
                Log.d("Spotify Email :", spotifyEmail)
                val spotifyAvatarArray = jsonObject.getJSONArray("images")
                //Check if user has Avatar
                var spotifyAvatarURL = ""
                if (spotifyAvatarArray.length() > 0) {
                    spotifyAvatarURL = spotifyAvatarArray.getJSONObject(0).getString("url")
                    Log.d("Spotify Avatar : ", spotifyAvatarURL)
                }
                Log.d("Spotify AccessToken :", token)


            }
        }
    }


     */
    private fun fetchSpotifyUserProfile(token: String?) {
        Log.d("Status: ", "Please Wait...")
        if (token == null) {
            Log.i("Status: ", "Something went wrong - No Access Token found")
            return
        }
        val getUserProfileURL = "https://api.spotify.com/v1/me"
        GlobalScope.launch(Dispatchers.Default) {
            val url = URL(getUserProfileURL)
            val httpsURLConnection = withContext(Dispatchers.IO) {url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            withContext(Dispatchers.Main) {
                val jsonObject = JSONObject(response)
                // Create an instance of UserProfile with the fetched details
                val userProfile = UserProfile(
                    Id = jsonObject.getString("id"),
                    Token = token,
                    Username = jsonObject.getString("display_name"),
                    Email = jsonObject.getString("email"),
                    RatedSongs = 0
                )
                // Log to check if everything worked
                Log.d("UserProfile", userProfile.toString())
                val intent = Intent(this@MainActivity, MainMenu::class.java).apply {
                    putExtra("USER_PROFILE", userProfile)
                }
                startActivity(intent)
            }
        }
    }

    /**
     * Function so access and save save the token so i can use it in a future
     */
    private fun saveAccessToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("SPOTIFY_ACCESS_TOKEN", token)
        editor.apply()
        Log.d("MainActivity", "Access Token saved")
    }
}
