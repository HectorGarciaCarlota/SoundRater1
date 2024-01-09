package com.example.soundrater1

import android.os.Parcel
import android.os.Parcelable
import android.util.Log


data class UserProfile(
    val Id: String?,
    val Token: String?,
    val Username: String?,
    val Email: String?,
    val ImageUrl: String?, // Add this line for the image URL
    val Country: String? = "Unknown",
    var ratedSongs: MutableList<RatedSong> = mutableListOf()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        mutableListOf<RatedSong>().apply {
            parcel.readList(this, RatedSong::class.java.classLoader) // Read the rated songs list from the parcel
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Id)
        parcel.writeString(Token)
        parcel.writeString(Username)
        parcel.writeString(Email)
        parcel.writeString(ImageUrl)
        parcel.writeString(Country)
        parcel.writeList(ratedSongs) // Write the rated songs list to the parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserProfile> {
        override fun createFromParcel(parcel: Parcel): UserProfile {
            return UserProfile(parcel)
        }

        override fun newArray(size: Int): Array<UserProfile?> {
            return arrayOfNulls(size)
        }
    }

    fun isSongInRated(ratedSongs: MutableList<RatedSong>, trackName: String, artistName: String): Boolean {
        return ratedSongs.any { it.trackName == trackName && it.artistName == artistName }
    }

    fun indexOfRatedSong(ratedSongs: MutableList<RatedSong>, foundRatedSong: RatedSong): Int {
       for ((index, ratedSong) in ratedSongs.withIndex()) {
            if (areSongsEquivalent(foundRatedSong, ratedSong)) {
                return index
            }
        }

        return -4
    }
    fun areSongsEquivalent(ratedSong1: RatedSong, ratedSong2: RatedSong): Boolean {
        return ratedSong1.trackName.equals(ratedSong2.trackName, ignoreCase = true) &&
                ratedSong1.artistName.equals(ratedSong2.artistName, ignoreCase = true)
    }

}
