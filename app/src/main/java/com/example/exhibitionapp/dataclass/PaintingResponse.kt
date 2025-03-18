package com.example.exhibitionapp.dataclass

import android.os.Parcel
import android.os.Parcelable

data class PaintingResponse(
    val id: Int,
    val name: String,
    val author: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(author)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PaintingResponse> {
        override fun createFromParcel(parcel: Parcel): PaintingResponse {
            return PaintingResponse(parcel)
        }

        override fun newArray(size: Int): Array<PaintingResponse?> {
            return arrayOfNulls(size)
        }
    }
}
