package com.example.soundrater1

import android.os.Parcel
import android.os.Parcelable

data class RatedSong(
    val trackName: String,
    val artistName: String,
    val imageUri: String,
    var rating: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",  // Handle nullable String with Elvis operator
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(trackName)
        parcel.writeString(artistName)
        parcel.writeString(imageUri)
        parcel.writeFloat(rating)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RatedSong> {
        override fun createFromParcel(parcel: Parcel): RatedSong {
            return RatedSong(parcel)
        }

        override fun newArray(size: Int): Array<RatedSong?> {
            return arrayOfNulls(size)
        }
    }
}
