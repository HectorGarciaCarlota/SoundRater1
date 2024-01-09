package com.example.soundrater1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.soundrater1.MainMenu
import com.example.soundrater1.R
import com.example.soundrater1.SpotifyApi
import com.example.soundrater1.UserProfile
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
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

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
            .setScopes(arrayOf("user-read-email", "user-read-private")) // necessitava permisos tmb de user-read-private per accedir al country en el JSON ^^
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
                    Country = jsonObject.optString("country"), // user pot tenir country o pot no tenir, per el que retornaria null, per aixo posem optString que retorna String?
                    ImageUrl = jsonObject.optJSONArray("images")?.optJSONObject(0)?.optString("url"),  // El json retorna una array en imatges

                )
                /**
                 * La documentació de la api retorna això
                 * "images": [
                 *     {
                 *       "url": "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228",
                 *       "height": 300,
                 *       "width": 300
                 *     }
                 *   ],
                 */
                // Log to check if everything worked
                Log.d("UserProfile", userProfile.toString())
                // Save the UserProfile object in SharedPreferences
                saveUserProfile(userProfile)
                val intent = Intent(this@MainActivity, MainMenu::class.java).apply {
                    putExtra("USER_PROFILE", userProfile)
                }
                startActivity(intent)
            }
        }

    }

    private fun saveAccessToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("SPOTIFY_ACCESS_TOKEN", token)
        editor.apply()
        Log.d("MainActivity", "Access Token saved")
    }

    private fun saveUserProfile(userProfile: UserProfile) {
        val gson = Gson()
        val userProfileJson = gson.toJson(UserProfile(userProfile.Id, userProfile.Token, userProfile.Username, userProfile.Email, userProfile.ImageUrl, userProfile.Country))
        val editor = sharedPreferences.edit()
        editor.putString("USER_PROFILE", userProfileJson)
        editor.apply()
        Log.d("MainActivity", "User Profile saved")

    }
}
