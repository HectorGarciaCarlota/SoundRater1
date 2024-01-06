package com.example.soundrater1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.AuthorizationClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val builder = AuthorizationRequest.Builder(SpotifyApi.CLIENT_ID, AuthorizationResponse.Type.TOKEN, SpotifyApi.REDIRECT_URI)
        builder.setScopes(arrayOf("streaming"))
        val request = builder.build()

        AuthorizationClient.openLoginInBrowser(this, request)
    }

    // Rest of your class code (e.g., onStart, onStop)
}
