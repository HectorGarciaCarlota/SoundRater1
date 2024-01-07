package com.example.soundrater1

import android.os.Parcel
import android.os.Parcelable


data class UserProfile(val Id: String?, val Token: String?, val Username: String?, val Email: String?, val RatedSongs: Int): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Id)
        parcel.writeString(Token)
        parcel.writeString(Username)
        parcel.writeString(Email)
        parcel.writeInt(RatedSongs)
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

}