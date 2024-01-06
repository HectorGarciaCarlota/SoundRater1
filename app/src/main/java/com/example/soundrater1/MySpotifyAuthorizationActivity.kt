package com.example.soundrater1

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.spotify.sdk.android.auth.AuthorizationResponse

class MySpotifyAuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_spotify_authorization)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri: Uri? = intent.data
        if (uri != null) {
            val response = AuthorizationResponse.fromUri(uri)

            when (response.type) {
                // Response was successful and contains auth token
                AuthorizationResponse.Type.TOKEN -> {
                    // Handle successful response
                    Log.d("SUCCESS", "NICE")
                }

                // Auth flow returned an error
                AuthorizationResponse.Type.ERROR -> {
                    Log.d("ERROR", "NICE")
                }

                // Most likely auth flow was cancelled
                else -> {
                    Log.d("ERROR AUTH FLOW", "NICE")
                }
            }
        }
    }

    // ... rest of your class code ...
}