package com.example.exhibitionapp

import android.os.Parcel
import android.os.Parcelable


data class Exhibition(
    val title: String,
    val description: String,
    val imageResId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeInt(imageResId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Exhibition> {
        override fun createFromParcel(parcel: Parcel): Exhibition {
            return Exhibition(parcel)
        }

        override fun newArray(size: Int): Array<Exhibition?> {
            return arrayOfNulls(size)
        }
    }
}
